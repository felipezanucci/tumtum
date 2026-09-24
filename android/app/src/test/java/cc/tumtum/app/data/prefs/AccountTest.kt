package cc.tumtum.app.data.prefs

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The profile on the phone belongs to one account (24/09): test 7 showed
 * Felipe's photo over *teste1*, because nothing asked whose it was.
 */
class AccountTest {

    private val felipe = Account(name = "Felipe", username = "felipe", email = "Felipe@Gmail.com", tribes = setOf("SHOWS"))

    @Test
    fun `the same address signing in keeps the profile, whatever the case`() {
        assertTrue(felipe.belongsTo("felipe@gmail.com"))
        assertTrue(felipe.belongsTo("  FELIPE@gmail.com "))
    }

    @Test
    fun `another address starts from its own`() {
        assertFalse(felipe.belongsTo("teste@teste.com"))
    }

    @Test
    fun `a profile with no address belongs to nobody`() {
        assertFalse(felipe.copy(email = "").belongsTo(""))
    }
}
