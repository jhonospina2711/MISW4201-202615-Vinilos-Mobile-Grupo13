package com.example.vinilosmobile.coreui.molecules

import android.content.Context
import android.util.AttributeSet
import com.example.vinilosmobile.R
import com.google.android.material.appbar.MaterialToolbar

class DetailToolbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : MaterialToolbar(context, attrs) {
    init {
        setNavigationIcon(R.drawable.ic_back)
        navigationContentDescription = context.getString(R.string.back)
    }
}
