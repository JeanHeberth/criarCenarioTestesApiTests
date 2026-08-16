package factories.cenario;

import models.request.cenario.CenarioRequest;

public final class CenarioRequestFactory {

    private CenarioRequestFactory() {
    }

    public static CenarioRequest valido() {
        return CenarioRequest.builder()
                .titulo("Login com credenciais válidas")
                .regraDeNegocio("O usuário deve conseguir autenticar informando e-mail e senha cadastrados.")
                .agent("gerador_cenarios_testes")
                .workflowType("RAPIDO")
                .build();
    }

    /**
     * Domínio diferente de login, de propósito — usado para provar que a
     * publicação no Zephyr (e a resolução de pasta por título) não fica
     * amarrada ao cenário de autenticação.
     */
    public static CenarioRequest validoCarrinhoDeCompras() {
        return CenarioRequest.builder()
                .titulo("Adicionar produto ao carrinho de compras")
                .regraDeNegocio("O usuário deve conseguir adicionar um produto disponível em estoque ao "
                        + "carrinho de compras, respeitando a quantidade máxima permitida por item e "
                        + "impedindo a adição de produtos sem estoque.")
                .agent("gerador_cenarios_testes")
                .workflowType("RAPIDO")
                .build();
    }

    /**
     * Mesmo cenário de carrinho de compras, mas vinculado a uma issue real
     * do Jira — cada caso de teste publicado no Zephyr deve aparecer na aba
     * "Traceability" ligado a essa issue.
     */
    public static CenarioRequest comIssueJira(String jiraIssueKey) {
        CenarioRequest request = validoCarrinhoDeCompras();
        request.setJiraIssueKey(jiraIssueKey);
        return request;
    }

    public static CenarioRequest semTitulo() {
        CenarioRequest request = valido();
        request.setTitulo(null);
        return request;
    }

    public static CenarioRequest semRegraDeNegocio() {
        CenarioRequest request = valido();
        request.setRegraDeNegocio(null);
        return request;
    }

    public static CenarioRequest comWorkflowInvalido() {
        CenarioRequest request = valido();
        request.setWorkflowType("WORKFLOW_QUE_NAO_EXISTE");
        return request;
    }
}
