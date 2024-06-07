package lucascantao.charitem.domain;

import java.util.UUID;

public class CharacterTestSamples {

    public static Character getCharacterSample1() {
        return new Character().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).title("title1");
    }

    public static Character getCharacterSample2() {
        return new Character().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).title("title2");
    }

    public static Character getCharacterRandomSampleGenerator() {
        return new Character().id(UUID.randomUUID()).title(UUID.randomUUID().toString());
    }
}
