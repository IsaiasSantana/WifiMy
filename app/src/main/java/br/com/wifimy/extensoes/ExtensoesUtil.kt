package br.com.wifimy.extensoes

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.ScanResult
import android.os.Build
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import br.com.wifimy.R
import io.evercam.network.discovery.IpTranslator
import java.net.NetworkInterface
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.log10

fun ViewGroup.inflar(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun View.setTextoTextView(id: Int, mensagem: String) {
    findViewById<TextView>(id).text = mensagem
}

fun View.getTextoTextViewEmUmLayout(id: Int): String {
    return findViewById<TextView>(id).text.toString()
}

fun Activity.fecharTeclado() {
    findViewById<View>(android.R.id.content)?.apply {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputManager?.hideSoftInputFromWindow(windowToken, 0)
    }
}

fun Int.getStringEnderecoIp() = IpTranslator.getIpFromIntSigned(this)

fun Long.paraHoras(): Long {
    return TimeUnit.MILLISECONDS.toHours(this) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(this))
}

fun Long.paraMinutos(): Long {
    val totalMinutos = TimeUnit.MILLISECONDS.toMinutes(this)
    val totalHorasMinutos = TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(this))
    return totalMinutos - totalHorasMinutos
}

fun Long.paraDias() = TimeUnit.MILLISECONDS.toDays(this)

fun Double.comUmaCasaDecimal() = DecimalFormat("#.#").format(this)

fun String.prefixoEnderecoMAC(): String {
    val split = this.split(":")
    return if (split.size >= 3) {
        "${split[0]}:${split[1]}:${split[2]}".toLowerCase(Locale.FRANCE)
    } else {
        ""
    }
}

fun isWifiAtivado(context: Context?): Boolean {
    val infoRede = context?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val redeAtiva = infoRede.activeNetworkInfo
    return redeAtiva?.isConnected == true && redeAtiva.type == ConnectivityManager.TYPE_WIFI
}

fun getEnderecoMacInterfaceRede(): String? {
    try {
        for (interfaceRede in Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (interfaceRede.name.equals("wlan0", ignoreCase = true)) {
                interfaceRede.hardwareAddress?.let {
                    val builder = StringBuilder()
                    for (byte in it) {
                        builder.append("${Integer.toHexString((byte.toInt() and 255))}:")
                    }
                    if (builder.isNotEmpty()) {
                        builder.deleteCharAt(builder.length - 1)
                    }
                    return builder.toString()
                }
            }
        }
        return null
    } catch (e: Exception) {
        return null
    }
}

fun Fragment.abrirNavegador(url: String) {
    val uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    if (intent.resolveActivity(activity?.packageManager) != null) {
        startActivity(intent)
    }
}

// https://en.wikipedia.org/wiki/Free-space_path_loss
fun ScanResult.calcularDistanciaDoPontoDeAcesso(): Double {
    val DIST_MHZ_METROS = 27.55
    return Math.pow(10.0, ((-20 * log10(frequency.toDouble())) + abs(level) + DIST_MHZ_METROS) / 20)
}

// https://stackoverflow.com/questions/5485759/how-to-determine-a-wifi-channel-number-used-by-wifi-ap-network
fun ScanResult.converteFrequenciaParaNumeroCanal(): Int {
    if (frequency == 2484) return 14

    if (frequency < 2484) return (frequency - 2407) / 5

    return frequency / 5 - 1000
}

fun ScanResult.getLarguraCanal(): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return when (channelWidth) {
            ScanResult.CHANNEL_WIDTH_20MHZ -> 20
            ScanResult.CHANNEL_WIDTH_40MHZ -> 40
            ScanResult.CHANNEL_WIDTH_80MHZ -> 80
            ScanResult.CHANNEL_WIDTH_160MHZ, ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ -> 160
            else -> -1
        }
    }
    return -1
}

/**
 * Retorna Spannable em bold.
 * @param text a String que contendo os a substring a ser colocada em bold.
 * @param words um vetor contendo as palavras a serem formatadas
 * @return the Spannable for TextView's consumption
 */
fun bold(text: String, words: Array<String>): Spannable {
    val spannable = SpannableString(text)
    for (word in words) {
        var substringStart = 0
        var start: Int = text.indexOf(word, substringStart)
        while (start >= 0) {
            spannable.setSpan(
                    StyleSpan(android.graphics.Typeface.BOLD), start, start + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            substringStart = start + word.length
            start = text.indexOf(word, substringStart)
        }
    }
    return spannable
}

fun getTotalPontosAcessoPorCanal(scanResults: List<ScanResult>): ArrayList<Pair<String, String>> {
    val listaContaPontosAcesso = ArrayList<Pair<String, String>>()
    val mapa = HashMap<Int, Int>()
    scanResults.forEach {
        val canal = it.converteFrequenciaParaNumeroCanal()
        mapa[canal] = if (mapa[canal] != null) mapa[canal]!! + 1 else 1
    }
    mapa.keys.sortedBy { it }.forEach { chave ->
        listaContaPontosAcesso.add(Pair("$chave", "${mapa[chave]}"))
    }
    return listaContaPontosAcesso
}

fun Context.copiarAreaTransferencia(string: String, label: String) {
    val clipBoard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText(label, string)
    clipBoard?.primaryClip = clip
    Toast.makeText(this, R.string.copiado_com_sucesso, Toast.LENGTH_SHORT).show()
}

fun Context.mostrarAlerta(@StringRes mensagem: Int) {
    Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
}