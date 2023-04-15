package com.lightspeed.app.ui

import android.os.Parcelable
import androidx.core.os.bundleOf
import com.lightspeed.app.domain.Contact
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewContact(
    val id: Int?,
    val name: String,
    val color: String,
) : Parcelable {
    fun toExtras() = bundleOf(BUNDLE_KEY to this)

    companion object {
        const val BUNDLE_KEY = "NEW_CONTACT_BUNDLE"
    }
}

@Parcelize
data class ContactSheetRequest(
    val key: String,
    val contact: UpdateContact? = null
) : Parcelable {

    @Parcelize
    data class UpdateContact(
        val id: Int,
        val firstName: String,
        val lastName: String,
        val color: String,
    ) : Parcelable
}

