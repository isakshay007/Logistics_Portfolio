import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from './api.service';

type NoticeType = 'success' | 'error';

interface Notice {
  type: NoticeType;
  text: string;
}

@Component({
  selector: 'app-signup-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './signup-page.component.html',
  styleUrl: './signup-page.component.css',
})
export class SignupPageComponent {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  signupForm = {
    name: '',
    email: '',
    company: '',
    phone: '',
    interest: '',
    password: '',
  };

  notice: Notice | null = null;
  isSubmitting = false;

  submitSignup(): void {
    if (this.isSubmitting) {
      return;
    }

    this.isSubmitting = true;

    this.api.submitSignup(this.signupForm).subscribe({
      next: () => {
        this.notice = {
          type: 'success',
          text: 'Portal account created. Redirecting to success page...',
        };
        setTimeout(() => {
          this.router.navigateByUrl('/signup-success');
        }, 700);
      },
      error: (error: HttpErrorResponse) => {
        this.isSubmitting = false;
        this.notice = {
          type: 'error',
          text: error.error?.message || 'Signup failed. Please try again.',
        };
      },
    });
  }
}
