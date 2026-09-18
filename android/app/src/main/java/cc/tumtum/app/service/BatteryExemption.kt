package cc.tumtum.app.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * Onde a maioria dos apps morre (§6): Motorola, Xiaomi, Oppo e Realme matam
 * serviços fora do padrão AOSP. Foreground service correto não basta nessas
 * ROMs — sem a isenção, a sessão não começa: melhor recusar a captura do que
 * produzir dado incompleto sem ninguém perceber.
 */
object BatteryExemption {

    fun isExempt(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    @SuppressLint("BatteryLife")
    fun requestIntent(context: Context): Intent =
        Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}"),
        )

    /** Caminho manual do menu, por fabricante (Build.MANUFACTURER). Null = AOSP-like, o diálogo basta. */
    fun manufacturerSteps(manufacturer: String = Build.MANUFACTURER): String? {
        val m = manufacturer.lowercase()
        return when {
            "xiaomi" in m || "redmi" in m || "poco" in m ->
                "Xiaomi: além do diálogo, ative o Autostart — Configurações → Apps → Gerenciar apps → TumTum → " +
                    "\"Início automático\" ligado, e em \"Economia de bateria\" escolha \"Sem restrições\"."
            "motorola" in m ->
                "Motorola: Configurações → Bateria → Otimização de bateria → Todos os apps → TumTum → \"Não otimizar\". " +
                    "Se existir \"Adaptive Battery\", desligue para esta noite."
            "oppo" in m || "realme" in m || "oneplus" in m ->
                "Oppo/Realme: Configurações → Bateria → mais configurações/Gerenciador → TumTum → permita " +
                    "\"Executar em segundo plano\" e desative a otimização inteligente para o app."
            else -> null
        }
    }
}
