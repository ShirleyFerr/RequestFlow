import { DatePipe } from '@angular/common';
import { AfterViewInit, Component, ViewChild, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import {
  Request as RequestFlowRequest,
  RequestCategory,
  RequestPriority,
  RequestStatus,
  Role,
  User,
} from '../../../core/models';
import { CurrentUserService, PageTitleService, PermissionService, RequestService } from '../../../core/services';
import {
  EmptyStateComponent,
  PriorityBadgeComponent,
  SlaBadgeComponent,
  StatusBadgeComponent,
} from '../../../shared/components';
import {
  CategoryLabelPipe,
  PriorityLabelPipe,
  StatusLabelPipe,
  categoryLabel,
  priorityLabel,
  statusLabel,
} from '../../../shared/pipes';

interface RequestFilters {
  search: string;
  status: RequestStatus | '';
  priority: RequestPriority | '';
  category: RequestCategory | '';
  responsibleId: string;
  requesterId: string;
}

type SlaState = 'overdue' | 'near' | 'ok' | 'closed';

@Component({
  selector: 'app-request-list',
  imports: [
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatSelectModule,
    MatSortModule,
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
    EmptyStateComponent,
    PriorityBadgeComponent,
    ReactiveFormsModule,
    RouterLink,
    SlaBadgeComponent,
    StatusBadgeComponent,
    CategoryLabelPipe,
    PriorityLabelPipe,
    StatusLabelPipe,
  ],
  templateUrl: './request-list.html',
  styleUrl: './request-list.scss',
})
export class RequestListComponent implements AfterViewInit {
  @ViewChild(MatPaginator) private paginator?: MatPaginator;
  @ViewChild(MatSort) private sort?: MatSort;

  private readonly currentUserService = inject(CurrentUserService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly pageTitleService = inject(PageTitleService);
  protected readonly permissionService = inject(PermissionService);
  private readonly requestService = inject(RequestService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly today = this.toDateOnly(new Date());
  private readonly requests = signal<RequestFlowRequest[]>([]);

  protected readonly categoryOptions = Object.values(RequestCategory);
  protected readonly priorityOptions = Object.values(RequestPriority);
  protected readonly statusOptions = Object.values(RequestStatus);
  protected readonly status = RequestStatus;
  protected readonly currentUser = this.currentUserService.currentUser;
  protected readonly availableFilters = computed(() => {
    const user = this.currentUser();
    return user
      ? this.requestService.getAvailableFiltersByRole(user.role)
      : this.requestService.getAvailableFiltersByRole(Role.USER);
  });
  protected readonly pageTitle = computed(() => {
    const user = this.currentUser();
    return user ? this.pageTitleService.requestsTitle(user.role) : 'Solicitações';
  });
  protected readonly displayedColumns = computed(() => {
    const filters = this.availableFilters();
    const columns = ['id', 'title', 'category', 'priority', 'status'];

    if (filters.requester) {
      columns.push('requester');
    }

    columns.push('responsible', 'createdAt', 'dueDate', 'sla', 'actions');
    return columns;
  });
  protected readonly dataSource = new MatTableDataSource<RequestFlowRequest>([]);
  protected readonly filterForm = this.formBuilder.nonNullable.group({
    search: '',
    status: '' as RequestStatus | '',
    priority: '' as RequestPriority | '',
    category: '' as RequestCategory | '',
    responsibleId: '',
    requesterId: '',
  });

  protected readonly responsibleOptions = computed(() => {
    const responsibleById = new Map<string, User>();

    this.scopedRequests().forEach((request) => {
      if (request.assignedTo) {
        responsibleById.set(request.assignedTo.id, request.assignedTo);
      }
    });

    return [...responsibleById.values()];
  });

  protected readonly requesterOptions = computed(() => {
    const requesterById = new Map<string, User>();

    this.scopedRequests().forEach((request) => {
      requesterById.set(request.requester.id, request.requester);
    });

    return [...requesterById.values()];
  });

  private readonly scopedRequests = computed(() => {
    return this.requests();
  });

  constructor() {
    this.dataSource.filterPredicate = (request, filter) => {
      const filters = JSON.parse(filter) as RequestFilters;
      const search = filters.search.trim().toLowerCase();
      const matchesSearch = !search || request.title.toLowerCase().includes(search);
      const matchesStatus = !filters.status || request.status === filters.status;
      const matchesPriority = !filters.priority || request.priority === filters.priority;
      const matchesCategory = !filters.category || request.category === filters.category;
      const matchesResponsible =
        !filters.responsibleId || request.assignedTo?.id === filters.responsibleId;
      const matchesRequester = !filters.requesterId || request.requester.id === filters.requesterId;

      return (
        matchesSearch &&
        matchesStatus &&
        matchesPriority &&
        matchesCategory &&
        matchesResponsible &&
        matchesRequester
      );
    };

    this.dataSource.sortingDataAccessor = (request, property) => {
      if (property === 'createdAt') {
        return new Date(request.createdAt).getTime();
      }

      if (property === 'dueDate') {
        return new Date(request.dueDate).getTime();
      }

      return String(request[property as keyof RequestFlowRequest] ?? '');
    };

    this.loadRequests();
    this.filterForm.valueChanges.subscribe(() => this.loadRequests());
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator ?? null;
    this.dataSource.sort = this.sort ?? null;
  }

  protected clearFilters(): void {
    this.filterForm.reset({
      search: '',
      status: '',
      priority: '',
      category: '',
      responsibleId: '',
      requesterId: '',
    });
  }

  protected canUpdateStatus(request: RequestFlowRequest): boolean {
    const user = this.currentUser();
    return !!user && this.permissionService.canChangeStatus(user.role, request);
  }

  protected canAssume(request: RequestFlowRequest): boolean {
    const user = this.currentUser();
    return !!user && this.permissionService.canAssumeRequest(user.role, request);
  }

  protected assumeRequest(request: RequestFlowRequest): void {
    const user = this.currentUser();

    if (!user) {
      return;
    }

    this.requestService.assumeRequest(request.id).subscribe({
      next: () => {
        this.loadRequests();
        this.snackBar.open('Solicitação assumida com sucesso.', 'Fechar', {
          duration: 4000,
        });
      },
      error: () => {
        this.snackBar.open('Não foi possível assumir esta solicitação.', 'Fechar', {
          duration: 4000,
        });
      },
    });
  }

  protected slaState(request: RequestFlowRequest): SlaState {
    if ([RequestStatus.RESOLVED, RequestStatus.CANCELLED].includes(request.status)) {
      return 'closed';
    }

    const dueDate = this.toDateOnly(new Date(request.dueDate));
    const daysToDue = Math.ceil((dueDate - this.today) / 86_400_000);

    if (daysToDue < 0) {
      return 'overdue';
    }

    if (daysToDue <= 3) {
      return 'near';
    }

    return 'ok';
  }

  protected slaLabel(request: RequestFlowRequest): string {
    const labels: Record<SlaState, string> = {
      overdue: 'Vencido',
      near: 'Próximo',
      ok: 'Dentro do prazo',
      closed: 'Encerrado',
    };

    return labels[this.slaState(request)];
  }

  protected statusLabel(status: RequestStatus): string {
    return statusLabel(status);
  }

  protected priorityLabel(priority: RequestPriority): string {
    return priorityLabel(priority);
  }

  protected categoryLabel(category: RequestCategory): string {
    return categoryLabel(category);
  }

  private applyFilters(): void {
    this.dataSource.data = this.scopedRequests();
    this.dataSource.filter = JSON.stringify(this.filterForm.getRawValue());
    this.dataSource.paginator?.firstPage();
  }

  private loadRequests(): void {
    const filters = this.filterForm.getRawValue();

    this.requestService
      .listRequests({
        size: 100,
        status: filters.status,
        priority: filters.priority,
        category: filters.category,
        assigneeId: filters.responsibleId,
        requesterId: filters.requesterId,
      })
      .subscribe((page) => {
        this.requests.set(page.items);
        this.applyFilters();
      });
  }

  private toDateOnly(date: Date): number {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime();
  }
}
