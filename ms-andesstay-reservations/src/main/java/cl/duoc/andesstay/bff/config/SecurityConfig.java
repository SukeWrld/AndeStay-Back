package cl.duoc.andesstay.reservations.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitamos CSRF porque usamos JWT (stateless)
            .csrf(csrf -> csrf.disable())

            // No se crean sesiones (todo se valida con el token)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Configuración de autorización
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos (si los necesitas)
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()

                // Ejemplo de control por roles (opcional)
                // .requestMatchers(HttpMethod.DELETE, "/api/reservations/**").hasRole("ADMIN")
                // .requestMatchers(HttpMethod.PUT, "/api/reservations/**/status").hasAnyRole("ADMIN", "OPERADOR")

                // El resto requiere autenticación (JWT válido)
                .anyRequest().authenticated()
            )

            // Configuramos el Resource Server para validar JWT de Azure AD
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
            );

        return http.build();
    }
}