package com.example.vinilosmobile.coreui.molecules

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.example.vinilosmobile.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

/** Reusable choice card for short onboarding and settings options. */
class RoleOptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : MaterialCardView(context, attrs) {
    private val icon: ImageView
    private val title: MaterialTextView
    private val description: MaterialTextView

    init {
        LayoutInflater.from(context).inflate(R.layout.vinilos_view_role_option, this, true)
        icon = findViewById(R.id.optionIcon)
        title = findViewById(R.id.optionTitle)
        description = findViewById(R.id.optionDescription)
        radius = resources.getDimension(R.dimen.vinilos_radius_card)
        strokeWidth = resources.getDimensionPixelSize(R.dimen.vinilos_option_stroke)
        strokeColor = ContextCompat.getColor(context, R.color.vinilos_outline)
        setCardBackgroundColor(ContextCompat.getColor(context, R.color.vinilos_surface_container))
        cardElevation = 0f
        isClickable = true
        isFocusable = true
    }

    fun bind(
        @StringRes titleRes: Int,
        @StringRes descriptionRes: Int,
        @DrawableRes iconRes: Int,
        onClick: () -> Unit,
    ) {
        title.setText(titleRes)
        description.setText(descriptionRes)
        icon.setImageResource(iconRes)
        contentDescription = "${context.getString(titleRes)}. ${context.getString(descriptionRes)}"
        setOnClickListener { onClick() }
    }

    fun setCurrentRole(current: Boolean) {
        isSelected = current
        strokeColor = ContextCompat.getColor(
            context,
            if (current) R.color.vinilos_primary else R.color.vinilos_outline,
        )
    }
}
