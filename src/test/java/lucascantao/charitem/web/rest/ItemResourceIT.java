package lucascantao.charitem.web.rest;

import static lucascantao.charitem.domain.ItemAsserts.*;
import static lucascantao.charitem.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import lucascantao.charitem.IntegrationTest;
import lucascantao.charitem.domain.Item;
import lucascantao.charitem.repository.ItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ItemResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ItemResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/items";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restItemMockMvc;

    private Item item;

    private Item insertedItem;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Item createEntity(EntityManager em) {
        Item item = new Item().title(DEFAULT_TITLE);
        return item;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Item createUpdatedEntity(EntityManager em) {
        Item item = new Item().title(UPDATED_TITLE);
        return item;
    }

    @BeforeEach
    public void initTest() {
        item = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedItem != null) {
            itemRepository.delete(insertedItem);
            insertedItem = null;
        }
    }

    @Test
    @Transactional
    void createItem() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Item
        var returnedItem = om.readValue(
            restItemMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(item)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Item.class
        );

        // Validate the Item in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertItemUpdatableFieldsEquals(returnedItem, getPersistedItem(returnedItem));

        insertedItem = returnedItem;
    }

    @Test
    @Transactional
    void createItemWithExistingId() throws Exception {
        // Create the Item with an existing ID
        insertedItem = itemRepository.saveAndFlush(item);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restItemMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(item)))
            .andExpect(status().isBadRequest());

        // Validate the Item in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllItems() throws Exception {
        // Initialize the database
        insertedItem = itemRepository.saveAndFlush(item);

        // Get all the itemList
        restItemMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(item.getId().toString())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)));
    }

    @Test
    @Transactional
    void getItem() throws Exception {
        // Initialize the database
        insertedItem = itemRepository.saveAndFlush(item);

        // Get the item
        restItemMockMvc
            .perform(get(ENTITY_API_URL_ID, item.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(item.getId().toString()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE));
    }

    @Test
    @Transactional
    void getNonExistingItem() throws Exception {
        // Get the item
        restItemMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingItem() throws Exception {
        // Initialize the database
        insertedItem = itemRepository.saveAndFlush(item);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the item
        Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedItem are not directly saved in db
        em.detach(updatedItem);
        updatedItem.title(UPDATED_TITLE);

        restItemMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedItem.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedItem))
            )
            .andExpect(status().isOk());

        // Validate the Item in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedItemToMatchAllProperties(updatedItem);
    }

    @Test
    @Transactional
    void putNonExistingItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        item.setId(UUID.randomUUID());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restItemMockMvc
            .perform(put(ENTITY_API_URL_ID, item.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(item)))
            .andExpect(status().isBadRequest());

        // Validate the Item in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        item.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restItemMockMvc
            .perform(put(ENTITY_API_URL_ID, UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(item)))
            .andExpect(status().isBadRequest());

        // Validate the Item in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        item.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restItemMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(item)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Item in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateItemWithPatch() throws Exception {
        // Initialize the database
        insertedItem = itemRepository.saveAndFlush(item);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the item using partial update
        Item partialUpdatedItem = new Item();
        partialUpdatedItem.setId(item.getId());

        partialUpdatedItem.title(UPDATED_TITLE);

        restItemMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedItem.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedItem))
            )
            .andExpect(status().isOk());

        // Validate the Item in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertItemUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedItem, item), getPersistedItem(item));
    }

    @Test
    @Transactional
    void fullUpdateItemWithPatch() throws Exception {
        // Initialize the database
        insertedItem = itemRepository.saveAndFlush(item);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the item using partial update
        Item partialUpdatedItem = new Item();
        partialUpdatedItem.setId(item.getId());

        partialUpdatedItem.title(UPDATED_TITLE);

        restItemMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedItem.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedItem))
            )
            .andExpect(status().isOk());

        // Validate the Item in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertItemUpdatableFieldsEquals(partialUpdatedItem, getPersistedItem(partialUpdatedItem));
    }

    @Test
    @Transactional
    void patchNonExistingItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        item.setId(UUID.randomUUID());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restItemMockMvc
            .perform(patch(ENTITY_API_URL_ID, item.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(item)))
            .andExpect(status().isBadRequest());

        // Validate the Item in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        item.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restItemMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(item))
            )
            .andExpect(status().isBadRequest());

        // Validate the Item in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamItem() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        item.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restItemMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(item)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Item in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteItem() throws Exception {
        // Initialize the database
        insertedItem = itemRepository.saveAndFlush(item);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the item
        restItemMockMvc
            .perform(delete(ENTITY_API_URL_ID, item.getId().toString()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return itemRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Item getPersistedItem(Item item) {
        return itemRepository.findById(item.getId()).orElseThrow();
    }

    protected void assertPersistedItemToMatchAllProperties(Item expectedItem) {
        assertItemAllPropertiesEquals(expectedItem, getPersistedItem(expectedItem));
    }

    protected void assertPersistedItemToMatchUpdatableProperties(Item expectedItem) {
        assertItemAllUpdatablePropertiesEquals(expectedItem, getPersistedItem(expectedItem));
    }
}
