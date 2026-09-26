package cc.tumtum.app.data.db

import android.content.Context
import android.util.Log
import cc.tumtum.app.data.prefs.SecurePrefs
import java.io.File
import java.security.SecureRandom
import net.zetetic.database.sqlcipher.SQLiteDatabase

/**
 * The key of the encrypted Room database (LGPD remediation, 26/09).
 *
 * 32 random bytes, generated once on this phone, written as 64 hex
 * characters into [SecurePrefs] (Keystore-backed) and never anywhere else.
 * The hex text itself is SQLCipher's passphrase: printable, so the very same
 * string works in the `ATTACH … KEY '…'` of the one-time migration below,
 * with no question of how a binary key would be quoted there.
 */
internal object DatabaseKey {
    private const val FILE = "tumtum_secure_db"
    private const val KEY = "db_passphrase"
    private const val TAG = "TumTumDb"

    /** The passphrase, and whether it was created just now (no database could have used it yet). */
    fun passphrase(context: Context): Pair<String, Boolean> {
        val prefs = SecurePrefs.open(context, FILE)
        val existing: String? = prefs.getString(KEY, null)
        if (existing != null) return existing to false
        val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val hex = bytes.joinToString("") { "%02x".format(it.toInt() and 0xff) }
        prefs.edit().putString(KEY, hex).commit()
        return hex to true
    }

    /** SQLite's own 16-byte header: a file that starts with it is not encrypted. */
    fun isPlaintext(file: File): Boolean {
        if (!file.exists() || file.length() < 16) return false
        val header = ByteArray(16)
        val read = runCatching { file.inputStream().use { it.read(header) } }.getOrDefault(-1)
        return read == 16 && String(header, Charsets.US_ASCII) == "SQLite format 3\u0000"
    }

    /**
     * Brings the database file to the state the encrypted Room can open.
     *
     * - **An unencrypted file from an older build** is exported into an
     *   encrypted copy with `sqlcipher_export` (the SQLCipher-documented
     *   migration), its schema version carried across, and the copy takes its
     *   place — the person's nights survive the update.
     * - **If that export fails**, the old file is deleted. Chosen on purpose,
     *   and acceptable while the app is only on test builds: a crash on every
     *   open would lose the same data and the app with it, and a plain-text
     *   copy left beside an encrypted one would defeat the point. Nights that
     *   were sent are still on the server.
     * - **An encrypted file with a key created just now** can never be opened
     *   (its key was lost with the Keystore entry), so it goes too, for the
     *   same reason.
     */
    fun prepare(context: Context, name: String, passphrase: String, keyIsNew: Boolean) {
        val file = context.getDatabasePath(name)
        if (!file.exists()) return
        if (isPlaintext(file)) {
            val ok = runCatching { encryptInPlace(context, file, name, passphrase) }
                .onFailure { Log.w(TAG, "could not encrypt the old database; starting over", it) }
                .getOrDefault(false)
            if (!ok) context.deleteDatabase(name)
        } else if (keyIsNew) {
            Log.w(TAG, "encrypted database without its key; starting over")
            context.deleteDatabase(name)
        }
    }

    private fun encryptInPlace(context: Context, file: File, name: String, passphrase: String): Boolean {
        val tmpName = "$name.encrypting"
        context.deleteDatabase(tmpName)
        val tmp = context.getDatabasePath(tmpName)
        // An empty password opens the file as plain SQLite (sqlcipher-android
        // keys a connection only when a password is given).
        val plain = SQLiteDatabase.openDatabase(file.absolutePath, "", null, SQLiteDatabase.OPEN_READWRITE, null, null)
        try {
            val version = plain.version
            plain.rawExecSQL("ATTACH DATABASE '${sql(tmp.absolutePath)}' AS encrypted KEY '${sql(passphrase)}'")
            plain.rawExecSQL("SELECT sqlcipher_export('encrypted')")
            plain.rawExecSQL("PRAGMA encrypted.user_version = $version")
            plain.rawExecSQL("DETACH DATABASE encrypted")
        } finally {
            plain.close()
        }
        // The old file with its journal and WAL go; the encrypted copy takes its name.
        context.deleteDatabase(name)
        if (!tmp.renameTo(file)) {
            context.deleteDatabase(tmpName)
            return false
        }
        return true
    }

    private fun sql(text: String): String = text.replace("'", "''")
}
