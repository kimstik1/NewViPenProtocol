package com.visoft.newvipenprotocol

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.visoft.newvipenprotocol.ble.Control
import com.visoft.newvipenprotocol.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity: AppCompatActivity(){

    private var bind: ActivityMainBinding? = null
    private val control by lazy { Control(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind?.root)

        bind?.btnDiscover?.setOnClickListener {

            CoroutineScope(Dispatchers.IO).launch {
                control.init()
            }
        }

        bind?.btnOnOff?.setOnClickListener {
            control.disconnect()
        }
    }



}