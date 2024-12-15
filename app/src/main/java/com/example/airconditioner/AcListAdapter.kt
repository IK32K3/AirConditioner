package com.example.airconditoner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AcListAdapter(
    private val acList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<AcListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)

        fun bind(acName: String) {
            textView.text = acName
            itemView.setOnClickListener { onItemClick(acName) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ac, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(acList[position])
    }

    override fun getItemCount() = acList.size
}