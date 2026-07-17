import { Routes } from '@angular/router';
import { Home } from './features/home/home';
import { AuctionDetail } from './features/auction-detail/auction-detail';
import { CategoryPage } from './features/category-page/category-page';
import { UserProfile } from './features/user-profile/user-profile';
import { Login } from './features/login/login';
import { authGuard } from './core/guards/auth.guard';
import { Register } from './features/register/register';
import { Sell } from './features/sell/sell';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'aste/:id', component: AuctionDetail },
  { path: 'categoria/:id', component: CategoryPage },
  { path: 'utente', component: UserProfile, canActivate: [authGuard] },
  { path: 'vendi', component: Sell, canActivate: [authGuard] },
  { path: 'login', component: Login },
  { path: 'registrati', component: Register },
  { path: '**', redirectTo: '' },
];
