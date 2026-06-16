import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MovimientoService } from '../../services/movimiento.service';
import { CuentaService } from '../../services/cuenta.service';
import { ClienteService } from '../../services/cliente.service';
import { ToastService } from '../../services/toast.service';
import { Movimiento } from '../../models/movimiento.model';
import { Cuenta } from '../../models/cuenta.model';
import { Cliente } from '../../models/cliente.model';

@Component({
  selector: 'app-movimientos',
  imports: [CommonModule, FormsModule],
  templateUrl: './movimientos.component.html',
  styleUrl: './movimientos.component.css'
})
export class MovimientosComponent implements OnInit {
  private service = inject(MovimientoService);
  private accountService = inject(CuentaService);
  private clienteService = inject(ClienteService);
  private toast = inject(ToastService);

  movements: Movimiento[] = [];
  filtered: Movimiento[] = [];
  accounts: Cuenta[] = [];
  clients: Cliente[] = [];
  searchTerm = '';

  showModal = false;
  showConfirm = false;
  editMode = false;

  form: Movimiento = this.empty();
  selectedType = 'Crédito';
  absoluteAmount = 0;
  fieldErrors: Record<string, string> = {};
  deleteId?: number;
  loading = false;

  ngOnInit(): void {
    this.load();
    this.accountService.getAll().subscribe({ next: data => this.accounts = data });
    this.clienteService.getAll().subscribe({ next: data => this.clients = data });
  }

  clientNameForAccount(account: Cuenta): string {
    return this.clients.find(c => c.id === account.clienteId)?.nombre ?? '';
  }

  load(): void {
    this.service.getAll().subscribe({
      next: data => { this.movements = data; this.filter(); },
      error: () => this.toast.error('Error al cargar los movimientos')
    });
  }

  filter(): void {
    const term = this.searchTerm.toLowerCase();
    this.filtered = term
      ? this.movements.filter(m =>
          m.tipoMovimiento.toLowerCase().includes(term) ||
          String(m.cuentaId).includes(term))
      : [...this.movements];
  }

  accountNumber(id: number): string {
    return this.accounts.find(a => a.id === id)?.numeroCuenta ?? `Cuenta #${id}`;
  }

  openCreate(): void {
    this.editMode = false;
    this.form = this.empty();
    this.selectedType = 'Crédito';
    this.absoluteAmount = 0;
    this.fieldErrors = {};
    this.showModal = true;
  }

  openEdit(movement: Movimiento): void {
    this.editMode = true;
    this.form = { ...movement };
    this.selectedType = movement.valor >= 0 ? 'Crédito' : 'Débito';
    this.absoluteAmount = Math.abs(movement.valor);
    this.fieldErrors = {};
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  validateFieldLive(field: string): void {
    const errors: Record<string, string> = { ...this.fieldErrors };

    switch (field) {
      case 'cuentaId':
        if (!this.form.cuentaId)
          errors['cuentaId'] = 'Debe seleccionar una cuenta';
        else
          delete errors['cuentaId'];
        break;
      case 'monto':
        if (!this.absoluteAmount || this.absoluteAmount <= 0)
          errors['monto'] = 'El monto debe ser mayor a 0';
        else if (this.absoluteAmount < 0.01)
          errors['monto'] = 'El monto mínimo es $0.01';
        else
          delete errors['monto'];
        break;
    }
    this.fieldErrors = errors;
  }

  blockNegativeAndLetters(event: KeyboardEvent): void {
    if (['-', 'e', 'E', '+'].includes(event.key)) {
      event.preventDefault();
    }
  }

  onMontoInput(): void {
    if (this.absoluteAmount < 0) this.absoluteAmount = 0;
    this.validateFieldLive('monto');
  }

  save(): void {
    if (!this.validateAll()) return;
    this.loading = true;

    const valor = this.selectedType === 'Débito'
      ? -Math.abs(this.absoluteAmount)
      : Math.abs(this.absoluteAmount);

    const payload: Movimiento = { ...this.form, valor, tipoMovimiento: this.selectedType };

    const request = this.editMode
      ? this.service.update(this.form.id!, payload)
      : this.service.create(payload);

    request.subscribe({
      next: () => {
        this.closeModal();
        this.load();
        this.toast.success(this.editMode ? 'Movimiento actualizado correctamente' : 'Movimiento registrado correctamente');
        this.loading = false;
      },
      error: err => {
        this.toast.error(err.error?.error || 'Error al guardar el movimiento');
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
        this.toast.success('Movimiento eliminado correctamente');
      },
      error: err => {
        this.showConfirm = false;
        this.toast.error(err.error?.error || 'Error al eliminar el movimiento');
      }
    });
  }

  private validateAll(): boolean {
    ['cuentaId', 'monto'].forEach(f => this.validateFieldLive(f));
    return Object.keys(this.fieldErrors).length === 0;
  }

  private empty(): Movimiento {
    return { tipoMovimiento: 'Crédito', valor: 0, cuentaId: 0 };
  }

}
