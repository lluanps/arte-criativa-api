package br.com.artecriativa.api.estoque;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Unidades de medida reconhecidas pra conversão automática entre a unidade cadastrada
 * numa {@link MateriaPrima} (texto livre, ex: "kg") e a unidade usada num item de
 * receita (ex: "g") — permite comprar em kg e escrever a ficha técnica em g sem o
 * usuário converter na mão.
 *
 * Importante: isso é só um utilitário interno, usado apenas quando um item de receita
 * escolhe uma unidade diferente da matéria-prima. {@code MateriaPrima.unidadeMedida}
 * continua sendo texto livre no banco e na API — não dá pra restringir isso a um enum
 * sem quebrar dados já cadastrados e o formulário de texto livre do front. Ver
 * {@link #deTexto} e {@link #converter}.
 */
public enum UnidadeMedida {

    GRAMA(Grandeza.MASSA, BigDecimal.ONE),
    QUILOGRAMA(Grandeza.MASSA, BigDecimal.valueOf(1000)),
    MILILITRO(Grandeza.VOLUME, BigDecimal.ONE),
    LITRO(Grandeza.VOLUME, BigDecimal.valueOf(1000)),
    CENTIMETRO(Grandeza.COMPRIMENTO, BigDecimal.ONE),
    METRO(Grandeza.COMPRIMENTO, BigDecimal.valueOf(100)),
    UNIDADE(Grandeza.CONTAGEM, BigDecimal.ONE);

    public enum Grandeza {
        MASSA, VOLUME, COMPRIMENTO, CONTAGEM
    }

    private final Grandeza grandeza;
    private final BigDecimal fatorParaBase;

    UnidadeMedida(Grandeza grandeza, BigDecimal fatorParaBase) {
        this.grandeza = grandeza;
        this.fatorParaBase = fatorParaBase;
    }

    private static final Pattern ACENTOS = Pattern.compile("\\p{M}");

    /**
     * Reconhece as grafias mais comuns (sigla, singular, plural, com/sem acento).
     * Retorna {@code null} quando o texto não é reconhecido — quem chama decide o que
     * fazer (ex: só falha se a conversão for realmente necessária).
     */
    public static UnidadeMedida deTexto(String texto) {
        if (texto == null) {
            return null;
        }
        String semAcento = ACENTOS.matcher(Normalizer.normalize(texto, Normalizer.Form.NFD)).replaceAll("");
        String chave = semAcento.trim().toLowerCase();
        return switch (chave) {
            case "g", "grama", "gramas" -> GRAMA;
            case "kg", "quilo", "quilos", "quilograma", "quilogramas" -> QUILOGRAMA;
            case "ml", "mililitro", "mililitros" -> MILILITRO;
            case "l", "lt", "litro", "litros" -> LITRO;
            case "cm", "centimetro", "centimetros" -> CENTIMETRO;
            case "m", "metro", "metros" -> METRO;
            case "un", "und", "unid", "unidade", "unidades", "pc", "peca", "pecas" -> UNIDADE;
            default -> null;
        };
    }

    /**
     * Converte {@code quantidade} da unidade {@code deTexto} pra unidade
     * {@code paraTexto} (ambos textos livres, ex: os cadastrados em matéria-prima).
     * Quando os dois textos são iguais (ignorando maiúsculas/acentos/espaços), devolve a
     * quantidade sem alteração — nem precisa reconhecer a unidade, então isso nunca
     * atrapalha uma receita já cadastrada com o mesmo texto dos dois lados. Só quando os
     * textos diferem é que exige reconhecer e converter, lançando erro claro se não der.
     */
    public static BigDecimal converter(BigDecimal quantidade, String deTexto, String paraTexto) {
        if (textosEquivalentes(deTexto, paraTexto)) {
            return quantidade;
        }
        UnidadeMedida origem = deTexto(deTexto);
        UnidadeMedida destino = deTexto(paraTexto);
        if (origem == null || destino == null) {
            String naoReconhecida = origem == null ? deTexto : paraTexto;
            throw new IllegalStateException(
                    ("Unidade '%s' não é reconhecida pra conversão automática. Use uma destas: "
                            + "g, kg, ml, l, cm, m ou un — ou repita a mesma unidade da matéria-prima "
                            + "pra não precisar converter.").formatted(naoReconhecida));
        }
        if (origem.grandeza != destino.grandeza) {
            throw new IllegalStateException(
                    "Não é possível converter '%s' para '%s': são grandezas diferentes (ex: massa x volume)."
                            .formatted(deTexto, paraTexto));
        }
        BigDecimal emUnidadeBase = quantidade.multiply(origem.fatorParaBase);
        return emUnidadeBase.divide(destino.fatorParaBase, 6, RoundingMode.HALF_UP);
    }

    private static boolean textosEquivalentes(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.trim().equalsIgnoreCase(b.trim());
    }
}
