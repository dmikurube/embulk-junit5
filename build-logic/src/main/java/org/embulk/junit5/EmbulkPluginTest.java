package org.embulk.junit5;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import javax.inject.Inject;

public class EmbulkPluginTest extends DefaultTask {
    @Inject
    public EmbulkPluginTest(final ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @TaskAction
    public void exec() {
    }

    private final ObjectFactory objectFactory;
}
