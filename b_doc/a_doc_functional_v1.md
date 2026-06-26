# Documentação Funcional - RequestFlow

## 1. Visão Geral

O RequestFlow é um portal corporativo para abertura, acompanhamento e atendimento de solicitações internas. A aplicação centraliza demandas que normalmente ficariam espalhadas em e-mails, mensagens ou planilhas, oferecendo rastreabilidade, controle de status, comentários, prazos de entrega e indicadores de acompanhamento.

O sistema possui três perfis principais: solicitante, analista e gestor. Cada perfil acessa as mesmas áreas base da aplicação, mas com permissões, menus, filtros e ações diferentes.

## 2. Objetivo do Sistema

O objetivo do RequestFlow é organizar o ciclo de vida de solicitações internas desde a abertura até a resolução ou cancelamento. A solução permite que usuários registrem demandas, analistas atendam e atualizem o andamento, e gestores acompanhem indicadores, usuários e responsabilidades.

O sistema também apoia a classificação das solicitações por meio de uma sugestão de IA para categoria, prioridade e resumo. Essa sugestão é editável e possui fallback local para que o fluxo manual continue funcionando mesmo se a IA externa falhar.

## 3. Perfis de Usuário

### USER / Solicitante

Perfil destinado ao usuário que abre solicitações internas e acompanha o andamento das próprias demandas.

Principais permissões:

- Fazer login.
- Acessar "Minhas solicitações".
- Criar nova solicitação.
- Visualizar detalhes das próprias solicitações.
- Adicionar comentários em solicitações permitidas.
- Acessar "Meu perfil".
- Alterar a própria senha.

Restrições:

- Não acessa dashboard.
- Não acessa gestão de usuários.
- Não altera status.
- Não assume solicitações.
- Não visualiza solicitações de outros usuários.

### ANALYST / Analista

Perfil destinado ao usuário responsável pelo atendimento operacional das solicitações.

Principais permissões:

- Fazer login.
- Acessar dashboard operacional.
- Visualizar solicitações abertas sem responsável ou atribuídas a ele.
- Assumir solicitações sem responsável.
- Alterar status de solicitações permitidas.
- Resolver solicitações.
- Cancelar solicitações válidas.
- Adicionar comentários.
- Acessar "Meu perfil".
- Alterar a própria senha.

Restrições:

- Não cria novas solicitações.
- Não acessa gestão de usuários.
- Não acessa dashboard gerencial global.
- Não reatribui solicitações para outro analista.

### MANAGER / Gestor

Perfil destinado ao acompanhamento gerencial e administração de usuários.

Principais permissões:

- Fazer login.
- Acessar dashboard gerencial.
- Visualizar todas as solicitações.
- Visualizar detalhes completos.
- Reatribuir responsável.
- Visualizar histórico.
- Adicionar comentários.
- Acessar gestão de usuários.
- Cadastrar usuários.
- Acessar "Meu perfil".
- Alterar a própria senha.

Restrições:

- Não cria nova solicitação.
- Não edita solicitação cancelada.
- Não altera regras de senha manualmente no cadastro de usuários.

## 4. Funcionalidades

### Login

Permite que usuários acessem a aplicação com e-mail e senha. Após autenticação, o sistema direciona o usuário conforme o perfil:

- USER: "Minhas solicitações".
- ANALYST: dashboard operacional.
- MANAGER: dashboard gerencial.

### Meu Perfil

Tela disponível para todos os perfis. Exibe dados não sensíveis do usuário autenticado, como nome, e-mail, perfil, status, data de criação e data de nascimento quando disponível.

Também permite alterar a própria senha informando nova senha e confirmação. A senha atual e o hash da senha nunca são exibidos.

### Solicitações

Permite consultar solicitações com filtros, paginação, indicação de SLA e acesso ao detalhe.

A listagem muda conforme o perfil:

- USER vê apenas as próprias solicitações.
- ANALYST vê solicitações abertas sem responsável ou atribuídas a ele.
- MANAGER vê todas as solicitações.

### Nova Solicitação

Disponível apenas para USER. O formulário exige:

- título;
- descrição;
- categoria;
- prioridade;
- data de entrega (`dueDate`).

O usuário pode solicitar sugestão de IA para categoria, prioridade e resumo. A sugestão pode ser aplicada e depois editada manualmente.

### Detalhe da Solicitação

Exibe os dados completos da solicitação:

- ID;
- título;
- descrição;
- categoria;
- prioridade;
- status;
- solicitante;
- responsável;
- data de criação;
- data de atualização;
- `dueDate`;
- data de resolução, quando houver;
- SLA;
- resumo da IA, quando houver;
- comentários;
- histórico de status.

As ações disponíveis dependem do perfil e do status da solicitação.

### Dashboard do Analista

Disponível para ANALYST. Apresenta visão operacional da fila do analista, com indicadores de atendimento, solicitações atribuídas, alertas de SLA e itens críticos ou de alta prioridade.

### Dashboard Gerencial

Disponível para MANAGER. Apresenta visão consolidada de todas as solicitações, incluindo indicadores globais, volume por status, volume por categoria, prioridade, alertas de SLA e desempenho da equipe.

### Gestão de Usuários

Disponível apenas para MANAGER. Permite listar usuários e cadastrar novos usuários.

O cadastro exige:

- nome;
- e-mail;
- perfil;
- data de nascimento;
- status ativo.

Não existe campo manual de senha. A senha inicial é gerada a partir da data de nascimento no formato `ddMMyyyy`.

## 5. Telas por Perfil

### USER / Solicitante

- Login.
- Meu perfil.
- Minhas solicitações.
- Nova solicitação.
- Detalhes da própria solicitação.

### ANALYST / Analista

- Login.
- Meu perfil.
- Dashboard do analista.
- Lista de solicitações abertas ou atribuídas.
- Detalhes da solicitação permitida.

### MANAGER / Gestor

- Login.
- Meu perfil.
- Dashboard gerencial.
- Lista geral de solicitações.
- Detalhes de qualquer solicitação.
- Gestão de usuários.

## 6. Regras de Negócio

- Somente MANAGER pode cadastrar usuários.
- USER pode criar solicitações.
- ANALYST pode assumir solicitações sem responsável.
- ANALYST pode atender, alterar status, resolver e cancelar solicitações permitidas.
- MANAGER pode visualizar todas as solicitações.
- MANAGER pode reatribuir responsável.
- Solicitação com status CANCELLED é somente leitura.
- Solicitação CANCELLED não permite comentário, alteração de status, assumir, resolver, cancelar novamente ou reatribuir.
- Toda solicitação deve possuir título, descrição, categoria, prioridade, status, solicitante, data de criação e `dueDate`.
- `dueDate` é usado para cálculo e exibição de SLA.
- Solicitações vencidas são destacadas quando `dueDate` é anterior à data atual e o status não é RESOLVED nem CANCELLED.
- Comentários ficam vinculados à solicitação e ao autor.
- Mudanças de status e reatribuições registram histórico.
- A senha inicial de usuário cadastrado é derivada da data de nascimento.
- Usuário autenticado pode acessar o próprio perfil.
- Usuário autenticado pode alterar a própria senha.
- Senhas e `passwordHash` nunca são exibidos nas respostas ou telas.
- Sugestão de IA é apenas auxiliar.
- Sugestão de IA pode ser aplicada e editada.
- Falha na IA não impede criação manual da solicitação.
- USER e ANALYST não acessam gestão de usuários.
- USER não acessa dashboard.
- ANALYST e MANAGER não acessam nova solicitação.

## 7. Fluxos Principais

### Fluxo do Solicitante

1. O solicitante acessa o sistema e faz login.
2. O sistema redireciona para "Minhas solicitações".
3. O solicitante cria uma nova solicitação com título, descrição, categoria, prioridade e `dueDate`.
4. Opcionalmente, solicita sugestão de IA.
5. O solicitante aplica ou edita a sugestão.
6. A solicitação é criada com status OPEN.
7. O solicitante acompanha o andamento pela listagem e detalhe.
8. O solicitante pode comentar enquanto a solicitação não estiver CANCELLED.
9. O solicitante pode acessar "Meu perfil" e alterar a própria senha.

### Fluxo do Analista

1. O analista acessa o sistema e faz login.
2. O sistema redireciona para o dashboard operacional.
3. O analista consulta a fila de solicitações abertas ou atribuídas.
4. O analista assume uma solicitação sem responsável.
5. O analista atualiza o status conforme o atendimento.
6. O analista registra comentários.
7. O analista resolve ou cancela a solicitação quando aplicável.
8. O histórico da solicitação registra as mudanças.
9. O analista pode acessar "Meu perfil" e alterar a própria senha.

### Fluxo do Gestor

1. O gestor acessa o sistema e faz login.
2. O sistema redireciona para o dashboard gerencial.
3. O gestor acompanha indicadores globais.
4. O gestor consulta todas as solicitações.
5. O gestor abre o detalhe de qualquer solicitação.
6. O gestor pode reatribuir responsável.
7. O gestor acompanha histórico e comentários.
8. O gestor acessa gestão de usuários.
9. O gestor cadastra novo usuário informando nome, e-mail, perfil, data de nascimento e status.
10. O sistema gera a senha inicial com base na data de nascimento.
11. O gestor pode acessar "Meu perfil" e alterar a própria senha.

## 8. Critérios de Aceitação

### Autenticação e Perfil

- Usuário com credenciais válidas consegue acessar o sistema.
- Usuário inválido não consegue acessar.
- Cada perfil é redirecionado para a área correta.
- "Meu perfil" está disponível para todos os perfis.
- Perfil não exibe senha nem `passwordHash`.
- Troca de senha exige nova senha e confirmação iguais.

### Solicitações

- USER cria solicitação com dados obrigatórios.
- USER visualiza apenas as próprias solicitações.
- ANALYST visualiza solicitações abertas sem responsável ou atribuídas a ele.
- MANAGER visualiza todas as solicitações.
- Listagens possuem filtros e paginação.
- `dueDate` aparece na criação, listagem e detalhe.
- SLA é exibido e destaca solicitações vencidas.
- Detalhe exibe dados, comentários e histórico.
- CANCELLED é somente leitura.

### IA

- Sugestão de IA retorna categoria, prioridade e resumo.
- Sugestão é editável.
- Falha na IA não bloqueia criação manual.
- Dados sensíveis não são enviados para a IA.

### Gestão de Usuários

- Somente MANAGER acessa gestão de usuários.
- MANAGER cadastra usuário com nome, e-mail, perfil, data de nascimento e status.
- Cadastro não possui campo manual de senha.
- Senha inicial é gerada a partir da data de nascimento.
- Usuários sem permissão recebem bloqueio ao tentar acessar gestão de usuários.

### Segurança e Permissões

- USER não acessa dashboard.
- USER não altera status.
- ANALYST não cria solicitação.
- ANALYST não acessa usuários.
- MANAGER não cria solicitação.
- Regras de perfil são aplicadas no front-end e no backend.
- Respostas não expõem `passwordHash`.

## 9. Glossário

| Termo | Definição |
|---|---|
| RequestFlow | Sistema de gestão de solicitações internas |
| Solicitação | Demanda interna registrada por um usuário |
| USER / Solicitante | Perfil que cria e acompanha as próprias solicitações |
| ANALYST / Analista | Perfil que atende solicitações abertas ou atribuídas |
| MANAGER / Gestor | Perfil que acompanha indicadores, visualiza tudo e gerencia usuários |
| Status | Etapa atual da solicitação no fluxo |
| Categoria | Tipo da solicitação |
| Prioridade | Grau de urgência da solicitação |
| dueDate | Data de entrega ou prazo esperado da solicitação |
| SLA | Indicação de cumprimento ou atraso com base na `dueDate` |
| Comentário | Mensagem registrada dentro da solicitação |
| Histórico | Registro das mudanças de status e movimentações relevantes |
| IA | Recurso de sugestão automática de categoria, prioridade e resumo |
| Fallback | Resposta alternativa usada quando a IA externa falha ou não está configurada |
