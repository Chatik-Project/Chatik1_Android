package com.fehtystudio.futurechat.Adapter

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fehtystudio.futurechat.DataClass.MessageData
import com.fehtystudio.futurechat.R
import java.text.SimpleDateFormat

class RecyclerViewAdapter(var list: MutableList<MessageData>? = mutableListOf()) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.template_for_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list!![position])
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    fun addItemToTheList(itemMessage: MessageData) {
        list!!.add(itemMessage)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val userName = view.findViewById<TextView>(R.id.userName)
        private val itemText = view.findViewById<TextView>(R.id.userMessage)
        private val timeOfAddition = view.findViewById<TextView>(R.id.timeOfAddition)

        fun bind(item: MessageData) {
            userName.text = item.userName
            itemText.text = item.message

            timeOfAddition.text = formatDate(item.timeOfAddition)
        }

        @SuppressLint("SimpleDateFormat")
        private fun formatDate(dateString: String): String? {
            return SimpleDateFormat("HH:mm").format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS").parse(dateString))
        }
    }
}