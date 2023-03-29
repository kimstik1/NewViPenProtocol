package com.expertuniversal.vipen.converter

import android.util.Log
import com.expertuniversal.vipen.ble.VI_PEN_DEVICE_NAME
import com.expertuniversal.vipen.data.ViPenUserData
import java.nio.ByteBuffer
import java.nio.ByteOrder


class ViPenMeasurementSchemaModelMapper {
    fun fromSchemaToModel(schema: DeviceData): ViPenUserData {
        Log.wtf("Schema", schema.toString())
        return if (schema.name == VI_PEN_DEVICE_NAME)
            mapForViPen(schema.data)
        else mapForViPen2(schema.data)
    }

    private fun mapForViPen(schema: ByteArray): ViPenUserData {
        schema.run {
            drop(1)
            reverse()
        }

        return ViPenUserData(
            schema.copyOfRange(0,2).toFloat(),
            schema.copyOfRange(3,5).toFloat(),
            schema.copyOfRange(6,8).toFloat(),
            schema.copyOfRange(9,11).toFloat()
        )
    }

    fun mapForViPen2(schema: ByteArray): ViPenUserData {
        schema.reverse()

        return ViPenUserData(
            schema.copyOfRange(2,4).toFloat(),
            schema.copyOfRange(4,6).toFloat(),
            schema.copyOfRange(6,8).toFloat(),
            schema.copyOfRange(8,10).toFloat()
        )
    }

    private fun ByteArray.toFloat(): Float{
        val bufferedWriter = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN)
        return bufferedWriter.short / 100f
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
