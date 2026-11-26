export class UserDTO {
  firstName: string = '';
  id: number = 0;
  image: string = '';
  lastName: string = '';
  password: string = '';
  username: string = '';
}

export class UserCreateDTO {
  email: string = '';
  firstName: string = '';
  lastName: string = '';
  image: File | null = null;
  password: string = '';
  
  constructor(
    firstName: string = '',
    lastName: string = '',
    email: string = '',
    password: string = '',
  ) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
  }
}

export interface ProductoDTO {
  id: number;
  nombre: string;
  marca: string;
  fechaPublicacion: string;
  precio: number;
  stock: number;
  tipoProducto: string;
  image: string;
}

export interface RatonDTO extends ProductoDTO {
  tipoAgarre: string;
  medidas: string;
  sensor: string;
  peso: number;
  switches: string;
}

export interface TecladoDTO extends ProductoDTO {
  tipoSwitch: string;
  layout: string;
  conectividad: string;
  retroiluminacion: string;
}

export interface SkateDTO extends ProductoDTO {
  material: string;
  grosor: number;
  compatibilidad: string;
}

export interface AlfombrillaDTO extends ProductoDTO {
  dimensiones: string;
  grosor: number;
  superficie: string;
}

export interface AuricularesDTO extends ProductoDTO {
  tipo: string;
  driver: string;
  conectividad: string;
  impedancia: string;
}

export interface MangaDTO extends ProductoDTO {
  talla: string;
  material: string;
  largo: number;
  color: string;
}

/** Página de resultados de Spring Data. */
export interface Pagina<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface CartItem {
  product: ProductoDTO;
  quantity: number;
}

export interface CheckoutItemRequest {
  productId: number;
  quantity: number;
}

export interface CreateCheckoutRequest {
  items: CheckoutItemRequest[];
}

export interface CheckoutSessionResponse {
  sessionId: string;
  /** URL de la página de pago de Stripe a la que redirigir. */
  url: string;
}

export interface RatingDTO {
  id: number;
  productId: number;
  userId: number;
  userName: string;
  rating: number;
  comment: string;
  createdDate: string;
  updatedDate: string;
}

export interface ProductRatingSummary {
  productId: number;
  averageRating: number;
  totalReviews: number;
  oneStar: number;
  twoStars: number;
  threeStars: number;
  fourStars: number;
  fiveStars: number;
}

export interface ProductReviewsResponse {
  summary: ProductRatingSummary;
  reviews: RatingDTO[];
}

export interface CreateRatingRequest {
  calificacion: number;
  contenido: string;
}
