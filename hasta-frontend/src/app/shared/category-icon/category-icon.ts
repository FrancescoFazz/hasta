import { Component, input } from '@angular/core';

@Component({
  selector: 'app-category-icon',
  standalone: true,
  templateUrl: './category-icon.html',
  styleUrl: './category-icon.scss',
})
export class CategoryIcon {
  icon = input.required<string>();
}
