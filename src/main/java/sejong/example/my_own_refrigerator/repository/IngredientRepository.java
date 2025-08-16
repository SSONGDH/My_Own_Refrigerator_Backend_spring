package sejong.example.my_own_refrigerator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sejong.example.my_own_refrigerator.entity.IngredientEntity;

import java.util.Optional;

public interface IngredientRepository extends JpaRepository<IngredientEntity, Long> {
    boolean existsByName(String name);
    Optional<IngredientEntity> findByName(String name);
}
