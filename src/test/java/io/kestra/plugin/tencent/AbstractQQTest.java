package io.kestra.plugin.notifications.qq;

import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
public abstract class AbstractQQTest {

    @Inject
    protected RunContextFactory runContextFactory;

    protected RunContext runContext() {
        return runContextFactory.of();
    }
}
