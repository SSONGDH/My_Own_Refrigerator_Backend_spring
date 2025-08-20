package sejong.example.my_own_refrigerator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sejong.example.my_own_refrigerator.dto.CommunityRecipeDto;
import sejong.example.my_own_refrigerator.dto.CommunityRecipeDetailsDto;
import sejong.example.my_own_refrigerator.dto.RecipeIngredientDto;
import sejong.example.my_own_refrigerator.entity.MyRecipeEntity;
import sejong.example.my_own_refrigerator.entity.UserEntity;
import sejong.example.my_own_refrigerator.entity.IngredientEntity;
import sejong.example.my_own_refrigerator.repository.MyRecipeRepository;
import sejong.example.my_own_refrigerator.repository.UserRepository;
import sejong.example.my_own_refrigerator.repository.IngredientRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final MyRecipeRepository myRecipeRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final ObjectMapper objectMapper;

    public List<CommunityRecipeDto> getCommunityRecipes() {
        List<MyRecipeEntity> publicRecipes = myRecipeRepository.findByIsPublicTrue();

        List<Long> authorIds = publicRecipes.stream()
                .map(MyRecipeEntity::getAuthorId)
                .distinct()
                .collect(Collectors.toList());

        List<UserEntity> users = userRepository.findAllById(authorIds);
        var userNicknameMap = users.stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getNickname));

        return publicRecipes.stream()
                .map(recipe -> CommunityRecipeDto.builder()
                        .id(recipe.getId())
                        .recipeName(recipe.getName())
                        .shortDescription(recipe.getShortDescription())
                        .userNickname(userNicknameMap.getOrDefault(recipe.getAuthorId(), "알 수 없음"))
                        .postedDate(recipe.getPostedDate())
                        .likeCount(recipe.getLikeCount())
                        .build())
                .collect(Collectors.toList());
    }

    public CommunityRecipeDetailsDto getRecipeDetails(Long recipeId) {
        MyRecipeEntity recipe = myRecipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + recipeId));

        if (!recipe.isPublic()) {
            throw new RuntimeException("This recipe is not shared with the community.");
        }

        UserEntity author = userRepository.findById(recipe.getAuthorId())
                .orElse(null);

        try {
            List<Map<String, Object>> ingredientsWithId = objectMapper.readValue(
                    recipe.getIngredientsJson(),
                    new TypeReference<>() {}
            );

            List<Long> ingredientIds = ingredientsWithId.stream()
                    .filter(item -> item.get("id") instanceof Number)
                    .map(item -> ((Number) item.get("id")).longValue())
                    .collect(Collectors.toList());
            List<IngredientEntity> ingredientEntities = ingredientRepository.findAllById(ingredientIds);
            Map<Long, String> ingredientNameMap = ingredientEntities.stream()
                    .collect(Collectors.toMap(IngredientEntity::getId, IngredientEntity::getName));

            List<RecipeIngredientDto> finalIngredients = ingredientsWithId.stream()
                    .filter(item -> item.get("id") instanceof Number)
                    .map(item -> {
                        RecipeIngredientDto dto = new RecipeIngredientDto();
                        Long id = ((Number) item.get("id")).longValue();
                        dto.setName(ingredientNameMap.getOrDefault(id, "알 수 없는 재료"));
                        dto.setQuantity((Integer) item.get("quantity"));
                        return dto;
                    })
                    .collect(Collectors.toList());

            return CommunityRecipeDetailsDto.builder()
                    .id(recipe.getId())
                    .name(recipe.getName())
                    .shortDescription(recipe.getShortDescription())
                    .userNickname(author != null ? author.getNickname() : "알 수 없음")
                    .postedDate(recipe.getPostedDate())
                    .likeCount(recipe.getLikeCount())
                    .cookingSteps(recipe.getCookingSteps())
                    .ingredients(finalIngredients)
                    .build();

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing recipe ingredients JSON", e);
        }
    }

    /**
     * ✅ 특정 레시피의 '좋아요' 수를 1 증가시키는 메서드
     * @param recipeId 좋아요를 누를 레시피의 ID
     */
    @Transactional
    public void incrementLikeCount(Long recipeId) {
        MyRecipeEntity recipe = myRecipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + recipeId));

        if (!recipe.isPublic()) {
            throw new RuntimeException("This recipe is not shared with the community.");
        }

        // 좋아요 수 1 증가
        recipe.setLikeCount(recipe.getLikeCount() + 1);
        myRecipeRepository.save(recipe);
    }
}