package io.kestra.plugin.notifications.qq;

import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.HttpResponse;
import io.kestra.core.http.client.HttpClient;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.VoidOutput;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.net.URI;

@SuperBuilder
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Schema(
    title = "Send a Tencent IM / QQ message using an Incoming Webhook",
    description = """
        Sends a direct message to Tencent Cloud IM / QQ using a webhook-style REST endpoint.
        Intended for use in `errors` tasks or custom notification logic.
        """
)
@Plugin(
    examples = {
        @Example(
            title = "Send a QQ notification on flow failure",
            full = true,
            code = """
                id: unreliable_flow
                namespace: company.team

                tasks:
                  - id: fail
                    type: io.kestra.plugin.scripts.shell.Commands
                    runner: PROCESS
                    commands:
                      - exit 1

                errors:
                  - id: qq_alert
                    type: io.kestra.plugin.notifications.qq.QQIncomingWebhook
                    url: "https://im.tencentcloudapi.com/v4/openim/sendmsg"
                    token: "{{ secret('TENCENT_IM_TOKEN') }}"
                    payload: |
                      {
                        "recipients": ["100001"],
                        "message": "Flow {{ flow.id }} failed with execution {{ execution.id }}"
                      }
                """
        )
    },
    aliases = "io.kestra.plugin.notifications.qq.QQIncomingWebhook"
)
public class QQIncomingWebhook extends AbstractQQConnection {

    @Schema(
        title = "Tencent IM REST API endpoint",
        description = "Tencent Cloud IM REST endpoint used to send the message"
    )
    @PluginProperty(dynamic = true)
    @NotBlank
    protected String url;

    @Schema(
        title = "Tencent IM authentication token",
        description = "Authentication token or signature for Tencent IM API"
    )
    protected Property<String> token;

    @Schema(
        title = "Request payload",
        description = "Raw JSON payload sent to the Tencent IM API"
    )
    protected Property<String> payload;

    @Override
    public VoidOutput run(RunContext runContext) throws Exception {
        String rUrl = runContext.render(this.url);
        String rPayload = runContext.render(this.payload)
            .as(String.class)
            .orElseThrow();

        try (HttpClient client =
                 new HttpClient(runContext, httpClientConfigurationWithOptions())) {

            HttpRequest.HttpRequestBuilder requestBuilder =
                createRequestBuilder(runContext)
                    .uri(URI.create(rUrl))
                    .method("POST")
                    .addHeader("Content-Type", "application/json");

            runContext.render(token)
                .as(String.class)
                .ifPresent(t ->
                    requestBuilder.addHeader("Authorization", "Bearer " + t)
                );

            HttpRequest request = requestBuilder
                .body(HttpRequest.StringRequestBody.builder()
                    .content(rPayload)
                    .build())
                .build();

            HttpResponse<String> response =
                client.request(request, String.class);

            if (response.getStatus().getCode() != 200) {
                throw new IllegalStateException(
                    "Tencent IM webhook request failed: " + response.getBody()
                );
            }

            runContext.logger().info("QQ IncomingWebhook message sent successfully");
        }

        return null;
    }
}
