import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header implements OnInit {
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private router = inject(Router);

  readonly user = this.userService.currentUser;
  readonly menuOpen = signal(false);

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      this.userService.loadCurrentUser().subscribe({
        error: (err) => console.error('Impossibile caricare il profilo utente', err),
      });
    }
  }

  toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  logout(): void {
    this.closeMenu();
    this.userService.clearCurrentUser();
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
