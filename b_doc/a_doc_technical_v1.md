# Documentação Técnica - RequestFlow

## 1. Visão Técnica Geral

O RequestFlow é uma aplicação web fullstack para gestão de solicitações internas. A solução é composta por:

- front-end Angular;
- backend Quarkus;
- banco de dados MySQL;
- execução local ou containerizada com Docker Compose.

A comunicação entre front-end e backend ocorre por API REST JSON. A autenticação usa JWT enviado no header `Authorization: Bearer <token>`.

## 2. Arquitetura da Solução

### Visão geral

```text
Navegador
  |
  | HTTP
  v
Angular + Nginx
  |
  | REST JSON / JWT
  v
Quarkus API
  |
  | Hibernate ORM / JDBC
  v
MySQL
```

### Responsabilidades

- **Front-end:** interface, rotas, guards, formulários, chamadas HTTP e experiência por perfil.
- **Backend:** autenticação, autorização, regras de negócio, persistência, IA/fallback e contratos REST.
- **MySQL:** armazenamento de usuários, solicitações, comentários e histórico.
- **Docker Compose:** orquestra MySQL, backend e front-end.

## 3. Front-end

### Stack

- Angular 21
- Angular Material
- TypeScript
- SCSS
- Reactive Forms
- HttpClient
- RxJS
- Guards
- Interceptors
- Lazy loading

### Estrutura principal

```text
src/app
├── core
│   ├── guards
│   ├── interceptors
│   ├── models
│   └── services
├── shared
│   ├── components
│   ├── directives
│   └── pipes
├── layout
│   ├── app-layout
│   ├── sidebar
│   └── topbar
└── features
    ├── auth
    ├── dashboard
    ├── profile
    ├── requests
    └── users
```

### Serviços principais

- `AuthService`: login, `/auth/me` e logout.
- `TokenService`: armazenamento e remoção do JWT.
- `CurrentUserService`: usuário autenticado no front-end.
- `PermissionService`: regras de exibição e ações por perfil.
- `RequestService`: solicitações, comentários, status, assumir e reatribuir.
- `DashboardService`: dashboards do analista e gestor.
- `UserService`: gestão de usuários.
- `ProfileService`: perfil autenticado e troca de senha.
- `AIService`: sugestão de categoria, prioridade e resumo.

### Interceptors

- `AuthInterceptor`: adiciona `Authorization: Bearer <token>` nas requisições autenticadas.
- `ErrorInterceptor`: trata erros HTTP e exibe mensagens amigáveis.

### Rotas principais

- `/auth/login`
- `/requests`
- `/requests/new`
- `/requests/:id`
- `/dashboard`
- `/users`
- `/profile`

As rotas usam guards e regras por perfil. Não existem telas duplicadas por persona; a mesma feature adapta conteúdo e ações conforme o usuário logado.

### Configuração de API

O front-end usa `environment.apiUrl`:

- em desenvolvimento Angular local: `http://localhost:8080/api`;
- em build containerizado: `/api`, com proxy pelo Nginx do front-end.

## 4. Backend

### Stack

- Java
- Quarkus
- Maven
- API REST JSON
- Hibernate ORM com Panache/JPA
- MySQL JDBC
- Bean Validation
- JWT
- BCrypt
- JUnit
- JaCoCo

### Estrutura de pacotes

```text
com.requestflow
├── config
├── controller
├── domain
├── dto
├── exception
├── integration
├── mapper
├── repository
├── security
└── service
```

### Camadas

- **controller:** expõe endpoints REST.
- **service:** concentra regras de negócio e permissões.
- **repository:** acesso a dados.
- **domain:** entidades e enums.
- **dto:** objetos de entrada e saída da API.
- **mapper:** conversão de entidades para DTOs.
- **security:** validação e geração de JWT.
- **integration:** integração com IA e fallback.
- **exception:** exceções customizadas e respostas de erro padronizadas.
- **config:** seed e configurações auxiliares.

### Controllers principais

- `AuthController`
- `ProfileController`
- `UserController`
- `RequestController`
- `DashboardController`
- `HealthController`, se disponível no projeto

### Regras no backend

O backend não confia apenas no front-end para permissões. As regras de perfil são aplicadas nos services e/ou via anotações de segurança.

## 5. Banco de Dados

Banco oficial: **MySQL**.

### Entidades principais

#### User

Tabela: `app_user`

Campos principais:

- `id`
- `name`
- `email`
- `passwordHash`
- `birthDate`
- `role`
- `active`
- `createdAt`

Observação: `passwordHash` é persistido, mas nunca deve ser retornado em DTOs de saída.

#### Request

Tabela: `internal_request`

Campos principais:

- `id`
- `title`
- `description`
- `category`
- `priority`
- `status`
- `requester`
- `assignee`
- `createdAt`
- `updatedAt`
- `dueDate`
- `resolvedAt`
- `aiSummary`

#### RequestComment

Tabela: `request_comment`

Campos principais:

- `id`
- `request`
- `author`
- `message`
- `createdAt`

#### StatusHistory

Tabela: `status_history`

Campos principais:

- `id`
- `request`
- `oldStatus`
- `newStatus`
- `changedBy`
- `changedAt`
- `note`

### Relacionamentos

- `User` 1:N `Request` como solicitante.
- `User` 1:N `Request` como responsável.
- `Request` 1:N `RequestComment`.
- `Request` 1:N `StatusHistory`.

## 6. Segurança

### Autenticação

O login é feito por `POST /auth/login`. Em caso de sucesso, o backend retorna um JWT.

O front-end envia o token nas requisições protegidas:

```text
Authorization: Bearer <token>
```

### JWT

O token contém informações necessárias para identificar o usuário autenticado, como:

- subject com id do usuário;
- e-mail;
- role;
- expiração.

### Senhas

- Senhas são armazenadas com hash seguro.
- O projeto usa BCrypt.
- Senha em texto puro não é persistida.
- `passwordHash` nunca deve aparecer em respostas da API ou telas.
- A senha inicial de usuário cadastrado por gestor é derivada da data de nascimento no formato `ddMMyyyy`.
- Usuários autenticados podem alterar a própria senha via `/profile/password`.

### Autorização por perfil

- `USER`: cria e acompanha as próprias solicitações.
- `ANALYST`: assume e atende solicitações abertas ou atribuídas.
- `MANAGER`: visualiza tudo, reatribui responsável e gerencia usuários.

Regras críticas, como bloqueio de ações em solicitações CANCELLED e acesso restrito a usuários, são aplicadas no backend.

## 7. IA

### Objetivo

A IA sugere:

- categoria;
- prioridade;
- resumo.

A sugestão é auxiliar e editável. A criação manual da solicitação não depende da IA.

### Provider Gemini

O backend possui integração preparada com Gemini. O front-end nunca chama Gemini diretamente.

### Fallback local

Quando o provider não está configurado, a chave está ausente, ocorre timeout, erro externo ou resposta inválida, o backend usa fallback local.

### Variáveis de ambiente

- `AI_PROVIDER`
- `AI_API_KEY`
- `AI_MODEL`
- `AI_TIMEOUT_MS`

Nenhuma chave real deve ser versionada.

### Segurança de dados enviados

O backend envia para a IA apenas a descrição da solicitação. Não envia:

- usuário;
- e-mail;
- token;
- senha;
- comentários;
- histórico;
- dados sensíveis.

## 8. Endpoints Principais

### Auth

- `POST /auth/login`
- `GET /auth/me`

### Profile

- `GET /profile`
- `PUT /profile/password`

### Users

- `GET /users`
- `GET /users/{id}`
- `GET /users/analysts`
- `POST /users`
- `PATCH /users/{id}/active`

Endpoints de usuários são restritos ao perfil MANAGER, exceto quando alguma consulta auxiliar for explicitamente liberada pelo backend.

### Requests

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

### Dashboard

- `GET /dashboard`
- `GET /dashboard/analyst`
- `GET /dashboard/manager`

## 9. Docker e Ambiente

### Dockerfiles

O projeto possui Dockerfiles separados:

- backend Quarkus;
- front-end Angular servido por Nginx.

### Compose

O `compose.yml` da raiz orquestra:

- `mysql`;
- `backend`;
- `frontend`.

O MySQL usa volume para persistência. O backend depende do MySQL saudável. O front-end depende do backend.

### Variáveis principais

Sem valores reais:

- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `MYSQL_ROOT_PASSWORD`
- `MYSQL_HOST`
- `MYSQL_PORT`
- `BACKEND_PORT`
- `FRONTEND_PORT`
- `CORS_ORIGINS`
- `FRONTEND_ORIGIN`
- `JWT_SECRET`
- `JWT_ISSUER`
- `JWT_EXPIRATION_MINUTES`
- `AI_PROVIDER`
- `AI_API_KEY`
- `AI_MODEL`
- `AI_TIMEOUT_MS`

Arquivos reais como `.env` e `application.properties` não devem ser versionados quando contiverem valores sensíveis.

### Execução local com Compose

```bash
docker compose up --build
```

Parar:

```bash
docker compose down
```

## 10. Testes e Validações

### Backend

O backend possui testes automatizados com JUnit e Quarkus Test.

Comando:

```bash
mvn test
```

Coberturas existentes incluem autenticação, usuários, solicitações, dashboards, IA/fallback, perfil e permissões principais.

### Front-end

O front-end é validado por build Angular.

Comando:

```bash
npm run build
```

O projeto também possui testes unitários de serviços e regras em algumas áreas.

### Pré-check e E2E

Foram criados relatórios operacionais de validação:

- `E2E_PRECHECK.md`
- `E2E_TEST_REPORT.md`

Eles registram validações de build, Docker Compose, endpoints principais, JWT, MySQL e fluxos por perfil.

## 11. Riscos Técnicos

### Dependência de IA externa

Quando Gemini está ativo, a resposta pode depender de rede externa, chave válida e tempo de resposta do provider. Mitigação: fallback local e timeout configurável.

### Segurança de variáveis

`.env`, chaves e segredos não devem ser versionados. Examples devem conter apenas placeholders.

### Crescimento do front-end

O build Angular apresenta warnings de budget. Não é bloqueador, mas pode ser revisado futuramente.

### Evolução de permissões

Como há regras diferentes por perfil, alterações futuras devem preservar a validação no backend, não apenas no front-end.

### Dados persistidos em Docker

O volume do MySQL preserva dados entre execuções. Para reset total em desenvolvimento, é necessário remover volumes intencionalmente.

## 12. Glossário Técnico

| Termo | Definição |
|---|---|
| Angular | Framework usado no front-end |
| Angular Material | Biblioteca de componentes visuais do front-end |
| Quarkus | Framework Java usado no backend |
| MySQL | Banco de dados relacional oficial do projeto |
| REST | Estilo de comunicação HTTP usado pela API |
| JWT | Token usado para autenticação |
| BCrypt | Algoritmo usado para hash de senha |
| DTO | Objeto de entrada ou saída usado na API |
| Entity | Classe persistida pelo ORM |
| Repository | Camada de acesso ao banco |
| Service | Camada de regra de negócio |
| Controller | Camada que expõe endpoints REST |
| Mapper | Conversor entre entidade e DTO |
| CORS | Controle de origens permitidas para chamadas HTTP |
| Dockerfile | Arquivo que define como gerar uma imagem Docker |
| Docker Compose | Ferramenta para subir múltiplos containers |
| Gemini | Provider externo de IA usado para sugestão |
| Fallback | Implementação local usada quando a IA externa não responde |
