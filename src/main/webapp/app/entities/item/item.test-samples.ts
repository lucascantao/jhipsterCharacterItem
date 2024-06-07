import { IItem, NewItem } from './item.model';

export const sampleWithRequiredData: IItem = {
  id: '4d413d2e-8c28-487c-9662-d323a25d9e6f',
};

export const sampleWithPartialData: IItem = {
  id: '0eb22f29-74c2-4627-8452-4f962f658647',
};

export const sampleWithFullData: IItem = {
  id: 'df0f2c6e-0ac2-40d4-94e4-1b22804b7b2d',
  title: 'ferociously versus',
};

export const sampleWithNewData: NewItem = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
