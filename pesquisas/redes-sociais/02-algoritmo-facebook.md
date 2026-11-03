# Como o algoritmo do Facebook funciona (nov/2026)

> **Legenda das fontes**
> - **[OFICIAL]**: Meta (Transparency Center, Newsroom, Creators, Engineering, Central de Ajuda).
> - **[TERCEIROS]**: imprensa ou blogs especializados.
> - **⚠**: não verificado ou com fontes em conflito.
>
> ⚠️ As páginas oficiais foram lidas por trechos de busca, porque o acesso direto estava bloqueado na rede desta sessão.
> As datas de atualização dos cartões de sistema devem ser conferidas no site.

---

## 1. A ideia central

O Facebook funciona como o Instagram: cada parte do app tem um sistema próprio de IA, com um "cartão de sistema" público. [OFICIAL]
https://transparency.meta.com/features/explaining-ranking/

O processo tem 4 etapas:
1. **Inventário:** reunir todos os posts que você poderia ver.
2. **Filtros de integridade:** tirar o que viola as regras.
3. **Previsões:** estimar o valor de cada post para você.
4. **Ordenação:** colocar primeiro os de maior pontuação, equilibrando os tipos de conteúdo. [OFICIAL] https://transparency.meta.com/features/explaining-ranking/fb-feed/

A Meta mostra só as cerca de 10 previsões mais importantes de cada sistema e **não publica pesos**.

### A grande diferença: dois sistemas no mesmo Feed
| | Conteúdo **conectado** | Conteúdo **recomendado** |
|---|---|---|
| O que é | Amigos, grupos e Páginas que a pessoa já segue | Posts públicos de quem a pessoa **não** segue |
| Cartão | "Facebook Feed" | "Feed Recommendations" |
| Para o criador | É como você **mantém** o público | É como o viral **chega em gente nova** |

Fontes: https://transparency.meta.com/features/explaining-ranking/fb-feed/ e https://transparency.meta.com/features/explaining-ranking/fb-feed-recommendations/ [OFICIAL]

---

## 2. O que o Facebook mede

### 🏠 Feed
**Previsões e sinais que a Meta cita** [OFICIAL] https://transparency.meta.com/features/ranking-and-content/
- chance de você **comentar**;
- chance de você **compartilhar**;
- chance de **seus amigos comentarem** se você compartilhar;
- chance de o post **gerar conversa**;
- **tempo** que você vai passar vendo o post;
- seu histórico de compartilhamentos (temas que você compartilhou nos últimos 30 dias, quantidade nos últimos 180 dias);
- as pesquisas **"Este post valeu o seu tempo?"**, cujas respostas treinam o sistema.

**Na prática:** o Facebook valoriza mais **comentário e conversa** do que o Instagram. Um post que vira discussão entre amigos ou é mandado no Messenger ou WhatsApp vai longe.

### 🎬 Reels e vídeo
Desde **jun/2025, todo vídeo novo no Facebook é Reel**, sem limite de duração. A aba "Vídeo" virou aba "Reels". [OFICIAL]
https://about.fb.com/news/2025/06/making-it-easier-create-videos-facebook/

**Previsões do cartão de Reels** [OFICIAL] https://transparency.meta.com/features/explaining-ranking/fb-reels/
- chance de curtir;
- quanto tempo você vai assistir;
- se você vai **terminar** o Reel;
- se você vai pedir para ver menos;
- entradas: duração, semelhança com outros Reels e o quanto combina com o que você costuma assistir.

**Previsões do cartão de Vídeo** [OFICIAL] https://transparency.meta.com/features/explaining-ranking/fb-video/
- **compartilhar**, inclusive **em conversa** (chat);
- assistir **mais de 30 segundos**;
- comentar.

**Novidade de jan/2026, o modelo "UTIS":**
- O Facebook pergunta a algumas pessoas: **"Quão bem este vídeo combina com seus interesses?"** (nota de 1 a 5).
- A resposta entra no ranking e na busca de candidatos.
- O objetivo declarado é ir **além de curtida e tempo assistido** e mostrar mais conteúdo de **nicho e de qualidade**.
- [OFICIAL] https://engineering.fb.com/2026/01/14/ml-applications/adapting-the-facebook-reels-recsys-ai-model-based-on-user-feedback/

**Mudança de out/2025:** cerca de **50% mais Reels do mesmo dia** no feed de Reels, ou seja, **conteúdo novo foi favorecido**.
Também surgiram as "bolhas de amigos", que mostram o que seus amigos curtiram. [TERCEIROS]
https://techcrunch.com/2025/10/07/facebook-updates-its-algorithm-to-give-users-more-control-over-which-videos-they-see/

### ⭕ Stories
- Só para quem já tem relação com você. Contam a chance de tocar, de ver vários stories seus e de reagir. [OFICIAL]
  https://transparency.meta.com/features/explaining-ranking/fb-stories/
- **Servem para reter, não para descobrir.**

### 👥 Grupos
- O padrão "Mais relevantes" ordena os posts pela chance de curtir e comentar, considerando seu histórico no grupo. [OFICIAL]
  https://transparency.meta.com/features/explaining-ranking/fb-individual-group-feed/
- **Para o criador:** participar de verdade dos grupos do seu nicho, conversando e não só jogando link, gera os comentários que o sistema mede.

### 🎛️ Controles do usuário
- "Tenho interesse" e "Não tenho interesse" (antigo "Mostrar mais/menos"); Favoritos (até 30); adiar perfis; aba Feeds em ordem cronológica.
- **"Your algorithm" (13/out/2026):** a pessoa **escreve** os temas que quer ver mais ou menos, e uma IA interpreta. [OFICIAL]
  https://about.fb.com/news/2026/10/introducing-your-algorithm-facebook/ ⚠ Cronograma no Brasil não informado.

> **Consequência para você:** com o UTIS e o "Your algorithm", um **nicho claro** pesa mais. Quem pede "mais vídeos de receita fácil"
> vai receber o seu vídeo se o sistema entender que ele é **sobre** receita fácil.

---

## 3. O que reduz o alcance no Facebook

### Diretrizes de Distribuição de Conteúdo
Lista oficial do que é rebaixado: https://transparency.meta.com/features/approach-to-ranking/types-of-content-we-demote/ [OFICIAL]

| O que é | Exemplo | Consequência |
|---|---|---|
| **Engagement bait** (isca de engajamento) | "Comente SIM", "Marque 3 amigos", "Curta se concorda" | Post rebaixado. Reincidentes perdem alcance em **todos** os posts [OFICIAL] |
| **Clickbait** | Título que esconde ou exagera o conteúdo | Rebaixado [OFICIAL] |
| **Conteúdo repetido de baixa qualidade** | Repostar material dos outros "sem adicionar valor" | Rebaixamento por um período [OFICIAL] https://transparency.meta.com/enforcement/taking-action/repeatedly-posting-low-quality-content/ |
| **Conteúdo limítrofe** | Sugestivo, violento, palavrões | Quem não quer ver acaba vendo menos [OFICIAL] |

> Pedir comentário com um **motivo real** ("me conta nos comentários qual foi sua maior dificuldade, que eu respondo em vídeo")
> é diferente de isca vazia ("comente SIM"). A regra mira a isca vazia.

### Originalidade: a regra mais importante desde 2025
**14/jul/2025, combate ao conteúdo não original** [OFICIAL] https://creators.facebook.com/blog/combating-unoriginal-content
- Contas que reaproveitam repetidamente vídeos, fotos ou textos de outros **perdem monetização** por um tempo e têm **alcance reduzido em tudo** o que postam.
- Quando há **vídeo duplicado**, as cópias perdem alcance e o original fica com o crédito.
- **Não é punido:** quem acrescenta algo seu (reação com opinião, comentário, participação em trend com conteúdo próprio).
- **Evite:** só costurar clipes dos outros e **marca d'água de outra plataforma**.

**13/mar/2026, definição de "original"** ⚠ (post oficial não acessado; fontes de terceiros)
https://www.mediapost.com/publications/article/413530/meta-defines-original-content-tests-content-pro.html
- **Original:** filmado ou produzido por você, ou uma transformação **significativa** de algo que já existe.
- **Não original:**
  - só reagir com caras e bocas;
  - costurar clipes;
  - narrar o que já aparece na tela;
  - repostar com borda, legenda ou mudança de velocidade.
- A Meta diz que **views e tempo assistido de Reels originais quase dobraram** no 2º semestre de 2025.

### Status de recomendação
Confira em **Qualidade da Página** e no **Painel Profissional**. Lá aparecem os avisos de risco de perder recomendação ou monetização. [OFICIAL]
https://creators.facebook.com/professional-dashboard-updates

---

## 4. Diferenças em relação ao Instagram

| | Instagram | Facebook |
|---|---|---|
| Sinal mais citado para gente nova | **Envios por DM** e retenção | **Comentários e conversa**, compartilhamento em chat e tempo assistido |
| Pesquisa com o usuário | Menos citada | **"Valeu seu tempo?"** e **UTIS** (combina com seus interesses?) |
| Estrutura | Seguidores | Amigos, **Grupos**, Páginas e aba Amigos |
| Vídeo | Reels até 3 min recomendáveis | **Todo vídeo é Reel**, sem limite de duração |
| Conteúdo novo | Recência é um dos princípios | **+50% de Reels do mesmo dia** (out/2025) |
| Dinheiro | Pouca monetização direta | **Pagamento por views** (Content Monetization, só por convite). Por isso a originalidade pesa no bolso |

### Postar o mesmo Reel no Instagram e no Facebook
- Pode, desde que o **conteúdo seja seu**. O problema é Página de terceiros repostando. [TERCEIROS]
- Requisitos: as duas contas na mesma **Central de Contas**, e conta Profissional ou de Criador.
- Cuidados práticos:
  - **música licenciada** pode não valer nas duas plataformas;
  - **nunca** suba vídeo com **marca d'água do TikTok**;
  - confira a proporção de tela.
- ⚠ A Meta **não diz** que o Reel cruzado tem o mesmo alcance do nativo. Vale testar.

---

## 5. Linha do tempo (2024 a 2026)

| Quando | O que mudou |
|---|---|
| mai/2024 | "O futuro do Facebook": foco em jovens de 18 a 29 anos e em recomendação por IA. [OFICIAL] |
| mar/2025 | **Aba Amigos** (só amigos), primeiro nos EUA e Canadá. [TERCEIROS] |
| jun/2025 | **Todo vídeo vira Reel.** [OFICIAL] |
| jul/2025 | **Combate ao conteúdo não original.** [OFICIAL] |
| out/2025 | +50% de Reels do mesmo dia, bolhas de amigos e "Não tenho interesse" melhorado. [TERCEIROS] |
| jan/2026 | **UTIS**: pesquisa "combina com seus interesses?" entra no ranking. [OFICIAL] |
| mar/2026 | Definição de "original" e ferramentas contra imitadores. ⚠ |
| mar/2026 | **Creator Fast Track**: pagamento garantido para quem já é grande em outra rede (15 Reels em 30 dias, em pelo menos 10 dias diferentes). ⚠ Brasil não confirmado. [OFICIAL] https://about.fb.com/news/2026/03/creator-fast-track-grow-your-audience-earn-money-on-facebook/ |
| jun/2026 | Assistente de criador, tradução de Reels por IA, alertas de cópia dos seus Reels e métrica "views qualificadas". [OFICIAL] |
| out/2026 | **"Your algorithm"**: o usuário descreve em texto o que quer ver. [OFICIAL] |

### Monetização no Brasil
- **Content Monetization:** só por convite. Manifeste interesse em Painel Profissional > Monetização. [OFICIAL]
  https://creators.facebook.com/tools/facebook-content-monetization
- O Brasil aparece nas listas de **anúncios in-stream, Estrelas e Assinaturas**. [OFICIAL]
  https://www.facebook.com/business/help/267128784014981
- ⚠ A disponibilidade do Content Monetization no Brasil não foi confirmada.

---

## 6. Resumo em 5 linhas

1. O Facebook tem **dois algoritmos no Feed**: um para quem já te segue e outro para **recomendar** você a quem não segue.
2. O que mais pesa: **comentário e conversa**, **compartilhamento em chat**, **tempo assistido** e **terminar o vídeo**.
3. **Nicho claro** pesa cada vez mais, por causa do UTIS e do "Your algorithm".
4. **Originalidade** é obrigatória. Cópia, compilado e marca d'água de outra rede derrubam alcance **e** monetização.
5. **Sem isca de engajamento.** Peça comentário com motivo real e poste com frequência, porque conteúdo do dia ganhou peso.
