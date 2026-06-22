import { Role, User } from '../models';

export const MOCK_USERS: User[] = [
  {
    id: 'usr-001',
    name: 'Marina Souza',
    email: 'user@requestflow.com',
    role: Role.USER,
    department: 'Financeiro',
    birthDate: '1992-04-18',
    active: true,
    createdAt: '2026-01-10T09:00:00-03:00',
  },
  {
    id: 'usr-002',
    name: 'Rafael Lima',
    email: 'analyst@requestflow.com',
    role: Role.ANALYST,
    department: 'TI',
    birthDate: '1988-09-27',
    active: true,
    createdAt: '2026-01-10T09:30:00-03:00',
  },
  {
    id: 'usr-003',
    name: 'Carolina Mendes',
    email: 'manager@requestflow.com',
    role: Role.MANAGER,
    department: 'Operacoes',
    birthDate: '1984-12-05',
    active: true,
    createdAt: '2026-01-10T10:00:00-03:00',
  },
  {
    id: 'usr-004',
    name: 'Bruno Alves',
    email: 'bruno.alves@requestflow.com',
    role: Role.USER,
    department: 'Comercial',
    birthDate: '1995-06-14',
    active: true,
    createdAt: '2026-02-02T11:15:00-03:00',
  },
];
