// TODO - Refatorar o nome do pacote para jobshedules. Está fora do padrão recomendado pelo google.
package br.com.wifimy.jobSchedules

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.os.ResultReceiver
import android.support.v4.app.JobIntentService
import br.com.wifimy.activities.MainActivity
import br.com.wifimy.extensoes.getStringEnderecoIp
import br.com.wifimy.model.DetalhesConexaoHelper
import com.stealthcopter.networktools.ARPInfo
import io.evercam.network.discovery.IpScan
import io.evercam.network.discovery.ScanRange
import io.evercam.network.discovery.ScanResult

class ScannearDispositivosSchedule : JobIntentService() {

    @SuppressLint("WakelockTimeout")
    override fun onHandleWork(intent: Intent) {
        val wakelock = ((getSystemService(Context.POWER_SERVICE) as PowerManager))
                .newWakeLock(1, "WifiScanLockLock")

        wakelock.acquire()

        val detConHelper = DetalhesConexaoHelper(applicationContext)
        val scanRange = ScanRange(detConHelper.gateway.getStringEnderecoIp(),
                detConHelper.mascaraRede.getStringEnderecoIp())
        val total = scanRange.size()
        val receiver = intent.getParcelableExtra(MainActivity.RECEIVER) as ResultReceiver
        val ipScan = IpScan(object : ScanResult {
            var contador = 0
            override fun onActiveIp(ip: String) {
                // não utilizado.
            }

            override fun onIpScanned(p0: String?) {
                synchronized(contador, {
                    val bundle = Bundle()
                    contador += 1
                    bundle.putInt(MainActivity.PROGRESSO_SCAN, contador)
                    bundle.putInt(MainActivity.TOTAL_PROGRESSO, total)
                    receiver.send(Activity.RESULT_OK, bundle)
                })
            }
        })
        ipScan.scanAll(scanRange)

        val bundle = Bundle()
        bundle.putStringArrayList(MainActivity.LISTA_DISPOSITIVOS_ATIVOS, ARPInfo.getAllIPAddressesInARPCache())
        receiver.send(Activity.RESULT_OK, bundle)
        wakelock.release()
    }

    companion object {
        private const val JOB_ID = 1000
        @JvmStatic
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, ScannearDispositivosSchedule::class.java, JOB_ID, work)
        }
    }
}