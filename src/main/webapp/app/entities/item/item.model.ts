export interface IItem {
  id: string;
  title?: string | null;
}

export type NewItem = Omit<IItem, 'id'> & { id: null };
