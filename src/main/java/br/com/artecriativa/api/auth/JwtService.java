package br.com.artecriativa.api.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Emite e valida os JWT usados pra autenticação. O token carrega o id, nome e e-mail do
 * usuário como claims, mais {@code empresaId} — usado pelo {@code JwtAuthFilter} pra
 * popular o {@link br.com.artecriativa.api.empresa.TenantContext} da requisição. Não há
 * refresh token nessa primeira versão — ao expirar, o usuário só faz login de novo (e
 * tokens emitidos antes desta mudança, sem a claim, ficam inválidos pro tenant — usuário
 * só precisa logar de novo).
 */
@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoMinutos;

    public JwtService(@Value("${app.jwt.secret}") String segredo,
                       @Value("${app.jwt.expiration-minutes}") long expiracaoMinutos) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMinutos = expiracaoMinutos;
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("usuarioId", usuario.getId())
                .claim("nome", usuario.getNome())
                .claim("empresaId", usuario.getEmpresaId())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(expiracaoMinutos * 60)))
                .signWith(chave)
                .compact();
    }

    /**
     * Retorna as claims do token se ele for válido (assinatura e expiração ok), ou
     * {@link Optional#empty()} caso contrário — nunca lança exceção pro caller.
     */
    public Optional<Claims> validarEExtrairClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(chave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
