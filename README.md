# Ultra Balance Tweaks

Addon pessoal para Forge 1.20.1 criado para equilibrar as formas divinas de fim de jogo de:

- DragonMineZ 2.1.3
- Unofficial DMZ Addon 10.3.1
- Forge 47.4.10

## Filosofia do balanceamento

O Instinto Superior representa controle, eficiência e sobrevivência. O Ultra Ego representa risco, resistência e poder destrutivo crescente. As duas escolhas têm forças bem diferentes, sem apagar a identidade mostrada no mangá de Dragon Ball Super.

## Ultra Ego

- Substitui o bônus baseado na vida atual por um medidor persistente de Ego de 0 a 100.
- Dano recebido enche o medidor; por padrão, receber dano cumulativo equivalente a cerca de 60% da vida máxima chega a 100.
- O multiplicador especial de dano cresce suavemente de x1,05 até x1,60.
- A penetração de defesa cresce de 0% até 20%.
- Todo dano recebido é reduzido em 15% enquanto a forma está ativa.
- Cura normal remove apenas metade do percentual curado do medidor. A regeneração passiva rápida do DMZ não apaga imediatamente o progresso.
- Após 10 segundos fora de combate, o medidor perde 5 pontos por segundo.
- Sair da forma ou morrer zera o medidor.
- VIT é ajustado para x1,60 e o consumo de stamina para 0,045 apenas em memória; nenhum JSON original é alterado.

## Técnicas de Destruição — versão 1.3.0

Ao alcançar **100 de maestria no Ultra Ego**, Hakai e Esfera da Destruição são aprendidos permanentemente e passam a funcionar também na forma base. Na base eles usam o dano de Ki não transformado e dispensam o medidor de Ego, mas conservam o custo de Ki, cooldown e todas as proteções. Durante o Ultra Ego, continuam exigindo o nível configurado do medidor. As técnicas não podem ser usadas sobre outra transformação.

### Hakai — tecla H

- Exige 70 pontos de Ego e consome 35% do Ki máximo.
- Alcance de 24 blocos e cooldown de 30 segundos.
- Usa um projétil de Ki nativo do DragonMineZ, com shader, cores, colisão, som e impacto do próprio mod.
- O dano escala com o atributo de dano de Ki do personagem transformado (x5 por padrão), em vez de ignorar o nível do jogador.
- Garante um piso de 35% da vida contra criaturas e 18% contra jogadores, além de um mínimo absoluto de 40 de dano; jogadores nunca são executados.
- Persegue o alvo marcado, possui 100% de penetração de armadura da técnica e não destrói blocos.
- Finaliza criaturas comuns abaixo de 15% de vida.
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

| Forma | Chance de esquiva | Custo por esquiva | Precisão em maestria máxima |
|---|---:|---:|---:|
| Sinal | 70% | 2,0% | 10% de chance, x1,15 |
| Completo | 90% | 1,8% | 15% de chance, x1,20 |
| Verdadeiro | 80% | 1,2% | 20% de chance, x1,30 |

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
