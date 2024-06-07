import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { ICharacter, NewCharacter } from '../character.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ICharacter for edit and NewCharacterFormGroupInput for create.
 */
type CharacterFormGroupInput = ICharacter | PartialWithRequiredKeyOf<NewCharacter>;

type CharacterFormDefaults = Pick<NewCharacter, 'id'>;

type CharacterFormGroupContent = {
  id: FormControl<ICharacter['id'] | NewCharacter['id']>;
  title: FormControl<ICharacter['title']>;
  item: FormControl<ICharacter['item']>;
};

export type CharacterFormGroup = FormGroup<CharacterFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class CharacterFormService {
  createCharacterFormGroup(character: CharacterFormGroupInput = { id: null }): CharacterFormGroup {
    const characterRawValue = {
      ...this.getFormDefaults(),
      ...character,
    };
    return new FormGroup<CharacterFormGroupContent>({
      id: new FormControl(
        { value: characterRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      title: new FormControl(characterRawValue.title),
      item: new FormControl(characterRawValue.item),
    });
  }

  getCharacter(form: CharacterFormGroup): ICharacter | NewCharacter {
    return form.getRawValue() as ICharacter | NewCharacter;
  }

  resetForm(form: CharacterFormGroup, character: CharacterFormGroupInput): void {
    const characterRawValue = { ...this.getFormDefaults(), ...character };
    form.reset(
      {
        ...characterRawValue,
        id: { value: characterRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): CharacterFormDefaults {
    return {
      id: null,
    };
  }
}
