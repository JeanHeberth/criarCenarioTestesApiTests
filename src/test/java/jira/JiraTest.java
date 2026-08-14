package jira;

import clients.jira.JiraApiClient;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import base.BaseTest;
import config.Configuration;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * JiraController valida o taskKey via @Pattern + @Validated ANTES de chamar
 * o JiraService — logo essas violações nunca dependem de credenciais reais
 * do Jira nem fazem chamada de rede externa, o que as torna seguras para a
 * suite smoke.
 *
 * Observação de contrato: o ApiExceptionHandler genérico deste projeto não
 * possui um @ExceptionHandler dedicado para violações de @Validated em
 * @PathVariable, então o status exato (400 esperado pelo Spring, mas pode
 * cair no handler genérico de Exception.class -> 500) não está garantido
 * apenas pela leitura do código. Por isso validamos aqui que a chamada
 * jamais "passa" (nunca é 2xx), sem fixar um código específico — e
 * recomendamos ao time confirmar/fechar esse contrato explicitamente.
 */
public class JiraTest extends BaseTest {

    private JiraApiClient jiraApiClient;

    // alwaysRun = true é obrigatório aqui: com <groups> filtrando os @Test
    // (ex.: suite smoke), o TestNG pula métodos @BeforeClass que não
    // pertençam a nenhum dos grupos incluídos, a menos que alwaysRun=true.
    @BeforeClass(alwaysRun = true)
    public void setupClient() {
        jiraApiClient = new JiraApiClient();
    }

    @DataProvider(name = "taskKeysInvalidas")
    public Object[][] taskKeysInvalidas() {
        return new Object[][]{
                {Configuration.getJiraTaskKeyInvalido()},
                {"semhifen"},
                {"1-EX-OP"},
                {"ex-op-1122"},
                {""}
        };
    }

    @Test(groups = "smoke", dataProvider = "taskKeysInvalidas",
            description = "GET de anexos com taskKey fora do padrão nunca deve responder com sucesso")
    public void listarAnexos_comTaskKeyForaDoPadrao_naoDeveResponderComSucesso(String taskKeyInvalida) {
        jiraApiClient.listarAnexos(taskKeyInvalida)
                .then()
                .statusCode(greaterThanOrEqualTo(400));
    }

    @Test(groups = "smoke", description = "Download de anexo com taskKey fora do padrão nunca deve responder com sucesso")
    public void baixarAnexo_comTaskKeyForaDoPadrao_naoDeveResponderComSucesso() {
        jiraApiClient.baixarAnexo(Configuration.getJiraTaskKeyInvalido(), "1")
                .then()
                .statusCode(greaterThanOrEqualTo(400));
    }

    @Test(groups = "smoke", description = "Download de todos os anexos com taskKey fora do padrão nunca deve responder com sucesso")
    public void baixarTodosAnexos_comTaskKeyForaDoPadrao_naoDeveResponderComSucesso() {
        jiraApiClient.baixarTodosAnexos(Configuration.getJiraTaskKeyInvalido())
                .then()
                .statusCode(greaterThanOrEqualTo(400));
    }

    @Test(groups = "e2e", description = "Deve listar anexos de uma issue real do Jira (requer credenciais configuradas na API)")
    public void listarAnexos_comTaskKeyValido_deveRetornar200() {
        jiraApiClient.listarAnexos(Configuration.getJiraTaskKeyValido())
                .then()
                .statusCode(200);
    }
}
