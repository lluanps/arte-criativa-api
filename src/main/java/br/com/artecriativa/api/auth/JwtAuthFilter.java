package br.com.artecriativa.api.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Lê o header {@code Authorization: Bearer <token>}, valida o JWT e — se válido — popula
 * o {@link SecurityContextHolder} com o e-mail do usuário como principal. Não bloqueia a
 * requisição se o token estiver ausente ou inválido: quem decide se a rota exige
 * autenticação é o {@link SecurityConfig}. Por enquanto nenhuma rota exige (Fase 5 monta
 * só a infra), mas o filtro já deixa tudo pronto pra quando isso for ligado.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIXO_BEARER = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        extrairToken(request)
                .flatMap(jwtService::validarEExtrairClaims)
                .ifPresent(this::autenticar);

        filterChain.doFilter(request, response);
    }

    private Optional<String> extrairToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(PREFIXO_BEARER)) {
            return Optional.of(header.substring(PREFIXO_BEARER.length()));
        }
        return Optional.empty();
    }

    private void autenticar(Claims claims) {
        var authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
