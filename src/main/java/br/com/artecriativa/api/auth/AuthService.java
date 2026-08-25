package br.com.artecriativa.api.auth;

import br.com.artecriativa.api.auth.dto.AuthResponse;
import br.com.artecriativa.api.auth.dto.EsqueciSenhaRequest;
import br.com.artecriativa.api.auth.dto.LoginRequest;
import br.com.artecriativa.api.auth.dto.RedefinirSenhaRequest;
import br.com.artecriativa.api.auth.dto.RegisterRequest;
import br.com.artecriativa.api.email.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private static final long EXPIRACAO_RESET_HORAS = 1;

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final String frontendUrl;

    public AuthService(UsuarioRepository usuarioRepository,
                        PasswordResetTokenRepository passwordResetTokenRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        EmailService emailService,
                        @Value("${app.frontend-url}") String frontendUrl) {
        this.usuarioRepository = usuarioRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
    }

    /**
     * {@code empresaIdDoChamador} é sempre a empresa do usuário AUTENTICADO que está
     * chamando (vem do {@code TenantContext} da requisição, via {@code AuthController}) —
     * nunca de um campo em {@link RegisterRequest}. Não confiar em {@code empresaId} vindo
     * de corpo de requisição é a regra de isolamento mais básica de todas: se viesse do
     * request, qualquer chamada poderia criar usuário em empresa alheia.
     */
    @Transactional
    public AuthResponse registrar(RegisterRequest request, Long empresaIdDoChamador) {
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalStateException("Já existe um usuário cadastrado com esse e-mail");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setEmpresaId(empresaIdDoChamador);
        usuario = usuarioRepository.save(usuario);

        emailService.enviar(usuario.getEmail(), "Bem-vindo(a) à Arte Criativa",
                htmlBoasVindas(usuario.getNome()));

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

    /**
     * Sempre "sucede" do ponto de vista do caller, exista ou não o e-mail cadastrado —
     * não dar pista de quais e-mails têm conta. Se existir, gera um token de uso único
     * (expira em 1h) e dispara o e-mail com o link de redefinição.
     */
    @Transactional
    public void esqueciSenha(EsqueciSenhaRequest request) {
        String email = request.email().trim().toLowerCase();
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUsuario(usuario);
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setExpiraEm(Instant.now().plus(EXPIRACAO_RESET_HORAS, ChronoUnit.HOURS));
            passwordResetTokenRepository.save(resetToken);

            String link = frontendUrl + "/redefinir-senha?token=" + resetToken.getToken();
            emailService.enviar(usuario.getEmail(), "Redefinição de senha - Arte Criativa",
                    htmlRecuperacaoSenha(usuario.getNome(), link));
        });
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new IllegalStateException("Link de redefinição inválido ou expirado"));

        if (!resetToken.valido()) {
            throw new IllegalStateException("Link de redefinição inválido ou expirado");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        resetToken.setUsadoEm(Instant.now());
    }

    private AuthResponse montarResposta(Usuario usuario) {
        String token = jwtService.gerarToken(usuario);
        return new AuthResponse(token, usuario.getId(), usuario.getNome(), usuario.getEmail());
    }

    private String htmlBoasVindas(String nome) {
        return """
                <p>Olá, %s!</p>
                <p>Seu acesso ao sistema da <strong>Arte Criativa</strong> foi criado com sucesso.</p>
                <p>Qualquer dúvida, é só responder este e-mail.</p>
                """.formatted(nome);
    }

    private String htmlRecuperacaoSenha(String nome, String link) {
        return """
                <p>Olá, %s!</p>
                <p>Recebemos um pedido pra redefinir sua senha no sistema da Arte Criativa.</p>
                <p><a href="%s">Clique aqui pra criar uma senha nova</a> — o link expira em 1 hora.</p>
                <p>Se você não pediu isso, pode ignorar este e-mail.</p>
                """.formatted(nome, link);
    }
}
