package util.cli;

import model.CliSummary;
import model.TaskParameter;

import java.util.List;

public interface CliTask {
    String getName();
    CliSummary run();
    List<TaskParameter> getParameters();
    CliSummary validateParameters();
    String getDescription();
}
