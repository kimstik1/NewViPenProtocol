package com.visoft.newvipenprotocol

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
import android.content.Context
import android.os.Build
import android.util.Log
import com.visoft.newvipenprotocol.callbacks.*
import com.visoft.newvipenprotocol.data.*
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

    var gatt: BluetoothGatt? = null
    var bluetoothDevice: BluetoothDevice? = null
    var isConnected: Boolean = false


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
        Log.wtf("onCharacteristicChanged.value", "${characteristic?.value?.toHexString()}")
        Log.wtf("onCharacteristicChanged.value.size", "${characteristic?.value?.size?.toString()}")
        characteristic?.let {
            onCharacteristicChangedListener.onCharacteristicChanged(it, it.value)
        }
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
        super.onCharacteristicRead(gatt, characteristic, value, status)
        onCharacteristicReadListener.onCharacteristicReadListener(value, status)
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        println("onCharacteristicWrite / Characteristic UUID: ${characteristic?.uuid}")
        println("onCharacteristicWrite: status = $status")
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        super.onCharacteristicChanged(gatt, characteristic, value)
        println("onCharacteristicChanged / Characteristic UUID: ${characteristic.uuid}")
        onCharacteristicChangedListener.onCharacteristicChanged(characteristic, value)
    }

    override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int, value: ByteArray) {
        super.onDescriptorRead(gatt, descriptor, status, value)
        println("onDescriptorRead: status = $status, value.size = ${value.size}, value = $value")
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        super.onDescriptorWrite(gatt, descriptor, status)
        println("onDescriptorWrite: status = $status")
    }

    inner class Controller {

        suspend fun writeFirstCharacteristic() {
            val service = isGattNotNull().getService(UUID.fromString(viPen2MainServiceUUID))
                ?: throw Exception("Service is not available") //413557AA-213F-4279-8530-D38E41390000

            val firstWrite = service.getCharacteristic(UUID.fromString(C_WRITE))
                ?: throw NullPointerException("Characteristic C_WRITE is not available") //42EC1288-B8A0-43DB-AE00-29F942ED0002

            val data = TVipen2MeasureSetup(
                VIPEN2_BT_COMMAND.START,
                MEAS_TYPE.SPECTRUM,
                MEAS_UNITS.VELOCITY,
                SETUP_ALLX.ALLX_8K,
                SETUP_DX.DX_25600_HZ,
                SETUP_AVG.AVG_4_STOP
            ).toByteArray()


            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                isGattNotNull().writeCharacteristic(firstWrite, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            } else {
                firstWrite.value = data
                firstWrite.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                isGattNotNull().writeCharacteristic(firstWrite)
            }
        }

        suspend fun readFirstCharacteristic() {
            val service = isGattNotNull().getService(UUID.fromString(viPen2MainServiceUUID))
                ?: throw Exception("Service is not available") // 413557AA-213F-4279-8530-D38E41390000

            val firstRead = service.getCharacteristic(UUID.fromString(C_READ))
                ?: throw NullPointerException("Characteristic C_READ is not available") // 42EC1288-B8A0-43DB-AE00-29F942ED0001

            if(gatt?.readCharacteristic(firstRead) == false) throw Exception("Characteristics read error!")
        }

        suspend fun writeSecondCharacteristic() {
            val service = isGattNotNull().getService(UUID.fromString(viPen2MainServiceUUID))
                ?: throw Exception("Service is not available") // 413557AA-213F-4279-8530-D38E41390000

            val secondWrite = service.getCharacteristic(UUID.fromString(C_WAV_WRITE))
                ?: throw NullPointerException("Characteristic C_WAV_WRITE is not available") // 42EC1288-B8A0-43DB-AE00-29F942ED0003


            val k = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                isGattNotNull().writeCharacteristic(
                    secondWrite, ViPen_Command_Data_START_SPECTOR, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )
            } else {
                secondWrite.value = ViPen_Command_Data_START_SPECTOR // byteArrayOf(0x10, 0)
                secondWrite.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                isGattNotNull().writeCharacteristic(secondWrite)
            }

            Log.wtf("isSecondWriteCorrectly", k.toString())
        }

        suspend fun requestDeviceDataStatus(){
            val service = isGattNotNull().getService(UUID.fromString(viPen2MainServiceUUID))
                ?: throw Exception("Service is not available") // 413557AA-213F-4279-8530-D38E41390000

            val firstRead = service.getCharacteristic(UUID.fromString(C_WRITE))
                ?: throw NullPointerException("Characteristic C_READ is not available") //42EC1288-B8A0-43DB-AE00-29F942ED0002

            if(gatt?.readCharacteristic(firstRead) == false) throw Exception("Characteristics read error!")
        }

        suspend fun startIndicate() {
            val service = isGattNotNull().getService(UUID.fromString(viPen2MainServiceUUID))
                ?: throw Exception("Service is not available") // 413557AA-213F-4279-8530-D38E41390000

            val secondIndicate = service.getCharacteristic(UUID.fromString(C_WAV_INDICATE))
                ?: throw NullPointerException("Characteristic C_WAV_INDICATE is not available") // 42EC1288-B8A0-43DB-AE00-29F942ED0004


            val CLIENT_CHARACTERISTIC_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
            val descriptor: BluetoothGattDescriptor = secondIndicate.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)

            val characteristicNotification = isGattNotNull().setCharacteristicNotification(secondIndicate, true)

            val description = writeDescriptor(descriptor, ENABLE_INDICATION_VALUE)

            Log.wtf("isNotificationEnabledCorrectly", characteristicNotification.toString())
            Log.wtf("isDescriptionEnabledCorrectly", description.toString())
        }

        suspend fun requestMtu() = isGattNotNull().requestMtu(512)

        suspend fun discoverServicesAsync() = isGattNotNull().discoverServices()

        private suspend fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray): Any {
            isGattNotNull().apply {
                return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    writeDescriptor(descriptor, payload)
                } else {
                    descriptor.value = payload
                    writeDescriptor(descriptor)
                }
            }
        }

        suspend fun connectAsync(device: BluetoothDevice) {
            bluetoothDevice = device

            gatt = device.connectGatt(context, false, this@CustomGattCallback, BluetoothDevice.TRANSPORT_LE)
            isConnected = gatt?.connect() ?: isGattNotNull().connect()
        }

        private fun isGattNotNull(): BluetoothGatt {
            return if(gatt == null) {
                throw Exception("BluetoothGatt is null")
            } else {
                gatt as BluetoothGatt
            }
        }

        suspend fun closeConnection() {
            gatt?.disconnect()
            gatt?.close()
            gatt = null
        }
    }

    companion object {
        private val ViPen_Command_Data_START_MEASURING = byteArrayOf(1, 0) // Starts measurement (presses the button)
        private val ViPen_Command_Data_START_SPECTOR = byteArrayOf(0x10, 0) // Starts measurement (presses the button)
        private val ViPen_Command_Data_STOP_MEASURING = byteArrayOf(2, 0) // Stops measurement (releases the button)
        private val ViPen_Command_Data_OFF = byteArrayOf(3, 0) // Switches off the device
        private val ViPen_Command_Data_IDLE = byteArrayOf(4, 0) // Prevents the device from automatically going to sleep
    }
}

fun ByteArray.toHexString(): String {
    return this.joinToString("") {
        String.format("%02x", it)
    }
}

fun ByteArray.toInt(): Int {
    var result = 0
    for(i in this.indices) {
        result = result or (this[i].toInt() shl 8 * i)
    }
    return result
}
