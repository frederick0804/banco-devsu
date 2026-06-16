import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'clientes', pathMatch: 'full' },
  {
    path: 'clientes',
    loadComponent: () => import('./pages/clientes/clientes.component').then(m => m.ClientesComponent)
  },
  {
    path: 'cuentas',
    loadComponent: () => import('./pages/cuentas/cuentas.component').then(m => m.CuentasComponent)
  },
  {
    path: 'movimientos',
    loadComponent: () => import('./pages/movimientos/movimientos.component').then(m => m.MovimientosComponent)
  },
  {
    path: 'reportes',
    loadComponent: () => import('./pages/reportes/reportes.component').then(m => m.ReportesComponent)
  }
];
