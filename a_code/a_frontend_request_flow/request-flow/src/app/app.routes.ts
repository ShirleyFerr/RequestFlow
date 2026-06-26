import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards';
import { Role } from './core/models';
import { AppLayoutComponent } from './layout/app-layout/app-layout';

export const routes: Routes = [
  {
    path: 'login',
    redirectTo: 'auth/login',
    pathMatch: 'full',
  },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: '',
    canActivate: [authGuard],
    component: AppLayoutComponent,
    children: [
      {
        path: 'dashboard',
        canActivate: [roleGuard],
        data: {
          roles: [Role.ANALYST, Role.MANAGER],
        },
        loadChildren: () => import('./features/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES),
      },
      {
        path: 'requests',
        loadChildren: () => import('./features/requests/requests.routes').then((m) => m.REQUESTS_ROUTES),
      },
      {
        path: 'profile',
        loadChildren: () => import('./features/profile/profile.routes').then((m) => m.PROFILE_ROUTES),
      },
      {
        path: 'users',
        canActivate: [roleGuard],
        data: {
          roles: [Role.MANAGER],
        },
        loadChildren: () => import('./features/users/users.routes').then((m) => m.USERS_ROUTES),
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'requests',
      },
    ],
  },
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];
