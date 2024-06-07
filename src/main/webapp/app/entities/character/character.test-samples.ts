import { ICharacter, NewCharacter } from './character.model';

export const sampleWithRequiredData: ICharacter = {
  id: '1efbbca9-5a87-45d1-ada7-e260a49d3192',
};

export const sampleWithPartialData: ICharacter = {
  id: '907e337d-8335-4dc1-b870-79cc3c998670',
  title: 'oh phew from',
};

export const sampleWithFullData: ICharacter = {
  id: '9b834212-3457-4d28-99f0-bbc64e304536',
  title: 'psst',
};

export const sampleWithNewData: NewCharacter = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
