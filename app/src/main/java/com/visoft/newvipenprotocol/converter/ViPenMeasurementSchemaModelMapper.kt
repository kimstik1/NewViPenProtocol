package com.visoft.newvipenprotocol.converter

import com.visoft.newvipenprotocol.ble.VI_PEN_DEVICE_NAME


class ViPenMeasurementSchemaModelMapper {

    fun fromSchemaToModel(schema: DeviceData): ViPenUserData {
        return if (schema.name == VI_PEN_DEVICE_NAME)
            mapForViPen(schema.data)
        else mapForViPen2(schema.data)
    }

    private fun mapForViPen(schema: ByteArray): ViPenUserData {
        schema.run {
            drop(1)
            reverse()
        }
        val temperatureHex = schema.copyOfRange(0, 2).toHexString()
        val excessHex = schema.copyOfRange(3, 5).toHexString()
        val vibrationVelocityHex = schema.copyOfRange(6, 8).toHexString()
        val vibrationAccelerationHex = schema.copyOfRange(9, 11).toHexString()

        val actualTemperature = temperatureHex.toLong(16) / 100f
        val actualExcess = excessHex.toLong(16) / 100f
        val actualAcceleration = vibrationAccelerationHex.toLong(16) / 100f
        val actualVelocity = vibrationVelocityHex.toLong(16) / 100f
        return ViPenUserData(actualTemperature, actualExcess, actualAcceleration, actualVelocity)
    }

    private fun mapForViPen2(schema: ByteArray): ViPenUserData {

        val temperatureHex = schema.copyOfRange(2, 4).toHexString()
        val excessHex = schema.copyOfRange(4, 6).toHexString()
        val vibrationAccelerationHex = schema.copyOfRange(6, 8).toHexString()
        val vibrationVelocityHex = schema.copyOfRange(8, 10).toHexString()

        val actualTemperature = temperatureHex.toLong(16) / 100f
        val actualExcess = excessHex.toLong(16) / 100f
        val actualAcceleration = vibrationAccelerationHex.toLong(16) / 100f
        val actualVelocity = vibrationVelocityHex.toLong(16) / 100f
        return ViPenUserData(actualTemperature, actualExcess, actualAcceleration, actualVelocity)
    }

    private fun ByteArray.toHexString(): String {
        return this.joinToString("") {
            String.format("%02x", it)
        }
    }
}
data class DeviceData(
    val name: String,
    val data: ByteArray
)
