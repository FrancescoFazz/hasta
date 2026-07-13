import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { UserService } from '../../core/services/user.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header implements OnInit {
  private userService = inject(UserService);
  readonly user = this.userService.currentUser;

  ngOnInit(): void {
    this.userService.loadCurrentUser().subscribe({
      error: (err) => console.error('Impossibile caricare il profilo utente', err),
    });
  }
}
