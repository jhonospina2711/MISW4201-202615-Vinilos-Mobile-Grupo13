package com.example.vinilosmobile.coreui.organisms

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.example.vinilosmobile.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

/** Reusable empty and error state for View based screens. */
class ContentStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {
    private val title: MaterialTextView
    private val body: MaterialTextView
    private val action: MaterialButton

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        LayoutInflater.from(context).inflate(R.layout.vinilos_view_content_state, this, true)
        title = findViewById(R.id.contentStateTitle)
        body = findViewById(R.id.contentStateBody)
        action = findViewById(R.id.contentStateAction)
    }

    fun show(
        @StringRes titleRes: Int,
        @StringRes bodyRes: Int? = null,
        @StringRes actionRes: Int? = null,
        onAction: (() -> Unit)? = null,
    ) {
        title.setText(titleRes)
        body.isVisible = bodyRes != null
        bodyRes?.let(body::setText)
        action.isVisible = actionRes != null && onAction != null
        actionRes?.let(action::setText)
        action.setOnClickListener(if (onAction == null) null else { _ -> onAction() })
    }
}
