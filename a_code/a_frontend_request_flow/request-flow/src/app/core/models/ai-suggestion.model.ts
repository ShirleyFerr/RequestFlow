import { RequestCategory } from './request-category.enum';
import { RequestPriority } from './request-priority.enum';

export interface AISuggestion {
  category: RequestCategory;
  priority: RequestPriority;
  summary: string;
  source?: 'AI' | 'FALLBACK';
  confidence?: number;
  generatedAt?: string;
}
