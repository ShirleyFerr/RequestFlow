-- Criação do banco de dados RequestFlow
CREATE DATABASE IF NOT EXISTS RequestFlow;
USE RequestFlow;

-- Tabela de Usuários
CREATE TABLE User (
    idUser INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    passwordHash VARCHAR(255) NOT NULL,
    role VARCHAR(45) NOT NULL,
    active TINYINT DEFAULT 1,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de Solicitações
CREATE TABLE Request (
    idRequest INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(45) NOT NULL,
    description TEXT,
    category VARCHAR(45),
    priority VARCHAR(45),
    status VARCHAR(45) NOT NULL,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    dueDate DATE,
    resolvedAt DATETIME,
    requesterId INT NOT NULL,
    assigneeId INT,
    FOREIGN KEY (requesterId) REFERENCES User(idUser) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (assigneeId) REFERENCES User(idUser) ON DELETE SET NULL ON UPDATE CASCADE
);

-- Tabela de Histórico de Status
CREATE TABLE StatusHistory (
    idStatusHistory INT AUTO_INCREMENT PRIMARY KEY,
    requestId INT NOT NULL,
    oldStatus VARCHAR(45),
    newStatus VARCHAR(45) NOT NULL,
    changedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    changedBy INT NOT NULL,
    FOREIGN KEY (requestId) REFERENCES Request(idRequest) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (changedBy) REFERENCES User(idUser) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Tabela de Comentários na Solicitação
CREATE TABLE RequestComment (
    idRequestComment INT AUTO_INCREMENT PRIMARY KEY,
    message TEXT NOT NULL,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    requestId INT NOT NULL,
    authorId INT NOT NULL,
    FOREIGN KEY (requestId) REFERENCES Request(idRequest) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (authorId) REFERENCES User(idUser) ON DELETE RESTRICT ON UPDATE CASCADE
);
