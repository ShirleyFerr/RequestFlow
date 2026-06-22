import { AISuggestion } from './ai-suggestion.model';
import { RequestCategory } from './request-category.enum';
import { RequestComment } from './request-comment.model';
import { RequestPriority } from './request-priority.enum';
import { RequestStatus } from './request-status.enum';
import { StatusHistory } from './status-history.model';
import { User } from './user.model';

export interface Request {
  id: string;
  title: string;
  description: string;
  summary: string;
  category: RequestCategory;
  priority: RequestPriority;
  status: RequestStatus;
  requester: User;
  assignedTo?: User;
  dueDate: string;
  createdAt: string;
  updatedAt: string;
  resolvedAt?: string;
  cancelledAt?: string;
  aiSuggestion?: AISuggestion;
  comments: RequestComment[];
  statusHistory: StatusHistory[];
  slaStatus?: 'OVERDUE' | 'ON_TIME' | 'RESOLVED' | 'CANCELLED';
  overdue?: boolean;
}
