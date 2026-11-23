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
  private readonly chatUrl = `${environment.apiUrl}/v1/chat`;

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

    this.askAssistant();
  }

  /** Envía la conversación al backend, que llama al modelo de IA con el catálogo real. */
  private askAssistant() {
    // El primer mensaje es el saludo local: no se envía.
    const history = this.messages.slice(1).slice(-10).map(m => ({
      role: m.isUser ? 'user' : 'assistant',
      content: m.content,
    }));

    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();

    this.http.post<{ reply: string }>(this.chatUrl, { messages: history }, { headers })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ({ reply }) => this.pushAssistant(reply),
        error: (error) => this.pushAssistant(
          error?.error?.message ?? 'El asistente no está disponible ahora mismo. Inténtalo en unos minutos.'
        ),
      });
  }

  private pushAssistant(content: string) {
    this.messages.push({ content, isUser: false, timestamp: new Date() });
    this.isLoading = false;
    setTimeout(() => this.scrollToBottom(), 100);
  }

  /**
   * Formato mínimo para las respuestas del asistente: se escapa todo el HTML
   * y solo se convierten **negritas** y viñetas "- " / "* ".
   */
  formatAssistant(text: string): string {
    const escaped = text
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    return escaped
      .replace(/^\s*[-*•]\s+/gm, '• ')
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
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
