package lucascantao.charitem.web.rest;

import static lucascantao.charitem.domain.CharacterAsserts.*;
import static lucascantao.charitem.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.UUID;
import lucascantao.charitem.IntegrationTest;
import lucascantao.charitem.domain.Character;
import lucascantao.charitem.repository.CharacterRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link CharacterResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class CharacterResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/characters";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CharacterRepository characterRepository;

    @Mock
    private CharacterRepository characterRepositoryMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCharacterMockMvc;

    private Character character;

    private Character insertedCharacter;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Character createEntity(EntityManager em) {
        Character character = new Character().title(DEFAULT_TITLE);
        return character;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Character createUpdatedEntity(EntityManager em) {
        Character character = new Character().title(UPDATED_TITLE);
        return character;
    }

    @BeforeEach
    public void initTest() {
        character = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedCharacter != null) {
            characterRepository.delete(insertedCharacter);
            insertedCharacter = null;
        }
    }

    @Test
    @Transactional
    void createCharacter() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Character
        var returnedCharacter = om.readValue(
            restCharacterMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(character)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Character.class
        );

        // Validate the Character in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertCharacterUpdatableFieldsEquals(returnedCharacter, getPersistedCharacter(returnedCharacter));

        insertedCharacter = returnedCharacter;
    }

    @Test
    @Transactional
    void createCharacterWithExistingId() throws Exception {
        // Create the Character with an existing ID
        insertedCharacter = characterRepository.saveAndFlush(character);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCharacterMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(character)))
            .andExpect(status().isBadRequest());

        // Validate the Character in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllCharacters() throws Exception {
        // Initialize the database
        insertedCharacter = characterRepository.saveAndFlush(character);

        // Get all the characterList
        restCharacterMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(character.getId().toString())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCharactersWithEagerRelationshipsIsEnabled() throws Exception {
        when(characterRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCharacterMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(characterRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCharactersWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(characterRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCharacterMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(characterRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getCharacter() throws Exception {
        // Initialize the database
        insertedCharacter = characterRepository.saveAndFlush(character);

        // Get the character
        restCharacterMockMvc
            .perform(get(ENTITY_API_URL_ID, character.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(character.getId().toString()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE));
    }

    @Test
    @Transactional
    void getNonExistingCharacter() throws Exception {
        // Get the character
        restCharacterMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCharacter() throws Exception {
        // Initialize the database
        insertedCharacter = characterRepository.saveAndFlush(character);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the character
        Character updatedCharacter = characterRepository.findById(character.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCharacter are not directly saved in db
        em.detach(updatedCharacter);
        updatedCharacter.title(UPDATED_TITLE);

        restCharacterMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedCharacter.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedCharacter))
            )
            .andExpect(status().isOk());

        // Validate the Character in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCharacterToMatchAllProperties(updatedCharacter);
    }

    @Test
    @Transactional
    void putNonExistingCharacter() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        character.setId(UUID.randomUUID());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCharacterMockMvc
            .perform(
                put(ENTITY_API_URL_ID, character.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(character))
            )
            .andExpect(status().isBadRequest());

        // Validate the Character in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCharacter() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        character.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCharacterMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(character))
            )
            .andExpect(status().isBadRequest());

        // Validate the Character in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCharacter() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        character.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCharacterMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(character)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Character in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCharacterWithPatch() throws Exception {
        // Initialize the database
        insertedCharacter = characterRepository.saveAndFlush(character);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the character using partial update
        Character partialUpdatedCharacter = new Character();
        partialUpdatedCharacter.setId(character.getId());

        restCharacterMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCharacter.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCharacter))
            )
            .andExpect(status().isOk());

        // Validate the Character in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCharacterUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedCharacter, character),
            getPersistedCharacter(character)
        );
    }

    @Test
    @Transactional
    void fullUpdateCharacterWithPatch() throws Exception {
        // Initialize the database
        insertedCharacter = characterRepository.saveAndFlush(character);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the character using partial update
        Character partialUpdatedCharacter = new Character();
        partialUpdatedCharacter.setId(character.getId());

        partialUpdatedCharacter.title(UPDATED_TITLE);

        restCharacterMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCharacter.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCharacter))
            )
            .andExpect(status().isOk());

        // Validate the Character in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCharacterUpdatableFieldsEquals(partialUpdatedCharacter, getPersistedCharacter(partialUpdatedCharacter));
    }

    @Test
    @Transactional
    void patchNonExistingCharacter() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        character.setId(UUID.randomUUID());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCharacterMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, character.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(character))
            )
            .andExpect(status().isBadRequest());

        // Validate the Character in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCharacter() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        character.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCharacterMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(character))
            )
            .andExpect(status().isBadRequest());

        // Validate the Character in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCharacter() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        character.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCharacterMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(character)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Character in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCharacter() throws Exception {
        // Initialize the database
        insertedCharacter = characterRepository.saveAndFlush(character);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the character
        restCharacterMockMvc
            .perform(delete(ENTITY_API_URL_ID, character.getId().toString()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return characterRepository.count();
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

    protected Character getPersistedCharacter(Character character) {
        return characterRepository.findById(character.getId()).orElseThrow();
    }

    protected void assertPersistedCharacterToMatchAllProperties(Character expectedCharacter) {
        assertCharacterAllPropertiesEquals(expectedCharacter, getPersistedCharacter(expectedCharacter));
    }

    protected void assertPersistedCharacterToMatchUpdatableProperties(Character expectedCharacter) {
        assertCharacterAllUpdatablePropertiesEquals(expectedCharacter, getPersistedCharacter(expectedCharacter));
    }
}
