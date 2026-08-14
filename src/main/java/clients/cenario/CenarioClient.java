package clients.cenario;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.request.cenario.CenarioRequest;

import static constants.endpoints.Endpoint.CENARIO;
import static constants.endpoints.Endpoint.CENARIO_WORKFLOWS;
import static io.restassured.RestAssured.given;

public class CenarioClient {

    public Response gerarCenario(CenarioRequest request) {
        return given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(CENARIO.getUrl());
    }

    public Response listarWorkflows() {
        return given()
                .when()
                .get(CENARIO_WORKFLOWS.getUrl());
    }

    public Response listarCenarios() {
        return given()
                .when()
                .get(CENARIO.getUrl());
    }

    public Response buscarCenario(String id) {
        return given()
                .when()
                .get(CENARIO.byId(id));
    }

    public Response excluirCenario(String id) {
        return given()
                .when()
                .delete(CENARIO.byId(id));
    }
}
