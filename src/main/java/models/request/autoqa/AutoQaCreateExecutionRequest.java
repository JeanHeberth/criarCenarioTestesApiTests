package models.request.autoqa;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Espelha AutoQaCreateExecutionRequest (scenario + projectPath).
 * "scenario" é mantido como Object porque seu shape completo não é
 * exposto publicamente pela API — os testes de contrato só precisam
 * garantir presença/ausência do campo, não seu conteúdo interno.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AutoQaCreateExecutionRequest {

    private Object scenario;
    private String projectPath;
}
