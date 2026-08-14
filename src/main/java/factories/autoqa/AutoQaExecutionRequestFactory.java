package factories.autoqa;

import models.request.autoqa.AutoQaCreateExecutionRequest;

public final class AutoQaExecutionRequestFactory {

    private AutoQaExecutionRequestFactory() {
    }

    public static AutoQaCreateExecutionRequest vazio() {
        return AutoQaCreateExecutionRequest.builder().build();
    }

    public static AutoQaCreateExecutionRequest semScenario() {
        return AutoQaCreateExecutionRequest.builder()
                .projectPath("/tmp/projeto-exemplo")
                .build();
    }

    public static AutoQaCreateExecutionRequest semProjectPath(Object scenario) {
        return AutoQaCreateExecutionRequest.builder()
                .scenario(scenario)
                .build();
    }
}
