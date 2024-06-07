import { IItem } from 'app/entities/item/item.model';

export interface ICharacter {
  id: string;
  title?: string | null;
  item?: IItem | null;
}

export type NewCharacter = Omit<ICharacter, 'id'> & { id: null };
