package br.com.artecriativa.api.common;

import java.math.BigDecimal;

/**
 * Formata quantidades (BigDecimal) pra exibição em mensagens, sem zeros decimais
 * desnecessários. Sem isso, um estoque de "5" (armazenado como NUMERIC(12,3)) aparece
 * em mensagens de erro como "5.000" — dando a falsa impressão de que o valor real é
 * diferente do mostrado no restante da tela (onde o JSON perde os zeros de escala).
 */
public final class FormatoNumerico {

    private FormatoNumerico() {
    }

    public static String semZerosDesnecessarios(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        BigDecimal semZeros = valor.stripTrailingZeros();
        // stripTrailingZeros pode devolver escala negativa (ex: 2E+2 pra 200), o que
        // faria toPlainString virar notação estranha; normaliza de volta pra escala 0.
        if (semZeros.scale() < 0) {
            semZeros = semZeros.setScale(0);
        }
        return semZeros.toPlainString();
    }
}
