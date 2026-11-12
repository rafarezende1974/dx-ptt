package br.com.dxlabs.dxptt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Recebe o toque no botao "Encerrar" da notificacao de transmissao
 * e encerra a transmissao, mesmo com o Zello na frente.
 * So recebe intents explicitos do proprio app (exported=false).
 */
class PttActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == PttSender.ACTION_END) {
            PttSender.keyUp(context)
            PttSender.log("Encerrado pelo botão da notificação.")
        }
    }
}
