import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from './api.service';

type NoticeType = 'success' | 'error';

interface Notice {
  type: NoticeType;
  text: string;
}

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.css',
})
export class LoginPageComponent {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  loginForm = {
    email: '',
    password: '',
  };

  notice: Notice | null = null;
  isSubmitting = false;

  submitLogin(): void {
    if (this.isSubmitting) {
      return;
    }

    this.isSubmitting = true;

    this.api.login(this.loginForm.email, this.loginForm.password).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.notice = {
          type: 'success',
          text: 'Login successful.',
        };
        this.cdr.detectChanges();
        this.router.navigateByUrl('/');
      },
      error: (error: HttpErrorResponse) => {
        this.isSubmitting = false;
        this.notice = {
          type: 'error',
          text:
            error.status === 401
              ? 'Invalid email or password.'
              : error.error?.message || 'Login failed. Please try again.',
        };
        this.cdr.detectChanges();
      },
    });
  }
}
