package sejong.example.my_own_refrigerator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sejong.example.my_own_refrigerator.entity.IngredientEntity;
import sejong.example.my_own_refrigerator.repository.IngredientRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    /**
     * 모든 재료 목록을 조회합니다.
     * @return 모든 IngredientEntity 리스트
     */
    public List<IngredientEntity> getAllIngredients() {
        return ingredientRepository.findAll();
    }

    /**
     * 이름으로 재료를 검색합니다.
     * @param name 검색할 재료 이름
     * @return 검색된 IngredientEntity 리스트
     */
    public List<IngredientEntity> searchIngredientsByName(String name) {
        return ingredientRepository.findByNameContaining(name);
    }
}