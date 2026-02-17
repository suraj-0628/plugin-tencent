package io.kestra.plugin.tencent;

import io.kestra.core.models.property.Property;
import io.kestra.plugin.tencent.qq.QQIncomingWebhook;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class QQIncomingWebhookTest extends AbstractQQTest {

    @Test
    void shouldSendWebhookPayload() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setResponseCode(200));
            server.start();

            QQIncomingWebhook task = QQIncomingWebhook.builder()
                .url(Property.ofValue(server.url("/send").toString()))
                .token(io.kestra.core.models.property.Property.ofValue("test-token"))
                .payload(io.kestra.core.models.property.Property.ofValue(
                    "{\"message\": \"hello\"}"
                ))
                .build();

            task.run(runContext());

            var request = server.takeRequest();
            assertThat(request.getBody().readUtf8(), containsString("hello"));
            assertThat(request.getHeader("Authorization"), containsString("Bearer"));
        }
    }
}
