package sejong.example.my_own_refrigerator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sejong.example.my_own_refrigerator.entity.UserEntity;
import sejong.example.my_own_refrigerator.repository.UserRepository;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    /**
     * 클라이언트가 보낸 카카오 Access Token으로 로그인 처리
     */
    public String loginWithKakao(String accessToken) throws IOException {
        // 1. 사용자 정보 가져오기
        JsonNode userInfo = getKakaoUserInfo(accessToken);
        String kakaoId = userInfo.get("id").asText();
        String nickname = userInfo.get("properties").get("nickname").asText();

        // 2. DB에 사용자 저장
        UserEntity user = userRepository.findByKakaoId(kakaoId).orElse(new UserEntity());
        user.setKakaoId(kakaoId);
        user.setNickname(nickname);
        userRepository.save(user);

        // 3. Access Token 그대로 반환 (DB에는 저장하지 않음)
        return accessToken;
    }

    /**
     * 카카오 사용자 정보 조회
     */
    private JsonNode getKakaoUserInfo(String accessToken) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                request,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(response.getBody());
    }
}
