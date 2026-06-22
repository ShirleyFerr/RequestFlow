import { RequestStatus } from './request-status.enum';
import { User } from './user.model';

export interface StatusHistory {
  id: string;
  requestId: string;
  fromStatus: RequestStatus | null;
  toStatus: RequestStatus;
  changedBy: User;
  changedAt: string;
  note?: string;
}
