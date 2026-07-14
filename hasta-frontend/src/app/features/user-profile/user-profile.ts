import { Component, OnInit, inject } from '@angular/core';
import { UserService } from '../../core/services/user.service';
import { formatCurrency } from '../../core/utils/format.util';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [],
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.scss',
})
export class UserProfile implements OnInit {
  private userService = inject(UserService);
  readonly user = this.userService.currentUser;
  readonly loading = signal(true);
  ngOnInit(): void {
    this.userService.loadCurrentUser().subscribe({
      next: (user) => {
        this.user.set(user);
        this.loading.set(false);
        },
      error: (err) => {
        console.error(err);
        this.loading.set(false);
        }
      });
  }

  formattedBalance(balance: number): string {
    return formatCurrency(balance);
  }
}
