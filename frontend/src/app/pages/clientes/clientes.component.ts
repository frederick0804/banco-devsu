import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../services/cliente.service';
import { ToastService } from '../../services/toast.service';
import { Cliente } from '../../models/cliente.model';

@Component({
  selector: 'app-clientes',
  imports: [CommonModule, FormsModule],
  templateUrl: './clientes.component.html',
  styleUrl: './clientes.component.css'
})
export class ClientesComponent implements OnInit {
  private service = inject(ClienteService);
  private toast = inject(ToastService);

  clients: Cliente[] = [];
  filtered: Cliente[] = [];
  searchTerm = '';

  showModal = false;
  showConfirm = false;
  editMode = false;

  form: Cliente = this.empty();
  fieldErrors: Record<string, string> = {};
  deleteId?: number;
  loading = false;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.service.getAll().subscribe({
      next: data => { this.clients = data; this.filter(); },
      error: () => this.toast.error('Error al cargar los clientes')
    });
  }

  filter(): void {
    const term = this.searchTerm.toLowerCase();
    this.filtered = term
      ? this.clients.filter(c =>
          c.nombre.toLowerCase().includes(term) ||
          c.clienteId.toLowerCase().includes(term) ||
          c.identificacion.toLowerCase().includes(term))
      : [...this.clients];
  }

  openCreate(): void {
    this.editMode = false;
    this.form = this.empty();
    this.fieldErrors = {};
    this.showModal = true;
  }

  openEdit(client: Cliente): void {
    this.editMode = true;
    this.form = { ...client };
    this.fieldErrors = {};
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  onNombreChange(): void {
    if (!this.editMode && this.form.nombre?.trim()) {
      this.form.clienteId = this.generateClienteId(this.form.nombre);
    }
    this.validateFieldLive('nombre');
  }

  validateFieldLive(field: string): void {
    const f = this.form;
    const errors: Record<string, string> = { ...this.fieldErrors };

    switch (field) {
      case 'nombre':
        if (!f.nombre?.trim())
          errors['nombre'] = 'El nombre es obligatorio';
        else if (!/^[a-záéíóúñüA-ZÁÉÍÓÚÑÜ\s]+$/.test(f.nombre.trim()))
          errors['nombre'] = 'El nombre solo puede contener letras y espacios';
        else
          delete errors['nombre'];
        break;
      case 'edad':
        if (f.edad == null || String(f.edad) === '')
          errors['edad'] = 'La edad es obligatoria';
        else if (!Number.isInteger(Number(f.edad)) || Number(f.edad) < 0)
          errors['edad'] = 'Ingrese una edad válida (número entero positivo)';
        else
          delete errors['edad'];
        break;
      case 'identificacion':
        if (!f.identificacion?.trim())
          errors['identificacion'] = 'La identificación es obligatoria';
        else if (!/^\d+$/.test(f.identificacion.trim()))
          errors['identificacion'] = 'La identificación solo puede contener dígitos';
        else
          delete errors['identificacion'];
        break;
      case 'telefono':
        if (f.telefono && !/^\d+$/.test(f.telefono.trim()))
          errors['telefono'] = 'El teléfono solo puede contener dígitos';
        else
          delete errors['telefono'];
        break;
      case 'clienteId':
        if (!f.clienteId?.trim())
          errors['clienteId'] = 'El Cliente ID es obligatorio';
        else if (!/^[a-z0-9]+$/.test(f.clienteId.trim()))
          errors['clienteId'] = 'Solo letras minúsculas y números';
        else
          delete errors['clienteId'];
        break;
      case 'contrasena':
        if (!f.contrasena?.trim())
          errors['contrasena'] = 'La contraseña es obligatoria';
        else if (f.contrasena.length < 4)
          errors['contrasena'] = 'La contraseña debe tener al menos 4 caracteres';
        else
          delete errors['contrasena'];
        break;
    }
    this.fieldErrors = errors;
  }

  blockNonDigits(event: KeyboardEvent): void {
    if (!/^\d$/.test(event.key) && !['Backspace','Delete','Tab','ArrowLeft','ArrowRight'].includes(event.key)) {
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
        this.toast.success(this.editMode ? 'Cliente actualizado correctamente' : 'Cliente creado correctamente');
        this.loading = false;
      },
      error: err => {
        this.toast.error(err.error?.error || 'Error al guardar el cliente');
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
        this.toast.success('Cliente eliminado correctamente');
      },
      error: err => {
        this.showConfirm = false;
        this.toast.error(err.error?.error || 'Error al eliminar el cliente');
      }
    });
  }

  private generateClienteId(nombre: string): string {
    const base = nombre.trim().toLowerCase()
      .normalize('NFD').replace(/[̀-ͯ]/g, '')
      .split(' ')[0].replace(/[^a-z0-9]/g, '');
    const num = Math.floor(100 + Math.random() * 900);
    return base + num;
  }

  private validateAll(): boolean {
    ['nombre', 'edad', 'identificacion', 'telefono', 'clienteId', 'contrasena']
      .forEach(f => this.validateFieldLive(f));
    return Object.keys(this.fieldErrors).length === 0;
  }

  private empty(): Cliente {
    return { nombre: '', genero: 'Masculino', edad: 0, identificacion: '', direccion: '', telefono: '', clienteId: '', contrasena: '', estado: true };
  }
}
