package cc.tumtum.app.ui.screens.account

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A 422 the server raises on purpose — under 18, the Terms not ticked
 * (26/09) — is shown as the server wrote it; Pydantic's field list is not.
 */
class AuthErrorsTest {

    @Test
    fun `the server's own sentence is shown as it came`() {
        assertTrue(AuthErrors.isSentence("Você precisa ter 18 anos ou mais para usar a TumTum."))
        assertTrue(AuthErrors.isSentence("Você precisa aceitar os Termos e a Política de Privacidade."))
    }

    @Test
    fun `a validation list or an object is not a sentence`() {
        assertFalse(AuthErrors.isSentence("""[{"loc":["body","birth_date"],"msg":"field required"}]"""))
        assertFalse(AuthErrors.isSentence("""{"detail":"x"}"""))
        assertFalse(AuthErrors.isSentence("Erro 422"))
        assertFalse(AuthErrors.isSentence("   "))
    }
}
