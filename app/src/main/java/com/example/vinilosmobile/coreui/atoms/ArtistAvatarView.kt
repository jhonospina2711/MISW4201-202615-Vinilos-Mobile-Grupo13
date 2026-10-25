package com.example.vinilosmobile.coreui.atoms

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.vinilosmobile.R
import com.google.android.material.card.MaterialCardView

/** Shared circular portrait for artist rows and detail headers. */
class ArtistAvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : MaterialCardView(context, attrs) {
    private val portrait = AppCompatImageView(context)
    private var bindingVersion = 0

    init {
        setCardBackgroundColor(ContextCompat.getColor(context, R.color.vinilos_cover_three))
        cardElevation = 0f
        portrait.scaleType = ImageView.ScaleType.CENTER_INSIDE
        addView(portrait, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun show(imageUrl: String?, isBand: Boolean) {
        Glide.with(this).clear(portrait)
        val icon = if (isBand) R.drawable.ic_collectors else R.drawable.ic_artist
        val version = ++bindingVersion
        post {
            if (bindingVersion != version) return@post
            showPlaceholder(icon)
            if (imageUrl.isNullOrBlank()) return@post
            Glide.with(this).load(imageUrl).circleCrop()
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean,
                    ): Boolean {
                        if (bindingVersion == version) showPlaceholder(icon)
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean,
                    ): Boolean {
                        if (bindingVersion != version) return true
                        portrait.setPadding(0, 0, 0, 0)
                        portrait.imageTintList = null
                        portrait.scaleType = ImageView.ScaleType.CENTER_CROP
                        return false
                    }
                }).into(portrait)
        }
    }

    private fun showPlaceholder(icon: Int) {
        val inset = (width * 0.27f).toInt()
        portrait.setPadding(inset, inset, inset, inset)
        portrait.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.vinilos_on_surface_variant),
        )
        portrait.scaleType = ImageView.ScaleType.CENTER_INSIDE
        portrait.setImageResource(icon)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        radius = minOf(w, h) / 2f
    }
}
