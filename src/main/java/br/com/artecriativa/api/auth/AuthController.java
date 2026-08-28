package br.com.artecriativa.api.auth;

import br.com.artecriativa.api.auth.dto.AuthResponse;
import br.com.artecriativa.api.auth.dto.EsqueciSenhaRequest;
import br.com.artecriativa.api.auth.dto.LoginRequest;
import br.com.artecriativa.api.auth.dto.RedefinirSenhaRequest;
import br.com.artecriativa.api.auth.dto.RegisterRequest;
import br.com.artecriativa.api.auth.dto.RegistroEmpresaRequest;
import br.com.artecriativa.api.empresa.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Exige usuário autenticado — ver {@link SecurityConfig}: não é mais aberto ao
     * público. O novo usuário sempre nasce na mesma empresa de quem chama (
     * {@link TenantContext#get()}) — ver o Javadoc de {@code AuthService.registrar}.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registrar(@Valid @RequestBody RegisterRequest request) {
        return authService.registrar(request, TenantContext.get());
    }

    /**
     * Público de propósito (ver {@link SecurityConfig}) — é a única forma de nascer uma
     * {@link br.com.artecriativa.api.empresa.Empresa} nova sem SQL direto: cria a empresa
     * e o primeiro usuário dela juntos, e já devolve o token (login automático).
     */
    @PostMapping("/registrar-empresa")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registrarEmpresa(@Valid @RequestBody RegistroEmpresaRequest request) {
        return authService.registrarEmpresa(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/esqueci-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void esqueciSenha(@Valid @RequestBody EsqueciSenhaRequest request) {
        authService.esqueciSenha(request);
    }

    @PostMapping("/redefinir-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
        authService.redefinirSenha(request);
    }
}
