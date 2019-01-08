package br.com.wifimy

import br.com.wifimy.extensoes.prefixoEnderecoMAC
import org.junit.Assert.assertEquals
import org.junit.Test

class TestesUnitariosTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun prefixoEnderecoMacTest() {
        assertEquals("14:cc:25", "14:cc:25:87:7v:ac".prefixoEnderecoMAC())
        assertEquals("b8:1d:aa", "B8:1D:AA:de:63:27".prefixoEnderecoMAC())
    }
}
