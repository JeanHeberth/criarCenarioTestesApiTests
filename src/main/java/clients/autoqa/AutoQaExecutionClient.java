package clients.autoqa;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.request.autoqa.AutoQaCancelRequest;
import models.request.autoqa.AutoQaCreateExecutionRequest;

import static constants.endpoints.Endpoint.AUTO_QA_EXECUTIONS;
import static constants.endpoints.Endpoint.AUTO_QA_EXECUTION_APPLY;
import static constants.endpoints.Endpoint.AUTO_QA_EXECUTION_APPLY_APPROVAL;
import static constants.endpoints.Endpoint.AUTO_QA_EXECUTION_BY_ID;
import static constants.endpoints.Endpoint.AUTO_QA_EXECUTION_CANCEL;
import static constants.endpoints.Endpoint.AUTO_QA_EXECUTION_CONTINUE;
import static constants.endpoints.Endpoint.AUTO_QA_EXECUTION_EXECUTE;
import static constants.endpoints.Endpoint.AUTO_QA_EXECUTION_EXECUTION_APPROVAL;
import static constants.endpoints.Endpoint.AUTO_QA_EXECUTION_GENERATE;
import static constants.endpoints.Endpoint.AUTO_QA_EXECUTION_START;
import static io.restassured.RestAssured.given;

public class AutoQaExecutionClient {

    public Response criar(AutoQaCreateExecutionRequest request) {
        return given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(AUTO_QA_EXECUTIONS.getUrl());
    }

    public Response listar() {
        return given()
                .when()
                .get(AUTO_QA_EXECUTIONS.getUrl());
    }

    public Response buscarPorId(String executionId) {
        return given()
                .when()
                .get(AUTO_QA_EXECUTION_BY_ID.comExecutionId(executionId));
    }

    public Response iniciar(String executionId) {
        return given()
                .when()
                .post(AUTO_QA_EXECUTION_START.comExecutionId(executionId));
    }

    public Response continuar(String executionId) {
        return given()
                .when()
                .post(AUTO_QA_EXECUTION_CONTINUE.comExecutionId(executionId));
    }

    public Response gerar(String executionId) {
        return given()
                .when()
                .post(AUTO_QA_EXECUTION_GENERATE.comExecutionId(executionId));
    }

    public Response aplicarAprovacao(String executionId, Object body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(AUTO_QA_EXECUTION_APPLY_APPROVAL.comExecutionId(executionId));
    }

    public Response aplicar(String executionId) {
        return given()
                .when()
                .post(AUTO_QA_EXECUTION_APPLY.comExecutionId(executionId));
    }

    public Response aprovarExecucao(String executionId, Object body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(AUTO_QA_EXECUTION_EXECUTION_APPROVAL.comExecutionId(executionId));
    }

    public Response executar(String executionId) {
        return given()
                .when()
                .post(AUTO_QA_EXECUTION_EXECUTE.comExecutionId(executionId));
    }

    public Response cancelar(String executionId, AutoQaCancelRequest body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(AUTO_QA_EXECUTION_CANCEL.comExecutionId(executionId));
    }
}
