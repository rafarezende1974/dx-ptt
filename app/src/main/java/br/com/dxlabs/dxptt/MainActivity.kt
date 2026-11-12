package br.com.dxlabs.dxptt

import android.content.Intent
import android.content.pm.PackageManager
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
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * DX PTT - Fase 0 (teste no celular, sem relogio ainda).
 *
 * Um botao grande que envia os broadcasts de PTT documentados pela Zello
 * (Zello Hardware Partner Technical Integration):
 *   apertar -> com.zello.ptt.down
 *   soltar  -> com.zello.ptt.up
 *
 * O Zello transmite para o contato/canal que estiver selecionado nele.
 * Quem captura o audio e o Zello, pelo microfone do celular ou do fone.
 * Este app nao grava nada e nao precisa de permissao de microfone.
 *
 * Se a Fase 0 passar, o mesmo envio vira o app companheiro do celular
 * e o botao passa a morar no Galaxy Watch 7 (Wear OS), mandando
 * "apertou/soltou" para o celular pela Data Layer.
 */
class MainActivity : ComponentActivity() {

    private lateinit var pttButton: Button
    private lateinit var statusText: TextView
    private lateinit var zelloText: TextView
    private lateinit var logText: TextView
    private lateinit var modeSwitch: Switch

    private var transmitting = false
    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private var pressCount = 0

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale("pt", "BR"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(buildUi())
        refreshZelloStatus()
        log("DX PTT Fase 0 pronto. Build 0.1.0-fase0.")
        log("Aperte e segure o botao, fale, solte para enviar.")
    }

    override fun onPause() {
        // Nunca deixar o PTT preso: se o app sair da frente no meio
        // de uma transmissao, solta o botao sozinho.
        if (transmitting) {
            keyUp()
            log("Transmissao encerrada porque o app saiu da frente.")
        }
        super.onPause()
    }

    private fun buildUi(): ScrollView {
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }

        val title = TextView(this).apply {
            text = "DX PTT — Teste Fase 0"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#F2F5F9"))
        }
        root.addView(title)

        val howto = TextView(this).apply {
            text = "1. Abra o Zello e selecione o canal.\n" +
                "2. Volte aqui, segure o botão, fale e solte.\n" +
                "A voz sai do microfone do celular ou do fone."
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(Color.parseColor("#9AA7B8"))
            setPadding(0, dp(12), 0, 0)
        }
        root.addView(howto)

        zelloText = TextView(this).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(Color.parseColor("#F2F5F9"))
            setPadding(0, dp(16), 0, 0)
        }
        root.addView(zelloText)

        statusText = TextView(this).apply {
            text = "Parado"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#9AA7B8"))
            setPadding(0, dp(20), 0, dp(12))
        }
        root.addView(statusText)

        pttButton = Button(this).apply {
            text = "SEGURE PARA FALAR"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#2FBF71"))
            contentDescription = "Botão push-to-talk. Segure para falar, solte para enviar."
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(200)
            )
        }
        pttButton.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    activePointerId = event.getPointerId(0)
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    keyDown()
                    true
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_POINTER_UP -> {
                    if (event.getPointerId(event.actionIndex) == activePointerId) {
                        activePointerId = MotionEvent.INVALID_POINTER_ID
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        keyUp()
                    }
                    true
                }
                else -> true
            }
        }
        root.addView(pttButton)

        modeSwitch = Switch(this).apply {
            text = "Broadcast explícito para o pacote do Zello"
            isChecked = true
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(Color.parseColor("#9AA7B8"))
            setPadding(0, dp(20), 0, 0)
            setOnCheckedChangeListener { _, checked ->
                log(
                    if (checked) {
                        "Modo: broadcast explícito para o pacote do Zello."
                    } else {
                        "Modo: broadcast implícito (forma original da documentação)."
                    }
                )
            }
        }
        root.addView(modeSwitch)

        val logTitle = TextView(this).apply {
            text = "Registro"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#F2F5F9"))
            setPadding(0, dp(20), 0, dp(8))
        }
        root.addView(logTitle)

        logText = TextView(this).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(Color.parseColor("#9AA7B8"))
        }
        root.addView(logText)

        return ScrollView(this).apply { addView(root) }
    }

    private fun keyDown() {
        if (transmitting) return
        transmitting = true
        pressCount++
        pttButton.text = "TRANSMITINDO — SOLTE PARA ENVIAR"
        pttButton.setBackgroundColor(Color.parseColor("#E5484D"))
        statusText.text = "TRANSMITINDO (toque $pressCount)"
        statusText.setTextColor(Color.parseColor("#E5484D"))
        sendPtt(down = true)
    }

    private fun keyUp() {
        if (!transmitting) return
        transmitting = false
        pttButton.text = "SEGURE PARA FALAR"
        pttButton.setBackgroundColor(Color.parseColor("#2FBF71"))
        statusText.text = "Parado"
        statusText.setTextColor(Color.parseColor("#9AA7B8"))
        sendPtt(down = false)
    }

    private fun sendPtt(down: Boolean) {
        val action = if (down) "com.zello.ptt.down" else "com.zello.ptt.up"
        val intent = Intent(action)
        val target = if (modeSwitch.isChecked) installedZelloPackage() else null
        if (target != null) intent.setPackage(target)
        try {
            sendBroadcast(intent)
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
    private fun installedZelloPackage(): String? {
        for (pkg in listOf("com.loudtalks", "net.loudtalks")) {
            try {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(pkg, 0)
                return pkg
            } catch (e: PackageManager.NameNotFoundException) {
                // tenta o proximo pacote
            }
        }
        return null
    }

    private fun refreshZelloStatus() {
        val pkg = installedZelloPackage()
        zelloText.text = if (pkg != null) {
            val version = try {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(pkg, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                "?"
            }
            "Zello instalado: SIM ($pkg, versão $version)"
        } else {
            "Zello instalado: NÃO ENCONTRADO neste aparelho"
        }
        if (pkg == null) {
            log("Atenção: nenhum pacote do Zello foi encontrado. Confira se o Zello está instalado.")
        }
    }

    private fun log(message: String) {
        val stamp = timeFormat.format(Date())
        val lines = (logText.text.toString() + "\n$stamp  $message")
            .lines()
            .takeLast(40)
        logText.text = lines.joinToString("\n").trimStart()
    }
}
