# Pokédex

Aplicativo Android que funciona como uma **Pokédex**: uma lista de Pokémon e uma tela de detalhes
com as informações de cada um (tipos, descrição, altura, peso, habilidades, evolução e fraquezas).
Na tela de detalhes também é possível **treinar** o Pokémon (aumentar/diminuir o nível) e
**registrá-lo na Pokédex**, e a interface é atualizada a cada interação.

Esta é a entrega da **etapa parcial** da disciplina: Android Views (XML), navegação com `Intent`
explícita e dados simulados (mocks), sem API e sem banco de dados.

## Como rodar o projeto localmente

1. Abra o Android Studio (versão compatível com o AGP `9.3.1`) e escolha **Open**, selecionando a
   pasta raiz deste projeto.
2. Aguarde o Gradle sincronizar. O wrapper do Gradle (`gradlew`) e o toolchain do JDK são baixados
   automaticamente na primeira sincronização.
3. Selecione um emulador ou dispositivo com **Android 13 (API 33) ou superior** (`minSdk = 33`).
4. Execute o módulo `app` (botão ▶ *Run*).

Pelo terminal, também é possível rodar:

```bash
./gradlew installDebug
```

Não há chaves, senhas ou arquivos `.env`: todos os dados são mocks definidos no próprio código.

## Bibliotecas externas

Nenhuma biblioteca além das que já vêm no template padrão de projeto Android:

- **AndroidX AppCompat / Core KTX / Activity KTX / Lifecycle** — base de uma `Activity` no Android.
- **Material Components (`com.google.android.material`)** — tema Material 3, `MaterialToolbar`,
  `FloatingActionButton`, `LinearProgressIndicator`, `MaterialDivider` e o `RecyclerView`.
- **Fontes Syne e JetBrains Mono** (arquivos `.ttf` em `res/font`, licença SIL Open Font License).

## Estrutura do projeto

```
app/src/main/java/com/pokedex/app/
├── Pokemon.kt                  # data class imutável + lista de mocks + busca por id
├── HomeActivity.kt             # tela 1: lista da Pokédex (RecyclerView + Adapter)
└── PokemonDetailActivity.kt    # tela 2: detalhes do Pokémon selecionado

app/src/main/res/layout/
├── activity_home.xml           # layout da tela 1
├── home_list_item_layout.xml   # layout de um item da lista
├── activity_pokemon_detail.xml # layout da tela 2
└── badge_layout.xml            # componente XML reutilizável (badge de tipo/fraqueza)
```

## Onde cada requisito foi atendido

| Requisito | Onde |
| --- | --- |
| Duas telas em Android Views com layouts XML | `activity_home.xml` e `activity_pokemon_detail.xml` |
| Views e ViewGroups adequados (`TextView`, `ImageView`, `Space`, `LinearLayout`, `FrameLayout`, `ScrollView`, `RecyclerView`) | layouts acima |
| Navegação por `Intent` explícita com passagem de dados | `HomeListAdapter.onBindViewHolder` envia `EXTRA_POKEMON_ID`; `PokemonDetailActivity` lê o extra |
| Views conectadas ao Kotlin por `ViewBinding` | todas as telas (não há `findViewById` no projeto) |
| Interação que atualiza a interface | botões de nível (`getPreviousLevel`/`getNextLevel`) e botão de registro (`toggleRegistered`), em `updateTrainingViews()` |
| Modelos imutáveis com `data class` | `Pokemon`, com `copy()` e validação no `init` |
| Tratamento de valores opcionais | `hiddenAbility` e `evolvesTo` são nullable e exibem `—`; o extra da `Intent` pode ser nulo/ inválido e a tela é encerrada com aviso |
| Dados simulados (mocks) | `pokedexItems` em `Pokemon.kt` |
| **Opcional:** apenas ViewBinding | nenhum `findViewById` no projeto |
| **Opcional:** componente XML reutilizável inflado em runtime | `badge_layout.xml`, usado via `<include>` na lista e inflado com `BadgeLayoutBinding.inflate()` nos badges de tipo e fraqueza |

## Observação sobre as imagens

As imagens dos Pokémon são representadas por um ícone vetorial genérico
(`res/drawable/sprite_placeholder.xml`) colorido de acordo com o tipo principal de cada Pokémon,
evitando o uso de artes oficiais protegidas por direitos autorais. Para usar imagens próprias,
basta adicionar os arquivos em `res/drawable` e trocar o `android:src` das `ImageView`
(`home_list_item_layout.xml` e `activity_pokemon_detail.xml`).
