package br.com.dxlabs.dxptt

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

/**
 * Companheiro do relogio no celular.
 *
 * Recebe "apertou" (/ptt/down) e "soltou" (/ptt/up) do Galaxy Watch 7
 * pela Data Layer do Wear OS e repassa para o PttSender, que envia os
 * broadcasts de PTT para o Zello. O Google Play services acorda este
 * servico quando a mensagem chega, mesmo com o app fechado.
 *
 * O envio mora no PttSender, fora de qualquer Activity, pelo mesmo
 * motivo da correcao da v0.1.1: o DOWN faz o proprio Zello vir para
 * a frente, entao nada pode depender de uma tela estar visivel.
 */
class PttMessageService : WearableListenerService() {

    override fun onMessageReceived(event: MessageEvent) {
        when (event.path) {
            "/ptt/down" -> {
                PttSender.log("Relógio: DOWN recebido.")
                PttSender.keyDown(this)
            }
            "/ptt/up" -> {
                PttSender.log("Relógio: UP recebido.")
                PttSender.keyUp(this)
            }
            else -> {
                // Mensagem de outro caminho: ignora.
            }
        }
    }
}
