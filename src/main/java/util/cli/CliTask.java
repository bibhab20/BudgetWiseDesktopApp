package util.cli;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.CliSummary;
import model.ParameterBatch;
import model.TaskParameter;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public abstract class CliTask {
    String name, description;
    List<ParameterBatch> parameterBatches;

    public CliTask() {
        this.parameterBatches = new ArrayList<>();
    }

    abstract CliSummary run();

    CliSummary validateParameters() {
        for (TaskParameter parameter: this.getParameters()) {
            if (parameter.getValue() == null || parameter.getValue().isBlank()) {
                parameter.setValueToDefault();
            }
        }
        return new CliSummary(CliSummary.Status.PASS, "Validation success");
    }

    public List<TaskParameter> getParameters() {
        List<TaskParameter> result = new ArrayList<>();
        for (ParameterBatch batch: parameterBatches) {
            result.addAll(batch.getParameters());
        }
        return result;
    }


}
