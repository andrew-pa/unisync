package com.lightspeed.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lightspeed.app.databinding.CreateContactFragmentBinding
import com.lightspeed.app.viewBinding

class CreateContactFragment : BottomSheetDialogFragment() {

    private var binding by viewBinding<CreateContactFragmentBinding>()
    private val args by navArgs<CreateContactFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
        = CreateContactFragmentBinding.inflate(inflater, container, false).also {
            binding = it
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveContactButton.setOnClickListener { _ ->
            val contact = NewContact(
                name = listOfNotNull(
                    binding.firstNameEditText.text,
                    binding.lastNameEditText.text,
                ).joinToString(" "),
                color = binding.colorEditText.text.toString()
            )
            setFragmentResult(args.requestKey, contact.toExtras())
            findNavController().apply {
                popBackStack()
            }
        }
    }
}