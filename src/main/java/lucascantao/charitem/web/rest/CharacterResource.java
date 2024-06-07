package lucascantao.charitem.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lucascantao.charitem.domain.Character;
import lucascantao.charitem.repository.CharacterRepository;
import lucascantao.charitem.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link lucascantao.charitem.domain.Character}.
 */
@RestController
@RequestMapping("/api/characters")
@Transactional
public class CharacterResource {

    private final Logger log = LoggerFactory.getLogger(CharacterResource.class);

    private static final String ENTITY_NAME = "character";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CharacterRepository characterRepository;

    public CharacterResource(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    /**
     * {@code POST  /characters} : Create a new character.
     *
     * @param character the character to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new character, or with status {@code 400 (Bad Request)} if the character has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Character> createCharacter(@RequestBody Character character) throws URISyntaxException {
        log.debug("REST request to save Character : {}", character);
        if (character.getId() != null) {
            throw new BadRequestAlertException("A new character cannot already have an ID", ENTITY_NAME, "idexists");
        }
        character = characterRepository.save(character);
        return ResponseEntity.created(new URI("/api/characters/" + character.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, character.getId().toString()))
            .body(character);
    }

    /**
     * {@code PUT  /characters/:id} : Updates an existing character.
     *
     * @param id the id of the character to save.
     * @param character the character to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated character,
     * or with status {@code 400 (Bad Request)} if the character is not valid,
     * or with status {@code 500 (Internal Server Error)} if the character couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Character> updateCharacter(
        @PathVariable(value = "id", required = false) final UUID id,
        @RequestBody Character character
    ) throws URISyntaxException {
        log.debug("REST request to update Character : {}, {}", id, character);
        if (character.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, character.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!characterRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        character = characterRepository.save(character);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, character.getId().toString()))
            .body(character);
    }

    /**
     * {@code PATCH  /characters/:id} : Partial updates given fields of an existing character, field will ignore if it is null
     *
     * @param id the id of the character to save.
     * @param character the character to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated character,
     * or with status {@code 400 (Bad Request)} if the character is not valid,
     * or with status {@code 404 (Not Found)} if the character is not found,
     * or with status {@code 500 (Internal Server Error)} if the character couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Character> partialUpdateCharacter(
        @PathVariable(value = "id", required = false) final UUID id,
        @RequestBody Character character
    ) throws URISyntaxException {
        log.debug("REST request to partial update Character partially : {}, {}", id, character);
        if (character.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, character.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!characterRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Character> result = characterRepository
            .findById(character.getId())
            .map(existingCharacter -> {
                if (character.getTitle() != null) {
                    existingCharacter.setTitle(character.getTitle());
                }

                return existingCharacter;
            })
            .map(characterRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, character.getId().toString())
        );
    }

    /**
     * {@code GET  /characters} : get all the characters.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of characters in body.
     */
    @GetMapping("")
    public List<Character> getAllCharacters(@RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload) {
        log.debug("REST request to get all Characters");
        if (eagerload) {
            return characterRepository.findAllWithEagerRelationships();
        } else {
            return characterRepository.findAll();
        }
    }

    /**
     * {@code GET  /characters/:id} : get the "id" character.
     *
     * @param id the id of the character to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the character, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Character> getCharacter(@PathVariable("id") UUID id) {
        log.debug("REST request to get Character : {}", id);
        Optional<Character> character = characterRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(character);
    }

    /**
     * {@code DELETE  /characters/:id} : delete the "id" character.
     *
     * @param id the id of the character to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharacter(@PathVariable("id") UUID id) {
        log.debug("REST request to delete Character : {}", id);
        characterRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
