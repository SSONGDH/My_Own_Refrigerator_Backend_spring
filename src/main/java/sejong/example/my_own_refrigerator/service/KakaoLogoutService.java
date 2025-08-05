    package sejong.example.my_own_refrigerator.service;

    import org.springframework.http.HttpEntity;
    import org.springframework.http.HttpHeaders;
    import org.springframework.http.ResponseEntity;
    import org.springframework.stereotype.Service;
    import org.springframework.web.client.RestTemplate;

    @Service
    public class KakaoLogoutService {

        public boolean logout(String accessToken) {
            String url = "https://kapi.kakao.com/v1/user/logout";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            // 상태 코드 200이면 성공
            return response.getStatusCode().is2xxSuccessful();
        }
    }
