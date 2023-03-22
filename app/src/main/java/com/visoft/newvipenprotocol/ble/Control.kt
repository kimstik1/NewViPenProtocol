package com.visoft.newvipenprotocol.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.visoft.newvipenprotocol.callbacks.*
import com.visoft.newvipenprotocol.converter.DeviceData
import kotlinx.coroutines.*
import no.nordicsemi.android.support.v18.scanner.*
import java.util.*

class Control(context: Context): OnConnectStateListener, OnServiceDiscoveredListener, OnCharacteristicReadListener, OnCharacteristicChangedListener, OnMtuChangedListener {

    var counter: Int = 0

    private val scanCallback = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.wtf("onScanResult/Device", result.device.toString())
        }

        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            results.map {
                val name = it?.device?.name

                if(name == VI_PEN2_DEVICE_NAME || name == VI_PEN_DEVICE_NAME) {
                    val scanner = BluetoothLeScannerCompat.getScanner()
                    scanner.stopScan(this)

                    CoroutineScope(Dispatchers.IO).launch {
                        connect(it.device)
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.wtf("onScanFailed, errorCode", errorCode.toString())
        }
    }

    private val gattCallback: CustomGattCallback.Controller = CustomGattCallback(
        context, this, this, this, this, this
    ).Controller()

    fun init() {
        startScan()
    }

    private fun startScan() {
        val scanner = BluetoothLeScannerCompat.getScanner()
        val settings: ScanSettings = ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(5000)
                .setUseHardwareBatchingIfSupported(true)
                .build()

        val filters: MutableList<ScanFilter> = ArrayList()
        scanner.startScan(filters, settings, scanCallback)
    }

    private suspend fun connect(device: BluetoothDevice) = gattCallback.connectAsync(device)

    private suspend fun discoverService() = gattCallback.discoverServicesAsync()

    private suspend fun requestMtu() = gattCallback.requestMtu()

    private suspend fun downloadFirstData() {
        gattCallback.apply {
            writeFirstCharacteristic()
            delay(1000)
            readFirstCharacteristic()
        }
    }

    private suspend fun downloadSecondData() {
        gattCallback.apply {
            startIndicate()
            delay(200)
            writeSecondCharacteristic()
        }
    }

    private suspend fun requestDeviceDataStatus() {
        delay(1000)
        gattCallback.requestDeviceDataStatus()
    }

    fun disconnect() = CoroutineScope(Dispatchers.IO).launch {
        gattCallback.closeConnection()
    }

    override fun serviceDiscovered(status: Int) {
        Log.wtf("OnServiceDiscovered", "Status = $status")
        CoroutineScope(Dispatchers.IO).launch {
            requestMtu()
        }
    }

    override fun changeConnectStatus(status: Int, newStatus: Int) {

        when(newStatus) {
            BluetoothProfile.STATE_CONNECTED     -> {
                CoroutineScope(Dispatchers.IO).launch {
                    discoverService()
                }
            }
            BluetoothProfile.STATE_DISCONNECTING -> disconnect()
            BluetoothProfile.STATE_DISCONNECTED  -> disconnect()
        }
        when(status) {
            19 -> disconnect()
        }
        Log.wtf("OnConnectStateChange", "Status = $status // newState = $newStatus")
    }

    override fun onCharacteristicReadListener(value: ByteArray, status: Int) {
        val scope = CoroutineScope(Dispatchers.IO)

        if(value.size == 17) {
            value.reverse()
            val data = DeviceData(VI_PEN2_DEVICE_NAME, value)
            Log.wtf("Data", data.toString())

            scope.launch {
                requestDeviceDataStatus()
            }
        } else if(value.size == 2) {

            if((status and 2) > 0){
                Log.wtf("DeviceDataStatus", "ViPen_State: Stopped / NoData")
                scope.launch {
                    requestDeviceDataStatus()
                }
            }else{
                Log.wtf("DeviceDataStatus", "ViPen_State: Data")
                scope.launch {
                    downloadSecondData()
                }
            }
        }
    }

    override fun onCharacteristicChanged(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        Log.wtf("onCharacteristicChanged", "Value.size = ${value.size}, Value = $value, HexString = ${value.toHexString()}")
    }

    override fun onMtuChanged(mtu: Int, status: Int) {
        Log.wtf("onMtuChanged", "mtu: $mtu, status: $status")
        CoroutineScope(Dispatchers.IO).launch {
            downloadFirstData()
        }
    }
}