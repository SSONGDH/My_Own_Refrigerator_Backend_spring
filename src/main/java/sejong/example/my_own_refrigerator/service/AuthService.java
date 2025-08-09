package sejong.example.my_own_refrigerator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sejong.example.my_own_refrigerator.entity.UserEntity;
import sejong.example.my_own_refrigerator.repository.UserRepository;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository; // DB 저장용

    @Value("${kakao.client.id}")
    private String clientId;

    @Value("${kakao.redirect.login.uri}")
    private String redirectUri;

    /**
     * 카카오 인증 코드로 액세스 토큰 발급 및 닉네임 저장
     */
    public String getKakaoAccessToken(String code) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 바디 파라미터 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        // 요청 엔티티 생성
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // POST 요청으로 토큰 발급
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token",
                request,
                String.class
        );

        // 응답 로그
        System.out.println("카카오 토큰 응답: " + response.getBody());

        // JSON 파싱하여 access_token 추출
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        String accessToken = jsonNode.get("access_token").asText();

        // 토큰으로 카카오 유저 정보 요청
        String nickname = getKakaoUserNickname(accessToken);

        // DB에 저장 (닉네임과 토큰 저장 예시)
        UserEntity user = new UserEntity();
        user.setNickname(nickname);
        user.setKakaoAccessToken(accessToken);
        userRepository.save(user);

        return accessToken;
    }

    /**
     * 카카오 사용자 닉네임 조회
     */
    private String getKakaoUserNickname(String accessToken) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        // 헤더에 Authorization 추가
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        // 사용자 정보 요청
        ResponseEntity<String> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                request,
                String.class
        );

        // 응답 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        return jsonNode.get("properties").get("nickname").asText();
    }
}
