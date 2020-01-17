package com.guet.flexbox.litho

import android.widget.ImageView.ScaleType
import com.facebook.litho.ComponentContext
import com.guet.flexbox.build.AttributeSet
import com.guet.flexbox.litho.widget.NetworkImage

internal object ToImage : ToComponent<NetworkImage.Builder>(Common) {

    override val attributeAssignSet: AttributeAssignSet<NetworkImage.Builder> by create {
        register("scaleType") { _, _, value: ScaleType ->
            scaleType(value)
        }
        register("blurRadius") { _, _, value: Double ->
            blurRadius(value.toFloat())
        }
        register("blurSampling") { _, _, value: Double ->
            blurSampling(value.toFloat())
        }
        register("aspectRatio") { _, _, value: Double ->
            imageAspectRatio(value.toFloat())
        }
        register("url") { display, _, value: String ->
            if (display) {
                url(value)
            } else {
                url("")
            }
        }
    }

    override fun create(
            c: ComponentContext,
            visibility: Boolean,
            attrs: AttributeSet
    ): NetworkImage.Builder {
        return NetworkImage.create(c)
    }
}