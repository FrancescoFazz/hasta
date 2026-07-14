import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';
import { CreateUserRequest, Gender } from '../../core/models/user.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private router = inject(Router);

  form: CreateUserRequest = {
    username: '',
    password: '',
    email: '',
    name: '',
    surname: '',
    gender: 'MALE',
  };

  confirmPassword = '';

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  onSubmit(): void {
    this.errorMessage.set(null);

    if (this.form.password !== this.confirmPassword) {
      this.errorMessage.set('Le password non coincidono');
      return;
    }

    this.loading.set(true);

    this.userService.register(this.form).subscribe({
      next: () => this.autoLogin(),
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(this.extractErrorMessage(err));
      },
    });
  }

  private autoLogin(): void {
    this.authService.login(this.form.username, this.form.password).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigateByUrl('/utente');
      },
      error: () => {                              /* La registrazione è comunque andata a buon fine: mandiamo l'utente
                                                       al login manuale invece di bloccarlo con un errore  */
        this.loading.set(false);
        this.router.navigateByUrl('/login');
      },
    });
  }

  private extractErrorMessage(err: HttpErrorResponse): string {
    const backendMessage = (err.error as { message?: string })?.message;
    if (err.status === 409) {
      return 'Username o email già in uso';
    }
    return backendMessage ?? 'Errore durante la registrazione, riprova';
  }
}
