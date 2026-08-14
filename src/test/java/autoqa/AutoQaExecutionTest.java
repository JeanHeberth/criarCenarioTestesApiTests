package autoqa;

import java.util.UUID;

import clients.autoqa.AutoQaExecutionClient;
import factories.autoqa.AutoQaExecutionRequestFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import base.BaseTest;

public class AutoQaExecutionTest extends BaseTest {

    private AutoQaExecutionClient autoQaExecutionClient;

    // alwaysRun = true é obrigatório aqui: com <groups> filtrando os @Test
    // (ex.: suite smoke), o TestNG pula métodos @BeforeClass que não
    // pertençam a nenhum dos grupos incluídos, a menos que alwaysRun=true.
    @BeforeClass(alwaysRun = true)
    public void setupClient() {
        autoQaExecutionClient = new AutoQaExecutionClient();
    }

    @Test(groups = "smoke", description = "POST sem scenario nem projectPath deve ser rejeitado por bean validation (400)")
    public void criar_comCorpoVazio_deveRetornar400() {
        autoQaExecutionClient.criar(AutoQaExecutionRequestFactory.vazio())
                .then()
                .statusCode(400);
    }

    @Test(groups = "smoke", description = "POST sem scenario deve ser rejeitado por bean validation (400)")
    public void criar_semScenario_deveRetornar400() {
        autoQaExecutionClient.criar(AutoQaExecutionRequestFactory.semScenario())
                .then()
                .statusCode(400);
    }

    @Test(groups = "smoke", description = "GET /api/auto-qa/executions deve responder 200 com a listagem paginada")
    public void listar_deveRetornar200() {
        autoQaExecutionClient.listar()
                .then()
                .statusCode(200);
    }

    @Test(groups = "smoke", description = "GET por executionId inexistente deve responder 404 (AutoQaExecutionNotFoundException)")
    public void buscarPorId_comIdInexistente_deveRetornar404() {
        autoQaExecutionClient.buscarPorId(UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @DataProvider(name = "transicoesDeExecucaoInexistente")
    public Object[][] transicoesDeExecucaoInexistente() {
        return new Object[][]{
                {"start"},
                {"continue"},
                {"generate"}
        };
    }

    @Test(groups = "smoke", dataProvider = "transicoesDeExecucaoInexistente",
            description = "Transições de ciclo de vida sobre um executionId inexistente devem responder 404")
    public void transicaoDeExecucao_comIdInexistente_deveRetornar404(String transicao) {
        String idInexistente = UUID.randomUUID().toString();

        var response = switch (transicao) {
            case "start" -> autoQaExecutionClient.iniciar(idInexistente);
            case "continue" -> autoQaExecutionClient.continuar(idInexistente);
            case "generate" -> autoQaExecutionClient.gerar(idInexistente);
            default -> throw new IllegalArgumentException("Transição desconhecida: " + transicao);
        };

        response.then().statusCode(404);
    }

    /**
     * Diferente de start/continue/generate, /execute é comprovadamente
     * (verificado em execução real) bloqueado pela flag
     * auto-qa.allow-command-execution ANTES de checar se o executionId
     * existe — por isso responde 403, não 404, mesmo para um id
     * inexistente. Contrato de ambiente, não bug do teste: se a flag for
     * habilitada em produção, este teste deve ser revisto.
     */
    @Test(groups = "smoke", description = "POST /execute com id inexistente responde 403 quando allow-command-execution=false")
    public void executar_comIdInexistenteEExecucaoDesabilitada_deveRetornar403() {
        autoQaExecutionClient.executar(UUID.randomUUID().toString())
                .then()
                .statusCode(403);
    }

    @Test(groups = "smoke", description = "GET com executionId em formato inválido (não-UUID) não deve retornar sucesso")
    public void buscarPorId_comIdEmFormatoInvalido_naoDeveResponderComSucesso() {
        autoQaExecutionClient.buscarPorId("id-que-nao-e-um-uuid")
                .then()
                .statusCode(org.hamcrest.Matchers.greaterThanOrEqualTo(400));
    }

    @Test(groups = "e2e", enabled = false, description = "Deve criar, iniciar e evoluir uma execução Auto QA real de ponta a ponta")
    public void cicloDeVidaCompleto_comProjetoValido_deveConcluirComSucesso() {
        // Fluxo completo depende do shape real de "scenario" (não exposto
        // publicamente pelos DTOs) e de um projectPath dentro de
        // auto-qa.allowed-roots configurado na API — habilitar quando o
        // ambiente de teste tiver esses pré-requisitos definidos.
        throw new org.testng.SkipException("Pendente: definir payload real de AutoQaCreateExecutionRequest#scenario");
    }
}
