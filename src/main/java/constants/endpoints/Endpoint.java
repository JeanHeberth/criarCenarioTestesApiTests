package constants.endpoints;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Mapeia os endpoints expostos pela API criar-cenario-testes
 * (com.br.criarcenariotestes.controller.*), servindo de fonte única
 * de verdade para os clients de teste.
 */
@Getter
@RequiredArgsConstructor
public enum Endpoint {

    CENARIO("/cenario"),
    CENARIO_WORKFLOWS("/cenario/workflows"),
    CENARIO_COM_PDF("/cenario/com-pdf"),

    JIRA_ATTACHMENTS("/jira/tasks/{taskKey}/attachments"),
    JIRA_ATTACHMENT_DOWNLOAD("/jira/tasks/{taskKey}/attachments/{attachmentId}/download"),
    JIRA_ATTACHMENTS_DOWNLOAD_ALL("/jira/tasks/{taskKey}/attachments/download-all"),

    AGENTS("/api/agents"),
    AGENTS_CHAT("/api/agents/chat"),
    AGENTS_SESSIONS_CHAT("/api/agents/sessions/chat"),
    AGENTS_SESSION_BY_ID("/api/agents/sessions/{sessionId}"),

    AUTO_QA_EXECUTIONS("/api/auto-qa/executions"),
    AUTO_QA_EXECUTION_BY_ID("/api/auto-qa/executions/{executionId}"),
    AUTO_QA_EXECUTION_START("/api/auto-qa/executions/{executionId}/start"),
    AUTO_QA_EXECUTION_CONTINUE("/api/auto-qa/executions/{executionId}/continue"),
    AUTO_QA_EXECUTION_GENERATE("/api/auto-qa/executions/{executionId}/generate"),
    AUTO_QA_EXECUTION_APPLY_APPROVAL("/api/auto-qa/executions/{executionId}/apply-approval"),
    AUTO_QA_EXECUTION_APPLY("/api/auto-qa/executions/{executionId}/apply"),
    AUTO_QA_EXECUTION_EXECUTION_APPROVAL("/api/auto-qa/executions/{executionId}/execution-approval"),
    AUTO_QA_EXECUTION_EXECUTE("/api/auto-qa/executions/{executionId}/execute"),
    AUTO_QA_EXECUTION_CANCEL("/api/auto-qa/executions/{executionId}/cancel");

    private final String url;

    /** Para endpoints simples cujo path é apenas "{base}/{id}" (ex.: CENARIO). */
    public String byId(Object id) {
        return url + "/" + id;
    }

    public String comExecutionId(Object executionId) {
        return url.replace("{executionId}", String.valueOf(executionId));
    }

    public String comSessionId(Object sessionId) {
        return url.replace("{sessionId}", String.valueOf(sessionId));
    }

    public String comTaskKey(String taskKey) {
        return url.replace("{taskKey}", taskKey);
    }

    public String comTaskKeyEAttachment(String taskKey, String attachmentId) {
        return url.replace("{taskKey}", taskKey)
                .replace("{attachmentId}", attachmentId);
    }
}
