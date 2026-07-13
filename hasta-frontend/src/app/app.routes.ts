import { Routes } from '@angular/router';
import { Home } from './features/home/home';
import { AuctionDetail } from './features/auction-detail/auction-detail';
import { CategoryPage } from './features/category-page/category-page';
import { UserProfile } from './features/user-profile/user-profile';
import { Settings } from './features/settings/settings';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'aste/:id', component: AuctionDetail },
  { path: 'categoria/:id', component: CategoryPage },
  { path: 'utente', component: UserProfile },
  { path: 'impostazioni', component: Settings },
  { path: '**', redirectTo: '' },
];
