package com.fehtystudio.futurechat.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.fehtystudio.futurechat.Api.GetUserData
import com.fehtystudio.futurechat.Api.GetUserDataInterface
import com.fehtystudio.futurechat.Fragment.MessageFragment
import com.fehtystudio.futurechat.Fragment.UserSettingsFragment
import com.fehtystudio.futurechat.R
import com.fehtystudio.futurechat.RealmDatabase.RealmDatabase
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user_settings.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setMainFragment()

        userSettings.setOnClickListener {
            setFragment(UserSettingsFragment())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
                    override fun onResult(res: VKAccessToken?) {
                        getUserData()
                        //Log.e("*#*#*#", "User has authorized!!! ${res!!.accessToken} ${res.userId}")
                    }

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
                //   Log.e("*#*#*#", "onSuccess ${response!!.body()!!.response[0].photoMaxOrig}")
           //     Picasso.get().load(response?.body()?.response!![0].photoMaxOrig).into(userSettings)
                userName.setText("${response!!.body()!!.response[0].firstName} ${response.body()!!.response[0].lastName}")
                vkAuth.setText("log out")
                val realm = Realm.getDefaultInstance()
                val realmDatabase = RealmDatabase()
                realm.executeTransaction {
                    realm.where(RealmDatabase::class.java).findFirst()?.deleteFromRealm()
                    realmDatabase.username = "${response.body()!!.response[0].firstName} ${response.body()!!.response[0].lastName}"
                    realm.insertOrUpdate(realmDatabase)
                }
            }

            override fun onFailure(call: Call<GetUserData>?, t: Throwable?) {
                Log.e("*#*#*", "onFailure", t)
            }
        })
    }

    private fun setMainFragment() {
        supportFragmentManager!!
                .beginTransaction()
                .replace(R.id.container, MessageFragment())
                .commit()
    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager!!
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, fragment)
                .commit()
    }
}
