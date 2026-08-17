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

## Instinto Superior

- A esquiva fixa original é substituída por uma chance dependente da maestria e da porcentagem atual de Ki.
- Cada esquiva bem-sucedida consome uma porcentagem do Ki máximo, impedindo defesa perfeita infinita.
- O Instinto Superior Completo tem a maior evasão quando está concentrado e com Ki cheio.
- O Instinto Superior Verdadeiro abre mão de parte da evasão em troca do melhor ataque de precisão.
- O proc ofensivo exagerado do addon original é substituído por golpes de precisão menores e específicos para cada estágio.

| Forma | Esquiva com Ki vazio | Esquiva com Ki cheio | Custo por esquiva | Precisão em maestria máxima |
|---|---:|---:|---:|---:|
| Sinal | 20% → 25% | 45% → 60% | 2,2% → 1,8% | 10% de chance, x1,15 |
| Completo | 25% → 35% | 70% → 90% | 2,3% → 1,8% | 15% de chance, x1,20 |
| Verdadeiro | 25% → 35% | 65% → 80% | 2,1% → 1,6% | 20% de chance, x1,30 |

As setas indicam a evolução entre 0 e 100 de maestria.

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

Os JARs e os JSONs dos mods originais nunca são sobrescritos. Remover `ultrabalancetweaks-1.0.0.jar` restaura os handlers e os atributos originais.

## Compilação

Os JARs de terceiros não fazem parte deste repositório. Há duas maneiras de disponibilizá-los ao Gradle:

1. Copiar para a pasta local `libs/` os JARs do DragonMineZ, Unofficial DMZ Addon, GeckoLib, TerraBlender e Curios usados pela instância; ou
2. Informar diretamente a pasta `mods` da instância:

```powershell
.\gradlew.bat build -Pdmz_mods_dir="C:/caminho/da/instancia/mods"
```

O JAR distribuível é criado em `build/libs/`.
