package br.com.wifimy.fragmentos

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.net.DhcpInfo
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.annotation.StringRes
import android.support.transition.TransitionManager
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Toast
import br.com.wifimy.DevicesListReceiver
import br.com.wifimy.R
import br.com.wifimy.activities.MainActivity.Companion.LISTA_DISPOSITIVOS_ATIVOS
import br.com.wifimy.activities.MainActivity.Companion.PROGRESSO_SCAN
import br.com.wifimy.activities.MainActivity.Companion.TOTAL_PROGRESSO
import br.com.wifimy.adapters.ListaDispositivosAdapter
import br.com.wifimy.contracts.BuscaDispositivosFragmentContract
import br.com.wifimy.extensoes.*
import br.com.wifimy.model.DetalhesConexaoHelper
import br.com.wifimy.model.DetalhesDispositivos
import br.com.wifimy.presenters.BuscaDispositivosFragmentPresenter
import br.com.wifimy.receivers.ChecaEstadoWifiReceiver
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import kotlinx.android.synthetic.main.arq_progress.*
import kotlinx.android.synthetic.main.cabecalho.*
import kotlinx.android.synthetic.main.lista_dispositivos.*
import kotlinx.android.synthetic.main.politica_privacidade_layout.view.*
import kotlinx.android.synthetic.main.sobre_layout.view.*

class BuscaDispositivosFragment : MvpAppCompatFragment(),
        BuscaDispositivosFragmentContract.View,
        ListaDispositivosAdapter.OnItemClick {

    private lateinit var receiver: DevicesListReceiver

    private lateinit var infoConexao: DetalhesConexaoHelper

    private lateinit var dispositivosAdapater: ListaDispositivosAdapter

    private lateinit var wifiManager: WifiManager

    private lateinit var anuncioTelaCheia: InterstitialAd

    private var listener: OnFragmentInteractionListener? = null

    private var wifiAtivado = true

    private var escaneando = false

    private val receiverLocal = LocalReceiverEstadoWifi()

    @InjectPresenter
    lateinit var presenter: BuscaDispositivosFragmentPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return container?.inflar(R.layout.fragment_busca_dispositivos, false)
    }

    override fun onClickItem(detalhesDispositivo: DetalhesDispositivos) {
        listener?.abrirFragmentoDetalhesDispositivo(detalhesDispositivo)
    }

    override fun fecharProgressoScan() {
        setVisibilidadeProgressoLayout(View.GONE)
        fabPesquisar?.visibility = View.VISIBLE
    }

    override fun mostrarProgressoScan() {
        setVisibilidadeProgressoLayout(View.VISIBLE)
        fabPesquisar?.visibility = View.GONE
    }

    private fun setVisibilidadeProgressoLayout(visibilidade: Int) {
        rootLayout?.let {
            TransitionManager.beginDelayedTransition(rootLayout)
        }
        progressoLayout?.visibility = visibilidade
    }

    private fun ajustarCabecalho() {
        with(infoConexao) {
            tvNomeRede?.text = ssid
            tvEnderecoIp?.text = gateway.getStringEnderecoIp()
            tvEnderecoMac?.text = getEnderecoMacRoteador() ?: getString(R.string.endereco_mac_desconhecido)
        }
    }

    private fun ajustarRecyclerView() {
        dispositivosAdapater = ListaDispositivosAdapter(detalhesConexaoHelper = infoConexao, listener = this)
        recyclerViewDispositivos?.adapter = dispositivosAdapater
        recyclerViewDispositivos?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerViewDispositivos?.setHasFixedSize(false)
    }

    private fun inicializarAnuncioTelaCheia() {
        anuncioTelaCheia = InterstitialAd(context)
        anuncioTelaCheia.adUnitId = getString(R.string.id_ad_interstitial)
        anuncioTelaCheia.adListener = object : AdListener() {
            override fun onAdClosed() {
                super.onAdClosed()
                anuncioTelaCheia.loadAd(AdRequest.Builder().build())
                exibirTituloCarregandoAnuncio(false)
                if (escaneando) {
                    escaneando = false
                }
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                try {
                    if (escaneando && isAdded) {
                        anuncioTelaCheia.show()
                    }
                } catch (e: Exception) {
                    Crashlytics.logException(e)
                }
            }
        }
        anuncioTelaCheia.loadAd(AdRequest.Builder().build())
    }

    private fun exibirTituloCarregandoAnuncio(mostrar: Boolean) {
        rootLayout?.let {
            TransitionManager.beginDelayedTransition(rootLayout)
        }
        if (mostrar) {
            tvCarregandoAnuncio?.visibility = View.VISIBLE
        } else {
            tvCarregandoAnuncio?.visibility = View.GONE
        }
    }

    private fun mostrarAnuncioTelaCheia() {
        if (anuncioTelaCheia.isLoaded) {
            exibirTituloCarregandoAnuncio(true)
            Handler().postDelayed({
                anuncioTelaCheia.show()
            }, 2000)
        } else {
            exibirTituloCarregandoAnuncio(true)
            anuncioTelaCheia.loadAd(AdRequest.Builder().build())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inicializarAnuncioTelaCheia()
        ajustarCabecalho()
        ajustarRecyclerView()
        circle_progress?.max = 100
        tvCarregandoAnuncio?.visibility = View.GONE
        fabPesquisar?.setOnClickListener {
            escaneando = true
            dispositivosAdapater.removerTudo()
            listener?.escanearDispositivos(receiver)
            mostrarProgressoScan()
            mostrarAnuncioTelaCheia()
        }
        progressoLayout?.visibility = View.GONE
        setupReceiver()
        if (activity != null) {
            val intentFilter = IntentFilter(ChecaEstadoWifiReceiver.MUDANCA_ESTADO_WIFI_ACTION)
            LocalBroadcastManager.getInstance(activity!!.applicationContext)
                    .registerReceiver(receiverLocal, intentFilter)
        }
        wifiManager = activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
            infoConexao = DetalhesConexaoHelper(context.applicationContext)
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun mostrarListaDispositivosEncontrados(listaDispositivos: ArrayList<DetalhesDispositivos?>) {
        if (isAdded) {
            val mac = if (context != null) {
                DetalhesConexaoHelper.getInterfaceMacAddress(context!!)
            } else {
                getString(R.string.endereco_mac_desconhecido)
            }
            val deviceUsuario = DetalhesDispositivos(infoConexao.enderecoIp.getStringEnderecoIp(), mac,
                    getString(R.string.seu_dispositivo))
            listaDispositivos.add(deviceUsuario)
            listaDispositivos.add(null)
            dispositivosAdapater.atualizarListaDispositivos(listaDispositivos)
        }
    }

    private fun updateProgress(progress: Int, total: Int) {
        circle_progress?.apply { setProgress(((100 * progress) / total)) }
    }

    private fun setupReceiver() {
        receiver = DevicesListReceiver(Handler())
        receiver.receiver = object : DevicesListReceiver.Receiver {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    val progresso = resultData.getInt(PROGRESSO_SCAN, -1)
                    val totalProgresso = resultData.getInt(TOTAL_PROGRESSO, -1)
                    if (progresso != -1 && totalProgresso != -1) {
                        updateProgress(progresso, totalProgresso)
                    } else {
                        updateProgress(totalProgresso, totalProgresso)
                        val listaDispAtivo = resultData.getStringArrayList(LISTA_DISPOSITIVOS_ATIVOS)
                        presenter.encontrarFabricantes(listaDispAtivo)
                        escaneando = false
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.busca_dispositivos_menu, menu)
    }

    @SuppressLint("InflateParams")
    private fun abrirDialogoSobre() {
        if (isAdded) {
            val builder = AlertDialog.Builder(activity)
            val inflater = activity?.layoutInflater
            inflater?.let { it ->
                val view = inflater.inflate(R.layout.sobre_layout, null)
                view.tvIconCredit?.setOnClickListener { abrirNavegador("https://www.flaticon.com/") }
                view.tvIconCredit?.text = bold(getString(R.string.direito_icone), arrayOf("Pixel Buddha", "www.flaticon.com"))
                view.tvMacVendor_co?.setOnClickListener { abrirNavegador("https://macvendors.co/") }
                view.tvMacVendor_co?.text = bold(getString(R.string.mac_vendor_api), arrayOf(getString(R.string.mac_vendor_api)))
                view.tvMeuGitHub?.text = bold(getString(R.string.my_email), arrayOf("https://github.com/IsaiasSantana"))
                view.tvMeuGitHub?.setOnClickListener { abrirNavegador("https://github.com/IsaiasSantana") }
                builder.setView(view)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            // sem ação
                        }
                        .create()
                        .show()
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun abrirDialogoPoliticaPrivacidade() {
        if (isAdded) {
            val builder = AlertDialog.Builder(activity)
            val inflater = activity?.layoutInflater
            inflater?.let {
                val view = inflater.inflate(R.layout.politica_privacidade_layout, null)
                view.webView.loadUrl("file:///android_asset/privacy_policy.html")
                builder.setView(view)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            // sem ação
                        }
                        .create()
                        .show()
            }
        }
    }

    private fun abrirDialogoApoie() {
        if (isAdded) {
            AlertDialog.Builder(activity)
                    .setTitle(R.string.apoie)
                    .setMessage(R.string.mensagem_exibir_anuncio)
                    .setPositiveButton(R.string.tudo_bem) { _, _ ->
                        if (anuncioTelaCheia.isLoaded) {
                            anuncioTelaCheia.show()
                        } else {
                            context?.mostrarAlerta(R.string.anuncio_nao_carregado)
                        }
                    }
                    .setNegativeButton(R.string.agora_nao) { _, _ ->
                        // sem ação.
                    }
                    .create()
                    .show()
        }
    }

    private fun abrirLoja() {
        val nomePacote = "br.com.wifimy"
        var intent: Intent
        try {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$nomePacote"))
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$nomePacote"))
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.mais_informacoes -> {
                if (wifiAtivado) {
                    listener?.abrirFragmentoMaisInformacoesAp(wifiManager.dhcpInfo, infoConexao.ssid)
                } else {
                    mostrarMensagemErro(R.string.wifi_desativado)
                }
                true
            }
            R.id.sobre -> {
                abrirDialogoSobre()
                true
            }
            R.id.politicaPrivacidade -> {
                abrirDialogoPoliticaPrivacidade()
                true
            }
            R.id.roteador -> {
                if (wifiAtivado) {
                    listener?.abrirFragmentoRoteador()
                } else {
                    mostrarMensagemErro(R.string.wifi_desativado)
                }
                true
            }
            R.id.apoieItem -> {
                abrirDialogoApoie()
                true
            }
            R.id.avaliarItem -> {
                abrirLoja()
                true
            }
            R.id.geradorSenhaItem -> {
                listener?.abrirGeradorSenhaFragment()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        menu?.findItem(R.id.mais_informacoes)?.isVisible = wifiAtivado
        menu?.findItem(R.id.mais_informacoes)?.isEnabled = wifiAtivado
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        if (activity != null) {
            LocalBroadcastManager.getInstance(activity!!.applicationContext)
                    .unregisterReceiver(receiverLocal)
        }
    }

    private fun mostrarMensagemErro(@StringRes mensagem: Int) {
        Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
    }

    private fun exibirCabecalhoInfoRede(visibilidade: Int) {
        if (isAdded) {
            rootLayout?.let {
                TransitionManager.beginDelayedTransition(it)
                tvNomeRede?.visibility = visibilidade
                layoutCabecalhoIp?.visibility = visibilidade
                layoutCabecalhoMac?.visibility = visibilidade
            }
        }
    }

    private inner class LocalReceiverEstadoWifi : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val isWifiAtivado = intent.getBooleanExtra(ChecaEstadoWifiReceiver.ESTADO_WIFI_RECEIVER_EXTRA,
                        false)
                val tempoEspera: Long = 2000
                if (!isWifiAtivado) {
                    if (context != null) {
                        fabPesquisar?.isEnabled = false
                        fabPesquisar?.alpha = 0.7f
                        mostrarMensagemErro(R.string.wifi_desativado)
                        exibirCabecalhoInfoRede(View.GONE)
                        wifiAtivado = false
                        activity?.invalidateOptionsMenu()
                    }
                } else {
                    if (context != null) {
                        Handler().postDelayed({
                            fabPesquisar?.isEnabled = true
                            fabPesquisar?.alpha = 1.0f
                            infoConexao = DetalhesConexaoHelper(context)
                            ajustarCabecalho()
                            exibirCabecalhoInfoRede(visibilidade = View.VISIBLE)
                            wifiAtivado = true
                            activity?.invalidateOptionsMenu()
                        }, tempoEspera)
                    }
                }
            }
        }
    }

    interface OnFragmentInteractionListener {
        fun escanearDispositivos(receiver: ResultReceiver)

        fun abrirFragmentoDetalhesDispositivo(detalhesDispositivo: DetalhesDispositivos)

        fun abrirFragmentoMaisInformacoesAp(dhcpInfo: DhcpInfo, nomeRede: String)

        fun abrirFragmentoRoteador()

        fun abrirGeradorSenhaFragment()
    }

    companion object {
        @JvmStatic
        fun newInstance() = BuscaDispositivosFragment()
    }
}
