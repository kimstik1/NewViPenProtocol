package com.expertuniversal.vipen.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
import android.content.Context
import android.os.Build
import com.expertuniversal.vipen.callbacks.*
import kotlinx.coroutines.delay
import java.util.*

@SuppressLint("MissingPermission")
class CustomGattCallback(
    private val context: Context,
    private val onConnectStateListener: OnConnectStateListener,
    private val onServiceDiscoveredListener: OnServiceDiscoveredListener,
    private val onCharacteristicChangedListener: OnCharacteristicChangedListener,
    private val onCharacteristicReadListener: OnCharacteristicReadListener,
    private val onMtuChanged: OnMtuChangedListener
): BluetoothGattCallback() {

    private var gatt: BluetoothGatt? = null

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        onConnectStateListener.changeConnectStatus(status, newState)
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        onServiceDiscoveredListener.serviceDiscovered(status)
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        onMtuChanged.onMtuChanged(mtu, status)
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        super.onCharacteristicRead(gatt, characteristic, status)
        characteristic?.value?.let {
            onCharacteristicReadListener.onCharacteristicReadListener(status = status, value = it)
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        super.onCharacteristicChanged(gatt, characteristic)
        characteristic?.let {
            onCharacteristicChangedListener.onCharacteristicChanged(it, it.value)
        }
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
        super.onCharacteristicRead(gatt, characteristic, value, status)
        onCharacteristicReadListener.onCharacteristicReadListener(value, status)
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        super.onCharacteristicChanged(gatt, characteristic, value)
        println("onCharacteristicChanged / Characteristic UUID: ${characteristic.uuid}")
        onCharacteristicChangedListener.onCharacteristicChanged(characteristic, value)
    }

    inner class Controller {

        suspend fun writeCharacteristic(serviceUUID: String, characteristicUUID: String, value: ByteArray): Boolean {
            val characteristic = getCharacteristic(serviceUUID, characteristicUUID)

            return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                isGattNotNull().writeCharacteristic(characteristic, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) == BluetoothStatusCodes.SUCCESS
            } else {
                characteristic.value = value
                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                isGattNotNull().writeCharacteristic(characteristic)
            }
        }

        suspend fun readCharacteristic(serviceUUID: String, characteristicUUID: String): Boolean {
            val characteristic = getCharacteristic(serviceUUID, characteristicUUID)

            return isGattNotNull().readCharacteristic(characteristic)
        }

        // Only ViPen2
        suspend fun requestDeviceDataStatus(): Boolean {
            val characteristic = getCharacteristic(viPen2MainServiceUUID_UUID, C_WRITE_UUID)

            return isGattNotNull().readCharacteristic(characteristic)
        }

        // Only ViPen2
        suspend fun startIndicate() {
            val characteristic = getCharacteristic(viPen2MainServiceUUID_UUID, C_WAV_INDICATE_UUID)

            val CLIENT_CHARACTERISTIC_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
            val descriptor: BluetoothGattDescriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)

            isGattNotNull().setCharacteristicNotification(characteristic, true)

            writeDescriptor(descriptor, ENABLE_INDICATION_VALUE)
        }

        suspend fun requestMtu() = isGattNotNull().requestMtu(512)

        suspend fun discoverServicesAsync() = isGattNotNull().discoverServices()

        // Only ViPen2
        private suspend fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray): Boolean {
            isGattNotNull().apply {
                return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    writeDescriptor(descriptor, payload) == BluetoothStatusCodes.SUCCESS
                } else {
                    descriptor.value = payload
                    writeDescriptor(descriptor)
                }
            }
        }

        suspend fun connectAsync(device: BluetoothDevice): Boolean {
            gatt = device.connectGatt(context, false, this@CustomGattCallback, BluetoothDevice.TRANSPORT_LE)
            delay(100)
            return isGattNotNull().connect()
        }

        suspend fun isGattNotNull(): BluetoothGatt = if(gatt == null) {
            throw Exception("BluetoothGatt is null")
        } else {
            gatt as BluetoothGatt
        }

        fun closeConnection() {
            gatt?.disconnect()
            gatt?.close()
            gatt = null
        }

        private suspend fun getCharacteristic(serviceUUID: String, characteristicUUID: String): BluetoothGattCharacteristic {
            val service = isGattNotNull().getService(UUID.fromString(serviceUUID))
                ?: throw Exception("Service is not available")

            return service.getCharacteristic(UUID.fromString(characteristicUUID))
                ?: throw NullPointerException("Characteristic $characteristicUUID is not available")
        }
    }
}