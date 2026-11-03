# Proposta: coletor de Insights em Docker

> 📌 **Status: PROPOSTA. Aguardando sua aprovação.**
> Nenhum código real foi criado ainda. Revise e responda "aprovado" ou diga o que mudar.
> Só faz sentido depois de concluir o arquivo 03 (token configurado).

---

## Objetivo
Uma ferramenta que **puxa os seus números do Instagram** pela API oficial, **só leitura**, e grava num arquivo local.
Assim eu analiso a evolução semana a semana sem você precisar tirar print.

## Arquitetura

```
┌─────────────────────────── docker compose ───────────────────────────┐
│                                                                      │
│  serviço "coletor" (python:3.12-slim + tini como PID 1)              │
│   ├─ src/dominio/      → entidades: Conta, Midia, Metricas           │
│   │                      (regras e validações centralizadas aqui:    │
│   │                       taxa de envio, conversão etc.)             │
│   ├─ src/api/          → cliente da Graph API (só GET, só leitura)   │
│   ├─ src/repositorio/  → grava em SQLite (volume persistente)        │
│   └─ src/app.py        → orquestra: busca → valida → salva           │
│                                                                      │
│  volume nomeado "insights_dados"  → /dados/insights.sqlite           │
│  env_file: .env (IG_ACCESS_TOKEN; fora do git)                       │
└──────────────────────────────────────────────────────────────────────┘
          │ HTTPS (só GET)
          ▼
   graph.instagram.com
```

### Decisões de design
| Princípio | Como é aplicado |
|---|---|
| **Responsabilidade única** (SOLID) | A API só busca, o repositório só grava, o domínio só calcula e valida |
| **Entidade central** | `Metricas` é a **única** classe que calcula taxas (envio, visita, conversão) e valida valores (sem negativos, alcance > 0). Nenhum outro lugar refaz essa conta |
| **Docker-first** | Tudo roda com `docker compose run --rm coletor`. Nada instalado no computador |
| **Volume persistente** | `insights_dados` sobrevive a `down` e rebuild. Só some com `docker volume rm` explícito |
| **Encerramento limpo** | `tini` como PID 1; handlers de `SIGTERM`/`SIGINT` fecham a sessão HTTP e o SQLite; container com `--rm`, sem processo solto |
| **Segurança** | Só GET; token via variável de ambiente, nunca no código nem no log; `.env` no `.gitignore` |

### Modos de uso
- `docker compose run --rm coletor coletar`: puxa os dados de hoje e sai. Pode ser rodado 1 vez por dia ou por semana.
- `docker compose run --rm coletor relatorio`: gera `relatorio.md` com as taxas por Reels, que você me manda ou eu leio.
- `docker compose run --rm coletor renovar-token`: renova o token antes de vencer.

---

## Exemplo de código (amostra do que será implementado)

### Entidade central: `src/dominio/metricas.py`
```python
from dataclasses import dataclass


@dataclass(frozen=True)
class Metricas:
    """Autoridade única para validar e calcular indicadores de um post."""
    alcance: int
    visualizacoes: int
    curtidas: int
    comentarios: int
    compartilhamentos: int
    salvamentos: int
    visitas_perfil: int | None = None
    seguidores_ganhos: int | None = None

    def __post_init__(self) -> None:
        for nome, valor in vars(self).items():
            if valor is not None and valor < 0:
                raise ValueError(f"{nome} não pode ser negativo: {valor}")

    def _taxa(self, parte: int | None, base: int | None) -> float | None:
        if parte is None or not base:
            return None
        return round(parte / base, 4)

    @property
    def taxa_envio(self) -> float | None:          # sinal forte para não seguidores
        return self._taxa(self.compartilhamentos, self.alcance)

    @property
    def taxa_curtida(self) -> float | None:        # sinal forte para seguidores
        return self._taxa(self.curtidas, self.alcance)

    @property
    def taxa_visita_perfil(self) -> float | None:  # o VÍDEO despertou curiosidade?
        return self._taxa(self.visitas_perfil, self.alcance)

    @property
    def taxa_conversao(self) -> float | None:      # o PERFIL convenceu?
        return self._taxa(self.seguidores_ganhos, self.visitas_perfil)
```

### Encerramento limpo: trecho de `src/app.py`
```python
import signal
import sys
from contextlib import ExitStack

def main() -> int:
    with ExitStack() as recursos:                      # fecha tudo, mesmo com erro
        http = recursos.enter_context(ClienteGraph.de_ambiente())   # lê IG_ACCESS_TOKEN
        repo = recursos.enter_context(RepositorioSQLite("/dados/insights.sqlite"))

        def encerrar(sinal, _frame):
            raise SystemExit(128 + sinal)              # aciona o ExitStack → fecha HTTP e SQLite
        signal.signal(signal.SIGTERM, encerrar)
        signal.signal(signal.SIGINT, encerrar)

        for midia in http.listar_midias(limite=50):
            repo.salvar(midia, http.metricas_da_midia(midia.id))
    return 0

if __name__ == "__main__":
    sys.exit(main())
```

### `compose.yaml`
```yaml
services:
  coletor:
    build: .
    init: true                 # tini: repassa sinais e não deixa zumbi
    env_file: .env             # IG_ACCESS_TOKEN (não versionado)
    volumes:
      - insights_dados:/dados
    read_only: true            # filesystem do container só leitura...
    tmpfs: [/tmp]              # ...exceto /tmp
    stop_grace_period: 10s

volumes:
  insights_dados:              # persiste entre down/rebuild
```

---

## Arquivos que serão criados (após aprovação)
```
consultoria-instagram/coletor/
├── Dockerfile
├── compose.yaml
├── .env.exemplo        (sem token real)
├── .gitignore          (.env, *.sqlite)
├── README.md           (como rodar)
└── src/
    ├── app.py
    ├── dominio/  (conta.py, midia.py, metricas.py)
    ├── api/      (cliente_graph.py)
    └── repositorio/ (sqlite.py)
```

## Pontos para você decidir
1. **Onde roda?** No seu computador (precisa do Docker Desktop) ou só aqui na sessão do Claude.
2. **Frequência:** manual (você roda quando quiser) ou agendada (ex.: toda segunda).
3. **Saída:** SQLite + relatório `.md` (proposto), ou também planilha `.csv` para abrir no Excel ou Google Sheets.
