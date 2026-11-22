package com.example.vinilosmobile.coreui.molecules

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import com.example.vinilosmobile.R
import com.google.android.material.textview.MaterialTextView

class SectionHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {
    private val title: MaterialTextView
    private val count: MaterialTextView

    init {
        orientation = HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.vinilos_view_section_header, this, true)
        title = findViewById(R.id.sectionHeaderTitle)
        count = findViewById(R.id.sectionHeaderCount)
        ViewCompat.setAccessibilityHeading(title, true)
    }

    fun show(@StringRes titleRes: Int, itemCount: Int) {
        title.setText(titleRes)
        count.text = itemCount.toString()
        count.contentDescription = context.getString(R.string.section_item_count_a11y, context.getString(titleRes), itemCount)
    }
}
