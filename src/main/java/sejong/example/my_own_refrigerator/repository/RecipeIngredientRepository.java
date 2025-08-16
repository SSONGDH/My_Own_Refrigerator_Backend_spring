package sejong.example.my_own_refrigerator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sejong.example.my_own_refrigerator.entity.RecipeIngredientEntity;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredientEntity, Long> {
}