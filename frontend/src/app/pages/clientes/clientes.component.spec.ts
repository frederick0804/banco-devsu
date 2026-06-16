import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ClientesComponent } from './clientes.component';
import { ClienteService } from '../../services/cliente.service';
import { Cliente } from '../../models/cliente.model';

const mockClientes: Cliente[] = [
  { id: 1, nombre: 'Jose Lema', genero: 'Masculino', edad: 30, identificacion: '1234567890', clienteId: 'jose123', contrasena: '1234', estado: true },
  { id: 2, nombre: 'Mariana Montalvo', genero: 'Femenino', edad: 25, identificacion: '0987654321', clienteId: 'mariana456', contrasena: '5678', estado: true }
];

const clienteServiceMock = {
  getAll: jest.fn(() => of(mockClientes)),
  create: jest.fn(() => of(mockClientes[0])),
  update: jest.fn(() => of(mockClientes[0])),
  delete: jest.fn(() => of(void 0))
};

async function setup(getAllReturn = of(mockClientes)) {
  clienteServiceMock.getAll.mockReturnValue(getAllReturn);

  await TestBed.configureTestingModule({
    imports: [ClientesComponent],
    providers: [{ provide: ClienteService, useValue: clienteServiceMock }]
  }).compileComponents();

  const fixture = TestBed.createComponent(ClientesComponent);
  fixture.detectChanges();
  return { fixture, component: fixture.componentInstance };
}

describe('ClientesComponent', () => {
  afterEach(() => {
    TestBed.resetTestingModule();
    jest.clearAllMocks();
  });

  it('deberia crearse el componente', async () => {
    const { component } = await setup();
    expect(component).toBeTruthy();
  });

  it('deberia cargar la lista de clientes al iniciar', async () => {
    const { component } = await setup();
    expect(clienteServiceMock.getAll).toHaveBeenCalled();
    expect(component.clients.length).toBe(2);
    expect(component.clients[0].nombre).toBe('Jose Lema');
  });

  it('deberia filtrar clientes por nombre', async () => {
    const { component } = await setup();
    component.searchTerm = 'jose';
    component.filter();
    expect(component.filtered.length).toBe(1);
    expect(component.filtered[0].nombre).toBe('Jose Lema');
  });

  it('deberia mostrar todos los clientes con filtro vacio', async () => {
    const { component } = await setup();
    component.searchTerm = '';
    component.filter();
    expect(component.filtered.length).toBe(2);
  });

  it('deberia abrir modal en modo creacion', async () => {
    const { component } = await setup();
    component.openCreate();
    expect(component.showModal).toBe(true);
    expect(component.editMode).toBe(false);
    expect(component.form.nombre).toBe('');
  });

  it('deberia abrir modal en modo edicion con datos del cliente', async () => {
    const { component } = await setup();
    component.openEdit(mockClientes[0]);
    expect(component.showModal).toBe(true);
    expect(component.editMode).toBe(true);
    expect(component.form.nombre).toBe('Jose Lema');
  });

  it('deberia cerrar el modal', async () => {
    const { component } = await setup();
    component.openCreate();
    component.closeModal();
    expect(component.showModal).toBe(false);
  });

  it('deberia autogenerar clienteId desde el nombre', async () => {
    const { component } = await setup();
    component.openCreate();
    component.form.nombre = 'Jose Lema';
    component.onNombreChange();
    expect(component.form.clienteId).toMatch(/^jose\d{3}$/);
  });

  it('deberia agregar error si nombre contiene numeros', async () => {
    const { component } = await setup();
    component.form.nombre = 'Jose123';
    component.validateFieldLive('nombre');
    expect(component.fieldErrors['nombre']).toBeDefined();
  });

  it('deberia agregar error si identificacion contiene letras', async () => {
    const { component } = await setup();
    component.form.identificacion = 'abc123';
    component.validateFieldLive('identificacion');
    expect(component.fieldErrors['identificacion']).toBeDefined();
  });

  it('deberia manejar error al cargar clientes', async () => {
    const { component } = await setup(throwError(() => new Error('Error de red')));
    expect(component.clients.length).toBe(0);
  });
});
