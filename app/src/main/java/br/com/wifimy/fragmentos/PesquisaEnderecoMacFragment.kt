package br.com.wifimy.fragmentos

import android.os.Bundle
import android.support.transition.TransitionManager
import android.view.*
import android.widget.Toast
import br.com.wifimy.R
import br.com.wifimy.contracts.PesquisaEnderecoMacContract
import br.com.wifimy.extensoes.abrirNavegador
import br.com.wifimy.extensoes.inflar
import br.com.wifimy.extensoes.setTextoTextView
import br.com.wifimy.model.Fabricante
import br.com.wifimy.presenters.PesquisaEnderecoMacPresenter
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.banner_layout.*
import kotlinx.android.synthetic.main.busca_endereco_mac_laytou.*
import kotlinx.android.synthetic.main.detalhes_fabricante_layout.*

class PesquisaEnderecoMacFragment : MvpAppCompatFragment(), PesquisaEnderecoMacContract.View {

    @InjectPresenter
    lateinit var presenter: PesquisaEnderecoMacPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.pesquisa_mac_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.macVendorApi -> {
                abrirNavegador("https://macvendors.co/")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return container?.inflar(R.layout.fragment_pesquisa_endereco_mac, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        detalhesFabricanteLayout.visibility = View.GONE
        progressoBar.visibility = View.GONE
        btnPesquisar.setOnClickListener { presenter.pesquisarEnderecoMac(etMac.text.toString()) }
        ajustarTitulosRows()
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun ajustarTitulosRows() {
        layoutRow1.setTextoTextView(R.id.tvNomeLabel, getString(R.string.companhia))
        layoutRow2.setTextoTextView(R.id.tvNomeLabel, getString(R.string.mac_prefixo))
        layoutRow3.setTextoTextView(R.id.tvNomeLabel, getString(R.string.tipo))
        layoutRow4.setTextoTextView(R.id.tvNomeLabel, getString(R.string.pais))
    }

    private fun mostrarProgresso(visibilidade: Int) {
        TransitionManager.beginDelayedTransition(rootLayout)
        progressoBar.visibility = visibilidade
    }

    override fun bloquearBotaoPesquisar() {
        btnPesquisar.alpha = 0.7f
        btnPesquisar.isEnabled = false
    }

    override fun liberarBotaoPesquisar() {
        btnPesquisar.alpha = 1.0f
        btnPesquisar.isEnabled = true
    }

    override fun mostrarLoading() {
        mostrarProgresso(View.VISIBLE)
    }

    override fun fecharLoading() {
        mostrarProgresso(View.GONE)
    }

    override fun fecharLayoutDetalhesFabricante() {
        TransitionManager.beginDelayedTransition(rootLayout)
        detalhesFabricanteLayout.visibility = View.GONE
    }

    override fun mostrarLayoutDetalhesFabricante(fabricante: Fabricante) {
        TransitionManager.beginDelayedTransition(rootLayout)
        val msgIndisponivel = getString(R.string.indisponivel)
        layoutRow1.setTextoTextView(R.id.tvConteudoLabel, fabricante.company)
        layoutRow2.setTextoTextView(R.id.tvConteudoLabel, fabricante.prefixoMAC)
        layoutRow3.setTextoTextView(R.id.tvConteudoLabel, fabricante.type ?: msgIndisponivel)
        layoutRow4.setTextoTextView(R.id.tvConteudoLabel, fabricante.country ?: msgIndisponivel)
        tvEnderecoFabricante.text = fabricante.address ?: msgIndisponivel
        detalhesFabricanteLayout.visibility = View.VISIBLE
    }

    override fun mostrarMensagemErro(mensagem: Int) {
        Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
    }

    companion object {
        @JvmStatic
        fun newInstance() = PesquisaEnderecoMacFragment()
    }
}
