import { Component, OnInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Subject, takeUntil } from 'rxjs';
import { AuthService } from '../../core/services/auth';
import { UserService } from '../../core/services/user';
import { UserDTO } from '../../models/types';
import { environment } from '../../../environments/environments';

interface ChatMessage {
  content: string;
  isUser: boolean;
  timestamp: Date;
}

@Component({
  selector: 'app-ai-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-chat.component.html',
  styleUrls: ['./ai-chat.component.css']
})
export class AiChatComponent implements OnInit, OnDestroy {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;
  
  isOpen = false;
  isLoading = false;
  messages: ChatMessage[] = [];
  currentMessage = '';
  currentUser: UserDTO | null = null;
  
  private destroy$ = new Subject<void>();
  // TODO: mover la llamada a la IA al backend; la clave va en backend/.env (OPENROUTER_API_KEY)
  private apiKey = '';
  private apiUrl = 'https://openrouter.ai/api/v1/chat/completions';

  private models = [
    'openrouter/free',
    'meta-llama/llama-3.2-3b-instruct:free',
    'google/gemini-2.0-flash-lite-preview-02-05:free'
  ];
  private currentModelIndex = 0;

  constructor(
    private http: HttpClient, 
    private authService: AuthService,
    private userService: UserService
  ) {}

  ngOnInit() {
    // Mensaje de bienvenida adaptado a GoGaming
    this.messages.push({
      content: '¡Hola! Soy tu asistente de GoGaming. Puedo ayudarte con dudas sobre nuestros ratones, teclados, alfombrillas y otros accesorios gaming. ¿En qué puedo ayudarte hoy?',
      isUser: false,
      timestamp: new Date()
    });

    // Obtener información del usuario si está logueado
    this.authService.isLoggedIn().pipe(takeUntil(this.destroy$)).subscribe(isLoggedIn => {
      if (isLoggedIn) {
        this.userService.fetchUser().pipe(takeUntil(this.destroy$)).subscribe({
          next: (user: UserDTO) => {
            this.currentUser = user;
          },
          error: (err) => {
            console.error('Error fetching user', err);
          }
        });
      }
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  toggleChat() {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      setTimeout(() => this.scrollToBottom(), 100);
    }
  }

  sendMessage() {
    if (!this.currentMessage.trim() || this.isLoading) return;

    const userMessage: ChatMessage = {
      content: this.currentMessage,
      isUser: true,
      timestamp: new Date()
    };

    this.messages.push(userMessage);
    const messageToSend = this.currentMessage;
    this.currentMessage = '';
    this.isLoading = true;

    setTimeout(() => this.scrollToBottom(), 100);

    this.callOpenRouterAPI(messageToSend);
  }

  private callOpenRouterAPI(message: string, retryCount = 0) {
    const currentModel = this.models[this.currentModelIndex];
    console.log(`Trying model: ${currentModel}`);

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.apiKey}`,
      'Content-Type': 'application/json',
      'HTTP-Referer': 'http://localhost:4200',
      'X-Title': 'GoGaming'
    });

    const systemPrompt = `Eres un asistente virtual especializado en la tienda GoGaming. Tu función es ayudar a los clientes con:

1. Información sobre productos gaming (ratones, teclados, alfombrillas, cascos, etc.)
2. Recomendaciones basadas en el tipo de juego (FPS, MOBA, etc.)
3. Detalles sobre envíos y devoluciones
4. Dudas sobre el proceso de compra
5. Características técnicas de los productos (DPI, switches, sensores)
6. Reportar problemas o errores en la web

Siempre responde de manera amigable, profesional y con un tono gamer pero respetuoso. Si no sabes algo específico sobre un pedido o stock en tiempo real, sugiere contactar al soporte técnico. Mantén las respuestas concisas pero informativas.`;

    const body = {
      model: currentModel,
      messages: [
        {
          role: 'system',
          content: systemPrompt
        },
        {
          role: 'user',
          content: message
        }
      ]
    };
    
    // Eliminamos max_tokens para compatibilidad con openrouter/free y otros modelos
    // (body as any).max_tokens = 500;

    this.http.post(this.apiUrl, body, { headers })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          const aiResponse = response.choices[0]?.message?.content || 'Lo siento, no pude procesar tu consulta. Por favor, intenta de nuevo.';
          
          this.messages.push({
            content: aiResponse,
            isUser: false,
            timestamp: new Date()
          });
          
          this.isLoading = false;
          setTimeout(() => this.scrollToBottom(), 100);
        },
        error: (error) => {
          
          // Intentar con el siguiente modelo si hay error
          if (this.currentModelIndex < this.models.length - 1) {
            this.currentModelIndex++;
            this.callOpenRouterAPI(message, retryCount + 1);
            return;
          }
          
          this.messages.push({
            content: 'Lo siento, todos mis servidores están ocupados en este momento. Por favor, intenta de nuevo más tarde. (En desarrollo)',
            isUser: false,
            timestamp: new Date()
          });
          this.isLoading = false;
          // Reset model index for next user attempt
          this.currentModelIndex = 0;
          setTimeout(() => this.scrollToBottom(), 100);
        }
      });
  }

  private scrollToBottom() {
    if (this.messagesContainer) {
      const element = this.messagesContainer.nativeElement;
      element.scrollTop = element.scrollHeight;
    }
  }

  onKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  getUserPhotoUrl(): string | null {
    if (this.currentUser && this.currentUser.image) {
      // Si la foto ya es una URL completa (aunque en este backend parece que devuelve UUID)
      if (this.currentUser.image.startsWith('http') || this.currentUser.image.startsWith('data:')) {
        return this.currentUser.image;
      }
      // Construir la URL completa usando el endpoint de imagen
      return `${environment.apiUrl}/v1/users/image/${this.currentUser.image}`;
    }
    return null;
  }

  getDefaultAvatar(): string {
    return 'data:image/svg+xml;base64,' + btoa(`
      <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 32 32">
        <circle cx="16" cy="16" r="16" fill="#e5e7eb"/>
        <circle cx="16" cy="12" r="5" fill="#9ca3af"/>
        <path d="M6 26 Q6 20 12 20 h8 Q26 20 26 26" fill="#9ca3af"/>
      </svg>
    `);
  }
}
