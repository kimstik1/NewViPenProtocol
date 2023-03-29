package com.expertuniversal.vipen.converter

import android.util.Log
import com.expertuniversal.vipen.data.StVPen2Block
import com.expertuniversal.vipen.data.StVPen2Data
import com.expertuniversal.vipen.data.StVPen2Header
import com.expertuniversal.vipen.data.ViPenUserData
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Converter{

    private val viPenMeasurementSchemaModelMapper = ViPenMeasurementSchemaModelMapper()

    fun convertWave(byteHeader: ByteArray, byteBody: MutableList<ByteArray>): Pair<List<Float>, UInt>{
        val floatList = convertToFloatList(StVPen2Data(convertHeader(byteHeader), convertBlocks(byteBody)))

        return Pair(floatList, floatList.size.toUInt())
    }

    fun convertHeader(byteArray: ByteArray): StVPen2Header {
        val ViPen2_Get_Data_Command = byteArray[0].toInt()
        val ViPen2_Get_Data_Block = byteArray[1].toInt()
        val ViPen2_Get_Wave_ID = byteArray[2].toInt()
        val ViPen2_Data_Blocks = byteArray[3].toInt()
        val Timestamp = byteArray.copyOfRange(4, 8).getUIntAt()
        val Coeff = byteArray.copyOfRange(8, 12).getFloat()
        val DataType = byteArray.copyOfRange(12, 16).getUIntAt()
        val DataUnits = byteArray.copyOfRange(16, 20).getUIntAt()
        val DataLen = byteArray.copyOfRange(20, 24).getUIntAt()
        val DataDX = byteArray.copyOfRange(24, 28).getFloat()
        val SpectrumAvg = byteArray.copyOfRange(28, 32).getInt()
        val SpectrumAvgMax = byteArray.copyOfRange(32, 36).getInt()

        return StVPen2Header(
            viPen2_Get_Data_Command = ViPen2_Get_Data_Command,
            viPen2_Get_Data_Block = ViPen2_Get_Data_Block,
            viPen2_Get_Wave_ID = ViPen2_Get_Wave_ID,
            viPen2_Data_Blocks = ViPen2_Data_Blocks,
            timestamp = Timestamp,
            coeff = Coeff,
            dataType = DataType,
            dataUnits = DataUnits,
            dataLen = DataLen,
            dataDX = DataDX,
            spectrumAvg = SpectrumAvg,
            spectrumAvgMax = SpectrumAvgMax,
        )
    }

    private fun convertBlocks(list: MutableList<ByteArray>): MutableList<StVPen2Block>{

        val finalList: MutableList<StVPen2Block> = mutableListOf()

        for(i in 1 until list.size){

            val currentBlock: ByteArray = list[i]

            val viPenDataBlock: Int = currentBlock[0].toInt()
            val viPenWaveID: Int = currentBlock[1].toInt()

            val shortArray:MutableList<Short> = mutableListOf()

            for(counter in 2 until currentBlock.size){

                if(counter % 2 == 0 && counter != currentBlock.size-1){
                    val byteArray = byteArrayOf(currentBlock[counter], currentBlock[counter+1])
                    shortArray.add(byteArray.getShort())
                }
            }

            finalList.add(
                StVPen2Block(
                ViPenDataBlock = viPenDataBlock,
                ViPenWaveID = viPenWaveID,
                data = shortArray.toList(),
                )
            )
        }

        return finalList
    }

    private fun convertToFloatList(stVPen2Data: StVPen2Data): List<Float>{
        val timeStamp = stVPen2Data.stVPen2Header.timestamp
        val factor = stVPen2Data.stVPen2Header.coeff

        val items: MutableList<Short> = mutableListOf()

        for(i in 0 until stVPen2Data.blocks.size){
            val block = stVPen2Data.blocks[i]
            if(block.ViPenWaveID != stVPen2Data.stVPen2Header.viPen2_Get_Wave_ID)
                throw Exception("Incorrect timestamp!")

            for(j in block.data.indices){
                items.add(block.data[j])
            }
        }

        val result: MutableList<Float> = mutableListOf()

        for(i in items.indices){
            result.add(i, items[i] * factor)
        }

        return result
    }

    fun convertDefaultValue(schema: DeviceData): ViPenUserData =
        viPenMeasurementSchemaModelMapper.fromSchemaToModel(schema)

    private fun ByteArray.getFloat(): Float{
        this.reverse()
        val buffer = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN)
        return buffer.float
    }

    private fun ByteArray.getInt(): Int{
        this.reverse()
        val buffer = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN)
        return buffer.int
    }

    private fun ByteArray.getShort(): Short{
        this.reverse()
        val buffer = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN)
        return buffer.short
    }

    private fun ByteArray.getUIntAt(): UInt{
        this.reverse()
        return((this[0].toUInt() and 0xFFu) shl 24) or
                ((this[1].toUInt() and 0xFFu) shl 16) or
                ((this[2].toUInt() and 0xFFu) shl 8) or
                (this[3].toUInt() and 0xFFu)
    }
}