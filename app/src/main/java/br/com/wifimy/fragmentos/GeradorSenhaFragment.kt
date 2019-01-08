package br.com.wifimy.fragmentos

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import br.com.wifimy.R
import br.com.wifimy.extensoes.copiarAreaTransferencia
import br.com.wifimy.extensoes.inflar
import br.com.wifimy.wifi.GeradorSenha
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.banner_layout.*
import kotlinx.android.synthetic.main.gerador_senha_layout.*

class GeradorSenhaFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return container?.inflar(R.layout.fragment_gerador_senha, false)
    }

    private fun gerarSenha() {
        fun getComprimentoSenha(): Int {
            return when (radioGroupTamSenha?.checkedRadioButtonId) {
                R.id.radioTamSenha8 -> 8
                R.id.radioTamSenha16 -> 16
                R.id.radioTamSenha24 -> 24
                else -> 8
            }
        }

        val senhaGerada = GeradorSenha.Builder()
                .temLetrasMaiusculas(checkLetrasMaiusculas?.isChecked ?: false)
                .temLetrasMinusculas(checkLetrasMinusculas?.isChecked ?: false)
                .temCaracteresEspecias(checkCaracteresEspeciais?.isChecked ?: false)
                .temDigitos(checkNumeros?.isChecked ?: false)
                .build()
                .gerarSenha(getComprimentoSenha())

        senhaGeradaTextView?.text = if (senhaGerada.isEmpty()) getString(R.string.dica_senha) else senhaGerada
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gerarSenhaButton?.setOnClickListener {
            gerarSenha()
        }
        imgCopiarSenha?.setOnClickListener {
            val senhaGerada = senhaGeradaTextView.text.toString()
            if (senhaGerada.isNotEmpty() && senhaGerada != getString(R.string.dica_senha)) {
                context?.copiarAreaTransferencia(senhaGerada, getString(R.string.gerar_senha))
            } else {
                Toast.makeText(context, R.string.senha_nao_gerada, Toast.LENGTH_SHORT).show()
            }
        }
        val adRequest = AdRequest.Builder().build()
        adView?.loadAd(adRequest)
    }

    companion object {
        const val TAG = "GERADOR_SENHA_FRAGMENT"

        @JvmStatic
        fun newInstance() = GeradorSenhaFragment()
    }
}
