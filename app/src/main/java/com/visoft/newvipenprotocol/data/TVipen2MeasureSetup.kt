package com.visoft.newvipenprotocol.data

class TVipen2MeasureSetup: java.io.Serializable {

    val MEAS_TYPE_SPECTRUM_MASK: UInt = 0u
    val MEAS_TYPE_WAVEFORM_MASK: UInt = 1u

    val SETUP_EXTERNAL_SENSOR: UInt = 0u
    val SETUP_INTERNAL_DAC: UInt = 1u

    val SETUP_READ_MODE: UInt = 0u
    val SETUP_CALIBRATION_MODE: UInt = 1u

    val command: UInt = VIPEN2_BT_COMMAND.START.value
    val measType :UInt = MEAS_TYPE.SPECTRUM.value
    val measUnits :UInt = MEAS_UNITS.VELOCITY.value
    val allX :UInt = SETUP_ALLX.ALLX_8K.value
    val dX :UInt = SETUP_DX.DX_25600_HZ.value
    val avg:UInt = SETUP_AVG.AVG_4_STOP.value

    val reserv: Array<UInt?> = arrayOfNulls(8)
}