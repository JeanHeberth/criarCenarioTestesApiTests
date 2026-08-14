package workflow;

import clients.cenario.CenarioClient;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.BaseTest;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * GET /cenario/workflows não depende de Mongo, IA ou Jira — é apenas o
 * enum WorkflowType espelhado em DTO. Por isso é 100% determinístico e
 * serve como o principal teste de contrato/smoke do pipeline.
 */
public class WorkflowTest extends BaseTest {

    private CenarioClient cenarioClient;

    // alwaysRun = true é obrigatório aqui: com <groups> filtrando os @Test
    // (ex.: suite smoke), o TestNG pula métodos @BeforeClass que não
    // pertençam a nenhum dos grupos incluídos, a menos que alwaysRun=true.
    @BeforeClass(alwaysRun = true)
    public void setupClient() {
        cenarioClient = new CenarioClient();
    }

    @Test(groups = "smoke", description = "Deve listar os 4 tipos de workflow na ordem declarada no enum")
    public void listarWorkflows_deveRetornarOsQuatroTipos() {
        cenarioClient.listarWorkflows()
                .then()
                .statusCode(200)
                .body("tipo", contains("COMPLETO", "RAPIDO", "REVISAO", "REGRESSAO"))
                .body("size()", equalTo(4));
    }

    @Test(groups = "smoke", description = "Workflow COMPLETO deve ter 6 agentes e tempo estimado de 3-5 minutos")
    public void listarWorkflows_completoDeveTerSeisAgentes() {
        cenarioClient.listarWorkflows()
                .then()
                .statusCode(200)
                .body("find { it.tipo == 'COMPLETO' }.quantidadeAgentes", equalTo(6))
                .body("find { it.tipo == 'COMPLETO' }.tempoEstimado", equalTo("3-5 minutos"))
                .body("find { it.tipo == 'COMPLETO' }.agentes", hasSize(6));
    }

    @Test(groups = "smoke", description = "Workflow RAPIDO deve ter 4 agentes e tempo estimado de 1-2 minutos")
    public void listarWorkflows_rapidoDeveTerQuatroAgentes() {
        cenarioClient.listarWorkflows()
                .then()
                .statusCode(200)
                .body("find { it.tipo == 'RAPIDO' }.quantidadeAgentes", equalTo(4))
                .body("find { it.tipo == 'RAPIDO' }.tempoEstimado", equalTo("1-2 minutos"))
                .body("find { it.tipo == 'RAPIDO' }.agentes", hasSize(4));
    }

    @Test(groups = "smoke", description = "Workflow REVISAO deve ter 2 agentes e tempo estimado de 30-60 segundos")
    public void listarWorkflows_revisaoDeveTerDoisAgentes() {
        cenarioClient.listarWorkflows()
                .then()
                .statusCode(200)
                .body("find { it.tipo == 'REVISAO' }.quantidadeAgentes", equalTo(2))
                .body("find { it.tipo == 'REVISAO' }.tempoEstimado", equalTo("30-60 segundos"))
                .body("find { it.tipo == 'REVISAO' }.agentes", hasSize(2));
    }

    @Test(groups = "smoke", description = "Workflow REGRESSAO deve ter 4 agentes e tempo estimado de 2-3 minutos")
    public void listarWorkflows_regressaoDeveTerQuatroAgentes() {
        cenarioClient.listarWorkflows()
                .then()
                .statusCode(200)
                .body("find { it.tipo == 'REGRESSAO' }.quantidadeAgentes", equalTo(4))
                .body("find { it.tipo == 'REGRESSAO' }.tempoEstimado", equalTo("2-3 minutos"))
                .body("find { it.tipo == 'REGRESSAO' }.agentes", hasSize(4));
    }

    @Test(groups = "smoke", description = "O endpoint não deve exigir nenhum header de autenticação")
    public void listarWorkflows_semAutenticacao_deveResponderComSucesso() {
        Response response = cenarioClient.listarWorkflows();
        response.then().statusCode(200);
    }
}
