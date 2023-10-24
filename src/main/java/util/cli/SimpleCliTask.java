package util.cli;

import model.CliSummary;
import model.TaskParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleCliTask implements CliTask{
    String name;
    Map<String, String> parameterMap;
    public static final String PARAMETER_A = "A";
    public static final String PARAMETER_B = "B";
    public static final String NONE = "none";

    public SimpleCliTask(String name) {
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
    public Map<String, String> getParameterMap() {
        return this.parameterMap;
    }

    @Override
    public List<TaskParameter> getParameters() {
        return null;
    }

    @Override
    public CliSummary validateParameterMap() {
        if (this.parameterMap.size() != 2) {
            return new CliSummary(CliSummary.Status.FAIL, String.format("Wrong numbers of parameters. Expecting 2 and is : %d", parameterMap.size()));
        }
        return new CliSummary(CliSummary.Status.PASS, "Validation passed");
    }

    @Override
    public CliSummary validateParameters() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }
}
