package sejong.example.my_own_refrigerator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sejong.example.my_own_refrigerator.dto.MyRecipeDto;
import sejong.example.my_own_refrigerator.dto.MyRecipeDetailsDto;
import sejong.example.my_own_refrigerator.dto.RecipeIngredientDto;
import sejong.example.my_own_refrigerator.entity.MyRecipeEntity;
import sejong.example.my_own_refrigerator.entity.UserEntity;
import sejong.example.my_own_refrigerator.entity.IngredientEntity;
import sejong.example.my_own_refrigerator.repository.MyRecipeRepository;
import sejong.example.my_own_refrigerator.repository.UserRepository;
import sejong.example.my_own_refrigerator.repository.IngredientRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyRecipeService {

    private final MyRecipeRepository myRecipeRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final ObjectMapper objectMapper;

    /**
     * 나만의 레시피를 생성하고 저장합니다.
     */
    public MyRecipeEntity createMyRecipe(MyRecipeDto recipeDto) {
        String kakaoId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found with kakaoId: " + kakaoId));

        try {
            String ingredientsJson = objectMapper.writeValueAsString(recipeDto.getIngredients());

            MyRecipeEntity newRecipe = MyRecipeEntity.builder()
                    .name(recipeDto.getName())
                    .shortDescription(recipeDto.getShortDescription())
                    .imageUrl(recipeDto.getImageUrl())
                    .ingredientsJson(ingredientsJson)
                    .cookingSteps(recipeDto.getCookingSteps())
                    .authorId(user.getId())
                    .build();

            return myRecipeRepository.save(newRecipe);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting ingredients to JSON", e);
        }
    }

    /**
     * 레시피 상세 조회 메서드
     */
    public MyRecipeDetailsDto getRecipeDetails(Long recipeId) {
        String kakaoId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found with kakaoId: " + kakaoId));

        MyRecipeEntity recipe = myRecipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + recipeId));

        if (!recipe.getAuthorId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to recipe: " + recipeId);
        }

        return convertToDetailsDto(recipe);
    }

    /**
     * 레시피 수정 메서드
     */
    public MyRecipeDetailsDto updateMyRecipe(Long recipeId, MyRecipeDto recipeDto) {
        String kakaoId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found with kakaoId: " + kakaoId));

        MyRecipeEntity existingRecipe = myRecipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + recipeId));

        if (!existingRecipe.getAuthorId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to update this recipe.");
        }

        try {
            existingRecipe.setName(recipeDto.getName());
            existingRecipe.setShortDescription(recipeDto.getShortDescription());
            existingRecipe.setImageUrl(recipeDto.getImageUrl());
            existingRecipe.setCookingSteps(recipeDto.getCookingSteps());

            String ingredientsJson = objectMapper.writeValueAsString(recipeDto.getIngredients());
            existingRecipe.setIngredientsJson(ingredientsJson);

            MyRecipeEntity updatedRecipe = myRecipeRepository.save(existingRecipe);

            return convertToDetailsDto(updatedRecipe);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting ingredients to JSON", e);
        }
    }

    /**
     * 레시피 공유 메서드
     */
    public void shareRecipe(Long recipeId) {
        String kakaoId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("User not found with kakaoId: " + kakaoId));

        MyRecipeEntity recipe = myRecipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + recipeId));

        if (!recipe.getAuthorId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to share this recipe.");
        }

        // ⏪ 레시피 공개 시, 날짜를 현재 시각으로 설정
        recipe.setPostedDate(LocalDateTime.now());
        recipe.setPublic(true);
        myRecipeRepository.save(recipe);
    }

    private MyRecipeDetailsDto convertToDetailsDto(MyRecipeEntity recipe) {
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

            return MyRecipeDetailsDto.builder()
                    .id(recipe.getId())
                    .name(recipe.getName())
                    .shortDescription(recipe.getShortDescription())
                    .imageUrl(recipe.getImageUrl())
                    .ingredients(finalIngredients)
                    .cookingSteps(recipe.getCookingSteps())
                    .authorId(recipe.getAuthorId())
                    .build();

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing recipe ingredients JSON", e);
        }
    }
}