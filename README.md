# config-service

Workspaces и настройки org (`/v1/config`). **Не** хранит MCP installations.

- **Контракт:** [`../api-contracts/config/openapi.yaml`](../api-contracts/config/openapi.yaml)
- **ERD:** `cursor-context/docs/erd-and-bounded-contexts.md` §3

## Запуск

```bash
docker compose up --build
```

- API: http://localhost:8084/v1/config/
- Health: http://localhost:8084/v1/config/health
- Postgres: localhost:5434

## Internal bootstrap (IAM)

При создании org **iam-service** вызывает:

```http
POST /v1/config/internal/orgs/{orgId}/bootstrap-default-workspace
X-Config-Internal-Key: dev-internal-key
Content-Type: application/json

{"defaultGroupId":"<uuid>","orgName":"Acme Corp"}
```

Создаёт workspace `slug=default` в default group (идемпотентно).

## Переменные

| Переменная | По умолчанию |
|------------|----------------|
| `DB_HOST` | localhost |
| `DB_NAME` | config |
| `CONFIG_INTERNAL_API_KEY` | dev-internal-key |

## Сборка

```bash
./gradlew test
./gradlew shadowJar
```
