import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards';
import { Role } from '../../core/models';
import { RequestCreateComponent } from './request-create/request-create';
import { RequestDetailComponent } from './request-detail/request-detail';
import { RequestListComponent } from './request-list/request-list';

export const REQUESTS_ROUTES: Routes = [
  {
    path: 'new',
    canActivate: [roleGuard],
    data: {
      roles: [Role.USER],
    },
    component: RequestCreateComponent,
  },
  {
    path: ':id',
    component: RequestDetailComponent,
  },
  {
    path: '',
    component: RequestListComponent,
  },
];
