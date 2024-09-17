package org.embulk.input.junit5example;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigSource;
import org.embulk.config.TaskReport;
import org.embulk.config.TaskSource;
import org.embulk.spi.BufferAllocator;
import org.embulk.spi.Column;
import org.embulk.spi.ColumnVisitor;
import org.embulk.spi.DataException;
import org.embulk.spi.Exec;
import org.embulk.spi.InputPlugin;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.PageOutput;
import org.embulk.spi.Schema;
import org.embulk.spi.type.TimestampType;
import org.embulk.spi.type.Types;
import org.embulk.util.config.Config;
import org.embulk.util.config.ConfigDefault;
import org.embulk.util.config.ConfigMapperFactory;
import org.embulk.util.config.Task;
import org.embulk.util.config.units.ColumnConfig;
import org.embulk.util.config.units.SchemaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleInputPlugin implements InputPlugin {
    private interface PluginTask extends Task {
    }

    @Override
    public ConfigDiff transaction(ConfigSource config, InputPlugin.Control control) {
        final PluginTask task = CONFIG_MAPPER_FACTORY.createConfigMapper().map(config, PluginTask.class);
        final Schema schema = Schema.builder().add("id", Types.STRING).build();

        final int taskCount = 0;

        return resume(task.toTaskSource(), schema, taskCount, control);
    }

    @Override
    public ConfigDiff resume(TaskSource taskSource,
            Schema schema, int taskCount,
            InputPlugin.Control control) {
        control.run(taskSource, schema, taskCount);
        return CONFIG_MAPPER_FACTORY.newConfigDiff();
    }

    @Override
    public void cleanup(TaskSource taskSource,
            Schema schema, int taskCount,
            List<TaskReport> successTaskReports) {}

    @Override
    public TaskReport run(TaskSource taskSource,
            Schema schema, int taskIndex,
            PageOutput output) {
        final PluginTask task = CONFIG_MAPPER_FACTORY.createTaskMapper().map(taskSource, PluginTask.class);

        try (final PageBuilder pageBuilder = Exec.getPageBuilder(Exec.getBufferAllocator(), schema, output)) {
            pageBuilder.finish();
        }

        return CONFIG_MAPPER_FACTORY.newTaskReport();
    }

    @Override
    public ConfigDiff guess(ConfigSource config) {
        return CONFIG_MAPPER_FACTORY.newConfigDiff();
    }

    private static final ConfigMapperFactory CONFIG_MAPPER_FACTORY = ConfigMapperFactory.builder().addDefaultModules().build();

    private static final Logger logger = LoggerFactory.getLogger(ExampleInputPlugin.class);
}
