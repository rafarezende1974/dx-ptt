# Teste — DX PTT Fase 0 (v0.1.1)

Objetivo: confirmar que o Zello do Galaxy S24 responde aos comandos de
PTT que o botão do relógio vai usar depois. Sem relógio nesta fase.

## Antes de instalar

- **Desinstale a v0.1.0 ("DX PTT (Teste)") antes de instalar a v0.1.1:**
  cada build de debug tem uma assinatura descartável e o Android
  recusa instalar por cima.

## Roteiro

1. Abra o Zello e selecione o canal (ou contato) do teste. Deixe
   alguém do outro lado avisado de que vai chegar um teste.
2. Abra o app **DX PTT (Teste)**. Na primeira abertura, ele pede
   permissão de notificações — permita, porque o botão de encerrar
   mora na notificação. Confira na tela:
   "Zello instalado: SIM" com o pacote e a versão.
3. Toque no botão verde. O Zello deve abrir transmitindo (é o
   comportamento esperado). Fale uma frase curta ("teste do botão,
   um dois três").
4. Encerre de um dos dois jeitos: puxe a central de notificações e
   toque em **Encerrar** na notificação "DX PTT transmitindo", ou
   volte ao app de teste e toque no botão vermelho.
5. Pergunte a quem estava ouvindo: a voz chegou? Chegou inteira,
   do começo ao fim? Houve atraso perceptível para começar?
6. Repita 3 vezes, alternando o jeito de encerrar (notificação e
   botão do app).
7. Se nada chegar: desligue o interruptor "Broadcast explícito" na
   tela do app e repita os passos 3–5. Anote se mudou alguma coisa.

## O que me mandar de volta

- Ouviram ou não ouviram? (e com qual modo: explícito ou implícito)
- A voz cortou o começo ou o fim de alguma frase?
- O Zello abriu sozinho ao tocar no botão? A notificação apareceu?
- Alguma transmissão ficou presa sem encerrar?
- Print da tela do app com o Registro, se algo falhar.

## Se passar

A v1 leva este mesmo botão para o Galaxy Watch 7, com segurar para
falar e soltar para enviar. O celular vira só o "companheiro" que
repassa apertou/soltou para o Zello.
