import { Injectable } from '@angular/core';
import { Category, CategoryInfo } from '../models/category.model';

const CATALOG: CategoryInfo[] = [
  { id: Category.ELETTRONICA, label: 'Elettronica', icon: 'cpu' },
  { id: Category.VESTITI, label: 'Vestiti', icon: 'shirt' },
  { id: Category.ACCESSORI, label: 'Accessori', icon: 'watch' },
  { id: Category.ARREDAMENTI, label: 'Arredamenti', icon: 'sofa' },
  { id: Category.CANCELLERIA, label: 'Cancelleria', icon: 'pen' },
  { id: Category.COLLEZIONISMO, label: 'Collezionismo', icon: 'gem' },
  { id: Category.GIOCHI_E_GIOCATTOLI, label: 'Giochi e giocattoli', icon: 'gamepad' },
  { id: Category.SPORT_E_TEMPO_LIBERO, label: 'Sport e tempo libero', icon: 'ball' },
  { id: Category.AUTO_E_MOTO, label: 'Auto e moto', icon: 'car' },
  { id: Category.ELETTRODOMESTICI, label: 'Elettrodomestici', icon: 'fridge' },
  { id: Category.LIBRI_E_MEDIA, label: 'Libri e media', icon: 'book' },
  { id: Category.SALUTE_E_BENESSERE, label: 'Salute e benessere', icon: 'heart' },
  { id: Category.ALTRO, label: 'Altro', icon: 'box' },
];

@Injectable({ providedIn: 'root' })
export class CategoryService {
  getAll(): CategoryInfo[] {
    return CATALOG;
  }
}
