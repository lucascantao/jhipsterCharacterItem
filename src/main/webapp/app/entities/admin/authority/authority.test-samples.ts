import { IAuthority, NewAuthority } from './authority.model';

export const sampleWithRequiredData: IAuthority = {
  name: 'c6f43a52-c1c7-440d-8fea-88e1b3a74073',
};

export const sampleWithPartialData: IAuthority = {
  name: '0e83b1ad-061a-47de-9b34-7a66543d8883',
};

export const sampleWithFullData: IAuthority = {
  name: '037439bf-1af5-4e40-9c3f-6cf6405b4ab7',
};

export const sampleWithNewData: NewAuthority = {
  name: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
