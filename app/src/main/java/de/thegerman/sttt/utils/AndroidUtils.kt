package de.thegerman.sttt.utils

import android.view.View

fun Boolean.toVisibility(hiddenVisibility: Int = View.GONE) =
        if (this) View.VISIBLE else hiddenVisibility