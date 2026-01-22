package io.kestra.plugin.notifications.qq;

import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

@MicronautTest
class QQExecutionTest {

    @Inject
    RunContextFactory runContextFactory;

    @Test
    void shouldRenderAndSendExecutionMessage() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setResponseCode(200));
            server.start();

            RunContext runContext = runContextFactory.of(Map.of());

            FakeQQExecution task = FakeQQExecution.builder()
                .url(Property.ofValue(server.url("/").toString()))
                .recipientIds(Property.ofValue(List.of("user-1")))
                .build();

            task.run(runContext);

            RecordedRequest request = server.takeRequest();
            assertThat(request, notNullValue());

            String body = request.getBody().readUtf8();
            assertThat(body, containsString("exec-123"));
            assertThat(body, containsString("FAILED"));
            assertThat(body, containsString("Test failure message"));
        }
    }
}
