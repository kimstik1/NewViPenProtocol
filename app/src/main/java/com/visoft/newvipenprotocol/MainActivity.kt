package com.visoft.newvipenprotocol

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.expertuniversal.vipen.ble.Control
import com.expertuniversal.vipen.callbacks.OnDataConvertedListener
import com.expertuniversal.vipen.data.ViPenData
import com.expertuniversal.vipen.data.ViPenUserData
import com.visoft.newvipenprotocol.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity: AppCompatActivity(), OnDataConvertedListener {

    private var bind: ActivityMainBinding? = null
    private val control: Control = Control(this, this)


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

    override fun receiveViPen2Data(viPenData: ViPenData) {
        Log.wtf("viPenUserData", viPenData.viPenUserData.toString())

        Log.wtf("viPen2Spector.size", viPenData.viPen2Spector.spectorData.size.toString())
        Log.wtf("viPen2Spector.length", viPenData.viPen2Spector.spectorLength.toString())

        Log.wtf("viPen2Spector.size", viPenData.viPen2Spector.signalData.size.toString())
        Log.wtf("viPen2Spector.length", viPenData.viPen2Spector.signalLength.toString())
    }

    override fun receiveStandardData(userData: ViPenUserData) {
        Log.wtf("receiveStandardData", userData.toString())
    }

    override fun onError(code: Int) {
        Log.wtf("OnError", code.toString())
        control.disconnect()
    }


}