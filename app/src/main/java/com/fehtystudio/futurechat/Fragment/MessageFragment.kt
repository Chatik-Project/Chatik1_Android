package com.fehtystudio.futurechat.Fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fehtystudio.futurechat.Adapter.RecyclerViewAdapter
import com.fehtystudio.futurechat.DataClass.MessageData
import com.fehtystudio.futurechat.R
import com.fehtystudio.futurechat.RealmDatabase.RealmDatabase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.socket.client.IO
import kotlinx.android.synthetic.main.fragment_message.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class MessageFragment : Fragment() {

    private val socket = IO.socket("http://138.68.234.86:7777/")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Observable.just(socket.connect()).subscribeOn(Schedulers.io()).subscribe()
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    private val adapter = RecyclerViewAdapter()
    private val realm = Realm.getDefaultInstance()
    private val realmDatabase = RealmDatabase()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        socketWork()

        sendMessage.setOnClickListener {
            when {
                inputMessage.text.isNotEmpty() -> {
                    recyclerView.scrollToPosition(adapter.list!!.size - 1)
                    socket.emit("changeName", realm.where(RealmDatabase::class.java).findFirst()?.username)
                    socket.emit("msg", inputMessage.text.toString().trim())
                    recyclerView.scrollToPosition(adapter.list!!.size - 1)
                    inputMessage.text.clear()
                }
                else -> Toast.makeText(activity, "Empty field", Toast.LENGTH_SHORT).show()
            }
        }

        recyclerView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            recyclerView.scrollToPosition(adapter.list!!.size - 1)
        }
    }


    override fun onResume() {
        super.onResume()
        val result = realm.where(RealmDatabase::class.java).findFirst()?.username
        when (result) {
            null -> {
                val random = Random().nextInt(9999999)
                realm.executeTransaction {
                    realmDatabase.username = "Anonymous$random"
                    realm.insertOrUpdate(realmDatabase)
                }
                // Возможно!!!
                socket.emit("changeName", result)
            }
        // Возможно!!!
            else -> socket.emit("changeName", result)
        }
    }

    private fun socketWork() {
        Observable.create<Any> { emitter ->
            socket.on("connected") {
                socket.emit("receiveHistory")
            }.on("history") {
                val obj = it[0] as JSONArray
                for (i in 0 until obj.length()) {
                    val userName = (obj[i] as JSONObject).getString("username")
                    val timeOfAddition = (obj[i] as JSONObject).getString("date")
                    val message = (obj[i] as JSONObject).getString("content")
                    emitter.onNext(MessageData(userName, timeOfAddition, message))
                }
            }.on("message") {
                val userName = (it[0] as JSONObject).getString("username")
                val timeOfAddition = (it[0] as JSONObject).getString("date")
                val message = (it[0] as JSONObject).getString("content")
                emitter.onNext(MessageData(userName, timeOfAddition, message))
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    adapter.addItemToTheList(it as MessageData)
                    recyclerView.scrollToPosition(adapter.list!!.size - 1)
                }
                .subscribe()
    }
}
