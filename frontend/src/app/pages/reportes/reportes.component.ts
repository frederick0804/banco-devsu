import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MovimientoService } from '../../services/movimiento.service';
import { ClienteService } from '../../services/cliente.service';
import { ToastService } from '../../services/toast.service';
import { Reporte } from '../../models/reporte.model';
import { Cliente } from '../../models/cliente.model';

@Component({
  selector: 'app-reportes',
  imports: [CommonModule, FormsModule],
  templateUrl: './reportes.component.html',
  styleUrl: './reportes.component.css'
})
export class ReportesComponent implements OnInit {
  private service = inject(MovimientoService);
  private clienteService = inject(ClienteService);
  private toast = inject(ToastService);

  clients: Cliente[] = [];
  report: Reporte[] = [];

  criteria = { clienteId: 0, startDate: '', endDate: '' };

  loading = false;
  downloading = false;
  searched = false;

  ngOnInit(): void {
    this.clienteService.getAll().subscribe({ next: data => this.clients = data });
    const today = new Date();
    const thirtyDaysAgo = new Date(today);
    thirtyDaysAgo.setDate(today.getDate() - 30);
    this.criteria.endDate   = this.toDatetimeLocal(today);
    this.criteria.startDate = this.toDatetimeLocal(thirtyDaysAgo);
  }

  search(): void {
    const validationError = this.validate();
    if (validationError) { this.toast.error(validationError); return; }
    this.loading = true;

    const startIso = new Date(this.criteria.startDate).toISOString();
    const endIso   = new Date(this.criteria.endDate).toISOString();

    this.service.getReporte(this.criteria.clienteId, startIso, endIso).subscribe({
      next: data => {
        this.report = data;
        this.searched = true;
        this.loading = false;
        if (data.length === 0) this.toast.error('No se encontraron movimientos para el período seleccionado');
      },
      error: err => {
        this.toast.error(err.error?.error || 'Error al generar el reporte');
        this.loading = false;
        this.searched = true;
      }
    });
  }

  downloadPdf(): void {
    const validationError = this.validate();
    if (validationError) { this.toast.error(validationError); return; }
    this.downloading = true;

    const startIso = new Date(this.criteria.startDate).toISOString();
    const endIso   = new Date(this.criteria.endDate).toISOString();

    this.service.getReportePdf(this.criteria.clienteId, startIso, endIso).subscribe({
      next: res => {
        const bytes = Uint8Array.from(atob(res.pdf), c => c.charCodeAt(0));
        const blob  = new Blob([bytes], { type: 'application/pdf' });
        const url   = URL.createObjectURL(blob);
        const link  = document.createElement('a');
        link.href     = url;
        link.download = `report_${this.criteria.clienteId}_${Date.now()}.pdf`;
        link.click();
        URL.revokeObjectURL(url);
        this.downloading = false;
        this.toast.success('PDF descargado correctamente');
      },
      error: err => {
        this.toast.error(err.error?.error || 'Error al generar el PDF');
        this.downloading = false;
      }
    });
  }

  get totalMovements(): number {
    return this.report.reduce((sum, r) => sum + r.movimiento, 0);
  }

  get selectedClientName(): string {
    return this.clients.find(c => c.id === this.criteria.clienteId)?.nombre ?? '';
  }

  private validate(): string | null {
    if (!this.criteria.clienteId) return 'Debe seleccionar un cliente';
    if (!this.criteria.startDate) return 'La fecha de inicio es obligatoria';
    if (!this.criteria.endDate)   return 'La fecha de fin es obligatoria';
    if (new Date(this.criteria.startDate) > new Date(this.criteria.endDate))
      return 'La fecha de inicio no puede ser mayor a la fecha de fin';
    return null;
  }

  private toDatetimeLocal(date: Date): string {
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
           `T${pad(date.getHours())}:${pad(date.getMinutes())}`;
  }
}
