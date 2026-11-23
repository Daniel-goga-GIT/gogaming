import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-error404',
  standalone: true,
  imports: [RouterLink, TranslateModule],
  templateUrl: './error404.html',
  styleUrl: './error404.css'
})
export class Error404 {
  constructor() {}
}
