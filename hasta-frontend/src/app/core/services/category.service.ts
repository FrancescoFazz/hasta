import { Injectable } from '@angular/core';
import { Category, CategoryInfo } from '../models/category.model';

const CATALOG: CategoryInfo[] = [
  {
    id: Category.ELETTRONICA,
    label: 'Elettronica',
    icon: 'cpu',
    accent: '#2f6672',
    description: 'Dispositivi usati e ricondizionati, testati prima di andare all\'asta.',
  },
  {
    id: Category.VESTITI,
    label: 'Vestiti',
    icon: 'shirt',
    accent: '#7a4566',
    description: 'Capi e capispalla di seconda mano, scelti uno a uno.',
  },
  {
    id: Category.ACCESSORI,
    label: 'Accessori',
    icon: 'watch',
    accent: '#8a6a24',
    description: 'Orologi, cinture e piccoli dettagli che fanno la differenza.',
  },
  {
    id: Category.ARREDAMENTI,
    label: 'Arredamenti',
    icon: 'sofa',
    accent: '#9c5b34',
    description: 'Mobili e complementi d\'arredo con una storia da continuare altrove.',
  },
  {
    id: Category.CANCELLERIA,
    label: 'Cancelleria',
    icon: 'pen',
    accent: '#3d5a73',
    description: 'Penne, quaderni e strumenti di scrittura per chi scrive ancora a mano.',
  },
  {
    id: Category.COLLEZIONISMO,
    label: 'Collezionismo',
    icon: 'gem',
    accent: '#7c2f38',
    description: 'Pezzi rari, francobolli e monete per collezionisti pazienti.',
  },
  {
    id: Category.GIOCHI_E_GIOCATTOLI,
    label: 'Giochi e giocattoli',
    icon: 'gamepad',
    accent: '#3f7a52',
    description: 'Giochi da tavolo, action figure e giocattoli da (ri)scoprire.',
  },
  {
    id: Category.SPORT_E_TEMPO_LIBERO,
    label: 'Sport e tempo libero',
    icon: 'ball',
    accent: '#2f6b4f',
    description: 'Attrezzatura sportiva per chi non si ferma mai.',
  },
  {
    id: Category.AUTO_E_MOTO,
    label: 'Auto e moto',
    icon: 'car',
    accent: '#a34a2e',
    description: 'Ricambi e accessori per due e quattro ruote.',
  },
  {
    id: Category.ELETTRODOMESTICI,
    label: 'Elettrodomestici',
    icon: 'fridge',
    accent: '#435a78',
    description: 'Elettrodomestici funzionanti, spesso ancora in garanzia.',
  },
  {
    id: Category.LIBRI_E_MEDIA,
    label: 'Libri e media',
    icon: 'book',
    accent: '#5f4478',
    description: 'Libri, vinili e media da leggere, ascoltare, riascoltare.',
  },
  {
    id: Category.SALUTE_E_BENESSERE,
    label: 'Salute e benessere',
    icon: 'heart',
    accent: '#4a7a5e',
    description: 'Attrezzatura e accessori per prendersi cura di sé.',
  },
  {
    id: Category.ALTRO,
    label: 'Altro',
    icon: 'box',
    accent: '#6f6658',
    description: 'Tutto quello che non entra in nessun\'altra categoria, e per questo prezioso.',
  },
];

const BY_ID = new Map<Category, CategoryInfo>(CATALOG.map((c) => [c.id, c]));

@Injectable({ providedIn: 'root' })
export class CategoryService {
  getAll(): CategoryInfo[] {
    return CATALOG;
  }

  getInfo(id: Category): CategoryInfo | undefined {
    return BY_ID.get(id);
  }

  getLabel(category: Category | string | null | undefined): string {
    if (!category) {
      return '';
    }
    return BY_ID.get(category as Category)?.label ?? category.replace(/_/g, ' ');
  }
}
