# Conectar meus Insights pela API oficial da Meta (somente leitura)

> **Opcional.** Faça isso **depois** dos prints, se quiser que eu acompanhe os números sem você precisar tirar print toda semana.
> Use um **computador**, porque o painel de desenvolvedor da Meta é ruim no celular.
>
> ⚠️ Os menus do painel da Meta mudam com frequência. Se algum nome estiver diferente, me mande um print e eu te guio.

---

## O que este acesso permite e o que não permite
| ✅ Permite (leitura) | ❌ Não permite |
|---|---|
| Ver seus posts e Reels (legenda, data, link) | Publicar, editar ou apagar posts |
| Ver os números de cada post (alcance, views, curtidas, comentários, compartilhamentos, salvamentos, tempo assistido) | Ler ou mandar DMs |
| Ver números da conta (alcance, seguidores, visitas) | Mudar a senha, a bio ou qualquer configuração |

As permissões que vamos pedir são só estas duas:
- `instagram_business_basic`: dados básicos do perfil e lista de posts;
- `instagram_business_manage_insights`: os números (Insights).

Usamos a **"API do Instagram com login do Instagram"**, que **não exige Página do Facebook**.
Fonte oficial: https://developers.facebook.com/docs/instagram-platform/instagram-api-with-instagram-login

---

## Pré-requisito
- [ ] A conta precisa ser **Profissional** (Criador ou Empresa).
  - No app: Configurações > **Tipo de conta e ferramentas** > **Mudar para conta profissional**.
  - É grátis e dá para voltar depois.

## Passo 1: Criar a conta de desenvolvedor (grátis)
1. Acesse **https://developers.facebook.com** e entre com o seu Facebook.
2. Clique em **Começar** e aceite os termos. Pode pedir confirmação por SMS.

## Passo 2: Criar o app
1. **Meus apps** > **Criar app**.
2. Nome do app: `Consultoria Insights` (só você vê).
3. Caso de uso: escolha a opção do **Instagram**, algo como *"Gerenciar mensagens e conteúdo no Instagram"*.
4. Quando perguntar por portfólio empresarial, pode pular ou escolher "não conectar".
5. Conclua a criação. O app fica em **modo de desenvolvimento**. Isso é bom: só você usa e **não precisa de revisão da Meta**.

## Passo 3: Ligar sua conta do Instagram
1. Dentro do app, vá em **Casos de uso** > Instagram > **Personalizar** ou **Configuração da API com login do Instagram**.
2. Confira se as permissões `instagram_business_basic` e `instagram_business_manage_insights` estão adicionadas.
3. Na seção **Gerar tokens de acesso**, clique em **Adicionar conta** e entre com o **seu Instagram**.
   - Se pedir para adicionar como **testador**: vá em **Funções do app** > **Funções** > adicione seu @ como **Testador do Instagram**.
   - Depois, no app do Instagram: Configurações > **Apps e sites** > **Convites de testador** > **Aceitar**.
4. Ao lado da sua conta, clique em **Gerar token**. Esse é o **token de longa duração** e vale **cerca de 60 dias**.

## Passo 4: Guardar o token com segurança 🔒
> **NÃO cole o token aqui no chat.** Quem tem o token lê os seus números.

1. Copie o token.
2. No Claude, abra o **menu do ambiente** (no topo da sessão) > **Editar**.
3. Em **Variáveis de ambiente / segredos**, adicione:
   ```
   IG_ACCESS_TOKEN=cole_o_token_aqui
   ```
4. Em **Acesso à rede** > **Domínios permitidos**, adicione:
   ```
   graph.instagram.com
   ```
5. Salve e me avise: **"token configurado"**. Eu testo numa sessão nova.

Passo a passo das configurações do ambiente: https://code.claude.com/docs/en/cloud-environments#network-access

## Passo 5: Renovação
- O token vence em **~60 dias**, mas pode ser **renovado** antes disso sem você fazer nada, desde que tenha mais de 24h de uso.
- A ferramenta do arquivo 04 faz essa renovação automaticamente.

## Como revogar o acesso a qualquer momento
- **No Instagram:** Configurações > **Apps e sites** > remova o app `Consultoria Insights`.
- **Ou** apague o app em developers.facebook.com.
- **E** apague o segredo `IG_ACCESS_TOKEN` do ambiente.

---

## Observações técnicas (para conferência)
- Os nomes das métricas da API mudaram em 2025:
  - "impressions" e "plays" foram aposentados em favor de **`views`** (a partir da v22.0, abr/2025);
  - relatos de jun/2026 falam numa nova troca para "media views" / "media viewers" na v25.
- **Antes de codar, vou conferir os nomes atuais** na referência oficial:
  - https://developers.facebook.com/docs/instagram-platform/insights
  - https://developers.facebook.com/docs/instagram-platform/reference/instagram-media/insights
- Anúncio oficial dos Insights nessa API (mar/2025): https://developers.facebook.com/blog/post/2025/03/24/user-and-media-insights-on-instagram-api-with-instagram-login/
