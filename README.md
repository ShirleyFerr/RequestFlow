# RequestFlow

RequestFlow e uma aplicacao corporativa para gestao de solicitacoes internas. Usuarios abrem solicitacoes, acompanham status, comentam e visualizam detalhes. Analistas assumem e tratam solicitacoes. Gestores acompanham indicadores, visualizam a operacao e gerenciam usuarios.

O projeto possui front-end Angular, backend Quarkus e banco MySQL.

## Tecnologias

- Angular
- Angular Material
- TypeScript
- SCSS
- Quarkus
- Java
- Maven
- MySQL
- JWT
- Docker
- Docker Compose

## Estrutura do projeto

```text
RequestFlow/
  a_code/
    a_frontend_request_flow/
      request-flow/        # Front-end Angular
    b_backend_request_flow/
      request-flow/        # Backend Quarkus
  compose.yml              # Orquestracao local com MySQL, backend e front-end
  .env.example             # Exemplo de variaveis de ambiente
  README.md                # Documentacao principal
```

Nos comandos abaixo, considere:

- `backend` = `a_code/b_backend_request_flow/request-flow`
- `frontend` = `a_code/a_frontend_request_flow/request-flow`

## Pre-requisitos

- Java 21
- Maven 3.9 ou Maven Wrapper do projeto
- Node.js compativel com Angular 21
- npm
- MySQL 8, se rodar localmente sem Docker
- Docker e Docker Compose, se rodar containerizado

## Configuracao de ambiente

Copie o arquivo de exemplo da raiz para criar seu `.env` local:

```powershell
copy .env.example .env
```

No Linux/macOS:

```bash
cp .env.example .env
```

Preencha o `.env` com valores locais. Nao versionar `.env`.

Principais variaveis:

```env
MYSQL_DATABASE=requestflow
MYSQL_USER=requestflow_user
MYSQL_PASSWORD=change_me
MYSQL_ROOT_PASSWORD=change_me
MYSQL_HOST=mysql
MYSQL_PORT=3306

BACKEND_PORT=8080
FRONTEND_PORT=80
QUARKUS_PROFILE=dev
FRONTEND_ORIGIN=http://localhost
CORS_ORIGINS=http://localhost,http://localhost:80,http://localhost:4200,http://127.0.0.1,http://127.0.0.1:4200

JWT_SECRET=change_me_generate_a_secure_secret
JWT_ISSUER=request-flow-api
JWT_EXPIRATION_MINUTES=120

AI_PROVIDER=gemini
AI_API_KEY=change_me
AI_MODEL=gemini-2.5-flash
AI_TIMEOUT_MS=3000
```

O backend tambem possui exemplo de configuracao em:

```text
a_code/b_backend_request_flow/request-flow/src/main/resources/application.properties.example
```

O arquivo real usado pelo Quarkus e:

```text
a_code/b_backend_request_flow/request-flow/src/main/resources/application.properties
```

Ele deve usar variaveis de ambiente e nao deve conter senhas reais hardcoded.

### IA Gemini

A sugestao de IA e opcional. Quando `AI_PROVIDER=gemini` e `AI_API_KEY` estiver configurada, o backend chama o Gemini para sugerir categoria, prioridade e resumo da solicitacao.

Se a chave nao existir, se o provider estiver invalido, se houver timeout ou se o Gemini retornar JSON invalido, o backend usa o fallback local automaticamente. A criacao manual da solicitacao nao depende da IA.

Somente a descricao da solicitacao e enviada para a IA. O backend nao envia usuario, e-mail, token, comentarios ou historico.

## Rodando localmente sem Docker

### 1. MySQL local

Crie um banco MySQL local chamado `requestflow` e um usuario para a aplicacao. Use os mesmos valores configurados no seu `.env` ou nas variaveis de ambiente do sistema.

Exemplo de URL esperada pelo backend:

```text
jdbc:mysql://localhost:3306/requestflow
```

### 2. Backend com Quarkus

Windows:

```powershell
cd a_code\b_backend_request_flow\request-flow
mvnw.cmd quarkus:dev
```

Linux/macOS:

```bash
cd a_code/b_backend_request_flow/request-flow
./mvnw quarkus:dev
```

Se preferir Maven instalado globalmente:

```powershell
cd a_code\b_backend_request_flow\request-flow
mvn quarkus:dev
```

API:

```text
http://localhost:8080/api
```

Swagger:

```text
http://localhost:8080/api/swagger-ui
```

### 3. Front-end com npm

```powershell
cd a_code\a_frontend_request_flow\request-flow
npm install
npm start
```

No navegador:

```text
http://localhost:4200
```

## Rodando com Dockerfiles individuais

Os comandos abaixo usam os caminhos reais deste repositorio.

Backend:

```powershell
docker build -t requestflow-backend .\a_code\b_backend_request_flow\request-flow
```

Front-end:

```powershell
docker build -t requestflow-frontend .\a_code\a_frontend_request_flow\request-flow
```

Equivalente conceitual, caso voce tenha pastas ou atalhos chamados `backend` e `frontend`:

```bash
docker build -t requestflow-backend ./backend
docker build -t requestflow-frontend ./frontend
```

## Rodando com Docker Compose

Na raiz do projeto:

```powershell
docker compose up --build
```

Para parar:

```powershell
docker compose down
```

Para subir em segundo plano:

```powershell
docker compose up -d --build
```

Para remover tambem o volume do banco local:

```powershell
docker compose down -v
```

Servicos do Compose:

- `mysql`: banco MySQL 8
- `backend`: API Quarkus
- `frontend`: aplicacao Angular servida por Nginx

URLs esperadas:

- Front-end: `http://localhost`
- Backend/API: `http://localhost:8080/api`
- Swagger: `http://localhost:8080/api/swagger-ui`
- MySQL: `localhost:3306`

## Usuarios de desenvolvimento

Se o seed de desenvolvimento estiver habilitado, os usuarios padrao sao:

| Perfil | E-mail | Senha |
| --- | --- | --- |
| USER | `user@requestflow.com` | `123456` |
| ANALYST | `analyst@requestflow.com` | `123456` |
| MANAGER | `manager@requestflow.com` | `123456` |

Essas credenciais sao apenas para desenvolvimento local.

## Endpoints principais

Todos os endpoints da API usam o prefixo:

```text
/api
```

Autenticacao:

- `POST /auth/login`
- `GET /auth/me`

Solicitacoes:

- `GET /requests`
- `POST /requests`
- `GET /requests/{id}`
- `POST /requests/{id}/comments`
- `PUT /requests/{id}/assume`
- `PUT /requests/{id}/status`
- `PUT /requests/{id}/resolve`
- `PUT /requests/{id}/cancel`
- `PUT /requests/{id}/reassign`
- `POST /requests/ai-suggestion`

Dashboard:

- `GET /dashboard`
- `GET /dashboard/analyst`
- `GET /dashboard/manager`

Usuarios:

- `GET /users`
- `GET /users/{id}`
- `POST /users`
- `PATCH /users/{id}/active`

O header JWT esperado nas rotas protegidas e:

```text
Authorization: Bearer <token>
```

## Comandos uteis

Backend:

```powershell
cd a_code\b_backend_request_flow\request-flow
mvn test
mvn quarkus:dev
```

Backend com Maven Wrapper no Windows:

```powershell
cd a_code\b_backend_request_flow\request-flow
mvnw.cmd quarkus:dev
```

Backend com Maven Wrapper no Linux/macOS:

```bash
cd a_code/b_backend_request_flow/request-flow
./mvnw quarkus:dev
```

Front-end:

```powershell
cd a_code\a_frontend_request_flow\request-flow
npm install
npm start
npm run build
```

Compose:

```powershell
docker compose up --build
docker compose down
docker compose logs mysql
docker compose logs backend
docker compose logs frontend
```


