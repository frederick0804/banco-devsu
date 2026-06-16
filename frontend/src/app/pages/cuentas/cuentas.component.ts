import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CuentaService } from '../../services/cuenta.service';
import { ClienteService } from '../../services/cliente.service';
import { ToastService } from '../../services/toast.service';
import { Cuenta } from '../../models/cuenta.model';
import { Cliente } from '../../models/cliente.model';

@Component({
  selector: 'app-cuentas',
  imports: [CommonModule, FormsModule],
  templateUrl: './cuentas.component.html',
  styleUrl: './cuentas.component.css'
})
export class CuentasComponent implements OnInit {
  private service = inject(CuentaService);
  private clienteService = inject(ClienteService);
  private toast = inject(ToastService);

  accounts: Cuenta[] = [];
  filtered: Cuenta[] = [];
  clients: Cliente[] = [];
  searchTerm = '';

  showModal = false;
  showConfirm = false;
  editMode = false;

  form: Cuenta = this.empty();
  fieldErrors: Record<string, string> = {};
  deleteId?: number;
  loading = false;

  ngOnInit(): void {
    this.load();
    this.clienteService.getAll().subscribe({ next: data => this.clients = data });
  }

  load(): void {
    this.service.getAll().subscribe({
      next: data => { this.accounts = data; this.filter(); },
      error: () => this.toast.error('Error al cargar las cuentas')
    });
  }

  filter(): void {
    const term = this.searchTerm.toLowerCase();
    this.filtered = term
      ? this.accounts.filter(a =>
          a.numeroCuenta.toLowerCase().includes(term) ||
          a.tipoCuenta.toLowerCase().includes(term))
      : [...this.accounts];
  }

  clientName(id: number): string {
    return this.clients.find(c => c.id === id)?.nombre ?? `Cliente #${id}`;
  }

  openCreate(): void {
    this.editMode = false;
    this.form = this.empty();
    this.form.numeroCuenta = this.generateAccountNumber();
    this.fieldErrors = {};
    this.showModal = true;
  }

  openEdit(account: Cuenta): void {
    this.editMode = true;
    this.form = { ...account };
    this.fieldErrors = {};
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  validateFieldLive(field: string): void {
    const f = this.form;
    const errors: Record<string, string> = { ...this.fieldErrors };

    switch (field) {
      case 'saldoInicial':
        if (f.saldoInicial == null || String(f.saldoInicial) === '')
          errors['saldoInicial'] = 'El saldo inicial es obligatorio';
        else if (isNaN(Number(f.saldoInicial)) || Number(f.saldoInicial) < 0)
          errors['saldoInicial'] = 'El saldo debe ser un número mayor o igual a 0';
        else
          delete errors['saldoInicial'];
        break;
      case 'clienteId':
        if (!f.clienteId)
          errors['clienteId'] = 'Debe seleccionar un cliente';
        else
          delete errors['clienteId'];
        break;
    }
    this.fieldErrors = errors;
  }

  blockNonNumeric(event: KeyboardEvent): void {
    if (!/[\d.]/.test(event.key) && !['Backspace','Delete','Tab','ArrowLeft','ArrowRight'].includes(event.key)) {
      event.preventDefault();
    }
  }

  save(): void {
    if (!this.validateAll()) return;
    this.loading = true;

    const request = this.editMode
      ? this.service.update(this.form.id!, this.form)
      : this.service.create(this.form);

    request.subscribe({
      next: () => {
        this.closeModal();
        this.load();
        this.toast.success(this.editMode ? 'Cuenta actualizada correctamente' : 'Cuenta creada correctamente');
        this.loading = false;
      },
      error: err => {
        this.toast.error(err.error?.error || 'Error al guardar la cuenta');
        this.loading = false;
      }
    });
  }

  confirmDelete(id: number): void {
    this.deleteId = id;
    this.showConfirm = true;
  }

  delete(): void {
    this.service.delete(this.deleteId!).subscribe({
      next: () => {
        this.showConfirm = false;
        this.load();
        this.toast.success('Cuenta eliminada correctamente');
      },
      error: err => {
        this.showConfirm = false;
        this.toast.error(err.error?.error || 'Error al eliminar la cuenta');
      }
    });
  }

  private generateAccountNumber(): string {
    return String(Math.floor(100000 + Math.random() * 900000));
  }

  private validateAll(): boolean {
    ['saldoInicial', 'clienteId'].forEach(f => this.validateFieldLive(f));
    return Object.keys(this.fieldErrors).length === 0;
  }

  private empty(): Cuenta {
    return { numeroCuenta: '', tipoCuenta: 'Ahorro', saldoInicial: 0, estado: true, clienteId: 0 };
  }

}
