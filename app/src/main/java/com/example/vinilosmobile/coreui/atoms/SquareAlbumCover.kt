package com.example.vinilosmobile.coreui.atoms

import android.content.Context
import android.util.AttributeSet
import android.view.View.MeasureSpec
import com.google.android.material.card.MaterialCardView

/** Keeps catalogue covers square at any screen width. */
class SquareAlbumCover @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : MaterialCardView(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY))
    }
}
