package cenario;

import java.util.UUID;

import clients.cenario.CenarioClient;
import factories.cenario.CenarioRequestFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.BaseTest;

import static org.hamcrest.Matchers.instanceOf;

public class CenarioTest extends BaseTest {

    private CenarioClient cenarioClient;

    // alwaysRun = true é obrigatório aqui: com <groups> filtrando os @Test
    // (ex.: suite smoke), o TestNG pula métodos @BeforeClass que não
    // pertençam a nenhum dos grupos incluídos, a menos que alwaysRun=true.
    @BeforeClass(alwaysRun = true)
    public void setupClient() {
        cenarioClient = new CenarioClient();
    }

    @Test(groups = "smoke", description = "GET /cenario deve responder 200 com uma lista (vazia ou não)")
    public void listarCenarios_deveRetornar200ComLista() {
        cenarioClient.listarCenarios()
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    /**
     * CenarioService#buscarCenario faz findById(id).orElse(null) — ou seja,
     * um id inexistente NÃO gera 404, apenas um corpo vazio/nulo com 200.
     * Documentamos o comportamento real da API aqui; se o time decidir que
     * o correto seria 404, este teste falhará de propósito ao ser corrigido.
     */
    @Test(groups = "smoke", description = "GET /cenario/{id} com id inexistente não deve retornar erro de servidor")
    public void buscarCenario_comIdInexistente_naoDeveRetornarErroDeServidor() {
        String idInexistente = UUID.randomUUID().toString();

        cenarioClient.buscarCenario(idInexistente)
                .then()
                .statusCode(200);
    }

    /**
     * CenarioService#excluirCenario delega direto a repository.deleteById,
     * que no Spring Data MongoDB é idempotente (não lança para id ausente).
     */
    @Test(groups = "smoke", description = "DELETE /cenario/{id} com id inexistente deve ser idempotente")
    public void excluirCenario_comIdInexistente_deveSerIdempotente() {
        String idInexistente = UUID.randomUUID().toString();

        cenarioClient.excluirCenario(idInexistente)
                .then()
                .statusCode(200);
    }

    @Test(groups = "e2e", description = "Deve gerar um cenário completo de teste via IA a partir de uma regra de negócio válida")
    public void gerarCenario_comDadosValidos_deveRetornarCenarioGerado() {
        cenarioClient.gerarCenario(CenarioRequestFactory.valido())
                .then()
                .statusCode(200)
                .body("id", org.hamcrest.Matchers.notNullValue())
                .body("cenarios", org.hamcrest.Matchers.not(org.hamcrest.Matchers.empty()));
    }
}
