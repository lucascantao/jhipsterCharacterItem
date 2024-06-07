package lucascantao.charitem.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lucascantao.charitem.domain.Character;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Character entity.
 */
@Repository
public interface CharacterRepository extends JpaRepository<Character, UUID> {
    default Optional<Character> findOneWithEagerRelationships(UUID id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Character> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Character> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select character from Character character left join fetch character.item",
        countQuery = "select count(character) from Character character"
    )
    Page<Character> findAllWithToOneRelationships(Pageable pageable);

    @Query("select character from Character character left join fetch character.item")
    List<Character> findAllWithToOneRelationships();

    @Query("select character from Character character left join fetch character.item where character.id =:id")
    Optional<Character> findOneWithToOneRelationships(@Param("id") UUID id);
}
