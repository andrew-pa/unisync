package com.lightspeed.app.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.lightspeed.app.databinding.ContactViewHolderBinding

class RecyclerAdapter(
    private val deleteContactCta: (row: Row.Contact) -> Unit,
    private val updateContactCta: (row: Row.Contact) -> Unit,
): ListAdapter<Row, ViewHolder>(DIFF_CALLBACK) {
    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is Row.Contact -> CONTACT_VIEW_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            CONTACT_VIEW_TYPE -> ContactViewHolder(
                ContactViewHolderBinding.inflate(inflater, parent, false),
                deleteContactCta = deleteContactCta,
                updateContactCta = updateContactCta,
            )
            else -> throw IllegalStateException("Unsupported viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = when (val item = getItem(position)) {
        is Row.Contact -> (holder as ContactViewHolder).bind(item)
    }

    companion object {
        private const val CONTACT_VIEW_TYPE = 1

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Row>() {
            override fun areItemsTheSame(
                oldItem: Row,
                newItem: Row
            ): Boolean {
                return when (oldItem) {
                    is Row.Contact -> newItem is Row.Contact && oldItem == newItem
                }
            }

            override fun areContentsTheSame(
                oldItem: Row,
                newItem: Row
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}

class ContactViewHolder(
    private val binding: ContactViewHolderBinding,
    private val deleteContactCta: (row: Row.Contact) -> Unit,
    private val updateContactCta: (row: Row.Contact) -> Unit,
) : ViewHolder(binding.root) {
    fun bind(item: Row.Contact) {
        binding.contactValue.text = item.name
        binding.image.setBackgroundColor(item.color)
        binding.root.setOnClickListener {
            updateContactCta(item)
        }
        binding.closeButton.setOnClickListener {
            deleteContactCta(item)
        }
    }
}

sealed class Row {
    data class Contact(
        val id: Int,
        val name: String,
        val color: Int,
    ) : Row()
}