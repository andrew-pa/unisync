package com.lightspeed.app.ui

import android.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lightspeed.app.domain.Contact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.*

class RecyclerBasedViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val stateFlow = MutableStateFlow(
        State(
            contactList = emptyList()
        )
    )
    val viewStateFlow: Flow<ViewState> = stateFlow.map { it.toViewState() }

    fun addContact(newContact: NewContact) {
        val contact = Contact(localId = 1, name = newContact.name, color = newContact.color, userId = UUID.randomUUID())
        Contact.addContact(contact)
        val list = Contact.getContacts()
//        val list = stateFlow.value.contactList + contact
        stateFlow.update { it.copy(contactList = list) }
    }

    data class State(
        val contactList: List<Contact>
    ) {
        private fun String.toColor() = when (this) {
            "green" -> Color.GREEN
            "red" -> Color.RED
            "blue" -> Color.BLUE
            "yellow" -> Color.YELLOW
            "light_grey" -> Color.LTGRAY
            else -> Color.BLUE
        }

        fun toViewState() = ViewState(
            loading = contactList.isEmpty(),
            adapterList = contactList.map {
                Row.Contact(
                    name = it.name,
                    color = it.color.toColor(),
                )
            }
        )
    }
}

data class ViewState(
    val loading: Boolean,
    val adapterList: List<Row>,
)