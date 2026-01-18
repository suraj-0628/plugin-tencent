package io.kestra.plugin.notifications.qq;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.client.configurations.HttpConfiguration;
import io.kestra.core.http.client.configurations.TimeoutConfiguration;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.models.tasks.VoidOutput;
import io.kestra.core.runners.RunContext;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@SuperBuilder
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class AbstractQQConnection extends Task implements RunnableTask<VoidOutput> {

    @PluginProperty(dynamic = true)
    protected RequestOptions options;

    protected HttpConfiguration httpClientConfigurationWithOptions()
        throws IllegalVariableEvaluationException {

        HttpConfiguration.HttpConfigurationBuilder builder =
            HttpConfiguration.builder()
                .defaultCharset(Property.ofValue(StandardCharsets.UTF_8));

        if (options != null) {
            builder.timeout(
                TimeoutConfiguration.builder()
                    .connectTimeout(options.getConnectTimeout())
                    .readIdleTimeout(options.getReadIdleTimeout())
                    .build()
            );
        }

        return builder.build();
    }

    protected HttpRequest.HttpRequestBuilder createRequestBuilder(RunContext runContext)
        throws IllegalVariableEvaluationException {

        HttpRequest.HttpRequestBuilder builder = HttpRequest.builder();

        if (options != null && options.getHeaders() != null) {
            Map<String, String> headers = runContext.render(options.getHeaders())
                .asMap(String.class, String.class);

            if (headers != null) {
                headers.forEach(builder::addHeader);
            }
        }

        return builder;
    }

    @Getter
    @Builder
    public static class RequestOptions {
        private Property<Duration> connectTimeout;
        private Property<Duration> readIdleTimeout;
        private Property<Map<String, String>> headers;
    }
}
