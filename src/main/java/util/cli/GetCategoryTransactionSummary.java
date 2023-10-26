package util.cli;

import model.CliSummary;
import model.TaskParameter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GetCategoryTransactionSummary implements CliTask{
    private static final int START_DATE_INDEX = 0;
    private static final int END_DATE_INDEX = 2;
    private static final int CATEGORY_NAME_INDEX = 3;
    private static final String START_DATE_DEFAULT_VALUE = "1/1/2022";
    private static final String END_DATE_DEFAULT_VALUE = "31/12/2025";
    private static final String CATEGORY_DEFAULT_VALUE = "Food";


    String name, description;
    Date startDate, endDate;
    String category;
    List<TaskParameter> parameters;


    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public CliSummary run() {
        return null;
    }

    @Override
    public List<TaskParameter> getParameters() {
        return this.parameters;
    }

    @Override
    public CliSummary validateParameters() {
        return null;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    private void loadParameters() {
        this.parameters = new ArrayList<>();
        this.parameters.add(new TaskParameter("Start Date", START_DATE_DEFAULT_VALUE));
        this.parameters.add(new TaskParameter("End Date", END_DATE_DEFAULT_VALUE));
        this.parameters.add(new TaskParameter("Category Name","NONE" ));
    }
}
