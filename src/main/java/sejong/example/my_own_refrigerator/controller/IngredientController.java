package sejong.example.my_own_refrigerator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sejong.example.my_own_refrigerator.entity.IngredientEntity;
import sejong.example.my_own_refrigerator.service.IngredientService;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    /**
     * 모든 재료 목록을 조회하는 API
     * GET /api/ingredients
     * @return 모든 재료 목록을 담은 ResponseEntity
     */
    @GetMapping
    public ResponseEntity<List<IngredientEntity>> getAllIngredients() {
        List<IngredientEntity> ingredients = ingredientService.getAllIngredients();
        return ResponseEntity.ok(ingredients);
    }

    /**
     * 재료 이름으로 검색하는 API
     * GET /api/ingredients/search?name=파프리카
     * @param name 검색할 재료 이름
     * @return 검색된 재료 목록을 담은 ResponseEntity
     */
    @GetMapping("/search")
    public ResponseEntity<List<IngredientEntity>> searchIngredients(@RequestParam String name) {
        List<IngredientEntity> ingredients = ingredientService.searchIngredientsByName(name);
        return ResponseEntity.ok(ingredients);
    }
}