package sejong.example.my_own_refrigerator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletResponse;
import sejong.example.my_own_refrigerator.config.KakaoAuthProperties;

import java.io.IOException;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoAuthProperties kakaoAuthProperties;

    public String getKakaoAccessToken(String code) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoAuthProperties.getClient());
        params.add("redirect_uri", kakaoAuthProperties.getRedirect());
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token",
                request,
                String.class
        );

        // 응답 바디 출력 (로그 찍기)
        System.out.println("카카오 토큰 응답: " + response.getBody());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        return jsonNode.get("access_token").asText();
    }

    public JsonNode getKakaoUserInfo(String accessToken) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);  // Bearer 토큰 인증 헤더 설정

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                entity,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(response.getBody());
    }
}

