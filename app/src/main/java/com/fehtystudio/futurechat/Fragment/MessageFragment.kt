package com.fehtystudio.futurechat.Fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
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
import io.socket.client.Socket
import kotlinx.android.synthetic.main.fragment_message.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class MessageFragment : Fragment() {

    private val socket = IO.socket("http://138.68.234.86:7777/")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = RecyclerViewAdapter()

        Observable.create<Any> { emitter ->

            recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapter

            socket.connect()
            socket.on(Socket.EVENT_CONNECT) {
                socket.emit("receiveHistory")
            }.on("history") {
                val obj = it[0] as JSONArray
                for (i in 0 until obj.length()) {
                    val data = obj[i] as JSONObject
                    val userId = data.getString("username")
                    val content = data.getString("content")
                    emitter.onNext(MessageData(userId, content))
                }
            }.on("message") {
                val userId = (it[0] as JSONObject).getString("username")
                val content = (it[0] as JSONObject).getString("content")
                emitter.onNext(MessageData(userId, content))
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
                try {
                    socket.emit("msg", inputMessage.text.toString())
                    inputMessage.text.clear()
                } catch (ex: Exception) {
                    Log.e("#*#*", ex.toString())
                }
            } else {
                Toast.makeText(activity, "Empty field", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        socket.disconnect()
    }

    override fun onResume() {
        super.onResume()
        socket.connect()
        val realm = Realm.getDefaultInstance()
        val realmDatabase = RealmDatabase()
        val result = realm.where(RealmDatabase::class.java).findFirst()?.username
        if (result == null) {
            val random = Random().nextInt(9999999)
            realmDatabase.username = "Anonymous$random"
            realm.executeTransaction {
                realm.insertOrUpdate(realmDatabase)
            }
            socket.emit("changeName", random.toString())
        }
        socket.emit("changeName", result)
    }
}
