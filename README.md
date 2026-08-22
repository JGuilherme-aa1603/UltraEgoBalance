# Ultra Balance Tweaks

Addon pessoal para Forge 1.20.1 criado para equilibrar as formas divinas de fim de jogo de:

- DragonMineZ 2.1.3
- Unofficial DMZ Addon 10.3.1
- Forge 47.4.10

## Filosofia do balanceamento

O Instinto Superior representa controle, eficiência e sobrevivência. O Ultra Ego representa risco, resistência e poder destrutivo crescente. As duas escolhas têm forças bem diferentes, sem apagar a identidade mostrada no mangá de Dragon Ball Super.

## Multiplicadores Saiyajin — versão 1.5.3

Os valores são aplicados e verificados em memória, no servidor e no cliente, apenas para a raça Saiyajin. Eles representam o multiplicador final de força, skill/velocidade, poder de Ki e resistência; o bônus oculto de até 35% por maestria do DMZ é neutralizado nessas formas para a tabela permanecer exata. Como o DragonMineZ calcula a resistência exibida pela média de DEF e STM, o addon ajusta os dois componentes ao mesmo valor da tabela. Os JSONs originais permanecem intactos.

| Transformação | Força | Velocidade | Poder | Resistência |
|---|---:|---:|---:|---:|
| Super Saiyajin 1 | x2,5 | x2,5 | x2,5 | x1,8 |
| Super Saiyajin 2 | x4,0 | x4,0 | x4,0 | x2,5 |
| Super Saiyajin 3 | x6,0 | x6,0 | x6,0 | x3,5 |
| Super Saiyajin 4 | x8,0 | x8,0 | x8,0 | x4,5 |
| Super Saiyajin God | x10,0 | x10,0 | x10,0 | x5,2 |
| Super Saiyajin Blue | x12,0 | x12,0 | x12,0 | x6,0 |
| Super Saiyajin Blue Evolved | x12,5 | x12,0 | x12,5 | x6,5 |
| Legendary Super Saiyajin (Full Power) | x12,5 | x11,5 | x12,5 | x8,0 |
| Ultra Ego | x11,0 | x10,5 | x11,0–13,0 | x7,0 |
| Beast | x14,0 | x13,0 | x14,0 | x7,5 |
| Instinto Superior SIGN | x10,5 | x12,0 | x10,5 | x5,5 |
| Instinto Superior MASTERED | x12,5 | x14,0 | x12,5 | x6,5 |
| Instinto Superior TRUE | x12,5 | x14,0 | x12,5 | x6,5 |

Todos os valores-base podem ser alterados na seção `saiyan_form_multipliers` do arquivo comum de configuração. O SSJ4 é tratado nas duas variantes internas usadas pelo DMZ, e Legendary SSJ corresponde à forma Full Power.

## Ultra Ego

- Substitui o bônus baseado na vida atual por um medidor persistente de Ego de 0 a 100.
- Dano recebido enche o medidor; por padrão, receber dano cumulativo equivalente a cerca de 60% da vida máxima chega a 100.
- O multiplicador especial de dano cresce suavemente de x1,05 até x1,60.
- O Poder de Ki cresce de x11,0 até x13,0 junto do medidor, sem substituir o multiplicador especial: com Ego cheio, ataques de Ki alcançam potencial efetivo próximo de x20,8.
- A penetração de defesa cresce de 0% até 20%.
- Todo dano recebido é reduzido em 15% enquanto a forma está ativa.
- Cura normal remove apenas metade do percentual curado do medidor. A regeneração passiva rápida do DMZ não apaga imediatamente o progresso.
- Após 10 segundos fora de combate, o medidor perde 5 pontos por segundo.
- Sair da forma ou morrer zera o medidor.
- VIT é ajustado para x1,60 e o consumo de stamina para 0,045 apenas em memória; nenhum JSON original é alterado.

## Técnicas de Destruição — versão 1.5.3

Ao alcançar **100 de maestria no Ultra Ego**, Hakai e Esfera da Destruição são aprendidos permanentemente e podem ser usados na forma base ou sobre qualquer outra transformação. Fora do Ultra Ego, eles dispensam o medidor de Ego, mas conservam custo de Ki, cooldown, progressão e proteções. No Ultra Ego, continuam exigindo o nível configurado do medidor.

### Hakai — tecla H

- Evolui em quatro níveis por uma combinação do Poder de Batalha nativo do DMZ e uma maestria própria do Hakai: I (matéria), II (energia), III (seres vivos) e IV (apagamento verdadeiro).
- Limites padrão, calibrados para a escala de PB do DragonMineZ 2.1.3: I = 10 mil PB/10 maestria; II = 100 mil/25; III = 750 mil/50; IV = 3 milhões/100. Todos os limites de PB são configuráveis.
- O poder efetivo compara o PB do usuário com o PB do alvo e recebe até 50% de bônus com a maestria. Alvos muito superiores resistem ou recebem dano parcial; ter o nível IV não garante apagar qualquer adversário.
- Ao alcançar 100 de maestria no Ultra Ego, Hakai e Esfera da Destruição deixam de exigir Ego em qualquer forma, inclusive durante o próprio Ultra Ego. A maestria própria do Hakai continua controlando seus quatro níveis; custo de Ki e cooldown permanecem ativos.
- Nível I apaga itens soltos e projéteis físicos; nível II também apaga projéteis de Ki; nível III atinge seres vivos; nível IV pode apagar criaturas elegíveis sem deixar drops quando o usuário é suficientemente superior.
- Exige 70 pontos de Ego e consome 35% do Ki máximo.
- Alcance de 24 blocos e cooldown de 30 segundos.
- Usa um projétil de Ki nativo do DragonMineZ, com shader, cores, colisão, som e impacto do próprio mod.
- O dano escala com o atributo de dano de Ki do personagem transformado (x5 por padrão), em vez de ignorar o nível do jogador.
- Garante um piso de 35% da vida contra criaturas e 18% contra jogadores, além de um mínimo absoluto de 40 de dano; jogadores nunca são executados.
- Persegue o alvo marcado, possui 100% de penetração de armadura da técnica e não destrói blocos.
- Finaliza criaturas comuns enfraquecidas quando a relação de poder permite.
- Jogadores criativos/espectadores, pets domesticados e entidades marcadas como chefes pelo Forge não podem ser apagados.

### Esfera da Destruição — tecla J

- Inspirada na esfera usada por Vegeta contra Granolah.
- Exige 50 pontos de Ego, consome 25% do Ki máximo e possui cooldown de 12 segundos.
- É uma Death Ball nativa do DragonMineZ recolorida e recalibrada: forma-se acima do jogador, usa o shader de Ki do mod, dispara na direção da mira e produz a explosão nativa.
- O dano escala com o dano de Ki transformado (x3,5 por padrão) e é aplicado em área; o mínimo absoluto é 24 de dano.
- Tamanho padrão 3,35, resultando em raio de explosão próximo de cinco blocos.
- Nunca destrói blocos. Dano contra outros jogadores vem desativado por padrão.

### Aura da Destruição

- Começa a se manifestar a partir de 80 pontos de Ego.
- A chance de apagar projéteis inimigos cresce até 40% com o medidor cheio.
- Cada projétil apagado consome cinco pontos de Ego, impedindo defesa gratuita infinita.

As teclas podem ser alteradas normalmente em **Opções → Controles → Ultra Balance Tweaks**. O HUD mostra requisitos e cooldowns em tempo real abaixo do medidor de Ego.

## Instinto Superior

- A chance é fixa e autoritativa no servidor; Ki baixo não reduz artificialmente o percentual. Se não houver Ki suficiente para pagar a esquiva, ela falha.
- O SIGN possui 70% de esquiva e consome 2,0% do Ki máximo por esquiva bem-sucedida.
- O MASTERED conserva 90% de esquiva e consome 1,8% do Ki máximo.
- O TRUE possui 80% de esquiva, mas seu custo foi reduzido de 2,1% para 1,2% do Ki máximo.
- O proc ofensivo exagerado do addon original é substituído por golpes de precisão menores e específicos para cada estágio.
- Cada esquiva bem-sucedida abre uma janela de contra-ataque contra o agressor. O próximo acerto válido recebe o bônus, e contra-ataque nunca acumula com precisão.

| Forma | Chance de esquiva | Custo por esquiva | Precisão em maestria máxima |
|---|---:|---:|---:|
| Sinal | 70% | 2,0% | 10% de chance, x1,15 |
| Completo | 90% | 1,8% | 15% de chance, x1,20 |
| Verdadeiro | 80% | 1,2% | 20% de chance, x1,30 |

### Contra-ataque instintivo

| Forma | Janela | Dano | Cooldown após acertar |
|---|---:|---:|---:|
| SIGN | 0,6 s | x1,15 | 1,5 s |
| MASTERED | 0,8 s | x1,35 | 1,0 s |
| TRUE | 0,8 s | x1,35 | 1,0 s |

- Só pode atingir o agressor da esquiva e usa o cálculo normal de dano do DMZ.
- Durante a janela, basta clicar para atacar: não é necessário mirar nem alcançar manualmente. O servidor vira o personagem para o agressor, aplica um impulso de aproximação e executa o ataque nativo automaticamente, com alcance máximo de 24 blocos.
- Hakai e Esfera da Destruição não podem consumir nem receber o bônus.
- Um indicador prateado junto da mira mostra a janela restante e o multiplicador.
- MASTERED e TRUE possuem o mesmo contra-ataque; MASTERED conserva mais esquiva, enquanto TRUE conserva o menor custo de Ki.

### Técnica do Instinto — Sinal

Ao alcançar **100 de maestria no Instinto Superior Verdadeiro**, a tecla **K** libera uma versão técnica e empilhável do SIGN:

- Funciona sobre qualquer transformação Saiyajin ativa, como Super Saiyajin, God, Blue e Blue Evolved.
- Usa o slot nativo de forma empilhável do DMZ, assim como Kaioken; portanto, não pode coexistir com Kaioken, Ultimate ou outra forma empilhada.
- Preserva cabelo, modelo, multiplicadores e força da transformação Saiyajin ativa.
- Acrescenta olhos e aura prateados nativos, HUD próprio e os efeitos de esquiva/precisão do SIGN.
- Não concede um segundo multiplicador de atributos: o benefício é técnico (70% de esquiva por 2,0% de Ki e precisão leve), evitando combinações ofensivas desbalanceadas.
- Desativa automaticamente ao perder a transformação compatível. Pressionar K novamente também desativa.

## HUD do Ego

- Barra segmentada roxa/magenta acima da hotbar.
- Percentual numérico e preenchimento interpolado para não “pular” entre valores.
- Brilho animado durante o carregamento.
- Pulso, partículas, som e mensagem quando chega a 100.
- Posição, largura, percentual e visibilidade podem ser configurados.
- O chip do Hakai mostra também o nível atual em algarismos romanos.

## HUD de maestria do Hakai

- Fica visível em qualquer transformação depois que a Destruição é desbloqueada com 100 de maestria no Ultra Ego.
- Mostra o nível atual, a maestria exata de 0 a 100 e uma barra com marcos em 10, 25 e 50.
- A linha inferior mostra o próximo nível e compara o Poder de Batalha atual com o requisito configurado no servidor.
- Ao chegar ao Hakai IV com 100 de maestria, o medidor muda para o estado dourado de domínio total.
- A posição pode ser ajustada separadamente no arquivo de configuração do cliente; quando o Ultra Ego está ativo, o medidor sobe automaticamente para não cobrir a barra de Ego.

## Super Kamehameha

- Nova técnica nativa do tipo onda, ensinada por Goku no menu de mestres por 6.000 TP.
- Dano-base x2,5, exatamente o mesmo do Final Flash original do DMZ; portanto, preserva a equivalência entre as duas técnicas de pico.
- Largura x3,0: três vezes o Kamehameha comum e duas vezes o Final Flash.
- A escala visual maior é compensada por velocidade ligeiramente menor, carga de 120 ticks e custo de Ki calculado pelo próprio sistema do DMZ.
- Reutiliza animação, som, shader, colisão, progressão e slots de técnicas do Kamehameha nativo.

## Auras divinas

- God, Blue, Blue Evolved e Instinto Superior TRUE deixam de manter a aura forçada o tempo todo. A aura começa desligada e volta a obedecer ao controle normal do menu do DMZ.
- God recebeu uma composição vermelho-dourada em duas camadas. Blue e Blue Evolved usam a camada nativa segura em tons ciano/azul com relâmpagos claros; a antiga camada azul-escura foi removida porque o sombreamento de carregamento podia dessaturar a pele do personagem.

## Configuração

O Forge gera estes arquivos na primeira inicialização:

- `config/ultrabalancetweaks-common.toml`: todos os números de balanceamento.
- `config/ultrabalancetweaks-client.toml`: aparência e posição do HUD.

## Segurança e reversão

Os JARs e os JSONs dos mods originais nunca são sobrescritos. Remover o JAR `ultrabalancetweaks` restaura os handlers e os atributos originais.

## Compilação

Os JARs de terceiros não fazem parte deste repositório. Há duas maneiras de disponibilizá-los ao Gradle:

1. Copiar para a pasta local `libs/` os JARs do DragonMineZ, Unofficial DMZ Addon, GeckoLib, TerraBlender e Curios usados pela instância; ou
2. Informar diretamente a pasta `mods` da instância:

```powershell
.\gradlew.bat build -Pdmz_mods_dir="C:/caminho/da/instancia/mods"
```

O JAR distribuível é criado em `build/libs/`.
