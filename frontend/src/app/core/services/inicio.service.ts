import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "../../../environments/environments";
import { Observable } from "rxjs";
import { RatonDTO } from "../../models/types";

@Injectable({
  providedIn: 'root'
})
export class RatonService {

  constructor(private http: HttpClient) { }

  getHomeRatones(): Observable<RatonDTO[]> {
    return this.http.get<RatonDTO[]>(`${environment.apiUrl}/v1/productos/home`);
  }
}