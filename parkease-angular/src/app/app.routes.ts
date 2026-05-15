import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

import { LoginComponent }    from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { ProfileComponent }  from './driver/profile/profile.component';
import { PrivacyPolicyComponent } from './static/privacy-policy/privacy-policy.component';
import { TermsComponent }         from './static/terms/terms.component';
import { CookiesComponent }       from './static/cookies/cookies.component';
import { AboutUsComponent }       from './static/about-us/about-us.component';
import { BlogComponent }          from './static/blog/blog.component';
import { CareersComponent }       from './static/careers/careers.component';
import { LearnMoreComponent }     from './static/learn-more/learn-more.component';

import { DriverDashboardComponent } from './driver/driver-dashboard/driver-dashboard.component';
import { SearchLotsComponent }      from './driver/search-lots/search-lots.component';
import { NearbyLotsComponent }      from './driver/nearby-lots/nearby-lots.component';
import { LotDetailComponent }       from './driver/lot-detail/lot-detail.component';
import { MyBookingsComponent }      from './driver/my-bookings/my-bookings.component';
import { MyVehiclesComponent }      from './driver/my-vehicles/my-vehicles.component';
import { PaymentHistoryComponent }  from './driver/payment-history/payment-history.component';
import { NotificationsComponent }   from './driver/notifications/notifications.component';

import { ManagerDashboardComponent } from './manager/manager-dashboard/manager-dashboard.component';
import { CreateLotComponent }        from './manager/create-lot/create-lot.component';
import { ManageSpotsComponent }      from './manager/manage-spots/manage-spots.component';
import { ManagerRevenueComponent }   from './manager/manager-revenue/manager-revenue.component';
import { ManagerAnalyticsComponent } from './manager/manager-analytics/manager-analytics.component';

import { AdminDashboardComponent } from './admin/admin-dashboard/admin-dashboard.component';
import { ManageLotsComponent }     from './admin/manage-lots/manage-lots.component';
import { AdminAnalyticsComponent } from './admin/admin-analytics/admin-analytics.component';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },

  { path: 'login',          component: LoginComponent },
  { path: 'register',       component: RegisterComponent },
  { path: 'profile',        component: ProfileComponent, canActivate: [authGuard] },
  { path: 'privacy-policy', component: PrivacyPolicyComponent },
  { path: 'terms',          component: TermsComponent },
  { path: 'cookies',        component: CookiesComponent },
  { path: 'about',          component: AboutUsComponent },
  { path: 'blog',           component: BlogComponent },
  { path: 'careers',        component: CareersComponent },
  { path: 'learn-more',     component: LearnMoreComponent },

  {
    path: 'driver',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['DRIVER'] },
    children: [
      { path: 'dashboard',     component: DriverDashboardComponent },
      { path: 'search-lots',   component: SearchLotsComponent },
      { path: 'nearby-lots',   component: NearbyLotsComponent },
      { path: 'lot/:id',       component: LotDetailComponent },
      { path: 'my-bookings',   component: MyBookingsComponent },
      { path: 'my-vehicles',   component: MyVehiclesComponent },
      { path: 'payments',      component: PaymentHistoryComponent },
      { path: 'notifications', component: NotificationsComponent },
    ]
  },

  {
    path: 'manager',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['MANAGER'] },
    children: [
      { path: 'dashboard',     component: ManagerDashboardComponent },
      { path: 'create-lot',    component: CreateLotComponent },
      { path: 'lots/:id/spots', component: ManageSpotsComponent },
      { path: 'revenue',       component: ManagerRevenueComponent },
      { path: 'analytics/:id', component: ManagerAnalyticsComponent },
    ]
  },

  {
    path: 'admin',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    children: [
      { path: 'dashboard',   component: AdminDashboardComponent },
      { path: 'manage-lots', component: ManageLotsComponent },
      { path: 'analytics',   component: AdminAnalyticsComponent },
    ]
  },

  { path: '**', redirectTo: '/login' }
];
