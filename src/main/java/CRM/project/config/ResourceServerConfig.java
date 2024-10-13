//package CRM.project.config;
//
//
//import org.apache.commons.io.IOUtils;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
//import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
//import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
//import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
//import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
//import org.springframework.security.oauth2.provider.token.TokenStore;
//import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
//import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//
//@Configuration
//@EnableResourceServer
//public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
//    private String PUBLIC_KEY = null;
//
//    public void configure(HttpSecurity http) throws Exception {
//        http.cors()
//                .and()
//                .authorizeRequests()
//                .antMatchers(
//                        "/", "/swagger/**", "/v2/api-docs/**", "/swagger-resources/**",
//                        "/swagger-ui.html", "/webjars/**", "/resources/**", "/swagger-ui/**",
//                        "/v3/api-docs/**", "/excluded/**", "/jwt/**",
//                        "/user/validate-user", "/requests/downloadRequest", "/solutions/downloadSolution",
//                        "/reports/export"
//                )
//                .permitAll()
//                .anyRequest()
//                .authenticated()
//                .and()
//                .headers()
//                .contentSecurityPolicy("frame-ancestors 'self';")
//                .and()
//                .httpStrictTransportSecurity()
//                .includeSubDomains(true)
//                .maxAgeInSeconds(31536000)
//                .and()
//                .frameOptions().sameOrigin() // Set X-Frame-Options to "SAMEORIGIN"
//                .and()
//                .csrf().disable(); // Review the need to disable CSRF
//    }
//
//
//    public void configure(ResourceServerSecurityConfigurer config) {
//        config.tokenServices((ResourceServerTokenServices)tokenServices());
//    }
//
//    @Bean
//    public BCryptPasswordEncoder encoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public TokenStore tokenStore() {
//        return (TokenStore)new JwtTokenStore(accessTokenConverter());
//    }
//
//    @Bean
//    public JwtAccessTokenConverter accessTokenConverter() {
//        ClassPathResource classPathResource = new ClassPathResource("templates/publickey.txt");
//        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
//        try {
//            this.PUBLIC_KEY = IOUtils.toString(classPathResource.getInputStream(), StandardCharsets.UTF_8);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        converter.setVerifierKey(this.PUBLIC_KEY);
//        return converter;
//    }
//
//    @Bean
//    @Primary
//    public DefaultTokenServices tokenServices() {
//        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
//        defaultTokenServices.setTokenStore(tokenStore());
//        return defaultTokenServices;
//    }
//}
//
