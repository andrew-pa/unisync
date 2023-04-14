package com.lightspeed.app.ui

import android.graphics.Color
import androidx.lifecycle.MutableLiveData
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
            loading = true,
            contactList = emptyList(),
        )
    )
    val viewStateFlow: Flow<ViewState> = stateFlow.map { it.toViewState() }
    val command = MutableLiveData<Command>()

    fun loadContacts() {
        stateFlow.update { it.copy(loading = true) }
        stateFlow.update { it.copy(contactList = Contact.getContacts(), loading = false) }
    }

    fun addContact(newContact: NewContact) {
        Contact.addContact(
            Contact(
                localId = 1,
                name = newContact.name,
                color = newContact.color,
                userId = UUID.randomUUID()
            )
        )
        stateFlow.update { it.copy(contactList = Contact.getContacts()) }
    }

    fun deleteContact(contact: Row.Contact) {
        Contact.deleteContact(contact.id)
        stateFlow.update { it.copy(contactList = Contact.getContacts()) }
    }

    fun editContactTapped(contact: Row.Contact) {
        stateFlow.value.contactList.firstOrNull { it.localId == contact.id }?.let {
            command.postValue(Command.EditContactCommand(it))
        }
    }

    fun updateContact(contact: NewContact) {
        contact.id?.let { id ->
            Contact.deleteContact(id)
            Contact.addContact(
                Contact(
                    localId = 1,
                    name = contact.name,
                    color = contact.color,
                    userId = UUID.randomUUID()
                )
            )
            stateFlow.update { it.copy(contactList = Contact.getContacts()) }
        }
    }

    data class State(
        val loading: Boolean,
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
                    id = it.localId,
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

sealed class Command {
    data class EditContactCommand(
        val contact: Contact
    ) : Command()
}