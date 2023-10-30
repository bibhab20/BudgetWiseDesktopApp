package util.cli;

import model.CliSummary;
import model.TaskParameter;

import java.util.List;

public abstract class CliTask {
    String name, description;
    List<TaskParameter> parameters;
    public String getName() {
        return this.name;
    }

    abstract CliSummary run();

    public List<TaskParameter> getParameters() {
        return this.parameters;
    }

    CliSummary validateParameters() {
        for (TaskParameter parameter: this.parameters) {
            if (parameter.getValue() == null || parameter.getValue().isBlank()) {
                parameter.setValueToDefault();
            }
        }
        return new CliSummary(CliSummary.Status.PASS, "Validation success");
    }

    public String getDescription() {
        return this.description;
    }
}
