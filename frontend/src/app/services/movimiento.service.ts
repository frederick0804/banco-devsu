import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Movimiento } from '../models/movimiento.model';
import { Reporte } from '../models/reporte.model';

@Injectable({ providedIn: 'root' })
export class MovimientoService {
  private http = inject(HttpClient);
  private urlMovimientos = '/api/movimientos';
  private urlReportes = '/api/reportes';

  getAll(): Observable<Movimiento[]> {
    return this.http.get<Movimiento[]>(this.urlMovimientos);
  }

  create(dto: Movimiento): Observable<Movimiento> {
    return this.http.post<Movimiento>(this.urlMovimientos, dto);
  }

  update(id: number, dto: Movimiento): Observable<Movimiento> {
    return this.http.put<Movimiento>(`${this.urlMovimientos}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.urlMovimientos}/${id}`);
  }

  getReporte(clienteId: number, fechaInicio: string, fechaFin: string): Observable<Reporte[]> {
    return this.http.get<Reporte[]>(this.urlReportes, {
      params: { clienteId, fechaInicio, fechaFin }
    });
  }

  getReportePdf(clienteId: number, fechaInicio: string, fechaFin: string): Observable<{ pdf: string }> {
    return this.http.get<{ pdf: string }>(`${this.urlReportes}/pdf`, {
      params: { clienteId, fechaInicio, fechaFin }
    });
  }
}
