package com.lightspeed.app.ui

import android.os.Parcelable
import androidx.core.os.bundleOf
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewContact(
    val name: String,
    val color: String,
) : Parcelable {
    fun toExtras() = bundleOf(BUNDLE_KEY to this)

    companion object {
        const val BUNDLE_KEY = "NEW_CONTACT_BUNDLE"
    }
}

