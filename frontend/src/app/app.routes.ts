import { Routes } from '@angular/router';
import { HomePageComponent } from './home-page.component';
import { LoginPageComponent } from './login-page.component';
import { SignupPageComponent } from './signup-page.component';
import { SignupSuccessPageComponent } from './signup-success-page.component';

export const routes: Routes = [
  { path: '', component: HomePageComponent },
  { path: 'login', component: LoginPageComponent },
  { path: 'signup', component: SignupPageComponent },
  { path: 'signup-success', component: SignupSuccessPageComponent },
  { path: '**', redirectTo: '' },
];
