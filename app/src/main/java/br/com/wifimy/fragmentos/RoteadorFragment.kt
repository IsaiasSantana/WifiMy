package br.com.wifimy.fragmentos

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.Toast

import br.com.wifimy.R
import br.com.wifimy.extensoes.getStringEnderecoIp
import br.com.wifimy.extensoes.isWifiAtivado
import br.com.wifimy.model.DetalhesConexaoHelper
import br.com.wifimy.receivers.ChecaEstadoWifiReceiver
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.banner_layout.*
import kotlinx.android.synthetic.main.fragment_roteador.*
import kotlinx.android.synthetic.main.login_roteador_layout.view.*

class RoteadorFragment : Fragment() {

    private lateinit var detalhesConexaoHelper: DetalhesConexaoHelper

    private var isWifiAtivo = true

    private val receiverLocal = LocalReceiverEstadoWifi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_roteador, container, false)
    }

    @SuppressLint("InflateParams")
    private fun abrirDialogoLogin(handler: HttpAuthHandler?) {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val inputSK = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inflater?.let {
            val view = inflater.inflate(R.layout.login_roteador_layout, null)
            inputSK?.showSoftInput(view.etNomeUsuario, 1)
            inputSK?.showSoftInput(view.etSenha, 1)
            builder.setView(view)
                    .setCancelable(false)
                    .setPositiveButton(R.string.logar) {_, _ ->
                        handler?.proceed(view.etNomeUsuario.text.toString(), view.etSenha.text.toString())
                    }
                    .setNegativeButton(R.string.cancelar) {dialog, _ ->
                        handler?.cancel()
                        dialog.dismiss()
                    }
                    .create()
                    .show()
        }
    }

    inner class WebViewCliente : WebViewClient() {
        override fun onReceivedHttpAuthRequest(view: WebView?,
                                               handler: HttpAuthHandler?,
                                               host: String?,
                                               realm: String?) {
            abrirDialogoLogin(handler)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            loadingTelaRoteador?.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            loadingTelaRoteador?.visibility = View.GONE
        }
    }

    private inner class LocalReceiverEstadoWifi : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val isWifiAtivado = intent.getBooleanExtra(ChecaEstadoWifiReceiver.ESTADO_WIFI_RECEIVER_EXTRA,
                        false)
                isWifiAtivo = isWifiAtivado
                if (!isWifiAtivado) {
                    webViewNavegador?.visibility = View.GONE
                    tvWifiDesativado?.visibility = View.VISIBLE
                } else {
                    webViewNavegador?.visibility = View.VISIBLE
                    tvWifiDesativado?.visibility = View.GONE
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingTelaRoteador.visibility = View.GONE
        tvWifiDesativado.visibility = View.GONE
        context?.let {
            detalhesConexaoHelper = DetalhesConexaoHelper(it)
            webViewNavegador.webViewClient = WebViewCliente()
            webViewNavegador.settings.javaScriptEnabled = true
            webViewNavegador.settings.builtInZoomControls = true
            webViewNavegador.settings.displayZoomControls = false
            webViewNavegador.settings.loadWithOverviewMode = true
            webViewNavegador.settings.useWideViewPort = true
            webViewNavegador.loadUrl("http://${detalhesConexaoHelper.gateway.getStringEnderecoIp()}/")
        }
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        isWifiAtivo = isWifiAtivado(context)
        if (!isWifiAtivo) {
            webViewNavegador?.visibility = View.GONE
            tvWifiDesativado?.visibility = View.VISIBLE
        }
        if (activity != null) {
            val intentFilter = IntentFilter(ChecaEstadoWifiReceiver.MUDANCA_ESTADO_WIFI_ACTION)
            LocalBroadcastManager.getInstance(activity!!.applicationContext)
                    .registerReceiver(receiverLocal, intentFilter)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_roteador, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.mais_informacoes_roteador -> {
                if (isWifiAtivo) {
                    webViewNavegador?.reload()
                } else {
                    Toast.makeText(context, R.string.wifi_desativado, Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (activity != null) {
            LocalBroadcastManager.getInstance(activity!!.applicationContext)
                    .unregisterReceiver(receiverLocal)
        }
    }

    companion object {
        const val TAG = "RoteadorFragment"
        @JvmStatic
        fun newInstance() = RoteadorFragment()
    }
}
