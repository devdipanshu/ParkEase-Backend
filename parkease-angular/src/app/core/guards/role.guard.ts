import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const router = inject(Router);
  const role = localStorage.getItem('role');
  const allowedRoles: string[] = route.data['roles'];

  if (!allowedRoles.includes(role || '')) {
    router.navigate(['/login']);
    return false;
  }
  return true;
};
