package zephyr;

import java.util.List;
import java.util.Map;

import clients.cenario.CenarioClient;
import clients.zephyr.ZephyrVerificationClient;
import config.Configuration;
import config.Environment;
import factories.cenario.CenarioRequestFactory;
import io.restassured.response.Response;
import models.request.cenario.CenarioRequest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.BaseTest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class ZephyrPublishingTest extends BaseTest {

    private CenarioClient cenarioClient;
    private ZephyrVerificationClient zephyrVerificationClient;

    // alwaysRun = true é obrigatório aqui: com <groups> filtrando os @Test
    // (ex.: suite smoke), o TestNG pula métodos @BeforeClass que não
    // pertençam a nenhum dos grupos incluídos, a menos que alwaysRun=true.
    @BeforeClass(alwaysRun = true)
    public void setupClients() {
        cenarioClient = new CenarioClient();
        zephyrVerificationClient = new ZephyrVerificationClient();
    }

    @Test(groups = "e2e",
            description = "Deve gerar um cenário real via IA, publicá-lo no Zephyr e confirmar, consultando a API oficial do Zephyr, que o caso de teste existe de verdade")
    public void gerarCenario_devePublicarCasosDeTesteReaisNoZephyr() {
        gerarEVerificarPublicacaoReal(CenarioRequestFactory.valido());
    }

    @Test(groups = "e2e",
            description = "Domínio diferente de login (carrinho de compras): prova que a publicação e a criação de pasta no Zephyr não ficam amarradas ao cenário de autenticação")
    public void gerarCenario_dominioDiferenteDeLogin_devePublicarCasosDeTesteReaisNoZephyr() {
        gerarEVerificarPublicacaoReal(CenarioRequestFactory.validoCarrinhoDeCompras());
    }

    // Issue real do board SCRUM do usuário (Automacao API) - ver
    // "Automacao JAVA do POST Usuario". Ajuste aqui se a issue for arquivada.
    private static final String JIRA_ISSUE_KEY_REAL = "SCRUM-29";

    @Test(groups = "e2e",
            description = "Quando o pedido informa jiraIssueKey, cada caso de teste publicado deve aparecer vinculado a essa issue na aba Traceability do Zephyr")
    public void gerarCenario_comJiraIssueKey_devePublicarCasosDeTesteVinculadosNoZephyr() {
        CenarioRequest request = CenarioRequestFactory.comIssueJira(JIRA_ISSUE_KEY_REAL);

        gerarEVerificarPublicacaoReal(request);
    }

    private void gerarEVerificarPublicacaoReal(CenarioRequest request) {
        String tokenZephyr = Environment.getZephyrApiToken();
        if (tokenZephyr == null || tokenZephyr.isBlank()) {
            fail("ZEPHYR_API_TOKEN não configurado neste projeto de testes - "
                    + "necessário para verificar direto na API do Zephyr.");
        }

        // 1) Aciona a geração real (IA) na API sob teste.
        Response cenarioResponse = cenarioClient.gerarCenario(request);
        cenarioResponse.then().statusCode(200);

        List<Map<String, Object>> cenarios = cenarioResponse.jsonPath().getList("cenarios");
        assertFalse(cenarios == null || cenarios.isEmpty(), "A geração deveria ter retornado ao menos 1 cenário");

        String padraoKey = Configuration.getZephyrTestCaseKeyPattern();

        // 2) Para cada cenário, confirma a publicação DE VERDADE no Zephyr -
        // não basta a nossa API dizer que publicou.
        for (Map<String, Object> cenario : cenarios) {
            String nome = String.valueOf(cenario.get("nome"));
            Object keyBruta = cenario.get("zephyrTestCaseKey");

            assertTrue(keyBruta != null && !String.valueOf(keyBruta).isBlank(),
                    "Cenário '" + nome + "' não recebeu zephyrTestCaseKey da nossa API");

            String testCaseKey = String.valueOf(keyBruta);
            assertTrue(testCaseKey.matches(padraoKey),
                    "zephyrTestCaseKey '" + testCaseKey + "' fora do padrão do Zephyr Scale (" + padraoKey + ")");

            Response zephyrResponse = zephyrVerificationClient.buscarCasoDeTeste(testCaseKey);
            zephyrResponse.then().statusCode(200);

            assertEquals(zephyrResponse.jsonPath().getString("key"), testCaseKey,
                    "Key retornada pelo Zephyr não bate com a key publicada");
            assertEquals(zephyrResponse.jsonPath().getString("name"), nome,
                    "Nome do caso de teste no Zephyr não bate com o cenário gerado");

            // 3) A pasta também precisa existir de verdade e ter o nome certo -
            // quando o item não define CenarioItem#pasta, ZephyrPublisherAgent
            // cai pro título do pedido (ver resolverFolderId).
            // getInt() devolve int primitivo: quando o caso de teste não tem
            // pasta, o unboxing de null estoura NPE antes do guard abaixo.
            // getObject(..., Integer.class) devolve null de verdade.
            Integer folderId = zephyrResponse.jsonPath().getObject("folder.id", Integer.class);
            if (folderId != null) {
                Response folderResponse = zephyrVerificationClient.buscarPasta(folderId);
                folderResponse.then().statusCode(200);
                assertEquals(folderResponse.jsonPath().getString("name"), request.getTitulo(),
                        "Nome da pasta no Zephyr não bate com o título do pedido");
            }

            // 4) Quando o pedido informou jiraIssueKey, o caso de teste precisa
            // estar vinculado de verdade a essa issue (aba Traceability).
            if (request.getJiraIssueKey() != null && !request.getJiraIssueKey().isBlank()) {
                Response linksResponse = zephyrVerificationClient.buscarLinks(testCaseKey);
                linksResponse.then().statusCode(200);

                List<Map<String, Object>> issuesLinkadas = linksResponse.jsonPath().getList("issues");
                assertFalse(issuesLinkadas == null || issuesLinkadas.isEmpty(),
                        "Caso de teste '" + testCaseKey + "' deveria estar vinculado à issue "
                                + request.getJiraIssueKey() + ", mas não tem nenhum link de issue no Zephyr");
            }
        }
    }
}
