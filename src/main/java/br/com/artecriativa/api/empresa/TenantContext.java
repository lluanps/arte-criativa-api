package br.com.artecriativa.api.empresa;

/**
 * Guarda o id da empresa (tenant) do usuário autenticado da requisição atual, numa
 * {@link ThreadLocal}. Setado por {@code JwtAuthFilter} logo após validar o JWT, e
 * <strong>sempre</strong> limpo em {@code finally} — como o servidor reusa threads entre
 * requisições, esquecer o {@link #clear()} vazaria o tenant de uma requisição pra próxima
 * que cair na mesma thread.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> EMPRESA_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(Long empresaId) {
        EMPRESA_ID.set(empresaId);
    }

    public static Long get() {
        return EMPRESA_ID.get();
    }

    public static void clear() {
        EMPRESA_ID.remove();
    }
}
