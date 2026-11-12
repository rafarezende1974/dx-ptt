# DX PTT

Botão push-to-talk do Zello no Galaxy Watch 7. O botão fica no relógio;
o áudio e o microfone continuam no celular ou no fone.

## Fase 0 — teste no celular (esta versão, 0.1.1-fase0)

Antes de escrever o app do relógio, esta versão prova a única incógnita
do projeto no aparelho real: o Zello instalado no Galaxy S24 responde
aos broadcasts de PTT documentados pela Zello?

- Começar a falar envia `com.zello.ptt.down`
- Encerrar envia `com.zello.ptt.up`

Fonte: Zello Hardware Partner Technical Integration (documentação
oficial para fabricantes de botões PTT). O Zello transmite para o
contato/canal que estiver selecionado nele. Quem captura o áudio é o
próprio Zello, pelo microfone do celular ou do fone — este app não
grava nada e não pede permissão de microfone.

O app confere se o Zello está instalado (`com.loudtalks`, o pacote
universal atual, ou `net.loudtalks`, o legado do Work) e manda o
broadcast explícito para o pacote encontrado. O interruptor na tela
troca para o broadcast implícito, a forma original da documentação,
caso o explícito não funcione.

### Por que o botão é liga/desliga (lição da v0.1.0)

Na v0.1.0 o botão era "segurar para falar" com um UP automático quando
o app saía da frente. O teste no S24 mostrou que o DOWN faz o próprio
Zello vir para a frente (comportamento documentado: com o Zello em
segundo plano, o botão abre o Zello antes de transmitir), então o UP
automático encerrava a transmissão no mesmo segundo, antes de
qualquer fala — nas 4 tentativas, nos dois modos. Os comandos
chegavam ao Zello; o que falhava era a duração.

Na v0.1.1 não há UP automático ao pausar: o envio mora no `PttSender`,
fora da Activity. A transmissão termina com outro toque no botão,
com o botão "Encerrar" da notificação ou após 120s. Isso é específico
do app de teste no celular: na v1, o botão fica no relógio e o lado
do celular é acionado pela Data Layer, sem Activity na frente.

Roteiro de teste: ver `TESTE-FASE-0.md`.

## Próximo passo (v1, só se a Fase 0 passar)

- App Wear OS no Galaxy Watch 7: um botão grande em tela cheia,
  segurar para falar, soltar para enviar.
- App companheiro no celular: recebe "apertou/soltou" do relógio pela
  Data Layer do Wear OS e envia os mesmos broadcasts desta Fase 0.
- O celular pode ficar no bolso; o fone continua sendo o áudio.

## Build

Build de debug pelo GitHub Actions (ver `BUILD-WORKFLOW.yml.txt`).
O push via API não cria arquivos de workflow: o arquivo
`.github/workflows/build-apk.yml` precisa ser criado pela interface
web do GitHub, colando o conteúdo do `.txt`.

Configuração de build igual à do JPG Diet (verde): AGP 8.10.1,
Kotlin 2.0.21, SDK 36, Gradle 8.11.1, Java 17. Sem NDK, sem
bibliotecas de terceiros além do AndroidX básico.

Pacote: `br.com.dxlabs.dxptt`
