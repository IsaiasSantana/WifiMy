package br.com.wifimy.fragmentos

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import br.com.wifimy.R
import br.com.wifimy.contracts.DetalhesFabricanteFragmentContract
import br.com.wifimy.extensoes.copiarAreaTransferencia
import br.com.wifimy.extensoes.getTextoTextViewEmUmLayout
import br.com.wifimy.extensoes.setTextoTextView
import br.com.wifimy.model.DetalhesDispositivos
import br.com.wifimy.model.Fabricante
import br.com.wifimy.presenters.DetalhesFabricanteFragmentPresenter
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import kotlinx.android.synthetic.main.fragment_detalhes_fabricante.*
import java.io.IOException
import java.nio.charset.Charset

private const val ARG_PARAM1 = "detalheFabricante"

class DetalhesFabricanteFragment : MvpAppCompatFragment(), DetalhesFabricanteFragmentContract.View {

    private lateinit var detalhesDispositivo: DetalhesDispositivos
    private var jsonPaises: String? = null

    @InjectPresenter
    lateinit var presenter: DetalhesFabricanteFragmentPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            detalhesDispositivo = it.getParcelable(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detalhes_fabricante, container, false)
    }

    private fun loadJSONFromAsset(): String? {
        try {
            val inputStream = activity?.assets?.open("paises/${getString(R.string.json_paises)}")
            val size = inputStream?.available()
            size?.let {
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                return String(buffer, Charset.forName("UTF-8"))
            }
        } catch (ex: IOException) {
            return null
        }
        return null
    }

    private fun ajustarTitulosRows() {
        rowEnderecoMac.setTextoTextView(R.id.tvTituloRow, getString(R.string.mac))
        rowEnderecoIp.setTextoTextView(R.id.tvTituloRow, getString(R.string.ip))
        rowPais.setTextoTextView(R.id.tvNomeLabel, getString(R.string.pais))
        rowCompanhia.setTextoTextView(R.id.tvNomeLabel, getString(R.string.companhia))
        rowTipo.setTextoTextView(R.id.tvNomeLabel, getString(R.string.tipo))
    }

    private fun adicionarToqueIconeCopiar() {
        rowEnderecoMac.findViewById<ImageView>(R.id.imgCopiar).setOnClickListener {
            context?.copiarAreaTransferencia(rowEnderecoMac.getTextoTextViewEmUmLayout(R.id.tvConteudoRow),
                    getString(R.string.endereco_mac))
        }

        rowEnderecoIp.findViewById<ImageView>(R.id.imgCopiar).setOnClickListener {
            context?.copiarAreaTransferencia(rowEnderecoIp.getTextoTextViewEmUmLayout(R.id.tvConteudoRow),
                    getString(R.string.endereco_ip))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ajustarTitulosRows()
        adicionarToqueIconeCopiar()
        presenter.validaderDetalhesDispositivo(detalhesDispositivo)
        presenter.buscarOutrasInformacoes(detalhesDispositivo.mac)
        jsonPaises = loadJSONFromAsset()
    }

    override fun ajustarRowEnderecoMacErro(mensagem: Int) {
        ajustarRowEnderecoMac(getString(mensagem))
    }

    override fun ajustarRowEnderecoMac(mensagem: String) {
        rowEnderecoMac.setTextoTextView(R.id.tvConteudoRow, mensagem)
    }

    override fun ajustarRowEnderecoIpErro(mensagem: Int) {
        ajustarRowEnderecoIp(getString(mensagem))
    }

    override fun ajustarRowEnderecoIp(mensagem: String) {
        rowEnderecoIp.setTextoTextView(R.id.tvConteudoRow, mensagem)
    }

    override fun ajustarRowCompanhiaErro(mensagem: Int) {
        ajustarRowCompanhia(getString(mensagem))
    }

    override fun ajustarRowCompanhia(mensagem: String) {
        rowCompanhia.setTextoTextView(R.id.tvConteudoLabel, mensagem)
    }

    override fun setMensagemErroCampoEndereco() {
        tvEndereco.text = getText(R.string.indisponivel)
    }

    override fun setMensagemErroCampoPais() {
        rowPais.setTextoTextView(R.id.tvConteudoLabel, getString(R.string.indisponivel))
    }

    override fun setMensagemErroCampoTipo() {
        rowTipo.setTextoTextView(R.id.tvConteudoLabel, getString(R.string.indisponivel))
    }

    override fun esconderTituloIpMac() {
        tvTituloMacIp.visibility = View.GONE
    }

    override fun preecherCampos(fabricante: Fabricante) {
        ajustarRowCompanhia(fabricante.company)
        val indisponivel = getString(R.string.indisponivel)
        val pais = presenter.getPais(fabricante.country, jsonPaises) ?: indisponivel
        rowPais.setTextoTextView(R.id.tvConteudoLabel, pais)
        rowTipo.setTextoTextView(R.id.tvConteudoLabel, fabricante.type ?: indisponivel)
        tvEndereco.text = fabricante.address ?: indisponivel
    }

    companion object {
        const val TAG = "DetalhesFabricanteFragmentContract"
        @JvmStatic
        fun newInstance(detalhesDispositivo: DetalhesDispositivos) =
                DetalhesFabricanteFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_PARAM1, detalhesDispositivo)
                    }
                }
    }
}
