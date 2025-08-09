package sejong.example.my_own_refrigerator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sejong.example.my_own_refrigerator.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    // application.properties에서 주입받으려면 @Value 사용
    @org.springframework.beans.factory.annotation.Value("${kakao.client.id}")
    private String clientId;

    @org.springframework.beans.factory.annotation.Value("${kakao.redirect.login.uri}")
    private String redirectUri;

    /**
     * 카카오 로그인 처리 엔드포인트
     * code 파라미터 없으면 카카오 로그인 페이지로 리다이렉트
     * code 있으면 토큰 발급 후 반환
     */
    @GetMapping("/login/kakao")
    public void kakaoLogin(@RequestParam(value = "code", required = false) String code,
                           HttpServletResponse response) throws IOException {
        if (code == null || code.isEmpty()) {
            // 인가 코드 없으면 카카오 로그인 URL로 리다이렉트
            String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                    + "?response_type=code"
                    + "&client_id=" + clientId
                    + "&redirect_uri=" + redirectUri
                    + "&prompt=login";  // 강제 로그인 화면 표시 옵션
            response.sendRedirect(kakaoAuthUrl);
            return;
        }

        // code가 있으면 토큰 발급
        String accessToken = authService.getKakaoAccessToken(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"accessToken\":\"" + accessToken + "\"}");
    }
}
