package br.com.dxlabs.dxptt

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Envio dos broadcasts de PTT e estado da transmissao, fora da Activity.
 *
 * Precisa viver fora da Activity porque o DOWN faz o proprio Zello vir
 * para a frente (comportamento documentado: com o Zello em segundo plano,
 * o botao abre o Zello antes de transmitir). Na v0.1.0 o botao era
 * "segurar para falar" e um UP automatico no onPause encerrava a
 * transmissao no mesmo segundo, antes de qualquer fala. Aqui nao ha
 * UP automatico ao pausar: a transmissao so termina por toque no botao,
 * pelo botao da notificacao ou pelo tempo maximo.
 *
 * Na v1 do relogio, este mesmo envio vira o companheiro do celular,
 * acionado pelo relogio pela Data Layer — sem Activity na frente.
 */
object PttSender {

    const val ACTION_END = "br.com.dxlabs.dxptt.ACTION_END"

    private const val CHANNEL_ID = "dxptt_transmitting"
    private const val NOTIFICATION_ID = 1
    private const val MAX_TRANSMISSION_MS = 120_000L

    var explicitMode: Boolean = true

    var isTransmitting: Boolean = false
        private set

    var pressCount: Int = 0
        private set

    var onStateChanged: (() -> Unit)? = null
    var onLog: (() -> Unit)? = null

    private val logLines = ArrayList<String>()
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale("pt", "BR"))
    private val handler = Handler(Looper.getMainLooper())
    private var appContext: Context? = null

    private val autoStop = Runnable {
        val ctx = appContext
        if (isTransmitting && ctx != null) {
            log("Tempo máximo de 120s atingido: encerrando.")
            keyUp(ctx)
        }
    }

    fun logLines(): List<String> = logLines.toList()

    fun log(message: String) {
        val line = "${timeFormat.format(Date())}  $message"
        logLines.add(line)
        while (logLines.size > 40) logLines.removeAt(0)
        onLog?.invoke()
    }

    fun keyDown(context: Context) {
        if (isTransmitting) {
            log("Já está transmitindo.")
            return
        }
        appContext = context.applicationContext
        isTransmitting = true
        pressCount++
        sendPtt(context, down = true)
        showNotification(context)
        handler.postDelayed(autoStop, MAX_TRANSMISSION_MS)
        onStateChanged?.invoke()
    }

    fun keyUp(context: Context) {
        if (!isTransmitting) return
        isTransmitting = false
        handler.removeCallbacks(autoStop)
        sendPtt(context, down = false)
        cancelNotification(context)
        onStateChanged?.invoke()
    }

    private fun sendPtt(context: Context, down: Boolean) {
        val action = if (down) "com.zello.ptt.down" else "com.zello.ptt.up"
        val intent = Intent(action)
        val target = if (explicitMode) installedZelloPackage(context) else null
        if (target != null) intent.setPackage(target)
        try {
            context.sendBroadcast(intent)
            log(
                (if (down) "DOWN" else "UP") +
                    " enviado: $action" +
                    if (target != null) " -> $target" else " (implícito)"
            )
        } catch (e: Exception) {
            log("ERRO ao enviar $action: ${e.message}")
        }
    }

    /** Pacote do Zello instalado: o universal atual ou o legado do Work. */
    fun installedZelloPackage(context: Context): String? {
        for (pkg in listOf("com.loudtalks", "net.loudtalks")) {
            try {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(pkg, 0)
                return pkg
            } catch (e: PackageManager.NameNotFoundException) {
                // tenta o proximo pacote
            }
        }
        return null
    }

    fun zelloVersion(context: Context): String? {
        val pkg = installedZelloPackage(context) ?: return null
        return try {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(pkg, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DX PTT transmitindo",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Aviso de transmissão em andamento, com botão para encerrar."
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context) {
        ensureChannel(context)
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val endIntent = Intent(context, PttActionReceiver::class.java).setAction(ACTION_END)
        val endPending = PendingIntent.getBroadcast(
            context,
            0,
            endIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("DX PTT transmitindo")
            .setContentText("Falando no Zello. Toque em Encerrar para enviar.")
            .setContentIntent(openPending)
            .addAction(0, "Encerrar", endPending)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            log("Notificação bloqueada (sem permissão). Volte ao app para encerrar.")
        }
    }

    private fun cancelNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}
