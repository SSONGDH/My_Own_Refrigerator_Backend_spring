    package sejong.example.my_own_refrigerator.config;

    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

    @Configuration
    @EnableWebSecurity
    public class SecurityConfig {

        private static final String[] ALLOWED_URLS = {
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/v3/api-docs/**",
                "/api/v1/posts/**",
                "/api/v1/replies/**",
                "/login",
                "/auth/login/kakao",
                "/auth/login/kakao/**",
                "/login/oauth2/**",
                "/auth/logout/kakao",
                "/auth/logout/kakao/**"
        };

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .authorizeHttpRequests(authz -> authz
                            .requestMatchers(ALLOWED_URLS).permitAll()
                            .anyRequest().authenticated()
                    )
                    .csrf(csrf -> csrf.disable())
                    .formLogin(form -> form.disable())
                    .httpBasic(basic -> basic.disable());

            return http.build();
        }
    }