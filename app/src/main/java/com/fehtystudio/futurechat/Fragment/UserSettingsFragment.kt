package com.fehtystudio.futurechat.Fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fehtystudio.futurechat.R
import com.fehtystudio.futurechat.RealmDatabase.RealmDatabase
import com.vk.sdk.VKSdk
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_user_settings.*
import java.util.*

class UserSettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_settings, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val realm = Realm.getDefaultInstance()
        val realmDatabase = RealmDatabase()
        realm.executeTransaction {
            val result = realm.where(RealmDatabase::class.java).findFirst()?.username.toString()
            userName.setText(result)
        }

        if (!VKSdk.isLoggedIn()) {
            vkAuth.text = "Log in"
        } else if (VKSdk.isLoggedIn()) {
            vkAuth.text = "Log out"
        }

        vkAuth.setOnClickListener {
            if (!VKSdk.isLoggedIn()) {
                VKSdk.login(this.activity!!, "")
            } else if (VKSdk.isLoggedIn()) {
                VKSdk.logout()
                userName.text.clear()
                vkAuth.text = "Log in"
                realm.executeTransaction {
                    realm.where(RealmDatabase::class.java).findFirst()?.deleteFromRealm()
                    realmDatabase.username = "guest${Random().nextInt(9999999)}"
                    realm.insertOrUpdate(realmDatabase)
                }
                userName.setText(realm.where(RealmDatabase::class.java).findFirst()!!.username)
            }
        }

        saveAndBack.setOnClickListener {
            realm.executeTransaction {
                realm.where(RealmDatabase::class.java).findAll().deleteAllFromRealm()
                realmDatabase.username = userName.text.toString()
                realm.insertOrUpdate(realmDatabase)
            }
            setMessageFragment()
        }
    }

    private fun setMessageFragment() {
        fragmentManager!!
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, MessageFragment())
                .commit()
    }
}
