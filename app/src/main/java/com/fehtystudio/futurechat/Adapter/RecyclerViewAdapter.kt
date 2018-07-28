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
        private val timeOfAddition = view.findViewById<TextView>(R.id.timeOfAddition)
        private val message = view.findViewById<TextView>(R.id.userMessage)

        @SuppressLint("SimpleDateFormat")
        fun bind(messageData: MessageData) {
            userName.text = messageData.userName
            when {
                messageData.timeOfAddition.length > 6 -> timeOfAddition.text = SimpleDateFormat("HH:mm").format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS").parse(messageData.timeOfAddition))
                else -> timeOfAddition.text = messageData.timeOfAddition
            }
            message.text = messageData.message
        }
    }
}