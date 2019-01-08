package br.com.wifimy.wifi

import android.os.Handler

class Scanner(private val handler: Handler,
              private val scanner: APScannerContract) : Runnable {

    var isRunning = false
    private val atrasoProximoScan: Long = 5000

    init {
        iniciarScan()
    }

    fun iniciarScan() {
        if (!isRunning) {
            proximoScan()
        }
    }

    private fun proximoScan() {
        pararScan()
        isRunning = true
        handler.postDelayed(this, atrasoProximoScan)
    }

    fun pararScan() {
        handler.removeCallbacks(this)
        isRunning = false
    }

    override fun run() {
        scanner.atualizar()
        proximoScan()
    }
}