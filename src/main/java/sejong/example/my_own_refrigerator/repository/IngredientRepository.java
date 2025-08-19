package sejong.example.my_own_refrigerator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sejong.example.my_own_refrigerator.entity.IngredientEntity;
import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<IngredientEntity, Long> {

    // 재료 이름이 정확히 일치하는 데이터를 조회합니다.
    Optional<IngredientEntity> findByName(String name);

    // 재료 이름에 특정 문자열이 포함된 모든 데이터를 조회합니다.
    List<IngredientEntity> findByNameContaining(String name);
}