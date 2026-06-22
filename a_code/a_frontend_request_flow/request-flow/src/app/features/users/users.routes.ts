import { Routes } from '@angular/router';
import { UserListComponent } from './user-list/user-list';

export const USERS_ROUTES: Routes = [
  {
    path: '',
    component: UserListComponent,
  },
];
