package clients.jira;

import io.restassured.response.Response;

import static constants.endpoints.Endpoint.JIRA_ATTACHMENTS;
import static constants.endpoints.Endpoint.JIRA_ATTACHMENTS_DOWNLOAD_ALL;
import static constants.endpoints.Endpoint.JIRA_ATTACHMENT_DOWNLOAD;
import static io.restassured.RestAssured.given;

public class JiraApiClient {

    public Response listarAnexos(String taskKey) {
        return given()
                .when()
                .get(JIRA_ATTACHMENTS.comTaskKey(taskKey));
    }

    public Response baixarAnexo(String taskKey, String attachmentId) {
        return given()
                .when()
                .get(JIRA_ATTACHMENT_DOWNLOAD.comTaskKeyEAttachment(taskKey, attachmentId));
    }

    public Response baixarTodosAnexos(String taskKey) {
        return given()
                .when()
                .get(JIRA_ATTACHMENTS_DOWNLOAD_ALL.comTaskKey(taskKey));
    }
}
