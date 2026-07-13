import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CategoryService } from '../../core/services/category.service';
import { CategoryIcon } from '../../shared/category-icon/category-icon';

@Component({
  selector: 'app-category-bar',
  standalone: true,
  imports: [RouterLink, CategoryIcon],
  templateUrl: './category-bar.html',
  styleUrl: './category-bar.scss',
})
export class CategoryBar {
  private categoryService = inject(CategoryService);
  readonly categories = this.categoryService.getAll();
}
