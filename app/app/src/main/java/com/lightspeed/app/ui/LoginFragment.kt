package com.lightspeed.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.lightspeed.app.databinding.LoginFragmentBinding
import com.lightspeed.app.domain.Contact
import com.lightspeed.app.ui.NewContact.Companion.BUNDLE_KEY
import com.lightspeed.app.viewBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private val viewModel: RecyclerBasedViewModel by viewModels()
    private var binding by viewBinding<LoginFragmentBinding>()
    private lateinit var adapter: RecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        LoginFragmentBinding.inflate(inflater, container, false).also {
            binding = it
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = RecyclerAdapter(
            deleteContactCta = viewModel::deleteContact,
            updateContactCta = viewModel::editContactTapped,
        )
        binding.recyclerView.adapter = adapter
        binding.addNewContact.setOnClickListener {
            launchContactSheet(CREATE_CONTACT_REQUEST_KEY)
        }
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadContacts()
        }
        lifecycleScope.launch {
            viewModel.viewStateFlow.collect {
                render(it)
            }
        }
        viewModel.command.observe(viewLifecycleOwner) {
            when (it) {
                is Command.EditContactCommand -> launchContactSheet(UPDATE_CONTACT_REQUEST_KEY, it.contact)
            }
        }
        viewModel.loadContacts()
        setFragmentResultListener(CREATE_CONTACT_REQUEST_KEY) { _, bundle ->
            bundle.getParcelable<NewContact>(BUNDLE_KEY)?.let {
                viewModel.addContact(it)
            }
        }
        setFragmentResultListener(UPDATE_CONTACT_REQUEST_KEY) { _, bundle ->
            bundle.getParcelable<NewContact>(BUNDLE_KEY)?.let { contact ->
                viewModel.updateContact(contact)
            }
        }
    }

    private fun render(viewState: ViewState) {
        adapter.submitList(viewState.adapterList)
        binding.swipeRefresh.isRefreshing = viewState.loading
    }

    private fun launchContactSheet(key: String, contact: Contact? = null) {
        findNavController().navigate(
            LoginFragmentDirections.createNewContact(
                ContactSheetRequest(
                    key = key,
                    contact = contact?.let {
                        val names = it.name.split(" ")
                        ContactSheetRequest.UpdateContact(
                            id = contact.localId,
                            firstName = names[0],
                            lastName = names[1],
                            color = contact.color
                        )
                    }
                )
            )
        )
    }

    companion object {
        private const val CREATE_CONTACT_REQUEST_KEY = "createContactRequestKey"
        private const val UPDATE_CONTACT_REQUEST_KEY = "updateContactRequestKey"
    }
}