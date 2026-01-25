package io.kestra.plugin.notifications.qq;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.VoidOutput;
import io.kestra.plugin.notifications.ExecutionInterface;
import io.kestra.plugin.notifications.services.ExecutionService;

import io.kestra.core.runners.RunContext;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@SuperBuilder
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Plugin(
    examples = {
        @Example(
            title = "Send a Tencent IM / QQ notification on failed executions",
            full = true,
            code = """
                id: failure_alert_qq
                namespace: company.team

                tasks:
                  - id: send_qq_alert
                    type: io.kestra.plugin.qq.QQExecution
                    url: "https://console.tim.qq.com/v4/openim/sendmsg?sdkappid=xxx&identifier=admin&usersig=xxx&random=9999&contenttype=json"
                    recipientIds:
                      - "QQ_USER_ID_1"
                      - "QQ_USER_ID_2"
                    executionId: "{{ trigger.executionId }}"
                    customMessage: "Flow {{ flow.id }} failed"

                triggers:
                  - id: failed_prod
                    type: io.kestra.plugin.core.trigger.Flow
                    conditions:
                      - type: io.kestra.plugin.core.condition.ExecutionStatus
                        in: [FAILED, WARNING]
                """
        )
    }
)
public class QQExecution extends QQTemplate implements ExecutionInterface {

    @Builder.Default
    private final Property<String> executionId =
        Property.ofExpression("{{ execution.id }}");

    private Property<String> customMessage;
    private Property<Map<String, Object>> customFields;

    @Override
    public VoidOutput run(RunContext runContext) throws Exception {
        this.templateUri = Property.ofValue("qq-execution.peb");
        this.templateRenderMap =
            Property.ofValue(ExecutionService.executionMap(runContext, this));
        return super.run(runContext);
    }
}
