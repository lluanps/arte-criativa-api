package br.com.artecriativa.api.financeiro;

import br.com.artecriativa.api.financeiro.dto.DashboardFinanceiroResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/financeiro")
@RequiredArgsConstructor
public class DashboardFinanceiroController {

    private final DashboardFinanceiroService dashboardFinanceiroService;

    /**
     * Resumo de fluxo de caixa. Sem {@code inicio}/{@code fim}, usa o mês corrente.
     */
    @GetMapping("/dashboard")
    public DashboardFinanceiroResponse dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return dashboardFinanceiroService.gerar(inicio, fim);
    }
}
