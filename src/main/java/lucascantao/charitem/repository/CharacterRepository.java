package lucascantao.charitem.repository;

import java.util.UUID;
import lucascantao.charitem.domain.Character;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Character entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CharacterRepository extends JpaRepository<Character, UUID> {}
