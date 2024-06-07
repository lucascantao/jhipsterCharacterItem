import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { CharacterComponent } from './list/character.component';
import { CharacterDetailComponent } from './detail/character-detail.component';
import { CharacterUpdateComponent } from './update/character-update.component';
import CharacterResolve from './route/character-routing-resolve.service';

const characterRoute: Routes = [
  {
    path: '',
    component: CharacterComponent,
    data: {},
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: CharacterDetailComponent,
    resolve: {
      character: CharacterResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: CharacterUpdateComponent,
    resolve: {
      character: CharacterResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: CharacterUpdateComponent,
    resolve: {
      character: CharacterResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default characterRoute;
