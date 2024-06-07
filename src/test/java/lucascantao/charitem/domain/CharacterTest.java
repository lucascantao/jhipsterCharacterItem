package lucascantao.charitem.domain;

import static lucascantao.charitem.domain.CharacterTestSamples.*;
import static lucascantao.charitem.domain.ItemTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import lucascantao.charitem.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CharacterTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Character.class);
        Character character1 = getCharacterSample1();
        Character character2 = new Character();
        assertThat(character1).isNotEqualTo(character2);

        character2.setId(character1.getId());
        assertThat(character1).isEqualTo(character2);

        character2 = getCharacterSample2();
        assertThat(character1).isNotEqualTo(character2);
    }

    @Test
    void itemTest() {
        Character character = getCharacterRandomSampleGenerator();
        Item itemBack = getItemRandomSampleGenerator();

        character.setItem(itemBack);
        assertThat(character.getItem()).isEqualTo(itemBack);

        character.item(null);
        assertThat(character.getItem()).isNull();
    }
}
