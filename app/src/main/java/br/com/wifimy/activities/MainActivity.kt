package br.com.wifimy.activities

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.DhcpInfo
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import br.com.wifimy.R
import br.com.wifimy.extensoes.fecharTeclado
import br.com.wifimy.fragmentos.*
import br.com.wifimy.jobSchedules.ScannearDispositivosSchedule
import br.com.wifimy.model.DetalhesDispositivos
import br.com.wifimy.receivers.ChecaEstadoWifiReceiver
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BuscaDispositivosFragment.OnFragmentInteractionListener {

    private lateinit var buscaDispositivosFragment: BuscaDispositivosFragment

    private val receiver = ChecaEstadoWifiReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.gradiente))
        setContentView(R.layout.activity_main)
        buscaDispositivosFragment = BuscaDispositivosFragment.newInstance()
        navigationView.setOnNavigationItemSelectedListener { item ->
            fecharTeclado()
            when (item.itemId) {
                R.id.navigation_home -> {
                    trocarFragmento(fragment = buscaDispositivosFragment, tag = HOME_FRAG_TAG)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_pesquisa_mac -> {
                    trocarFragmento(fragment = PesquisaEnderecoMacFragment.newInstance(), tag = PESQUISA_MAC_FRAG_TAG)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_canais_wifi -> {
                    trocarFragmento(fragment = CanaisWifiFragment.newInstance(), tag = CANAIS_WIFI_FRAG_TAG)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
        if (savedInstanceState == null) {
            trocarFragmento(HOME_FRAG_TAG, buscaDispositivosFragment)
        }
        adicionarListenerTrocaFragmentos()
        registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun escanearDispositivos(receiver: ResultReceiver) {
        val intent = Intent(this, ScannearDispositivosSchedule::class.java)
        intent.putExtra(RECEIVER, receiver)
        ScannearDispositivosSchedule.enqueueWork(this, intent)
    }

    override fun abrirFragmentoDetalhesDispositivo(detalhesDispositivo: DetalhesDispositivos) {
        val frag = DetalhesFabricanteFragment.newInstance(detalhesDispositivo)
        trocarFragmento(tag = DetalhesFabricanteFragment.TAG, fragment = frag)
    }

    override fun abrirFragmentoMaisInformacoesAp(dhcpInfo: DhcpInfo, nomeRede: String) {
        val frag = MaisInformacoesApFragment.newInstance(dhcpInfo, nomeRede)
        trocarFragmento(tag = MaisInformacoesApFragment.TAG, fragment = frag)
    }

    override fun abrirFragmentoRoteador() {
        trocarFragmento(tag = RoteadorFragment.TAG, fragment = RoteadorFragment.newInstance())
    }

    override fun abrirGeradorSenhaFragment() {
        trocarFragmento(GeradorSenhaFragment.TAG, GeradorSenhaFragment.newInstance())
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) finish() else {
            super.onBackPressed()
            supportFragmentManager.beginTransaction()
                    .show(getCurrentFragment())
                    .commitAllowingStateLoss()
        }
    }

    /**
     * Checa se o fragmento já empilhado é da mesma instância do fragmento do topo da pilha.
     */
    private fun isFragmentoBarraNavegacao(fragmentoEmpilhado: Fragment, fragmentoTopoPilha: Fragment): Boolean {
        return (fragmentoEmpilhado is BuscaDispositivosFragment && fragmentoTopoPilha is BuscaDispositivosFragment) ||
                (fragmentoEmpilhado is CanaisWifiFragment && fragmentoTopoPilha is CanaisWifiFragment) ||
                (fragmentoEmpilhado is PesquisaEnderecoMacFragment && fragmentoTopoPilha is PesquisaEnderecoMacFragment)
    }

    private fun mostrarFragmento(fragmento: Fragment, mostrar: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        if (mostrar) {
            transaction.show(fragmento).commit()
        } else {
            transaction.hide(fragmento).commit()
        }
    }

    fun getCurrentFragment() = supportFragmentManager.findFragmentById(R.id.container)

    private fun trocarFragmento(tag: String, fragment: Fragment) {
        fun removeFragmentoTopo() {
            supportFragmentManager.popBackStackImmediate()
        }

        fun adicionarFragmento() {
            supportFragmentManager.beginTransaction()
                    .add(R.id.container, fragment, tag)
                    .addToBackStack(tag)
                    .commitAllowingStateLoss()
        }
        // Pilha vazia, adiciona o primeiro fragmento.
        if (supportFragmentManager.backStackEntryCount == 0) {
            adicionarFragmento()
        } else {
            // Checa se o fragmento a ser adicionado já está empilhado. Se estiver, o mantém no topo.
            val fragmentoEmpilhado = supportFragmentManager.findFragmentByTag(tag)
            if (fragmentoEmpilhado != null && isFragmentoBarraNavegacao(fragmentoEmpilhado, fragmentoEmpilhado)) {
                var fragmentoTopoPilha = getCurrentFragment()
                while (!isFragmentoBarraNavegacao(fragmentoEmpilhado, fragmentoTopoPilha)) {
                    removeFragmentoTopo()
                    fragmentoTopoPilha = getCurrentFragment()
                }
                mostrarFragmento(fragmentoTopoPilha)
            } else {
                // Adiciona um novo fragmento ao topo da pilha.
                mostrarFragmento(getCurrentFragment(), mostrar = false)
                adicionarFragmento()
            }
        }
    }

    private fun adicionarListenerTrocaFragmentos() {
        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.container)

            if (fragment is BuscaDispositivosFragment) {
                navigationView.menu.findItem(R.id.navigation_home).isChecked = true
                supportActionBar?.title = getString(R.string.encontrar_dispositivos)
                return@addOnBackStackChangedListener
            }

            if (fragment is PesquisaEnderecoMacFragment) {
                navigationView.menu.findItem(R.id.navigation_pesquisa_mac).isChecked = true
                supportActionBar?.title = getString(R.string.pesquisa_de_mac)
                return@addOnBackStackChangedListener
            }

            if (fragment is CanaisWifiFragment) {
                navigationView.menu.findItem(R.id.navigation_canais_wifi).isChecked = true
                supportActionBar?.title = getString(R.string.canais_wifi)
                return@addOnBackStackChangedListener
            }

            if (fragment is DetalhesFabricanteFragment) {
                supportActionBar?.title = getString(R.string.detalhes_fabricante)
                return@addOnBackStackChangedListener
            }

            if (fragment is MaisInformacoesApFragment) {
                supportActionBar?.title = getString(R.string.informacoes_dhcp)
                return@addOnBackStackChangedListener
            }

            if (fragment is RoteadorFragment) {
                supportActionBar?.title = getString(R.string.acesso_roteador)
                return@addOnBackStackChangedListener
            }

            if (fragment is GeradorSenhaFragment) {
                supportActionBar?.title = getString(R.string.gerador_senha)
                return@addOnBackStackChangedListener
            }
        }
    }

    companion object {
        const val PROGRESSO_SCAN = "progresso_scan"
        const val TOTAL_PROGRESSO = "total_progresso"
        const val RECEIVER = "receiver"
        const val LISTA_DISPOSITIVOS_ATIVOS = "lista_dispositivos_ativo"
        private const val HOME_FRAG_TAG = "HOME"
        private const val PESQUISA_MAC_FRAG_TAG = "PESQUISA_ENDERECO_MAC"
        private const val CANAIS_WIFI_FRAG_TAG = "CANAIS_WIFI"
    }
}
