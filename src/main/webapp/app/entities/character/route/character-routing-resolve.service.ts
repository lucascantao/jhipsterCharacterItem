import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { ICharacter } from '../character.model';
import { CharacterService } from '../service/character.service';

const characterResolve = (route: ActivatedRouteSnapshot): Observable<null | ICharacter> => {
  const id = route.params['id'];
  if (id) {
    return inject(CharacterService)
      .find(id)
      .pipe(
        mergeMap((character: HttpResponse<ICharacter>) => {
          if (character.body) {
            return of(character.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default characterResolve;
