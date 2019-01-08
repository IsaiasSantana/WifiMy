package br.com.wifimy

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

class DevicesListReceiver(handler: Handler?) : ResultReceiver(handler) {
    var receiver: Receiver? = null

    interface Receiver {
        fun onReceiveResult(resultCode: Int, resultData: Bundle?)
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        super.onReceiveResult(resultCode, resultData)
        receiver?.onReceiveResult(resultCode, resultData)
    }
}