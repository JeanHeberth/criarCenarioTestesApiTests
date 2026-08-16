package clients.zephyr;

import config.Environment;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * Client que fala DIRETO com a API oficial do Zephyr Scale Cloud — não com
 * a API criar-cenario-testes. Existe só para a verificação e2e: confirmar,
 * na fonte, que o caso de teste que a nossa API diz ter publicado
 * (CenarioItem#zephyrTestCaseKey) realmente existe no Zephyr. Sem isso, um
 * teste que só olhasse a resposta da nossa própria API estaria confiando
 * "de teatro" nela, não verificando o efeito real no sistema externo.
 */
public class ZephyrVerificationClient {

    public Response buscarCasoDeTeste(String testCaseKey) {
        return given()
                .baseUri(Environment.getZephyrBaseUri())
                .auth().oauth2(Environment.getZephyrApiToken())
                .accept(ContentType.JSON)
                .when()
                .get("/testcases/{testCaseKey}", testCaseKey);
    }

    public Response buscarPasta(long folderId) {
        return given()
                .baseUri(Environment.getZephyrBaseUri())
                .auth().oauth2(Environment.getZephyrApiToken())
                .accept(ContentType.JSON)
                .when()
                .get("/folders/{folderId}", folderId);
    }

    public Response buscarLinks(String testCaseKey) {
        return given()
                .baseUri(Environment.getZephyrBaseUri())
                .auth().oauth2(Environment.getZephyrApiToken())
                .accept(ContentType.JSON)
                .when()
                .get("/testcases/{testCaseKey}/links", testCaseKey);
    }
}
