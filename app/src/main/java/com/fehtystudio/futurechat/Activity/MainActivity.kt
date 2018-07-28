package com.fehtystudio.futurechat.Activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.fehtystudio.futurechat.Fragment.MessageFragment
import com.fehtystudio.futurechat.Fragment.UserSettingsFragment
import com.fehtystudio.futurechat.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val userSettingsFragment = UserSettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager!!
                .beginTransaction()
                .replace(R.id.container, MessageFragment())
                .commit()

        userSettings.setOnClickListener {
            userSettingsFragment.show(fragmentManager, "1")
        }
    }
}
