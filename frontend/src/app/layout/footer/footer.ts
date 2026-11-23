import { RouterLink } from '@angular/router';
import { Component } from '@angular/core'
import { TranslateModule } from '@ngx-translate/core'

@Component({
  selector: 'app-footer',
  imports: [RouterLink, TranslateModule],
  templateUrl: './footer.html',
  styleUrl: './footer.css'
})
export class Footer {
  year = new Date().getFullYear()
}