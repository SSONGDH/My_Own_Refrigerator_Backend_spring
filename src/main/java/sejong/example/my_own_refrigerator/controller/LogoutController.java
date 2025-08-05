package sejong.example.my_own_refrigerator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sejong.example.my_own_refrigerator.service.KakaoLogoutService;

@RestController
@RequiredArgsConstructor
public class LogoutController {

    private final KakaoLogoutService kakaoLogoutService;

    @GetMapping("/auth/logout/kakao")
    public ResponseEntity<?> logout(@RequestParam("accessToken") String accessToken) {
        boolean result = kakaoLogoutService.logout(accessToken);
        if (result) {
            return ResponseEntity.ok("카카오 로그아웃 성공");
        } else {
            return ResponseEntity.status(500).body("카카오 로그아웃 실패");
        }
    }
}
