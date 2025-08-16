package sejong.example.my_own_refrigerator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sejong.example.my_own_refrigerator.entity.RecipeStepEntity;

public interface RecipeStepRepository extends JpaRepository<RecipeStepEntity, Long> {
}