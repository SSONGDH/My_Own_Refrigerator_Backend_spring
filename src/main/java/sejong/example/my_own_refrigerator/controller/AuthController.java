package sejong.example.my_own_refrigerator.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sejong.example.my_own_refrigerator.service.AuthService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class AuthController {

    private final AuthService authService;

    @Value("${kakao.client.id}")
    private String clientId;  // application.properties에 설정된 값 주입

    @Value("${kakao.redirect.login.uri}")
    private String redirectUri;  // application.properties에 설정된 값 주입

    @GetMapping("/auth/login/kakao")
    public Object kakaoLogin(@RequestParam(value = "code", required = false) String accessCode,
                             HttpServletResponse response) throws IOException {

        if (accessCode == null || accessCode.isEmpty()) {
            // code 파라미터 없으면 카카오 인증 URL로 리다이렉트 (prompt=login 추가)
            String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                    + "?response_type=code"
                    + "&client_id=" + clientId
                    + "&redirect_uri=" + redirectUri
                    + "&prompt=login";

            response.sendRedirect(kakaoAuthUrl);
            return null; // 리다이렉트 했으므로 종료
        }

        try {
            // code 파라미터가 있으면 토큰 발급 및 유저정보 조회
            String accessToken = authService.getKakaoAccessToken(accessCode);
            JsonNode userInfo = authService.getKakaoUserInfo(accessToken);

            String kakaoId = userInfo.get("id").asText();
            String nickname = userInfo.get("properties").get("nickname").asText();

            // TODO: 회원 가입 또는 로그인 처리

            return ResponseEntity.ok("카카오 ID: " + kakaoId + ", 닉네임: " + nickname);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("카카오 로그인 실패: " + e.getMessage());
        }
    }
}
