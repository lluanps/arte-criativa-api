package br.com.artecriativa.api.auth;

import br.com.artecriativa.api.auth.dto.LoginRequest;
import br.com.artecriativa.api.auth.dto.RegistroEmpresaRequest;
import br.com.artecriativa.api.email.EmailService;
import br.com.artecriativa.api.empresa.Empresa;
import br.com.artecriativa.api.empresa.EmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Sem {@code @InjectMocks} de propósito: {@link AuthService} tem um {@code String}
 * (frontendUrl) no construtor, que o Mockito não sabe preencher sozinho — construído na
 * mão em {@link #montarService()}.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private EmailService emailService;

    private AuthService service;

    @BeforeEach
    void montarService() {
        service = new AuthService(usuarioRepository, empresaRepository, passwordResetTokenRepository,
                passwordEncoder, jwtService, emailService, "http://localhost:3000");
    }

    private static Empresa empresaExistente(Long id, boolean ativa) {
        Empresa empresa = new Empresa();
        empresa.setId(id);
        empresa.setNome("Empresa Teste");
        empresa.setAtiva(ativa);
        return empresa;
    }

    private static Usuario usuarioExistente(Long id, Long empresaId, String senhaHash) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Fulano");
        usuario.setEmail("fulano@example.com");
        usuario.setSenhaHash(senhaHash);
        usuario.setEmpresaId(empresaId);
        return usuario;
    }

    @Test
    void registrarEmpresa_criaEmpresaEUsuarioVinculados() {
        when(usuarioRepository.existsByEmail("nova@example.com")).thenReturn(false);
        when(empresaRepository.save(any())).thenAnswer(inv -> {
            Empresa empresa = inv.getArgument(0);
            empresa.setId(42L);
            return empresa;
        });
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("hash-fake");
        when(jwtService.gerarToken(any())).thenReturn("token-fake");

        RegistroEmpresaRequest request = new RegistroEmpresaRequest(
                "Empresa Nova", "Fulano", "Nova@Example.com", "senha12345");

        service.registrarEmpresa(request);

        ArgumentCaptor<Empresa> empresaCaptor = ArgumentCaptor.forClass(Empresa.class);
        verify(empresaRepository).save(empresaCaptor.capture());
        assertThat(empresaCaptor.getValue().getNome()).isEqualTo("Empresa Nova");

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario usuarioSalvo = usuarioCaptor.getValue();
        // e-mail sempre normalizado (minúsculo/sem espaço), mesma regra de login/registrar.
        assertThat(usuarioSalvo.getEmail()).isEqualTo("nova@example.com");
        assertThat(usuarioSalvo.getEmpresaId()).isEqualTo(42L);
    }

    @Test
    void registrarEmpresa_emailJaCadastrado_lancaExcecaoENaoCriaNada() {
        when(usuarioRepository.existsByEmail("ja-existe@example.com")).thenReturn(true);

        RegistroEmpresaRequest request = new RegistroEmpresaRequest(
                "Empresa Nova", "Fulano", "ja-existe@example.com", "senha12345");

        assertThatThrownBy(() -> service.registrarEmpresa(request))
                .isInstanceOf(IllegalStateException.class);

        verify(empresaRepository, never()).save(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void login_empresaInativa_bloqueiaComMensagemClara() {
        Usuario usuario = usuarioExistente(1L, 42L, "hash-correta");
        when(usuarioRepository.findByEmail("fulano@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha-certa", "hash-correta")).thenReturn(true);
        when(empresaRepository.findById(42L)).thenReturn(Optional.of(empresaExistente(42L, false)));

        assertThatThrownBy(() -> service.login(new LoginRequest("fulano@example.com", "senha-certa")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("suspensa");

        verify(jwtService, never()).gerarToken(any());
    }

    @Test
    void login_empresaAtiva_geraToken() {
        Usuario usuario = usuarioExistente(1L, 42L, "hash-correta");
        when(usuarioRepository.findByEmail("fulano@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha-certa", "hash-correta")).thenReturn(true);
        when(empresaRepository.findById(42L)).thenReturn(Optional.of(empresaExistente(42L, true)));
        when(jwtService.gerarToken(usuario)).thenReturn("token-fake");

        var resposta = service.login(new LoginRequest("fulano@example.com", "senha-certa"));

        assertThat(resposta.token()).isEqualTo("token-fake");
    }
}
