package com.guet.flexbox.litho.widget

import android.app.Application
import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.recyclerview.widget.RecyclerView
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentTree
import com.facebook.litho.LithoView
import com.guet.flexbox.build.Kit
import com.guet.flexbox.litho.LayoutThreadHandler
import java.util.*
import java.util.concurrent.atomic.AtomicReference

internal object LithoPoolsManager : ComponentCallbacks, Kit {

    override fun onConfigurationChanged(newConfig: Configuration?) {}

    @MainThread
    override fun onLowMemory() {
        lithoViewPool.clear()
        synchronized(componentTreePool) {
            while (!componentTreePool.isEmpty()) {
                componentTreePool.pop().release()
            }
        }
    }

    private val application = AtomicReference<Application>(null)

    val LITHO_VIEW_TYPE = LithoView::class.java.name.hashCode()

    private val lithoViewPool = RecyclerView.RecycledViewPool()

    private val componentTreePool = LinkedList<ComponentTree>()

    @MainThread
    fun attachPool(recyclerView: RecyclerView) {
        recyclerView.setRecycledViewPool(lithoViewPool)
    }

    override fun init(c: Context) {
        val app = c.applicationContext as Application
        if (application.compareAndSet(null, app)) {
            app.registerComponentCallbacks(this)
        }
    }

    //可回收的内容ctx必须是app
    @MainThread
    fun obtainViewHolder(): LithoViewHolder {
        return lithoViewPool.getRecycledView(LITHO_VIEW_TYPE)
                as? LithoViewHolder
                ?: LithoViewHolder(application.get())
    }

    @AnyThread
    fun releaseTree(tree: ComponentTree) {
        synchronized(componentTreePool) {
            if (componentTreePool.size < 10) {
                componentTreePool.push(tree)
            } else {
                tree.release()
            }
        }
    }

    //可回收的内容ctx必须是app
    @AnyThread
    fun obtainTree(): ComponentTree {
        return synchronized(componentTreePool) {
            if (componentTreePool.isEmpty()) {
                //必须使用Application创建tree
                ComponentTree.create(
                        ComponentContext(application.get())
                ).layoutThreadHandler(LayoutThreadHandler)
                        .isReconciliationEnabled(false)
                        .build()
            } else {
                componentTreePool.pop()
            }
        }
    }
}