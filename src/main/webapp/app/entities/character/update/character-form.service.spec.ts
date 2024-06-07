import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../character.test-samples';

import { CharacterFormService } from './character-form.service';

describe('Character Form Service', () => {
  let service: CharacterFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CharacterFormService);
  });

  describe('Service methods', () => {
    describe('createCharacterFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createCharacterFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            title: expect.any(Object),
            item: expect.any(Object),
          }),
        );
      });

      it('passing ICharacter should create a new form with FormGroup', () => {
        const formGroup = service.createCharacterFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            title: expect.any(Object),
            item: expect.any(Object),
          }),
        );
      });
    });

    describe('getCharacter', () => {
      it('should return NewCharacter for default Character initial value', () => {
        const formGroup = service.createCharacterFormGroup(sampleWithNewData);

        const character = service.getCharacter(formGroup) as any;

        expect(character).toMatchObject(sampleWithNewData);
      });

      it('should return NewCharacter for empty Character initial value', () => {
        const formGroup = service.createCharacterFormGroup();

        const character = service.getCharacter(formGroup) as any;

        expect(character).toMatchObject({});
      });

      it('should return ICharacter', () => {
        const formGroup = service.createCharacterFormGroup(sampleWithRequiredData);

        const character = service.getCharacter(formGroup) as any;

        expect(character).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing ICharacter should not enable id FormControl', () => {
        const formGroup = service.createCharacterFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewCharacter should disable id FormControl', () => {
        const formGroup = service.createCharacterFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
