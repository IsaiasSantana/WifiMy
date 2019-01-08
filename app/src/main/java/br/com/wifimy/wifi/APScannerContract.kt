package br.com.wifimy.wifi

interface APScannerContract {
    fun iniciarScan()

    fun pararScan()

    fun atualizar()

    fun isRunning(): Boolean
}