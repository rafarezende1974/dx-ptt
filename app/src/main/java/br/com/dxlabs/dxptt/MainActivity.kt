package br.com.dxlabs.dxptt

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * DX PTT - Fase 0 (teste no celular, sem relogio ainda).
 *
 * v0.1.1: botao liga/desliga. O DOWN faz o proprio Zello vir para a
 * frente (comportamento documentado), entao "segurar para falar" nao
 * funciona num app de teste no celular: na v0.1.0 um UP automatico no
 * onPause encerrava a transmissao no mesmo segundo. Aqui o toque
 * comeca a transmissao e ela so termina com outro toque, com o botao
 * "Encerrar" da notificacao ou apos 120s. O envio mora no PttSender,
 * fora da Activity, de proposito.
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

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                PttSender.log(
                    "Notificações negadas: para encerrar, volte ao app e toque no botão."
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(buildUi())
        if (PttSender.logLines().isEmpty()) {
            PttSender.log("DX PTT Fase 0 pronto. Build 0.1.1-fase0.")
            PttSender.log("Toque para falar; toque de novo (ou use a notificação) para encerrar.")
        }
        requestNotificationPermissionIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        PttSender.onLog = { refreshLog() }
        PttSender.onStateChanged = { refreshState() }
        refreshZelloStatus()
        refreshState()
        refreshLog()
    }

    override fun onPause() {
        PttSender.onLog = null
        PttSender.onStateChanged = null
        // De proposito, SEM encerrar aqui: o DOWN traz o Zello para a
        // frente e esta Activity pausa no meio da transmissao. Encerrar
        // no onPause foi o bug da v0.1.0.
        super.onPause()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
                "2. Volte aqui e toque no botão: o Zello abre transmitindo.\n" +
                "3. Fale e encerre pelo botão da notificação ou voltando\n" +
                "aqui e tocando de novo. A voz sai do microfone do\n" +
                "celular ou do fone. Após 120s, encerra sozinho."
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
            text = "TOQUE PARA FALAR"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#2FBF71"))
            contentDescription = "Botão push-to-talk. Toque para começar a falar, toque de novo para enviar."
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(200)
            )
        }
        pttButton.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (PttSender.isTransmitting) {
                PttSender.keyUp(this)
            } else {
                PttSender.keyDown(this)
            }
        }
        root.addView(pttButton)

        modeSwitch = Switch(this).apply {
            text = "Broadcast explícito para o pacote do Zello"
            isChecked = PttSender.explicitMode
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(Color.parseColor("#9AA7B8"))
            setPadding(0, dp(20), 0, 0)
            setOnCheckedChangeListener { _, checked ->
                PttSender.explicitMode = checked
                PttSender.log(
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

    private fun refreshState() {
        if (PttSender.isTransmitting) {
            pttButton.text = "TRANSMITINDO — TOQUE PARA ENCERRAR"
            pttButton.setBackgroundColor(Color.parseColor("#E5484D"))
            statusText.text = "TRANSMITINDO (toque ${PttSender.pressCount})"
            statusText.setTextColor(Color.parseColor("#E5484D"))
        } else {
            pttButton.text = "TOQUE PARA FALAR"
            pttButton.setBackgroundColor(Color.parseColor("#2FBF71"))
            statusText.text = "Parado"
            statusText.setTextColor(Color.parseColor("#9AA7B8"))
        }
    }

    private fun refreshZelloStatus() {
        val pkg = PttSender.installedZelloPackage(this)
        zelloText.text = if (pkg != null) {
            "Zello instalado: SIM ($pkg, versão ${PttSender.zelloVersion(this) ?: "?"})"
        } else {
            "Zello instalado: NÃO ENCONTRADO neste aparelho"
        }
        if (pkg == null) {
            PttSender.log("Atenção: nenhum pacote do Zello foi encontrado. Confira se o Zello está instalado.")
        }
    }

    private fun refreshLog() {
        logText.text = PttSender.logLines().joinToString("\n")
    }
}
