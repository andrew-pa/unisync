package com.lightspeed.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.lightspeed.app.databinding.LoginFragmentBinding
import com.lightspeed.app.ui.NewContact.Companion.BUNDLE_KEY
import com.lightspeed.app.viewBinding
import kotlinx.coroutines.flow.collect
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
        adapter = RecyclerAdapter()
        binding.recyclerView.adapter = adapter
        binding.addNewContact.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.createNewContact(CREATE_CONTACT_REQUEST_KEY))
        }
        lifecycleScope.launch {
            viewModel.viewStateFlow.collect {
                render(it)
            }
        }

        setFragmentResultListener(CREATE_CONTACT_REQUEST_KEY) { _, bundle ->
            bundle.getParcelable<NewContact>(BUNDLE_KEY)?.let {
                viewModel.addContact(it)
            }
        }
    }

    private fun render(viewState: ViewState) {
        adapter.submitList(viewState.adapterList)
    }

    companion object {
        private const val CREATE_CONTACT_REQUEST_KEY = "createContactRequestKey"
    }
}