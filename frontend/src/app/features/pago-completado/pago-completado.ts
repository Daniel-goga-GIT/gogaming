import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-pago-completado',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './pago-completado.html',
  styleUrls: ['./pago-completado.css']
})
export class PagoCompletado implements OnInit {
  constructor(private cartService: CartService) {}

  ngOnInit(): void {
    this.cartService.clearCart();
  }
}
