# Teste — DX PTT Fase 0

Objetivo: confirmar que o Zello do Galaxy S24 responde aos comandos de
PTT que o botão do relógio vai usar depois. Sem relógio nesta fase.

## Antes de instalar

- Desinstale qualquer versão anterior do "DX PTT (Teste)" antes de
  instalar um build novo: cada build de debug tem uma assinatura
  descartável e o Android recusa instalar por cima.

## Roteiro

1. Abra o Zello e selecione o canal (ou contato) do teste. Deixe
   alguém do outro lado avisado de que vai chegar um teste.
2. Abra o app **DX PTT (Teste)**. Confira na tela:
   "Zello instalado: SIM" com o pacote e a versão.
3. Segure o botão verde, fale uma frase curta ("teste do botão,
   um dois três"), solte.
4. Pergunte a quem estava ouvindo: a voz chegou? Chegou inteira,
   do começo ao fim? Houve atraso perceptível para começar?
5. Repita 3 vezes, incluindo uma com o Zello em segundo plano
   (aperte Início no meio do caminho e volte ao app de teste).
6. Se nada chegar: desligue o interruptor "Broadcast explícito" na
   tela do app e repita o passo 3. Anote se mudou alguma coisa.
7. Se o Zello abrir sozinho ao apertar o botão, anote também — a
   documentação diz que isso acontece quando ele está em segundo
   plano, e é comportamento esperado.

## O que me mandar de volta

- Ouviram ou não ouviram? (e com qual modo: explícito ou implícito)
- A voz cortou o começo ou o fim de alguma frase?
- O botão ficou preso transmitindo alguma vez?
- Print da tela do app com o Registro, se algo falhar.

## Se passar

A v1 leva este mesmo botão para o Galaxy Watch 7. O celular vira
só o "companheiro" que repassa apertou/soltou para o Zello.
