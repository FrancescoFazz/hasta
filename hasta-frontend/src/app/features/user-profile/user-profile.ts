import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { UserService } from '../../core/services/user.service';
import { formatCurrency } from '../../core/utils/format.util';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.scss',
})
export class UserProfile implements OnInit {
  private userService = inject(UserService);
  readonly user = this.userService.currentUser;
  readonly loading = signal(true);

  readonly topUpAmounts = [10, 50, 100];
  readonly chargingAmount = signal<number | null>(null);
  readonly chargeError = signal<string | null>(null);

  ngOnInit(): void {
    this.userService.loadCurrentUser().subscribe({
      next: (user) => {
        this.user.set(user);
        this.loading.set(false);
      },
      error: (err) => {
        console.error(err);
        this.loading.set(false);
      },
    });
  }

  formattedBalance(balance: number): string {
    return formatCurrency(balance);
  }

  addCredit(amount: number): void {
    const user = this.user();
    if (!user) {
      return;
    }
    this.chargingAmount.set(amount);
    this.chargeError.set(null);

    this.userService.addCredit(user.id, amount).subscribe({
      next: (updated) => {
        this.user.set(updated);
        this.chargingAmount.set(null);
      },
      error: (err) => {
        this.chargingAmount.set(null);
        this.chargeError.set('Ricarica non riuscita, riprova.');
        console.error(err);
      },
    });
  }
}
