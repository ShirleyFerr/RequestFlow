import { User } from './user.model';

export interface RequestComment {
  id: string;
  requestId: string;
  author: User;
  message: string;
  createdAt: string;
}
