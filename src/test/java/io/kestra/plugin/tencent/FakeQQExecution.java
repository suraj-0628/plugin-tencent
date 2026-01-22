package io.kestra.plugin.notifications.qq;

import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.VoidOutput;
import io.kestra.core.runners.RunContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@SuperBuilder
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class FakeQQExecution extends QQTemplate {

    @Override
    public VoidOutput run(RunContext runContext) throws Exception {
        this.templateUri = Property.ofValue("qq-execution.peb");

        this.templateRenderMap = Property.ofValue(
            Map.of(
                // --- execution ---
                "execution", Map.of(
                    "id", "exec-123",
                    "state", Map.of("current", "FAILED"),
                    "url", "http://localhost/ui/executions/exec-123"
                ),

                // --- flow ---
                "flow", Map.of(
                    "id", "qq-test-flow",
                    "namespace", "io.kestra.tests"
                ),

                // --- required template fields ---
                "startDate", Instant.parse("2024-01-01T10:00:00Z"),
                "duration", Duration.ofSeconds(42),

                // --- optional fields ---
                "firstFailed", Map.of(
                    "taskId", "failing-task"
                ),
                "customMessage", "Test failure message"
            )
        );

        return super.run(runContext);
    }
}
