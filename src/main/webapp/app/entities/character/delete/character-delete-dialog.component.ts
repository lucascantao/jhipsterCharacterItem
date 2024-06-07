import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { ICharacter } from '../character.model';
import { CharacterService } from '../service/character.service';

@Component({
  standalone: true,
  templateUrl: './character-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class CharacterDeleteDialogComponent {
  character?: ICharacter;

  protected characterService = inject(CharacterService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: string): void {
    this.characterService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
