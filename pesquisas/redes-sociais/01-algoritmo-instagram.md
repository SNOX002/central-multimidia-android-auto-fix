# Como o algoritmo do Instagram funciona (nov/2026)

> **Legenda das fontes**
> - **[OFICIAL]**: Meta, Instagram ou Adam Mosseri (chefe do Instagram) em canal próprio.
> - **[TERCEIROS]**: imprensa ou blogs especializados, muitas vezes citando o Mosseri.
> - **[NÃO VERIFICADO]**: não foi possível confirmar na fonte original.
>
> ⚠️ As páginas oficiais (transparency.meta.com, about.instagram.com) foram lidas por trechos de busca, porque
> o acesso direto estava bloqueado na rede desta sessão. As citações podem ter pequenas diferenças de palavras em relação ao original.

---

## 1. A ideia central

Não existe **um** algoritmo. Cada parte do app tem o seu: **Feed, Stories, Reels, Explorar, Busca, Contas sugeridas e Notificações**.
A Meta publica um "cartão de sistema" para cada um, explicando o que ele tenta prever. [OFICIAL]
https://transparency.meta.com/features/explaining-ranking/

Em todos eles a lógica é a mesma:

1. **Juntar candidatos:** posts que você poderia ver.
2. **Filtrar:** tirar o que viola as regras ou não pode ser recomendado.
3. **Prever:** calcular, para cada post, a chance de você assistir, curtir, enviar, comentar, seguir ou pular.
4. **Ordenar:** mostrar primeiro o que tem a maior pontuação combinada.

Duas coisas importantes:
- A Meta **não divulga pesos**. Qualquer "o envio vale 5 vezes a curtida" que circula por aí não tem fonte oficial.
- Os sinais "mudam com frequência conforme o sistema aprende". [OFICIAL]

---

## 2. Os sinais que mais pesam (segundo o Mosseri)

| Sinal | Pesa mais para | O que significa |
|---|---|---|
| **Tempo assistido / retenção** | Todos | As pessoas assistem até o fim? Saem nos primeiros 3 segundos? |
| **Envios por alcance** (sends per reach) | **Quem NÃO te segue** | Das pessoas que viram, quantas mandaram por DM para alguém |
| **Curtidas por alcance** | **Quem JÁ te segue** | Das pessoas que viram, quantas curtiram |

- "Likes are slightly more important for connected content, and sends are slightly more important for unconnected content." Mosseri, jan/2025. [TERCEIROS citando Mosseri]
  https://www.socialmediatoday.com/news/instagram-shares-algorithm-insights-2025/738034/
- "**A taxa é mais importante do que a contagem.**" Mosseri, mai/2024. [TERCEIROS citando Mosseri]
  https://www.socialmediatoday.com/news/instagram-chief-post-share-rates-key-driver-reach/716540/
- Em set/2026 o Mosseri citou **quatro princípios**, além do engajamento: [TERCEIROS]
  https://www.socialmediatoday.com/news/instagram-chief-shares-algorithm-insights-and-posting-tips/831326/
  1. **Originalidade:** quem criou primeiro é recomendado; cópias não.
  2. **Recência:** conteúdo novo é favorecido.
  3. **"Breaking content":** posts de criadores pequenos são mostrados a não seguidores para testar a reação. É assim que conta pequena viraliza.
  4. **Modelos de IA maiores:** entendem melhor o assunto do vídeo e a quem ele interessa.
- A orientação dele é acompanhar **taxas** (% que curtiu, % que enviou, tempo médio assistido), e não views brutas.

**Tradução prática:** para chegar em gente nova, faça conteúdo que a pessoa **manda para um amigo** e que ela **assiste até o fim**.
Para manter quem já segue, faça conteúdo que eles **curtem** e **respondem**.

---

## 3. Cada parte do app

### 🎬 Reels: a maior porta de entrada para gente nova
**O que tenta prever** (cartão "Reels Chaining"): [OFICIAL] https://transparency.meta.com/features/explaining-ranking/ig-reels-chaining/
- chance de assistir **menos de 3 segundos** (pular);
- chance de assistir mais do que 95% dos espectadores parecidos;
- curtir, comentar, recompartilhar e **compartilhar para fora do Instagram**;
- **seguir o autor**;
- **usar o áudio** num Reel próprio;
- tocar em "Interessado" ou "Não interessado".

**Sinais usados:** tempo assistido, cliques no áudio, quantos seguidores o Reel já trouxe ao autor, salvamentos, duração e semelhança com outros Reels.

**Segundo o FAQ do Creators, o fator mais importante** é o "engajamento por espectador (tempo assistido, compartilhamentos, follows, curtidas, comentários, visitas ao perfil)". [OFICIAL] https://creators.instagram.com/faq

**O que você controla:**
- gancho nos **primeiros 3 segundos** (os Insights mostram a "taxa de pulo");
- retenção até o fim;
- motivo para enviar a alguém;
- motivo para seguir (série, continuação, nicho claro);
- áudio original que outras pessoas possam reutilizar.

### 🔍 Explorar: descoberta por popularidade
**O que tenta prever:** seguir o autor, assistir mais de 5 segundos, curtir, recompartilhar, salvar e "Não tenho interesse". [OFICIAL]
https://transparency.meta.com/features/explaining-ranking/ig-explore/

No Explorar, os sinais de popularidade, isto é, **"quantas pessoas e com que rapidez estão curtindo, comentando, compartilhando e salvando"**,
"importam muito mais do que no Feed ou nos Stories". [OFICIAL] https://about.instagram.com/blog/announcements/instagram-ranking-explained

**O que você controla:** a **velocidade** do engajamento nas primeiras horas, os salvamentos e os follows vindos do post.

### 🏠 Feed: quem te segue mais recomendações
**O que tenta prever:** curtir, pular, compartilhar por DM, passar tempo no perfil do autor e deslizar o carrossel até o fim. [OFICIAL]
https://transparency.meta.com/features/explaining-ranking/ig-feed/

As 5 interações mais observadas no Feed são: **passar alguns segundos no post, comentar, curtir, compartilhar e tocar na foto do perfil**. [OFICIAL] Ranking Explained, 2023.

**O que você controla:** carrosséis que fazem a pessoa ir até o fim, posts que geram envio e visita ao perfil.

### ⭕ Stories: só para quem já te segue
- **Não são recomendados para quem não te segue.** Servem para **relacionamento**, não para descoberta. [OFICIAL]
  https://transparency.meta.com/features/explaining-ranking/ig-stories/
- O que conta: toques, curtidas no story, respostas por DM e proximidade com a pessoa.
- Mosseri: repostar um Reel no Story "não muda o alcance de forma significativa". [TERCEIROS]
  https://www.socialmediatoday.com/news/instagram-stories-ranking-factors-2025/738541/

**O que você controla:** depois de um viral, é nos Stories que você **transforma seguidor novo em fã**. Use enquetes, caixinhas de pergunta e "me responde aqui".

### 🔎 Busca: palavras-chave valem
- **O texto pesquisado é o sinal principal.** Também conta com que frequência você interage com aquele perfil. [OFICIAL]
  https://transparency.meta.com/features/explaining-ranking/ig-search/
- **O que você controla:**
  - coloque palavras do seu nicho no **Nome** do perfil (ex.: "Ana | Receitas Fit"), além do @;
  - use termos que as pessoas pesquisam em português na **bio** e na **legenda**.
- **Hashtags não aumentam o alcance.** Elas só ajudam a dizer do que o post trata. [TERCEIROS citando Mosseri]

---

## 4. O que tira você das recomendações

### Diretrizes de Recomendação
- Conteúdo "permitido mas não recomendável" continua no ar para os seus seguidores, mas **não aparece para gente nova** no Reels, Explorar, Busca e Sugestões. [OFICIAL]
  https://help.instagram.com/338481628002750/
- Quem viola repetidamente pode ficar com **nenhum post recomendável por um período**.

### Confira o Status da conta
**Configurações > Status da conta.** Ali aparece:
- se o seu conteúdo pode ser recomendado a não seguidores;
- se a conta aparece na Busca e nas Sugestões;
- o que foi violado;
- o botão para **recorrer**.

**Depois de um viral, confira esse status**, porque viral atrai denúncia.

### Originalidade: a regra que mais derruba alcance hoje
- **Desde abr/2024 (Reels):** [OFICIAL] https://creators.instagram.com/recommendations-and-Originality
  - quem reposta conteúdo alheio sem editar de forma relevante **10 ou mais vezes em 30 dias** sai das recomendações;
  - quando há cópias iguais, **só o original é recomendado**, e o original **substitui** o repost.
- **Desde 30/abr/2026 (fotos e carrosséis):** a regra foi estendida. A conta volta a ser recomendada quando **a maioria** dos posts dos últimos 30 dias é original. [TERCEIROS citando oficial]
  https://techcrunch.com/2026/04/30/instagram-restricts-reach-of-content-aggregators-in-new-crackdown/
- **Não conta como edição:** só juntar clipes ou só pôr a sua marca d'água. **Vídeo com marca d'água de outro app** (ex.: TikTok) é menos recomendado. [OFICIAL]
  https://help.instagram.com/1800814370401535/
- **Como usar conteúdo de outros sem ser punido:** Remix, Collab ou parceria com rótulo.

### Formato
- Proporção entre 1,91:1 e 9:16, mínimo de 720p e 30 fps. [OFICIAL] https://help.instagram.com/1038071743007909/
- Reels de **até 3 minutos** podem ser recomendados. Acima disso, a tendência é não serem. [TERCEIROS]
- **Conta comercial não pode usar música licenciada.** Use a Biblioteca de Sons ou áudio original. [OFICIAL] https://help.instagram.com/329208821595430

---

## 5. Linha do tempo das mudanças (2024 a 2026)

| Quando | O que mudou |
|---|---|
| abr/2024 | Regra de originalidade em Reels. Criadores pequenos ganham mais chance de serem testados. [OFICIAL] |
| ago/2024 | **"Views"** vira a métrica principal em todos os formatos. [TERCEIROS] |
| nov/2024 | "Redefinir recomendações" para o usuário. [OFICIAL] |
| dez/2024 | **Trial Reels**: o Reel vai **só para não seguidores** primeiro e você decide se publica no perfil. Depois ganhou agendamento. [OFICIAL] https://creators.instagram.com/blog/instagram-trial-reels |
| jan/2025 | Reels de até 3 minutos e grade do perfil retangular vertical. [OFICIAL/TERCEIROS] |
| jan/2025 | Insights com **retenção**, views de seguidores separadas das de não seguidores e **taxa de pulo** (saída nos primeiros 3 s). [TERCEIROS] |
| abr/2025 | App **Edits** (editor de vídeo da Meta). Mosseri citou um pequeno "boost" para vídeos feitos nele, mas pediu para não usar o Edits só para "passar" um vídeo já pronto. [OFICIAL/TERCEIROS] |
| dez/2025 | **"Seu algoritmo"**: o usuário vê e ajusta os tópicos dos Reels dele. Ainda não confirmado em português. [TERCEIROS] |
| abr/2026 | Originalidade estendida a fotos e carrosséis. [TERCEIROS citando oficial] |
| jun/2026 | Teste de **"Séries"** (Reels em episódios) no Instagram e Facebook. [TERCEIROS] |
| set/2026 | Mosseri explica os 4 princípios de ranking (seção 2). [TERCEIROS] |
| out/2026 | Teste de abrir o app direto nos Reels. **"Features"**: vídeos horizontais de até 90 min, **testados primeiro no Brasil**. [TERCEIROS] https://techcrunch.com/2026/10/26/watch-out-youtube-instagram-goes-from-short-form-to-90-minute-videos/ |

---

## 6. Mitos que o próprio Instagram desmentiu

| Mito | Realidade |
|---|---|
| "Hashtag aumenta alcance" | Não aumenta. Só ajuda a classificar o assunto. [TERCEIROS citando Mosseri] |
| "Legenda longa dá mais alcance" | Não muda o alcance. [TERCEIROS citando Mosseri] |
| "Tem que interagir com contas do nicho antes de postar" | Não é necessário. [TERCEIROS citando Mosseri] |
| "Existe hack secreto" | "Às vezes funciona, normalmente não, e quando descobrimos, desligamos." [TERCEIROS citando Mosseri] |
| "Envio vale 3 a 5 vezes a curtida" | Não há fonte oficial para nenhum número de peso. |

Fontes dos mitos:
- https://www.socialmediatoday.com/news/instagram-chief-says-longer-captions-dont-impact-post-reach/758462/
- https://www.socialmediatoday.com/news/instagram-chief-debunks-popular-engagement-hack/816781/
- https://www.socialmediatoday.com/news/instagram-implements-new-limits-on-hashtag-use/808309/

---

## 7. Resumo em 5 linhas

1. **Gente nova chega pelos Reels e pelo Explorar.** O que mais pesa é **retenção** e **envios por DM**.
2. **Quem já segue é mantido pelo Feed e pelos Stories.** O que mais pesa é **curtida**, **resposta** e **proximidade**.
3. **Seguir o autor** é um sinal que o algoritmo mede. Dê um motivo claro para a pessoa seguir.
4. **Originalidade é obrigatória.** Repost e marca d'água de outro app tiram você das recomendações.
5. **Acompanhe taxas, não números absolutos**, e teste ideias com **Trial Reels**.
