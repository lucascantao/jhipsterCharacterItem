package lucascantao.charitem.domain;

import java.util.UUID;

public class ItemTestSamples {

    public static Item getItemSample1() {
        return new Item().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).title("title1");
    }

    public static Item getItemSample2() {
        return new Item().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).title("title2");
    }

    public static Item getItemRandomSampleGenerator() {
        return new Item().id(UUID.randomUUID()).title(UUID.randomUUID().toString());
    }
}
