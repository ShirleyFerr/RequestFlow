import { Role } from './role.enum';

export interface User {
  id: string;
  name: string;
  email: string;
  role: Role;
  department?: string;
  birthDate?: string;
  active: boolean;
  createdAt?: string;
}
