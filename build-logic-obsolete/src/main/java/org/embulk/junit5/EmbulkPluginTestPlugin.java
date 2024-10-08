package org.embulk.junit5;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.testing.Test;

public class EmbulkPluginTestPlugin implements Plugin<Project> {
    @Override
    public void apply(final Project project) {
        project.getTasks().create("embulkPluginTest", Test.class);
    }
}
