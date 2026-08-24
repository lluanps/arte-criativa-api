package br.com.artecriativa.api.producao.dto;

import br.com.artecriativa.api.estoque.MateriaPrima;
import br.com.artecriativa.api.estoque.Produto;
import br.com.artecriativa.api.producao.Receita;
import br.com.artecriativa.api.producao.ReceitaItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cobre "quanto dá pra produzir com o estoque atual" — o mínimo entre
 * {@code unidadesProduziveisComEsteItem} de cada item da receita é o que de fato limita
 * a produção (a matéria-prima mais escassa é quem manda), sempre arredondado pra baixo.
 */
class ReceitaResponseTest {

    private static MateriaPrima materiaPrima(String nome, BigDecimal estoque, String unidade) {
        MateriaPrima materiaPrima = new MateriaPrima();
        materiaPrima.setNome(nome);
        materiaPrima.setUnidadeMedida(unidade);
        materiaPrima.setEstoqueAtual(estoque);
        materiaPrima.setCustoUnitario(new BigDecimal("1.00"));
        return materiaPrima;
    }

    private static ReceitaItem item(MateriaPrima materiaPrima, BigDecimal quantidade) {
        ReceitaItem item = new ReceitaItem();
        item.setMateriaPrima(materiaPrima);
        item.setQuantidade(quantidade);
        item.setUnidadeMedida(materiaPrima.getUnidadeMedida());
        return item;
    }

    private static Receita receita(BigDecimal rendimento, ReceitaItem... itens) {
        Produto produto = new Produto();
        produto.setNome("Produto teste");
        produto.setPrecoVenda(new BigDecimal("10.00"));

        Receita receita = new Receita();
        receita.setProduto(produto);
        receita.setNome("Receita teste");
        receita.setRendimento(rendimento);
        receita.setCustoMaoDeObra(BigDecimal.ZERO);
        receita.setCustoEmbalagemOutros(BigDecimal.ZERO);
        receita.substituirItens(List.of(itens));
        return receita;
    }

    @Test
    void oItemComMenosEstoqueRelativoLimitaAProducaoDaReceitaInteira() {
        MateriaPrima cera = materiaPrima("Cera de soja", new BigDecimal("10"), "kg"); // 2kg/un -> 5 unidades
        MateriaPrima pavio = materiaPrima("Pavio", new BigDecimal("3"), "un"); // 1un/un -> 3 unidades

        Receita receita = receita(BigDecimal.ONE,
                item(cera, new BigDecimal("2")),
                item(pavio, new BigDecimal("1")));

        ReceitaResponse response = ReceitaResponse.de(receita);

        assertThat(response.itens().get(0).unidadesProduziveisComEsteItem()).isEqualTo(5L);
        assertThat(response.itens().get(1).unidadesProduziveisComEsteItem()).isEqualTo(3L);
        assertThat(response.quantidadeProduzivelComEstoqueAtual()).isEqualTo(3L);
        assertThat(response.materiaPrimaLimitanteNome()).isEqualTo("Pavio");
    }

    @Test
    void arredondaParaBaixoQuandoSobraFracaoDeUnidade() {
        MateriaPrima cera = materiaPrima("Cera de soja", new BigDecimal("7"), "kg");
        Receita receita = receita(BigDecimal.ONE, item(cera, new BigDecimal("2")));

        ReceitaResponse response = ReceitaResponse.de(receita);

        // 7kg / 2kg por unidade = 3,5 -> arredonda pra baixo
        assertThat(response.quantidadeProduzivelComEstoqueAtual()).isEqualTo(3L);
    }

    @Test
    void consideraRendimentoMaiorQueUmNoConsumoPorUnidade() {
        MateriaPrima cera = materiaPrima("Cera de soja", new BigDecimal("7"), "kg");
        // rendimento 5 (lote produz 5 unidades), consumindo 10kg no lote inteiro -> 2kg/unidade
        Receita receita = receita(new BigDecimal("5"), item(cera, new BigDecimal("10")));

        ReceitaResponse response = ReceitaResponse.de(receita);

        assertThat(response.quantidadeProduzivelComEstoqueAtual()).isEqualTo(3L);
    }

    @Test
    void estoqueZerado_naoDaPraProduzirNenhumaUnidade() {
        MateriaPrima cera = materiaPrima("Cera de soja", BigDecimal.ZERO, "kg");
        Receita receita = receita(BigDecimal.ONE, item(cera, new BigDecimal("2")));

        ReceitaResponse response = ReceitaResponse.de(receita);

        assertThat(response.quantidadeProduzivelComEstoqueAtual()).isEqualTo(0L);
    }
}
