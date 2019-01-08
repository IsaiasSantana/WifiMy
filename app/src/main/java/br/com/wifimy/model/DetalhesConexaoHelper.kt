package br.com.wifimy.model

import android.content.Context
import android.net.wifi.WifiManager
import br.com.wifimy.R
import br.com.wifimy.extensoes.getEnderecoMacInterfaceRede
import br.com.wifimy.extensoes.getStringEnderecoIp
import com.stealthcopter.networktools.ARPInfo
import java.io.Serializable

class DetalhesConexaoHelper(context: Context) : Serializable {
    val dns1: Int
    val dns2: Int
    val gateway: Int
    val enderecoIp: Int
    val bssid: String
    val ssid: String
    val mascaraRede: Int

    init {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        dns1 = wifiManager.dhcpInfo.dns1
        dns2 = wifiManager.dhcpInfo.dns2
        gateway = wifiManager.dhcpInfo.gateway
        enderecoIp = wifiManager.connectionInfo.ipAddress
        bssid = wifiManager.connectionInfo.bssid ?: context.getString(R.string.endereco_mac_desconhecido) ?: "--"
        ssid = if (wifiManager.connectionInfo.ssid != null) {
            wifiManager.connectionInfo.ssid.replace("\"", "")
                    .replace("<", "")
                    .replace(">", "")
        } else {
            context.getString(R.string.ssid_desconhecido) ?: "--"
        }
        mascaraRede = wifiManager.dhcpInfo.netmask
    }

    fun getEnderecoMacRoteador() = ARPInfo.getMACFromIPAddress(gateway.getStringEnderecoIp())

    companion object {
        @JvmStatic
        fun getInterfaceMacAddress(context: Context): String {
            return getEnderecoMacInterfaceRede()
                    ?: context.getString(R.string.endereco_mac_desconhecido)
        }
    }
}
