package br.com.wifimy.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import br.com.wifimy.extensoes.isWifiAtivado

class ChecaEstadoWifiReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val intent2 = Intent(MUDANCA_ESTADO_WIFI_ACTION)
        intent2.putExtra(ESTADO_WIFI_RECEIVER_EXTRA, isWifiAtivado(context))
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent2)
    }

    companion object {
        const val MUDANCA_ESTADO_WIFI_ACTION = "estado_wifi_mudou"
        const val ESTADO_WIFI_RECEIVER_EXTRA = "extra_estado_WIFI"
    }
}