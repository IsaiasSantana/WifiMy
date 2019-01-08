package br.com.wifimy.fragmentos

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.support.transition.TransitionManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatRadioButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.RelativeLayout
import br.com.wifimy.R
import br.com.wifimy.adapters.ContaCanaisAdapater
import br.com.wifimy.adapters.PontosAcessoAdapter
import br.com.wifimy.extensoes.getTotalPontosAcessoPorCanal
import br.com.wifimy.extensoes.inflar
import br.com.wifimy.extensoes.mostrarAlerta
import br.com.wifimy.extensoes.setTextoTextView
import br.com.wifimy.wifi.PontosAcessoScanner
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.banner_layout.*
import kotlinx.android.synthetic.main.fragment_canais_wifi.*

private const val REQUEST_CODE_PERMISSOES = 1994

/**
 * @author Isaías Santana
 */
class CanaisWifiFragment : Fragment() {

    private var pontosAcessoScanner: PontosAcessoScanner? = null

    private val pontosAcessoAdapter = PontosAcessoAdapter()

    private var contaCanaisAdapater: ContaCanaisAdapater? = null

    private var ordenador: PontosAcessoScanner.Ordenador? = null

    private fun mostrarLoadingView(mostrar: Boolean) {
        if (mostrar) {
            if (loadingView?.visibility == View.VISIBLE) {
                return
            }
            rootLayout?.let { TransitionManager.beginDelayedTransition(rootLayout) }
            loadingView?.visibility = View.VISIBLE
        } else {
            if (loadingView?.visibility == View.GONE) {
                return
            }
            rootLayout?.let { TransitionManager.beginDelayedTransition(rootLayout) }
            loadingView?.visibility = View.GONE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return container?.inflar(R.layout.fragment_canais_wifi, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewAPs?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerViewAPs?.setHasFixedSize(false)
        recyclerViewAPs?.adapter = pontosAcessoAdapter
        tvDescricaoApsAndroid6Superior?.visibility = View.GONE
        mostrarLoadingView(false)
        adView?.loadAd(AdRequest.Builder().build())
        checarPermissoes()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        pontosAcessoScanner?.iniciarScan()
    }

    override fun onPause() {
        super.onPause()
        pontosAcessoScanner?.pararScan()
    }

    override fun onDetach() {
        super.onDetach()
        pontosAcessoScanner?.pararScan()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.pontos_de_acesso_menu, menu)
    }

    private fun montarOrdenador(view: View) {
        val isCrescente = when (view.findViewById<RadioGroup>(R.id.ordemRadioGroup).checkedRadioButtonId) {
            R.id.crescenteRadioButton -> true
            R.id.decrescenteRadioButton -> false
            else -> false
        }
        when (view.findViewById<RadioGroup>(R.id.tipoOrdenacaoRadioGroup).checkedRadioButtonId) {
            R.id.canalRadioButton -> {
                ordenador = PontosAcessoScanner.Ordenador.OrdenadorBuilder()
                        .ordenarPorCanal()
                        .comOrdemCrescente(isCrescente)
            }
            R.id.nivelRadioButton -> {
                ordenador = PontosAcessoScanner.Ordenador.OrdenadorBuilder()
                        .ordenarPorNivelSinal()
                        .comOrdemCrescente(isCrescente)
            }
            R.id.distanciaRadioButton -> {
                ordenador = PontosAcessoScanner.Ordenador.OrdenadorBuilder()
                        .ordenarPorDistancia()
                        .comOrdemCrescente(isCrescente)
            }
        }
        mostrarLoadingView(mostrar = true)
    }

    @SuppressLint("InflateParams")
    private fun mostrarAlertaOrdenacao() {
        fun configurarView(view: View) {
            ordenador?.let {
                view.findViewById<AppCompatRadioButton>(R.id.canalRadioButton).isChecked = it.canal
                view.findViewById<AppCompatRadioButton>(R.id.distanciaRadioButton).isChecked = it.distancia
                view.findViewById<AppCompatRadioButton>(R.id.nivelRadioButton).isChecked = it.nivelSinal
                if (it.isOrdemCrescente) {
                    view.findViewById<AppCompatRadioButton>(R.id.crescenteRadioButton).isChecked = true
                    view.findViewById<AppCompatRadioButton>(R.id.decrescenteRadioButton).isChecked = false
                } else {
                    view.findViewById<AppCompatRadioButton>(R.id.crescenteRadioButton).isChecked = false
                    view.findViewById<AppCompatRadioButton>(R.id.decrescenteRadioButton).isChecked = true
                }
            }
        }

        fun criarAlerta() {
            val builder = AlertDialog.Builder(activity)
            val inflater = activity?.layoutInflater
            inflater?.let {
                val view = inflater.inflate(R.layout.ordernar_pontos_acesso_layout, null)
                configurarView(view)
                builder.setView(view)
                        .setTitle(R.string.ordenar_por)
                        .setPositiveButton(R.string.ordenar) { _, _ ->
                            montarOrdenador(view)
                        }
                        .setNegativeButton(R.string.cancelar) { _, _ ->
                            // sem ação
                        }
                        .create()
                        .show()
            }
        }
        if (isAdded) {
            criarAlerta()
        }
    }

    @SuppressLint("InflateParams")
    private fun abrirAlertaPontosAcessoPorCanal() {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        inflater?.let {
            val view = inflater.inflate(R.layout.ponto_acesso_por_canal_layout, null)
            view.findViewById<LinearLayout>(R.id.tituloPontoAcessoPorCanalLayout)
                    .setTextoTextView(R.id.canalTextView, getString(R.string.canal))
            view.findViewById<LinearLayout>(R.id.tituloPontoAcessoPorCanalLayout)
                    .setTextoTextView(R.id.totalPontosAcessoTextView, getString(R.string.title_notifications))
            val progresso = view.findViewById<RelativeLayout>(R.id.progressoAps)

            contaCanaisAdapater = ContaCanaisAdapater(callback = {
                progresso.visibility = View.GONE
            })

            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewContaAps)
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.setHasFixedSize(false)
            recyclerView.adapter = contaCanaisAdapater

            builder.setView(view)
                    .setTitle(R.string.pontos_acesso_por_canal)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        contaCanaisAdapater?.limpar()
                        contaCanaisAdapater = null
                    }
                    .create()
                    .show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.ordenarApsItem -> {
                mostrarAlertaOrdenacao()
                true
            }
            R.id.contaApsItem -> {
                abrirAlertaPontosAcessoPorCanal()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun irParaTelaConfiguracoes() {
        val intent = Intent()
        intent.action = ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", activity?.packageName, null)
        intent.data = uri
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            context?.mostrarAlerta(mensagem = R.string.indisponivel)
            Crashlytics.logException(e)
        } catch (e: Exception) {
            context?.mostrarAlerta(mensagem = R.string.indisponivel)
            Crashlytics.logException(e)
        }
    }

    private fun mostrarAlertaRationale() {
        if (isAdded) {
            AlertDialog.Builder(context)
                    .setTitle(R.string.permissoes_titulo_rationale)
                    .setMessage(R.string.permissoes_localizacao_rationale)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        // sem ação.
                    }
                    .setNegativeButton(R.string.configuracoes_app_rationale) { _, _ ->
                        irParaTelaConfiguracoes()
                    }
                    .show()
        }
    }

    private fun realizarScan() {
        mostrarLoadingView(true)
        val wifiManager = activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        pontosAcessoScanner = PontosAcessoScanner(wifiManager, object : PontosAcessoScanner.OnScanResult {
            override fun onScanResult(scanResults: List<ScanResult>) {
                val lista = ordenador?.ordenar(ArrayList(scanResults)) ?: ArrayList(scanResults)
                pontosAcessoAdapter.atualizarListaPontosAcesso(lista)
                contaCanaisAdapater?.atualizar(getTotalPontosAcessoPorCanal(scanResults))
                mostrarLoadingView(false)
                if (scanResults.isEmpty()) {
                    tvDescricaoApsAndroid6Superior?.visibility = View.VISIBLE
                } else {
                    if (tvDescricaoApsAndroid6Superior?.visibility == View.VISIBLE) {
                        tvDescricaoApsAndroid6Superior?.visibility = View.GONE
                    }
                }
            }
        }, Handler())
        pontosAcessoScanner?.iniciarScan()
    }

    private fun checarPermissoes() {
        val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)

        fun isPermGarantida(permissao: String): Boolean {
            if (activity != null) {
                return ActivityCompat.checkSelfPermission(activity!!, permissao) == PackageManager.PERMISSION_GRANTED
            }
            return false
        }

        val listaPermsNaoGarantidas = ArrayList<String>()
        for (permissao in perms) {
            if (isPermGarantida(permissao)) {
                continue
            }
            listaPermsNaoGarantidas.add(permissao)
        }
        if (listaPermsNaoGarantidas.isEmpty()) {
            realizarScan()
        } else {
            var deveMostrarRationale = false
            for (permissaoNaoGarantida in listaPermsNaoGarantidas) {
                if (activity != null) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permissaoNaoGarantida)) {
                        deveMostrarRationale = true
                        break
                    }
                }
            }
            if (deveMostrarRationale) {
                mostrarAlertaRationale()
            } else {
                if (activity != null) {
                    requestPermissions(listaPermsNaoGarantidas.toTypedArray(), REQUEST_CODE_PERMISSOES)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSOES -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    var todasPermsAceitas = true
                    for (result in grantResults) {
                        if (result == PackageManager.PERMISSION_GRANTED) {
                            continue
                        }
                        todasPermsAceitas = false
                        break
                    }
                    if (todasPermsAceitas) {
                        realizarScan()
                    } else {
                        mostrarAlertaRationale()
                    }
                } else {
                    mostrarAlertaRationale()
                }
                return
            }
            else -> return
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = CanaisWifiFragment()
    }
}
