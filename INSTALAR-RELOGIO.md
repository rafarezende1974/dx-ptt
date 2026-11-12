# Instalar o DX PTT no Galaxy Watch 7

O app do relógio instala por **adb, por Wi-Fi** (depuração sem fio).
O relógio e o PC precisam estar na **mesma rede Wi-Fi**. O app do
celular já precisa estar instalado no S24 (artefato
`dx-ptt-phone-apk`); o do relógio é o artefato `dx-ptt-watch-apk`.

## 1. No PC: ter o adb

O `adb` faz parte do Android platform-tools. Se o PC ainda não tem,
baixe em https://developer.android.com/tools/releases/platform-tools,
descompacte e abra o terminal dentro da pasta (onde está o `adb.exe`).

## 2. No relógio: ativar a depuração sem fio

1. Ajustes → Sobre o relógio → Informações de software → toque **7 vezes**
   em "Versão do software" (ativa as Opções do desenvolvedor).
2. Volte em Ajustes → Opções do desenvolvedor → ative **Depuração ADB**
   e **Depuração sem fio**.
3. Toque em **Depuração sem fio** → **Parear novo dispositivo**. Anote
   o endereço (IP:porta) e o código de 6 dígitos que aparecem.

## 3. No PC: parear, conectar e instalar

```bat
adb pair 192.168.0.10:45673
```

(digite o código de 6 dígitos quando pedir; troque pelo IP:porta que
o relógio mostrou na tela de parear)

```bat
adb connect 192.168.0.10:34567
```

(a porta de **conectar** é a que aparece na tela principal de
"Depuração sem fio", diferente da porta de parear)

```bat
adb devices
adb install wear-debug.apk
```

`adb devices` precisa listar o relógio como `device`. Se a instalação
der erro de assinatura, desinstale a versão anterior do relógio antes:
`adb uninstall br.com.dxlabs.dxptt`.

## 4. Testar

1. No celular, abra o Zello e selecione o canal. Avise alguém do outro
   lado que vai chegar um teste.
2. No relógio, abra o **DX PTT** e confira: "Celular conectado".
3. Segure o botão, fale uma frase curta, solte.
4. Pergunte a quem ouviu: a voz chegou? Chegou inteira? Demorou para
   começar?
5. Se alguma transmissão ficar presa: encerre pela notificação
   "DX PTT transmitindo" no celular, ou pelo próprio botão do Zello.

## Observação

A depuração sem fio desliga sozinha quando o relógio reinicia ou
quando a Wi-Fi cai. Para reinstalar depois, basta repetir os passos
2 e 3. O app instalado continua funcionando normalmente com a
depuração desligada.
