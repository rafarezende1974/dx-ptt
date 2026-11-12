package br.com.dxlabs.dxptt

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * DX PTT no Galaxy Watch 7 (Wear OS).
 *
 * Um botao em tela cheia: segurar para falar, soltar para enviar.
 * O relogio nao fala com o Zello diretamente: ele manda "apertou" e
 * "soltou" para o celular pela Data Layer (MessageClient), e o
 * companheiro no celular (PttMessageService) envia os broadcasts de
 * PTT para o Zello. O audio e o microfone continuam no celular/fone.
 *
 * Mesmo applicationId do app do celular (br.com.dxlabs.dxptt): a
 * Data Layer so entrega mensagens entre apps de mesmo pacote.
 * Os envios passam por um executor de uma so thread, para o UP
 * nunca ultrapassar o DOWN.
 */
class WatchMainActivity : ComponentActivity() {

    private lateinit var pttButton: Button
    private lateinit var statusText: TextView
    private lateinit var lastActionText: TextView

    private val executor = Executors.newSingleThreadExecutor()
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale("pt", "BR"))

    @Volatile
    private var transmitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(buildUi())
        refreshButton()
        refreshConnection()
    }

    override fun onResume() {
        super.onResume()
        refreshConnection()
    }

    override fun onPause() {
        // No relogio, pausar no meio de uma transmissao significa que o
        // usuario saiu do app: encerra por seguranca. (O bug da Fase 0
        // no celular era o oposto: la o Zello vinha para a frente e o
        // app pausava no meio da fala.)
        if (transmitting) {
            endTransmission()
        }
        super.onPause()
    }

    override fun onDestroy() {
        // shutdown() sem Now: deixa um UP ja enfileirado terminar.
        executor.shutdown()
        super.onDestroy()
    }

    private fun buildUi(): LinearLayout {
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        statusText = TextView(this).apply {
            text = "Procurando o celular…"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#9AA7B8"))
            setPadding(0, 0, 0, dp(10))
        }
        root.addView(
            statusText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        pttButton = Button(this).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            contentDescription = "Botão push-to-talk. Segure para falar, solte para enviar."
        }
        pttButton.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    beginTransmission()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.performClick()
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    endTransmission()
                    true
                }
                else -> true
            }
        }
        root.addView(
            pttButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        lastActionText = TextView(this).apply {
            text = "Segure o botão para falar."
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#9AA7B8"))
            setPadding(0, dp(10), 0, 0)
        }
        root.addView(
            lastActionText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        return root
    }

    private fun beginTransmission() {
        if (transmitting) return
        transmitting = true
        refreshButton()
        sendToPhone(down = true)
    }

    private fun endTransmission() {
        if (!transmitting) return
        transmitting = false
        refreshButton()
        sendToPhone(down = false)
    }

    private fun refreshButton() {
        if (transmitting) {
            pttButton.text = "FALANDO…\nSOLTE PARA ENVIAR"
            pttButton.setBackgroundColor(Color.parseColor("#E5484D"))
        } else {
            pttButton.text = "SEGURE\nPARA FALAR"
            pttButton.setBackgroundColor(Color.parseColor("#2FBF71"))
        }
    }

    private fun refreshConnection() {
        executor.execute {
            try {
                val nodes = Tasks.await(
                    Wearable.getNodeClient(this).connectedNodes,
                    TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
                )
                runOnUiThread {
                    val connected = nodes.isNotEmpty()
                    statusText.text = if (connected) {
                        "Celular conectado"
                    } else {
                        "Celular não conectado"
                    }
                    statusText.setTextColor(
                        Color.parseColor(if (connected) "#2FBF71" else "#E5484D")
                    )
                }
            } catch (e: Exception) {
                runOnUiThread {
                    statusText.text = "Sem conexão com o celular"
                    statusText.setTextColor(Color.parseColor("#E5484D"))
                }
            }
        }
    }

    private fun sendToPhone(down: Boolean) {
        val path = if (down) PATH_DOWN else PATH_UP
        executor.execute {
            try {
                val nodes = Tasks.await(
                    Wearable.getNodeClient(this).connectedNodes,
                    TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
                )
                if (nodes.isEmpty()) {
                    runOnUiThread {
                        lastActionText.text =
                            "${timeFormat.format(Date())}  Celular não conectado."
                        if (down) {
                            transmitting = false
                            refreshButton()
                        }
                    }
                    return@execute
                }
                for (node in nodes) {
                    Tasks.await(
                        Wearable.getMessageClient(this)
                            .sendMessage(node.id, path, ByteArray(0)),
                        TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                    )
                }
                runOnUiThread {
                    lastActionText.text = "${timeFormat.format(Date())}  " +
                        if (down) "Falando… (enviado ao celular)" else "Enviado."
                }
            } catch (e: Exception) {
                runOnUiThread {
                    lastActionText.text =
                        "${timeFormat.format(Date())}  Falha ao enviar: ${e.message}"
                    if (down) {
                        transmitting = false
                        refreshButton()
                    }
                }
            }
        }
    }

    companion object {
        private const val PATH_DOWN = "/ptt/down"
        private const val PATH_UP = "/ptt/up"
        private const val TIMEOUT_SECONDS = 10L
    }
}
