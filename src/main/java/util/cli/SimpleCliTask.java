package util.cli;

import model.CliSummary;
import model.TaskParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleCliTask extends CliTask {
    String name;
    Map<String, String> parameterMap;
    public static final String PARAMETER_A = "A";
    public static final String PARAMETER_B = "B";
    public static final String NONE = "none";

    public SimpleCliTask(String name) {
        super();
        this.name = name;
        parameterMap = new HashMap<>();
        parameterMap.put(PARAMETER_A, NONE);
        parameterMap.put(PARAMETER_B, NONE);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CliSummary run() {
        int res = Integer.parseInt(parameterMap.get(PARAMETER_A)) + Integer.parseInt(parameterMap.get(PARAMETER_A));
        return new CliSummary(CliSummary.Status.PASS, String.format("The response is %d", res));
    }

    @Override
    public List<TaskParameter> getParameters() {
        return null;
    }


    @Override
    public CliSummary saveParameters() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }
}
