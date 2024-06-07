import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ICharacter } from '../character.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../character.test-samples';

import { CharacterService } from './character.service';

const requireRestSample: ICharacter = {
  ...sampleWithRequiredData,
};

describe('Character Service', () => {
  let service: CharacterService;
  let httpMock: HttpTestingController;
  let expectedResult: ICharacter | ICharacter[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(CharacterService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find('9fec3727-3421-4967-b213-ba36557ca194').subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a Character', () => {
      const character = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(character).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Character', () => {
      const character = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(character).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Character', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Character', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Character', () => {
      const expected = true;

      service.delete('9fec3727-3421-4967-b213-ba36557ca194').subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addCharacterToCollectionIfMissing', () => {
      it('should add a Character to an empty array', () => {
        const character: ICharacter = sampleWithRequiredData;
        expectedResult = service.addCharacterToCollectionIfMissing([], character);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(character);
      });

      it('should not add a Character to an array that contains it', () => {
        const character: ICharacter = sampleWithRequiredData;
        const characterCollection: ICharacter[] = [
          {
            ...character,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addCharacterToCollectionIfMissing(characterCollection, character);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Character to an array that doesn't contain it", () => {
        const character: ICharacter = sampleWithRequiredData;
        const characterCollection: ICharacter[] = [sampleWithPartialData];
        expectedResult = service.addCharacterToCollectionIfMissing(characterCollection, character);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(character);
      });

      it('should add only unique Character to an array', () => {
        const characterArray: ICharacter[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const characterCollection: ICharacter[] = [sampleWithRequiredData];
        expectedResult = service.addCharacterToCollectionIfMissing(characterCollection, ...characterArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const character: ICharacter = sampleWithRequiredData;
        const character2: ICharacter = sampleWithPartialData;
        expectedResult = service.addCharacterToCollectionIfMissing([], character, character2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(character);
        expect(expectedResult).toContain(character2);
      });

      it('should accept null and undefined values', () => {
        const character: ICharacter = sampleWithRequiredData;
        expectedResult = service.addCharacterToCollectionIfMissing([], null, character, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(character);
      });

      it('should return initial array if no Character is added', () => {
        const characterCollection: ICharacter[] = [sampleWithRequiredData];
        expectedResult = service.addCharacterToCollectionIfMissing(characterCollection, undefined, null);
        expect(expectedResult).toEqual(characterCollection);
      });
    });

    describe('compareCharacter', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareCharacter(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: '9fec3727-3421-4967-b213-ba36557ca194' };
        const entity2 = null;

        const compareResult1 = service.compareCharacter(entity1, entity2);
        const compareResult2 = service.compareCharacter(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: '9fec3727-3421-4967-b213-ba36557ca194' };
        const entity2 = { id: '1361f429-3817-4123-8ee3-fdf8943310b2' };

        const compareResult1 = service.compareCharacter(entity1, entity2);
        const compareResult2 = service.compareCharacter(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: '9fec3727-3421-4967-b213-ba36557ca194' };
        const entity2 = { id: '9fec3727-3421-4967-b213-ba36557ca194' };

        const compareResult1 = service.compareCharacter(entity1, entity2);
        const compareResult2 = service.compareCharacter(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
