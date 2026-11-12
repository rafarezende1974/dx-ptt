# DX PTT

Botão push-to-talk do Zello no Galaxy Watch 7. O botão fica no relógio;
o áudio e o microfone continuam no celular ou no fone.

## Como funciona (v1, 0.2.0)

Dois módulos, mesmo pacote (`br.com.dxlabs.dxptt` — a Data Layer do
Wear OS só entrega mensagens entre apps de mesmo pacote):

- **wear** — app do Galaxy Watch 7: um botão em tela cheia, segurar
  para falar, soltar para enviar. Manda `/ptt/down` e `/ptt/up` para
  o celular pela Data Layer (MessageClient). Mostra se o celular está
  conectado. App independente (`standalone`), instala direto no
  relógio por adb — ver `INSTALAR-RELOGIO.md`.
- **app** — app do celular: o `PttMessageService` (WearableListenerService)
  recebe apertou/soltou do relógio e repassa para o `PttSender`, que
  envia os broadcasts de PTT para o Zello. A tela do app continua
  funcionando como botão de teste (liga/desliga, com encerrar pela
  notificação). O envio mora fora de qualquer Activity de propósito:
  o DOWN faz o próprio Zello vir para a frente.

O Zello transmite para o contato/canal que estiver selecionado nele.
Quem captura o áudio é o próprio Zello, pelo microfone do celular ou
do fone — nenhum dos dois apps grava nada ou pede microfone.

Comandos (documentação oficial da Zello, Hardware Partner Technical
Integration): `com.zello.ptt.down` para começar, `com.zello.ptt.up`
para encerrar. O app confere se o Zello está instalado (`com.loudtalks`
ou `net.loudtalks`, o legado do Work) e manda o broadcast explícito
para o pacote encontrado; o interruptor na tela do celular troca para
o broadcast implícito, a forma original da documentação.

Segurança: a transmissão encerra sozinha após 120s; se o relógio
perder a conexão no meio de uma fala, dá para encerrar pela
notificação no celular ou pelo próprio botão do Zello.

## Fase 0 — aprovada em 12/11/2026

Antes do relógio, um app só no celular provou a única incógnita do
projeto no aparelho real: o Zello do Galaxy S24 responde aos
broadcasts de PTT. Na v0.1.0, um UP automático no `onPause` encerrava
a transmissão no mesmo segundo (o DOWN traz o Zello para a frente e
o app de teste pausava). Na v0.1.1, botão liga/desliga + encerrar pela
notificação: 3 transmissões de 20s, 29s e 5s, e a equipe ouviu a voz.
Roteiro original: ver `TESTE-FASE-0.md`.

## Build

Build de debug pelo GitHub Actions (ver `BUILD-WORKFLOW.yml.txt`):
dois artefatos, `dx-ptt-phone-apk` (celular) e `dx-ptt-watch-apk`
(relógio). O push via API não cria arquivos de workflow: o arquivo
`.github/workflows/build-apk.yml` precisa ser criado/editado pela
interface web do GitHub, colando o conteúdo do `.txt`.

Configuração de build igual à do JPG Diet (verde): AGP 8.10.1,
Kotlin 2.0.21, SDK 36, Gradle 8.11.1, Java 17. Sem NDK, sem
bibliotecas de terceiros além do AndroidX básico e do
play-services-wearable (Data Layer).

Pacote: `br.com.dxlabs.dxptt`
