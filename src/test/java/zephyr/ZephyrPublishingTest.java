package zephyr;

import java.util.List;
import java.util.Map;

import clients.cenario.CenarioClient;
import clients.zephyr.ZephyrVerificationClient;
import config.Configuration;
import config.Environment;
import factories.cenario.CenarioRequestFactory;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.BaseTest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Verificação e2e de ponta a ponta: gera um cenário real via IA
 * (POST /cenario, que aciona o pipeline BMAD incluindo o novo
 * ZephyrPublisherAgent) e, para cada item retornado, consulta a API OFICIAL
 * do Zephyr Scale (não a nossa API) para confirmar que o caso de teste
 * publicado existe de verdade e tem o nome esperado. Sem essa segunda
 * chamada independente, estaríamos só confiando na palavra da nossa própria
 * API — o que não prova nada sobre o sistema externo.
 *
 * Cria artefatos REAIS e permanentes no projeto Zephyr configurado (custa
 * tempo de IA + fica um caso de teste novo no board a cada execução), por
 * isso fica desabilitado por padrão. Para rodar de verdade:
 *   1. Na API sob teste: ZEPHYR_ENABLED=true, ZEPHYR_API_TOKEN,
 *      ZEPHYR_PROJECT_KEY configurados e apontando pro projeto certo.
 *   2. Neste projeto de testes: variável de ambiente ZEPHYR_API_TOKEN (ou
 *      -Dzephyr.apiToken=...) com um token válido do Zephyr Scale.
 *   3. Remover enabled = false abaixo.
 */
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

    @Test(groups = "e2e", enabled = false,
            description = "Deve gerar um cenário real via IA, publicá-lo no Zephyr e confirmar, consultando a API oficial do Zephyr, que o caso de teste existe de verdade")
    public void gerarCenario_devePublicarCasosDeTesteReaisNoZephyr() {
        String tokenZephyr = Environment.getZephyrApiToken();
        if (tokenZephyr == null || tokenZephyr.isBlank()) {
            fail("ZEPHYR_API_TOKEN não configurado neste projeto de testes - "
                    + "necessário para verificar direto na API do Zephyr.");
        }

        // 1) Aciona a geração real (IA) na API sob teste.
        Response cenarioResponse = cenarioClient.gerarCenario(CenarioRequestFactory.valido());
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
        }
    }
}
