package io.kestra.plugin.notifications.qq;

import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.HttpResponse;
import io.kestra.core.http.client.HttpClient;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.VoidOutput;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.JacksonMapper;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.io.IOUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuperBuilder
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class QQTemplate extends AbstractQQConnection {

    protected Property<String> url;
    protected Property<String> token;
    protected Property<List<String>> recipientIds;
    protected Property<String> templateUri;
    protected Property<Map<String, Object>> templateRenderMap;

    @Override
    public VoidOutput run(RunContext runContext) throws Exception {
        String endpoint = runContext.render(url).as(String.class).orElseThrow();

        String template = IOUtils.toString(
            Objects.requireNonNull(
                getClass().getClassLoader()
                    .getResourceAsStream(
                        runContext.render(templateUri).as(String.class).orElseThrow()
                    )
            ),
            StandardCharsets.UTF_8
        );

        Map<String, Object> variables =
            runContext.render(templateRenderMap).asMap(String.class, Object.class);

        String messageText = runContext.render(template, variables);

        try (HttpClient client =
                 new HttpClient(runContext, httpClientConfigurationWithOptions())) {

            for (String recipient :
                runContext.render(recipientIds).asList(String.class)) {

                Map<String, Object> payload = Map.of(
                    "To_Account", recipient,
                    "MsgRandom", new Random().nextInt(Integer.MAX_VALUE),
                    "MsgBody", List.of(
                        Map.of(
                            "MsgType", "TIMTextElem",
                            "MsgContent", Map.of("Text", messageText)
                        )
                    )
                );

                HttpRequest request = createRequestBuilder(runContext)
                    .uri(URI.create(endpoint))
                    .method("POST")
                    .addHeader("Content-Type", "application/json")
                    .body(HttpRequest.StringRequestBody.builder()
                        .content(JacksonMapper.ofJson().writeValueAsString(payload))
                        .build())
                    .build();

                HttpResponse<String> response =
                    client.request(request, String.class);

                if (response.getStatus().getCode() != 200) {
                    throw new IllegalStateException(response.getBody());
                }
            }
        }
        return null;
    }
}
