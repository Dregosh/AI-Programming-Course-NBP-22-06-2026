import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RequestForm } from './features/request-form/request-form';
import { Chat } from './features/chat/chat';

const routes: Routes = [
  { path: '', component: RequestForm },
  { path: 'chat/:sessionId', component: Chat },
  { path: '**', redirectTo: '' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
