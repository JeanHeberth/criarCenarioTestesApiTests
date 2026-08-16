package models.request.cenario;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Espelha com.br.criarcenariotestes.business.dto.CenarioRequest.
 * workflowType aceita: COMPLETO, RAPIDO, REVISAO, REGRESSAO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CenarioRequest {

    private String titulo;
    private String regraDeNegocio;
    private String agent;
    private String workflowType;
    // Opcional: quando informada, ZephyrPublisherAgent vincula cada caso de
    // teste criado a essa issue do Jira (aba "Traceability" no Zephyr).
    private String jiraIssueKey;
}
