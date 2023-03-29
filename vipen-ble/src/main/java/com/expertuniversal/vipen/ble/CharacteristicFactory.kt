package com.expertuniversal.vipen.ble

class CharacteristicFactory{

    fun getCharacteristicCommands(name: String): CharacteristicCommands {
        return when(name){
            VI_PEN_DEVICE_NAME -> {ViPenCharacteristic()}
            VI_PEN2_DEVICE_NAME -> {ViPen2Characteristic()}
            else -> throw Exception("Wrong device name")
        }
    }

    private inner class ViPenCharacteristic: CharacteristicCommands {
        override fun getStandardWriteCharacteristic(): CharacteristicFactoryData =
            CharacteristicFactoryData(viPenMainServiceUUID_UUID, viPenWrite_UUID, ViPen_Command_Data_START_MEASURING)

        override fun getSpecialStartCharacteristic(): CharacteristicFactoryData =
            throw Exception("Device don't support wave")

        override fun getWaveStartCharacteristic(): CharacteristicFactoryData =
            throw Exception("Device don't support wave")

        override fun getStopCharacteristic(): CharacteristicFactoryData =
            CharacteristicFactoryData(viPenMainServiceUUID_UUID, viPenWrite_UUID, ViPen_Command_Data_STOP_MEASURING)

        override fun getReadCharacteristic(): CharacteristicFactoryData =
            CharacteristicFactoryData(viPenMainServiceUUID_UUID, viPenRead_UUID, byteArrayOf())

        override fun isWaveSupported(): Boolean = false

        override fun getDeviceName(): String = VI_PEN_DEVICE_NAME
    }

    private inner class ViPen2Characteristic: CharacteristicCommands {
        override fun getStandardWriteCharacteristic(): CharacteristicFactoryData =
            CharacteristicFactoryData(viPen2MainServiceUUID_UUID, C_WRITE_UUID, ViPen2_Command_Data_START_MEASURING_SPECTOR)

        override fun getSpecialStartCharacteristic(): CharacteristicFactoryData =
            CharacteristicFactoryData(viPen2MainServiceUUID_UUID, C_WRITE_UUID, ViPen2_Command_Data_START_MEASURING_SIGNAL)

        override fun getWaveStartCharacteristic(): CharacteristicFactoryData =
            CharacteristicFactoryData(viPen2MainServiceUUID_UUID, C_WAV_WRITE_UUID, ViPen_Command_Data_START_SPECTOR)

        override fun getStopCharacteristic(): CharacteristicFactoryData =
            CharacteristicFactoryData(viPen2MainServiceUUID_UUID, C_WRITE_UUID, ViPen_Command_Data_STOP_MEASURING)

        override fun getReadCharacteristic(): CharacteristicFactoryData =
            CharacteristicFactoryData(viPen2MainServiceUUID_UUID, C_READ_UUID, byteArrayOf())

        override fun isWaveSupported(): Boolean = true

        override fun getDeviceName(): String = VI_PEN2_DEVICE_NAME
    }

    companion object {
        private val ViPen2_Command_Data_START_MEASURING_SPECTOR = byteArrayOf(
            1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        )

        private val ViPen2_Command_Data_START_MEASURING_SIGNAL = byteArrayOf(
            1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        )

        private val ViPen_Command_Data_START_SPECTOR = byteArrayOf(0x10, 0) // Starts measurement (presses the button)
        private val ViPen_Command_Data_STOP_MEASURING = byteArrayOf(2, 0) // Stops measurement (releases the button)
        private val ViPen_Command_Data_START_MEASURING = byteArrayOf(1, 0) // Stops measurement (releases the button)

    }

    interface CharacteristicCommands{
        fun getStandardWriteCharacteristic(): CharacteristicFactoryData
        fun getSpecialStartCharacteristic(): CharacteristicFactoryData
        fun getWaveStartCharacteristic(): CharacteristicFactoryData
        fun getStopCharacteristic(): CharacteristicFactoryData
        fun getReadCharacteristic(): CharacteristicFactoryData
        fun isWaveSupported(): Boolean
        fun getDeviceName(): String
    }
}




const val VI_PEN_DEVICE_NAME = "ViPen"
const val viPenMainServiceUUID_UUID = "378B4074-C2B8-45FF-894C-418739E60000"
const val viPenRead_UUID = "3890BE9F-3A5E-459D-B799-102365770001"
const val viPenWrite_UUID = "3890BE9F-3A5E-459D-B799-102365770002"

const val VI_PEN2_DEVICE_NAME = "ViP-2"
const val viPen2MainServiceUUID_UUID = "413557AA-213F-4279-8530-D38E41390000"
const val C_READ_UUID = "42EC1288-B8A0-43DB-AE00-29F942ED0001"
const val C_WRITE_UUID = "42EC1288-B8A0-43DB-AE00-29F942ED0002"
const val C_WAV_WRITE_UUID = "42EC1288-B8A0-43DB-AE00-29F942ED0003"
const val C_WAV_INDICATE_UUID = "42EC1288-B8A0-43DB-AE00-29F942ED0004"

data class CharacteristicFactoryData(
    val serviceUUID: String,
    val characteristicUUID: String,
    val data: ByteArray
)