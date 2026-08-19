package br.com.artecriativa.api.common;

import java.util.List;

/**
 * Monta a lista legível de "o que está vinculado" usada nas mensagens de bloqueio de
 * exclusão (produto, matéria-prima, categoria, cliente, canal de venda) — em vez do
 * genérico "existem outros registros vinculados", diz exatamente quais e quantos.
 */
public final class MensagemVinculo {

    private MensagemVinculo() {
    }

    /** Adiciona "1 {singular}" ou "N {plural}" à lista, só se quantidade > 0. */
    public static void add(List<String> vinculos, long quantidade, String singular, String plural) {
        if (quantidade > 0) {
            vinculos.add(quantidade == 1 ? "1 " + singular : quantidade + " " + plural);
        }
    }

    /** "a" / "a, b" / "a, b e c" */
    public static String juntarComE(List<String> itens) {
        if (itens.isEmpty()) {
            return "";
        }
        if (itens.size() == 1) {
            return itens.get(0);
        }
        return String.join(", ", itens.subList(0, itens.size() - 1)) + " e " + itens.get(itens.size() - 1);
    }
}
