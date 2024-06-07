import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'Authorities' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'character',
    data: { pageTitle: 'Characters' },
    loadChildren: () => import('./character/character.routes'),
  },
  {
    path: 'item',
    data: { pageTitle: 'Items' },
    loadChildren: () => import('./item/item.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
