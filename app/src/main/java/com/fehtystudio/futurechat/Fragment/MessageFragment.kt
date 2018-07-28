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

    private val socket = IO.socket("http://138.68.234.86:7777/")!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = RecyclerViewAdapter()

        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        socket.connect()

        Observable.create<Any> { emitter ->
            socket.on("connected") {
                socket.emit("receiveHistory")
            }.on("history") {
                val obj = it[0] as JSONArray
                for (i in 0 until obj.length()) {
                    val data = obj[i] as JSONObject
                    val userName = data.getString("username")
                    val content = data.getString("content")
                    val timeOfAddition = data.getString("date")
                    emitter.onNext(MessageData(userName, content, timeOfAddition))
                }
            }.on("message") {
                val userName = (it[0] as JSONObject).getString("username")
                val content = (it[0] as JSONObject).getString("content")
                val timeOfAddition = (it[0] as JSONObject).getString("date")
                emitter.onNext(MessageData(userName, content, timeOfAddition))
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { emitter ->
                    adapter.addItemToTheList(emitter as MessageData)
                    recyclerView.scrollToPosition(adapter.list!!.size - 1)
                }
                .subscribe()

        sendMessage.setOnClickListener {
            if (inputMessage.text.isNotEmpty()) {
                recyclerView.scrollToPosition(adapter.list!!.size - 1)
                val realm = Realm.getDefaultInstance()
                socket.emit("changeName", realm.where(RealmDatabase::class.java).findFirst()?.username)
                socket.emit("msg", inputMessage.text.toString().trim())
                recyclerView.scrollToPosition(adapter.list!!.size - 1)
                inputMessage.text.clear()
            } else {
                Toast.makeText(activity, "Empty field", Toast.LENGTH_SHORT).show()
            }
        }

        recyclerView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            recyclerView.scrollToPosition(adapter.list!!.size - 1)
        }
    }

    private val realmDatabase = RealmDatabase()

    override fun onResume() {
        super.onResume()
        val realm = Realm.getDefaultInstance()
        val result = realm.where(RealmDatabase::class.java).findFirst()?.username
        if (result == null) {
            val random = Random().nextInt(9999999)
            realmDatabase.username = "Anonymous$random"
            realm.executeTransaction {
                realm.insertOrUpdate(realmDatabase)
            }
            socket.emit("changeName", realm.where(RealmDatabase::class.java).findFirst()?.username)
        } else {
            socket.emit("changeName", realm.where(RealmDatabase::class.java).findFirst()?.username)
        }
    }

    fun changeName(name: String) {
        socket.emit("changeName", name)
    }
}
