import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cuenta } from '../models/cuenta.model';

@Injectable({ providedIn: 'root' })
export class CuentaService {
  private http = inject(HttpClient);
  private url = '/api/cuentas';

  getAll(): Observable<Cuenta[]> {
    return this.http.get<Cuenta[]>(this.url);
  }

  create(dto: Cuenta): Observable<Cuenta> {
    return this.http.post<Cuenta>(this.url, dto);
  }

  update(id: number, dto: Cuenta): Observable<Cuenta> {
    return this.http.put<Cuenta>(`${this.url}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
