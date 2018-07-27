package com.fehtystudio.futurechat.Application

import android.app.Application
import com.vk.sdk.VKSdk
import io.realm.Realm

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        VKSdk.initialize(applicationContext)
    }
}