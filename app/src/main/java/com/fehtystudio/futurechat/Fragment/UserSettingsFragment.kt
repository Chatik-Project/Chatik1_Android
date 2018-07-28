package com.fehtystudio.futurechat.Fragment

import android.annotation.SuppressLint
import android.app.DialogFragment
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fehtystudio.futurechat.Api.GetUserData
import com.fehtystudio.futurechat.Api.GetUserDataInterface
import com.fehtystudio.futurechat.R
import com.fehtystudio.futurechat.RealmDatabase.RealmDatabase
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_user_settings.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class UserSettingsFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_settings, container, false)
    }

    private val realm = Realm.getDefaultInstance()
    private val realmDatabase = RealmDatabase()

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        realm.executeTransaction {
            val result = realm.where(RealmDatabase::class.java).findFirst()?.username.toString()
            userName.setText(result)
        }

        if (!VKSdk.isLoggedIn()) vkAuth.text = "Log in"
        else if (VKSdk.isLoggedIn()) vkAuth.text = "Log out"

        saveAndBack.setOnClickListener {
            when {
                userName.text.isNotEmpty() -> realm.executeTransaction {
                    realm.where(RealmDatabase::class.java).findAll().deleteAllFromRealm()
                    realmDatabase.username = userName.text.toString().trim()
                    realm.insertOrUpdate(realmDatabase)
                }
                else -> {
                    realm.executeTransaction {
                        val random = Random().nextInt(9999999)
                        realm.where(RealmDatabase::class.java).findAll().deleteAllFromRealm()
                        realmDatabase.username = "Anonymous$random"
                        realm.insertOrUpdate(realmDatabase)
                    }
                }
            }
            this.dismiss()
        }

        vkAuth.setOnClickListener {
            when {
                !VKSdk.isLoggedIn() -> VKSdk.login(this, "")
                VKSdk.isLoggedIn() -> {
                    VKSdk.logout()
                    userName.text.clear()
                    vkAuth.text = "Log in"
                    realm.executeTransaction {
                        realm.where(RealmDatabase::class.java).findFirst()?.deleteFromRealm()
                        realmDatabase.username = "Anonymous${Random().nextInt(9999999)}"
                        realm.insertOrUpdate(realmDatabase)
                        userName.setText(realm.where(RealmDatabase::class.java).findFirst()!!.username)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
                    override fun onResult(res: VKAccessToken?) = getUserData()
                    override fun onError(error: VKError?) {
                        Log.e("*##*#*", "User hasn't authorized")
                    }
                }))
            super.onActivityResult(requestCode, resultCode, data)
    }

    fun getUserData() {

        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.vk.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GetUserDataInterface::class.java)

        retrofit.getUserData().enqueue(object : Callback<GetUserData> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<GetUserData>?, response: Response<GetUserData>?) {
                userName.setText("${response!!.body()!!.response[0].firstName} ${response.body()!!.response[0].lastName}")
                vkAuth.text = "log out"
                realm.executeTransaction {
                    realm.where(RealmDatabase::class.java).findFirst()?.deleteFromRealm()
                    realmDatabase.username = "${response.body()!!.response[0].firstName} ${response.body()!!.response[0].lastName}"
                    realm.insertOrUpdate(realmDatabase)
                }
            }

            override fun onFailure(call: Call<GetUserData>?, t: Throwable?) = Unit
        })
    }
}
