package org.embulk.junit5;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class EmbulkPluginTestPlugin implements Plugin<Project> {
    @Override
    public void apply(final Project project) {
        project.getTasks().withType(EmbulkPluginTest.class, configure -> {
        });
    }
}
