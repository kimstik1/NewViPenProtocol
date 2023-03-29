package com.expertuniversal.vipen.callbacks

import com.expertuniversal.vipen.data.ViPenData
import com.expertuniversal.vipen.data.ViPenUserData

interface OnDataConvertedListener {

    fun receiveViPen2Data(viPenData: ViPenData)

    fun receiveStandardData(userData: ViPenUserData)

    fun onError(code: Int)
}