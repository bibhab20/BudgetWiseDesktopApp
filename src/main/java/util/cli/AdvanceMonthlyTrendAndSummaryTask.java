package util.cli;

import model.CliSummary;
import model.ParameterBatch;
import model.TaskParameter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdvanceMonthlyTrendAndSummaryTask extends CliTask{
    Date startDate, endDate;
    @Override
    CliSummary run() {
        return null;
    }

    @Override
    public CliSummary validateParameters() {
        CliSummary superSummary = super.validateParameters();
        if (superSummary.getStatus().equals(CliSummary.Status.FAIL)) {
            return superSummary;
        }
        List<TaskParameter> parameters = this.parameterBatches.get(0).getParameters();
        this.startDate = CliUtils.parseDate(parameters.get(0).getValue());
        this.endDate = CliUtils.parseEndDate(parameters.get(1).getValue());
        return superSummary;
    }

    public void loadParameters() {
        List<TaskParameter> parameters = new ArrayList<>();
        parameters.add(new TaskParameter("Start Date", "1/22"));
        parameters.add(new TaskParameter("End Date", "10/23"));

        this.parameterBatches.add(new ParameterBatch("ONE", parameters));
    }
}
