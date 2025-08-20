package sejong.example.my_own_refrigerator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import sejong.example.my_own_refrigerator.entity.RefrigeratorEntity;
import sejong.example.my_own_refrigerator.entity.UserEntity;
import sejong.example.my_own_refrigerator.repository.RefrigeratorRepository;
import sejong.example.my_own_refrigerator.repository.UserRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RefrigeratorRepository refrigeratorRepository;
    private final UserRepository userRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final String PYTHON_API_URL = "http://localhost:5000";

    // ✅ userId를 파라미터에서 제거하고 JWT 토큰에서 가져옵니다.
    public Mono<String> getRecommendedRecipesFromDb() {
        String kakaoId = SecurityContextHolder.getContext().getAuthentication().getName();

        return Mono.fromSupplier(() -> userRepository.findByKakaoId(kakaoId))
                .flatMap(userOpt -> {
                    if (userOpt.isEmpty()) {
                        return Mono.just("{\"error\":\"사용자를 찾을 수 없습니다.\"}");
                    }

                    UserEntity user = userOpt.get();
                    Optional<RefrigeratorEntity> refrigeratorOpt = refrigeratorRepository.findByUser(user);

                    if (refrigeratorOpt.isEmpty()) {
                        return Mono.just("{\"error\":\"사용자에게 냉장고가 없습니다.\"}");
                    }

                    RefrigeratorEntity refrigerator = refrigeratorOpt.get();
                    List<String> ingredients;

                    try {
                        List<Map<String, Object>> foodsList = objectMapper.readValue(
                                refrigerator.getFoodsJson(),
                                new TypeReference<List<Map<String, Object>>>() {}
                        );
                        ingredients = foodsList.stream()
                                .map(food -> (String) food.get("name"))
                                .collect(Collectors.toList());
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("냉장고 데이터 파싱 오류", e));
                    }
                    if (ingredients.isEmpty()) {
                        return Mono.just("{\"error\":\"냉장고에 재료가 없습니다.\"}");
                    }
                    String ingredientsString = String.join(", ", ingredients);
                    Map<String, String> requestBody = new HashMap<>();
                    requestBody.put("ingredients", ingredientsString);

                    WebClient webClient = webClientBuilder.baseUrl(PYTHON_API_URL).build();
                    return webClient.post()
                            .uri("/recommend")
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToMono(String.class)
                            .onErrorResume(e -> {
                                System.err.println("Python API 호출 실패: " + e.getMessage());
                                return Mono.just("{\"error\":\"레시피 추천 서버에 문제가 발생했습니다.\"}");
                            });
                });
    }

    public Mono<String> getRecipeDetails(String recipeId) {
        WebClient webClient = webClientBuilder.baseUrl(PYTHON_API_URL).build();
        return webClient.get()
                .uri("/details/{recipeId}", recipeId)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    System.err.println("Python API 호출 실패: " + e.getMessage());
                    return Mono.just("{\"error\":\"레시피 상세 정보 서버에 문제가 발생했습니다.\"}");
                });
    }
}