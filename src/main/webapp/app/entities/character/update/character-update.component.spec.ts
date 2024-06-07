import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject, from } from 'rxjs';

import { IItem } from 'app/entities/item/item.model';
import { ItemService } from 'app/entities/item/service/item.service';
import { CharacterService } from '../service/character.service';
import { ICharacter } from '../character.model';
import { CharacterFormService } from './character-form.service';

import { CharacterUpdateComponent } from './character-update.component';

describe('Character Management Update Component', () => {
  let comp: CharacterUpdateComponent;
  let fixture: ComponentFixture<CharacterUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let characterFormService: CharacterFormService;
  let characterService: CharacterService;
  let itemService: ItemService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, CharacterUpdateComponent],
      providers: [
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(CharacterUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(CharacterUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    characterFormService = TestBed.inject(CharacterFormService);
    characterService = TestBed.inject(CharacterService);
    itemService = TestBed.inject(ItemService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Item query and add missing value', () => {
      const character: ICharacter = { id: '1361f429-3817-4123-8ee3-fdf8943310b2' };
      const item: IItem = { id: 'f74bd512-f67d-480e-a3c6-37db3a917b4b' };
      character.item = item;

      const itemCollection: IItem[] = [{ id: 'b369b105-d711-4c31-be5a-4871d04bcffd' }];
      jest.spyOn(itemService, 'query').mockReturnValue(of(new HttpResponse({ body: itemCollection })));
      const additionalItems = [item];
      const expectedCollection: IItem[] = [...additionalItems, ...itemCollection];
      jest.spyOn(itemService, 'addItemToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ character });
      comp.ngOnInit();

      expect(itemService.query).toHaveBeenCalled();
      expect(itemService.addItemToCollectionIfMissing).toHaveBeenCalledWith(
        itemCollection,
        ...additionalItems.map(expect.objectContaining),
      );
      expect(comp.itemsSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const character: ICharacter = { id: '1361f429-3817-4123-8ee3-fdf8943310b2' };
      const item: IItem = { id: '8c874ccd-c6da-4ba1-834b-aa79d01e3138' };
      character.item = item;

      activatedRoute.data = of({ character });
      comp.ngOnInit();

      expect(comp.itemsSharedCollection).toContain(item);
      expect(comp.character).toEqual(character);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ICharacter>>();
      const character = { id: '9fec3727-3421-4967-b213-ba36557ca194' };
      jest.spyOn(characterFormService, 'getCharacter').mockReturnValue(character);
      jest.spyOn(characterService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ character });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: character }));
      saveSubject.complete();

      // THEN
      expect(characterFormService.getCharacter).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(characterService.update).toHaveBeenCalledWith(expect.objectContaining(character));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ICharacter>>();
      const character = { id: '9fec3727-3421-4967-b213-ba36557ca194' };
      jest.spyOn(characterFormService, 'getCharacter').mockReturnValue({ id: null });
      jest.spyOn(characterService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ character: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: character }));
      saveSubject.complete();

      // THEN
      expect(characterFormService.getCharacter).toHaveBeenCalled();
      expect(characterService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ICharacter>>();
      const character = { id: '9fec3727-3421-4967-b213-ba36557ca194' };
      jest.spyOn(characterService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ character });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(characterService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareItem', () => {
      it('Should forward to itemService', () => {
        const entity = { id: '9fec3727-3421-4967-b213-ba36557ca194' };
        const entity2 = { id: '1361f429-3817-4123-8ee3-fdf8943310b2' };
        jest.spyOn(itemService, 'compareItem');
        comp.compareItem(entity, entity2);
        expect(itemService.compareItem).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
