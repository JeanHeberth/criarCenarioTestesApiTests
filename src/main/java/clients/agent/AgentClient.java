package clients.agent;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static constants.endpoints.Endpoint.AGENTS;
import static constants.endpoints.Endpoint.AGENTS_CHAT;
import static constants.endpoints.Endpoint.AGENTS_SESSIONS_CHAT;
import static constants.endpoints.Endpoint.AGENTS_SESSION_BY_ID;
import static io.restassured.RestAssured.given;

public class AgentClient {

    public Response listarAgentes() {
        return given()
                .when()
                .get(AGENTS.getUrl());
    }

    public Response chat(Object body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(AGENTS_CHAT.getUrl());
    }

    public Response chatComSessao(Object body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(AGENTS_SESSIONS_CHAT.getUrl());
    }

    public Response buscarHistoricoSessao(String sessionId) {
        return given()
                .when()
                .get(AGENTS_SESSION_BY_ID.comSessionId(sessionId));
    }

    public Response limparSessao(String sessionId) {
        return given()
                .when()
                .delete(AGENTS_SESSION_BY_ID.comSessionId(sessionId));
    }
}
