package agent;

import java.util.Collections;

import clients.agent.AgentClient;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.BaseTest;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;

public class AgentTest extends BaseTest {

    private AgentClient agentClient;

    // alwaysRun = true é obrigatório aqui: com <groups> filtrando os @Test
    // (ex.: suite smoke), o TestNG pula métodos @BeforeClass que não
    // pertençam a nenhum dos grupos incluídos, a menos que alwaysRun=true.
    @BeforeClass(alwaysRun = true)
    public void setupClient() {
        agentClient = new AgentClient();
    }

    @Test(groups = "smoke", description = "GET /api/agents deve responder 200 com a lista de agentes empacotados na aplicação")
    public void listarAgentes_deveRetornar200ComLista() {
        agentClient.listarAgentes()
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .body("size()", greaterThanOrEqualTo(0));
    }

    /**
     * AgentController#chatWithAgent usa @Valid, mas o ApiExceptionHandler
     * genérico deste projeto não trata MethodArgumentNotValidException de
     * forma explícita — apenas ResponseStatusException e Exception (500).
     * Diferente de AutoQaExecutionController, que tem um handler dedicado
     * garantindo 400. Aqui validamos apenas que um payload vazio nunca é
     * aceito como sucesso; o código exato (idealmente 400) deveria ser
     * fechado pelo time como contrato explícito.
     */
    @Test(groups = "smoke", description = "POST /api/agents/chat com corpo vazio nunca deve responder com sucesso")
    public void chat_comCorpoVazio_naoDeveResponderComSucesso() {
        agentClient.chat(Collections.emptyMap())
                .then()
                .statusCode(greaterThanOrEqualTo(400));
    }

    @Test(groups = "e2e", enabled = false,
            description = "Deve conversar com um agente existente e receber uma resposta gerada por IA")
    public void chat_comAgenteEMensagemValidos_deveRetornarResposta() {
        // TODO: preencher com um agentId válido (ver GET /api/agents) e uma
        // mensagem real antes de habilitar — depende de OPENAI_API_KEY/
        // GEMINI_API_KEY reais e dos nomes de campo exatos de AgentChatRequest.
        throw new org.testng.SkipException("Pendente: definir payload real de AgentChatRequest");
    }
}
