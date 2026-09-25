package cc.tumtum.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.prefs.UserState
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType

/**
 * The top-right corner of every tab: who is on this phone (25/09).
 *
 * Signed in, the person's photo or initials, to their profile. Signed out,
 * an ENTRAR pill, to sign in. Until then the corner drew the phone's saved
 * profile whatever the session said, and Felipe, signed out, read FZ on
 * every tab: *"tudo dá a entender de que eu ainda estou na minha conta"*.
 */
@Composable
fun AccountCorner(user: UserState?, nav: NavHostController, skin: Skin, modifier: Modifier = Modifier) {
    val account = user?.account
    if (user?.signedIn == true && account != null) {
        UserAvatar(
            account.initials,
            skin,
            photoPath = user.avatarPath,
            modifier = modifier.clickable { nav.navigate(Routes.profile(account.username)) },
        )
    } else {
        Badge(
            stringResource(R.string.corner_sign_in),
            background = TT.Rose,
            contentColor = TT.Ink,
            hPad = 12.dp,
            vPad = 8.dp,
            modifier = modifier.clickable { nav.navigate(Routes.Login) },
        )
    }
}

/**
 * What a screen that belongs to an account says when nobody is signed in:
 * the sentence, and the one way in, in Pink.
 */
@Composable
fun SignInPrompt(title: String, body: String?, onSignIn: () -> Unit, onDark: Boolean = false, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Text(title, style = TTType.TitleSmall, color = if (onDark) TT.Paper else TT.Ink)
        body?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = TTType.Body, color = TT.Gray45)
        }
        Spacer(Modifier.height(16.dp))
        TTButton(stringResource(R.string.settings_sign_in), TTButtonStyle.Rose, onClick = onSignIn)
    }
}
