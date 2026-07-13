import { Component, OnInit, inject } from '@angular/core';
import { UserService } from '../../core/services/user.service';

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

  ngOnInit(): void {
    if (!this.user()) {
      this.userService.loadCurrentUser().subscribe();
    }
  }
}
