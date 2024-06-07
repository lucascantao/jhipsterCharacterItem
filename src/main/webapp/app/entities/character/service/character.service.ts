import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ICharacter, NewCharacter } from '../character.model';

export type PartialUpdateCharacter = Partial<ICharacter> & Pick<ICharacter, 'id'>;

export type EntityResponseType = HttpResponse<ICharacter>;
export type EntityArrayResponseType = HttpResponse<ICharacter[]>;

@Injectable({ providedIn: 'root' })
export class CharacterService {
  protected http = inject(HttpClient);
  protected applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/characters');

  create(character: NewCharacter): Observable<EntityResponseType> {
    return this.http.post<ICharacter>(this.resourceUrl, character, { observe: 'response' });
  }

  update(character: ICharacter): Observable<EntityResponseType> {
    return this.http.put<ICharacter>(`${this.resourceUrl}/${this.getCharacterIdentifier(character)}`, character, { observe: 'response' });
  }

  partialUpdate(character: PartialUpdateCharacter): Observable<EntityResponseType> {
    return this.http.patch<ICharacter>(`${this.resourceUrl}/${this.getCharacterIdentifier(character)}`, character, { observe: 'response' });
  }

  find(id: string): Observable<EntityResponseType> {
    return this.http.get<ICharacter>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ICharacter[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: string): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getCharacterIdentifier(character: Pick<ICharacter, 'id'>): string {
    return character.id;
  }

  compareCharacter(o1: Pick<ICharacter, 'id'> | null, o2: Pick<ICharacter, 'id'> | null): boolean {
    return o1 && o2 ? this.getCharacterIdentifier(o1) === this.getCharacterIdentifier(o2) : o1 === o2;
  }

  addCharacterToCollectionIfMissing<Type extends Pick<ICharacter, 'id'>>(
    characterCollection: Type[],
    ...charactersToCheck: (Type | null | undefined)[]
  ): Type[] {
    const characters: Type[] = charactersToCheck.filter(isPresent);
    if (characters.length > 0) {
      const characterCollectionIdentifiers = characterCollection.map(characterItem => this.getCharacterIdentifier(characterItem));
      const charactersToAdd = characters.filter(characterItem => {
        const characterIdentifier = this.getCharacterIdentifier(characterItem);
        if (characterCollectionIdentifiers.includes(characterIdentifier)) {
          return false;
        }
        characterCollectionIdentifiers.push(characterIdentifier);
        return true;
      });
      return [...charactersToAdd, ...characterCollection];
    }
    return characterCollection;
  }
}
