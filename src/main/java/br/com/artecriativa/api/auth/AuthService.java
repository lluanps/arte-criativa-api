package br.com.artecriativa.api.auth;

import br.com.artecriativa.api.auth.dto.AuthResponse;
import br.com.artecriativa.api.auth.dto.LoginRequest;
import br.com.artecriativa.api.auth.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse registrar(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalStateException("Já existe um usuário cadastrado com esse e-mail");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario = usuarioRepository.save(usuario);

        return montarResposta(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new IllegalStateException("E-mail ou senha inválidos"));

        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            // Mesma mensagem do "não encontrado" de propósito: não dar pista se o
            // e-mail existe ou não.
            throw new IllegalStateException("E-mail ou senha inválidos");
        }

        return montarResposta(usuario);
    }

    private AuthResponse montarResposta(Usuario usuario) {
        String token = jwtService.gerarToken(usuario);
        return new AuthResponse(token, usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
