import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { MovimientosComponent } from './movimientos.component';
import { MovimientoService } from '../../services/movimiento.service';
import { CuentaService } from '../../services/cuenta.service';
import { ClienteService } from '../../services/cliente.service';
import { Movimiento } from '../../models/movimiento.model';
import { Cuenta } from '../../models/cuenta.model';

const mockMovimientos: Movimiento[] = [
  { id: 1, tipoMovimiento: 'Crédito', valor: 600, saldo: 700, cuentaId: 1, fecha: '2022-10-02' },
  { id: 2, tipoMovimiento: 'Débito', valor: -540, saldo: 0, cuentaId: 2, fecha: '2022-08-02' }
];

const mockCuentas: Cuenta[] = [
  { id: 1, numeroCuenta: '225487', tipoCuenta: 'Corriente', saldoInicial: 100, estado: true, clienteId: 1 },
  { id: 2, numeroCuenta: '496825', tipoCuenta: 'Ahorros', saldoInicial: 540, estado: true, clienteId: 2 }
];

const movimientoServiceMock = {
  getAll: jest.fn(() => of(mockMovimientos)),
  create: jest.fn(() => of(mockMovimientos[0])),
  update: jest.fn(() => of(mockMovimientos[0])),
  delete: jest.fn(() => of(void 0))
};

const cuentaServiceMock = { getAll: jest.fn(() => of(mockCuentas)) };
const clienteServiceMock = { getAll: jest.fn(() => of([])) };

async function setup(movimientosReturn = of(mockMovimientos)) {
  movimientoServiceMock.getAll.mockReturnValue(movimientosReturn);

  await TestBed.configureTestingModule({
    imports: [MovimientosComponent],
    providers: [
      { provide: MovimientoService, useValue: movimientoServiceMock },
      { provide: CuentaService, useValue: cuentaServiceMock },
      { provide: ClienteService, useValue: clienteServiceMock }
    ]
  }).compileComponents();

  const fixture = TestBed.createComponent(MovimientosComponent);
  fixture.detectChanges();
  return { fixture, component: fixture.componentInstance };
}

describe('MovimientosComponent', () => {
  afterEach(() => {
    TestBed.resetTestingModule();
    jest.clearAllMocks();
    cuentaServiceMock.getAll.mockReturnValue(of(mockCuentas));
    clienteServiceMock.getAll.mockReturnValue(of([]));
  });

  it('deberia crearse el componente', async () => {
    const { component } = await setup();
    expect(component).toBeTruthy();
  });

  it('deberia cargar movimientos y cuentas al iniciar', async () => {
    const { component } = await setup();
    expect(movimientoServiceMock.getAll).toHaveBeenCalled();
    expect(cuentaServiceMock.getAll).toHaveBeenCalled();
    expect(component.movements.length).toBe(2);
    expect(component.accounts.length).toBe(2);
  });

  it('deberia filtrar movimientos por tipo', async () => {
    const { component } = await setup();
    component.searchTerm = 'Crédito';
    component.filter();
    expect(component.filtered.length).toBe(1);
    expect(component.filtered[0].tipoMovimiento).toBe('Crédito');
  });

  it('deberia abrir modal en modo creacion con monto en cero', async () => {
    const { component } = await setup();
    component.openCreate();
    expect(component.showModal).toBe(true);
    expect(component.editMode).toBe(false);
    expect(component.absoluteAmount).toBe(0);
  });

  it('deberia abrir modal en modo edicion con datos del movimiento', async () => {
    const { component } = await setup();
    component.openEdit(mockMovimientos[0]);
    expect(component.showModal).toBe(true);
    expect(component.editMode).toBe(true);
    expect(component.absoluteAmount).toBe(600);
    expect(component.selectedType).toBe('Crédito');
  });

  it('deberia detectar tipo Debito para valores negativos', async () => {
    const { component } = await setup();
    component.openEdit(mockMovimientos[1]);
    expect(component.selectedType).toBe('Débito');
    expect(component.absoluteAmount).toBe(540);
  });

  it('deberia cerrar el modal', async () => {
    const { component } = await setup();
    component.openCreate();
    component.closeModal();
    expect(component.showModal).toBe(false);
  });

  it('deberia validar que el monto debe ser mayor a 0', async () => {
    const { component } = await setup();
    component.absoluteAmount = 0;
    component.validateFieldLive('monto');
    expect(component.fieldErrors['monto']).toBeDefined();
  });

  it('deberia limpiar el error de monto cuando el valor es valido', async () => {
    const { component } = await setup();
    component.absoluteAmount = 100;
    component.validateFieldLive('monto');
    expect(component.fieldErrors['monto']).toBeUndefined();
  });

  it('deberia resolver el numero de cuenta por id', async () => {
    const { component } = await setup();
    expect(component.accountNumber(1)).toBe('225487');
  });

  it('deberia retornar fallback cuando la cuenta no existe', async () => {
    const { component } = await setup();
    expect(component.accountNumber(999)).toBe('Cuenta #999');
  });

  it('deberia manejar error al cargar movimientos', async () => {
    const { component } = await setup(throwError(() => new Error('Error de red')));
    expect(component.movements.length).toBe(0);
  });
});
