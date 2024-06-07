import { IUser } from './user.model';

export const sampleWithRequiredData: IUser = {
  id: 26575,
  login: 'O2WAy',
};

export const sampleWithPartialData: IUser = {
  id: 17139,
  login: '5',
};

export const sampleWithFullData: IUser = {
  id: 4369,
  login: '8OAx',
};
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
