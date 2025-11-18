package com.example.mad_practical_7

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PersonAdapter(
    private val context: Context,
    private val personList: ArrayList<Person>
) : RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {

    inner class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewPhone: TextView = itemView.findViewById(R.id.textViewPhone)
        val textViewEmail: TextView = itemView.findViewById(R.id.textViewEmail)
        val textViewAddress: TextView = itemView.findViewById(R.id.textViewAddress)
        val buttonDelete: ImageView = itemView.findViewById(R.id.buttonDelete)
        // Removed buttonMap since we don't need MapActivity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.person_item, parent, false)
        return PersonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = personList[position]

        holder.textViewName.text = person.name
        holder.textViewPhone.text = person.phoneNo
        holder.textViewEmail.text = person.emailId
        holder.textViewAddress.text = person.address

        // Delete button click
        holder.buttonDelete.setOnClickListener {
            if (context is MainActivity) {
                context.deletePerson(person)
            }
        }
    }

    override fun getItemCount(): Int {
        return personList.size
    }
}