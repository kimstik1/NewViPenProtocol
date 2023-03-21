package com.visoft.newvipenprotocol.data

import com.visoft.newvipenprotocol.data.*

class TVipen2MeasureSetup(
    command: VIPEN2_BT_COMMAND,
    measType : MEAS_TYPE,
    measUnits : MEAS_UNITS,
    allX : SETUP_ALLX,
    dX : SETUP_DX,
    avg: SETUP_AVG
): java.io.Serializable {
    val MEAS_TYPE_SPECTRUM_MASK: UInt = 0u
    val MEAS_TYPE_WAVEFORM_MASK: UInt = 1u

    val SETUP_EXTERNAL_SENSOR: UInt = 0u
    val SETUP_INTERNAL_DAC: UInt = 1u

    val SETUP_READ_MODE: UInt = 0u
    val SETUP_CALIBRATION_MODE: UInt = 1u

    val command: UInt = command.value
    val measType :UInt = measType.value
    val measUnits :UInt = measUnits.value
    val allX :UInt = allX.value
    val dX :UInt = dX.value
    val avg:UInt = avg.value

    val reserv: Array<UInt?> = arrayOfNulls(8)
}