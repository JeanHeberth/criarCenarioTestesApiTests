package factories.cenario;

import models.request.cenario.CenarioRequest;

public final class CenarioRequestFactory {

    private CenarioRequestFactory() {
    }

    public static CenarioRequest valido() {
        return CenarioRequest.builder()
                .titulo("Login com credenciais válidas")
                .regraDeNegocio("O usuário deve conseguir autenticar informando e-mail e senha cadastrados.")
                .workflowType("RAPIDO")
                .build();
    }

    public static CenarioRequest semTitulo() {
        CenarioRequest request = valido();
        request.setTitulo(null);
        return request;
    }

    public static CenarioRequest semRegraDeNegocio() {
        CenarioRequest request = valido();
        request.setRegraDeNegocio(null);
        return request;
    }

    public static CenarioRequest comWorkflowInvalido() {
        CenarioRequest request = valido();
        request.setWorkflowType("WORKFLOW_QUE_NAO_EXISTE");
        return request;
    }
}
