package br.com.wifimy.wifi

import java.util.Random
import kotlin.collections.ArrayList

private const val MINUSCULAS = "abcdefghijklmnopqrstuvwxyz"
private const val MAIUSCULAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
private const val CARACTERES_ESPECIAS = "!@#\$%&*()_+-=[]|,./?><"
private const val DIGITOS = "0123456789"
private const val TOTAL_CATEGORIAS = 4

/**
 * @author Isaías Santana on 10/07/2018
 */
class GeradorSenha private constructor(private val temLetrasMinusculas: Boolean,
                                       private val temLetrasMaiusculas: Boolean,
                                       private val temCarateresEspeciais: Boolean,
                                       private val temDigitos: Boolean) {

    class Builder {
        private var minusculas = false
        private var maiusculas = false
        private var caracteresEspecias = false
        private var digitos = false

        fun temLetrasMinusculas(minusculas: Boolean) = apply { this.minusculas = minusculas }

        fun temLetrasMaiusculas(maiusculas: Boolean) = apply { this.maiusculas = maiusculas }

        fun temCaracteresEspecias(caracteresEspecias: Boolean) = apply { this.caracteresEspecias = caracteresEspecias }

        fun temDigitos(digitos: Boolean) = apply { this.digitos = digitos }

        fun build() = GeradorSenha(minusculas, maiusculas, caracteresEspecias, digitos)
    }

    private fun getSenha(comprimento: Int): String {
        val senha = StringBuilder(comprimento)
        val random = Random(System.nanoTime())
        val categoriasCaracteres = ArrayList<String>(TOTAL_CATEGORIAS)

        if (temLetrasMinusculas) {
            categoriasCaracteres.add(MINUSCULAS)
        }

        if (temLetrasMaiusculas) {
            categoriasCaracteres.add(MAIUSCULAS)
        }

        if (temDigitos) {
            categoriasCaracteres.add(DIGITOS)
        }

        if (temCarateresEspeciais) {
            categoriasCaracteres.add(CARACTERES_ESPECIAS)
        }
        if (categoriasCaracteres.isEmpty()) {
            return ""
        }
        return try {
            for (i in 0 until comprimento) {
                val categoria = categoriasCaracteres[random.nextInt(categoriasCaracteres.size)]
                val posCaracter = random.nextInt(categoria.length)
                senha.append(categoria[posCaracter])
            }
            senha.toString()
        } catch (e: Exception) {
            return ""
        }
    }

    fun gerarSenha(comprimento: Int): String {
        return if (comprimento <= 0) {
            ""
        } else {
            getSenha(comprimento)
        }
    }
}