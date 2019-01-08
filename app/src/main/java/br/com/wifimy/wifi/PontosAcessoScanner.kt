package br.com.wifimy.wifi

import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Handler
import br.com.wifimy.extensoes.calcularDistanciaDoPontoDeAcesso
import br.com.wifimy.extensoes.converteFrequenciaParaNumeroCanal
import com.crashlytics.android.Crashlytics

class PontosAcessoScanner(private val wifiManager: WifiManager,
                          private val listener: OnScanResult?,
                          handler: Handler) : APScannerContract {

    private val scanner: Scanner = Scanner(handler, this)

    override fun iniciarScan() {
        scanner.iniciarScan()
    }

    override fun pararScan() {
        scanner.pararScan()
    }

    override fun atualizar() {
        try {
            if (!wifiManager.isWifiEnabled) {
                wifiManager.isWifiEnabled = true
            }
            listener?.onScanResult(wifiManager.scanResults)
            wifiManager.startScan()
        } catch (ex: Exception) {
            // não usado.
        }
    }

    override fun isRunning() = scanner.isRunning

    interface OnScanResult {
        fun onScanResult(scanResults: List<ScanResult>)
    }

    class Ordenador private constructor(val nivelSinal: Boolean,
                                        val distancia: Boolean,
                                        val canal: Boolean,
                                        val isOrdemCrescente: Boolean) {

        class OrdenadorBuilder {
            private var nivelSinal = false
            private var distancia = false
            private var canal = false
            private var isOrdemCrescente = false

            interface MetodoOrdenacao {
                fun comOrdemCrescente(ordemCrescente: Boolean): Ordenador
            }

            fun ordenarPorNivelSinal(): MetodoOrdenacao {
                nivelSinal = true
                return object : MetodoOrdenacao {
                    override fun comOrdemCrescente(ordemCrescente: Boolean): Ordenador {
                        isOrdemCrescente = ordemCrescente
                        return Ordenador(nivelSinal, distancia, canal, isOrdemCrescente)
                    }
                }
            }

            fun ordenarPorDistancia(): MetodoOrdenacao {
                distancia = true
                return object : MetodoOrdenacao {
                    override fun comOrdemCrescente(ordemCrescente: Boolean): Ordenador {
                        isOrdemCrescente = ordemCrescente
                        return Ordenador(nivelSinal, distancia, canal, isOrdemCrescente)
                    }
                }
            }

            fun ordenarPorCanal(): MetodoOrdenacao {
                canal = true
                return object : MetodoOrdenacao {
                    override fun comOrdemCrescente(ordemCrescente: Boolean): Ordenador {
                        isOrdemCrescente = ordemCrescente
                        return Ordenador(nivelSinal, distancia, canal, isOrdemCrescente)
                    }
                }
            }
        }

        fun ordenar(scanResults: List<ScanResult>): ArrayList<ScanResult> {
            try {
                if (nivelSinal) {
                    return if (isOrdemCrescente) {
                        ArrayList(scanResults.sortedByDescending { it.level })
                    } else {
                        ArrayList(scanResults.sortedBy { it.level })
                    }
                }
                if (distancia) {
                    return if (isOrdemCrescente) {
                        ArrayList(scanResults.sortedBy { it.calcularDistanciaDoPontoDeAcesso() })
                    } else {
                        ArrayList(scanResults.sortedByDescending { it.calcularDistanciaDoPontoDeAcesso() })
                    }
                }
                if (canal) {
                    return if (isOrdemCrescente) {
                        ArrayList(scanResults.sortedBy { it.converteFrequenciaParaNumeroCanal() })
                    } else {
                        ArrayList(scanResults.sortedByDescending { it.converteFrequenciaParaNumeroCanal() })
                    }
                }
            } catch (e: Exception) {
                Crashlytics.logException(e)
            }
            return ArrayList(scanResults)
        }
    }
}