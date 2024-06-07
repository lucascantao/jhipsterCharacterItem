import { Component, inject, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IItem } from 'app/entities/item/item.model';
import { ItemService } from 'app/entities/item/service/item.service';
import { ICharacter } from '../character.model';
import { CharacterService } from '../service/character.service';
import { CharacterFormService, CharacterFormGroup } from './character-form.service';

@Component({
  standalone: true,
  selector: 'jhi-character-update',
  templateUrl: './character-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class CharacterUpdateComponent implements OnInit {
  isSaving = false;
  character: ICharacter | null = null;

  itemsSharedCollection: IItem[] = [];

  protected characterService = inject(CharacterService);
  protected characterFormService = inject(CharacterFormService);
  protected itemService = inject(ItemService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: CharacterFormGroup = this.characterFormService.createCharacterFormGroup();

  compareItem = (o1: IItem | null, o2: IItem | null): boolean => this.itemService.compareItem(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ character }) => {
      this.character = character;
      if (character) {
        this.updateForm(character);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const character = this.characterFormService.getCharacter(this.editForm);
    if (character.id !== null) {
      this.subscribeToSaveResponse(this.characterService.update(character));
    } else {
      this.subscribeToSaveResponse(this.characterService.create(character));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICharacter>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(character: ICharacter): void {
    this.character = character;
    this.characterFormService.resetForm(this.editForm, character);

    this.itemsSharedCollection = this.itemService.addItemToCollectionIfMissing<IItem>(this.itemsSharedCollection, character.item);
  }

  protected loadRelationshipsOptions(): void {
    this.itemService
      .query()
      .pipe(map((res: HttpResponse<IItem[]>) => res.body ?? []))
      .pipe(map((items: IItem[]) => this.itemService.addItemToCollectionIfMissing<IItem>(items, this.character?.item)))
      .subscribe((items: IItem[]) => (this.itemsSharedCollection = items));
  }
}
