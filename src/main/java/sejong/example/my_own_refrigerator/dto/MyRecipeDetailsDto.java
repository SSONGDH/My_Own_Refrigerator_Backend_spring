package sejong.example.my_own_refrigerator.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class MyRecipeDetailsDto {
    private Long id;
    private String name;
    private String shortDescription;
    private String imageUrl;
    private List<RecipeIngredientDto> ingredients;
    private String cookingSteps;
    private Long authorId;
}