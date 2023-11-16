package controller;

import lombok.extern.slf4j.Slf4j;
import model.CliSummary;
import model.CsvTable;
import model.ParameterBatch;
import model.TaskParameter;
import util.AppConfig;
import util.CsvTableUtil;
import util.cli.CliTaskManager;
import util.cli.CliUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Slf4j
public class CLIController {
    private static final int SECTION_HEADER_LENGTH = 250;
    private static final int SESSION_HEADER_LENGTH = 600;
    private static final String SESSION_HEADER_LABEL = "SESSION HEADER";
    AppConfig config;
    CliTaskManager taskManager;
    int session = 0;

    public CLIController(AppConfig config, CliTaskManager taskManager) {
        this.config = config;
        this.taskManager = taskManager;
    }

    public void start() {
        /*
        - Print the welcome screen
        - Print list of operation the app can perform
        - Read, validate and store the input
        - Ask for parameters
        - Run the operation
        - Print summary
         */
        loadHome();

    }

    public void loadHome() {
        session++;
        printSessionHeader(session);
        System.out.println(CliUtils.getColoredString("Welcome to Budgetwise app", CliUtils.ANSI_CYAN));
        System.out.println(CliUtils.getColoredString("Please enter x to exit the app else enter anything else", CliUtils.ANSI_MAGENTA));
        if (readInput().equals("x")) {
            return;
        }
        runTaskManager();
    }

    public void runTaskManager() {
        System.out.println(CliUtils.getColoredString("Starting Task Manager...", CliUtils.ANSI_GREEN));
        printSeparators("TASK SELECTION");
        setUpTask();
        printSeparators("PARAMETER SET UP");
        setUpParameters();
        printSeparators("EXECUTION-");
        execute();
        loadHome();
    }

    private void setUpTask() {
        if (taskManager.getCurrentTask() != null) {
            String sb = CliUtils.getColoredString("Task ", CliUtils.ANSI_YELLOW) +
                    CliUtils.getColoredString(taskManager.getCurrentTask().getName(), CliUtils.ANSI_CYAN) +
                    CliUtils.getColoredString(" is selected. Enter ", CliUtils.ANSI_YELLOW) + CliUtils.getColoredString("n", CliUtils.ANSI_RED) +
                    CliUtils.getColoredString(" to select a new task", CliUtils.ANSI_YELLOW);
            System.out.println(sb);
            if (!readInput().equalsIgnoreCase("n"))
                return;
        }
        System.out.println(CliUtils.getColoredString("Please enter the number to select the corresponding task", CliUtils.ANSI_MAGENTA));
        List<String> tasks = taskManager.getAllTaskNames();
        for (int i=0; i<tasks.size(); i++) {
            System.out.println(CliUtils.getColoredString(String.format("%d : %s", i+1, tasks.get(i)), CliUtils.ANSI_BLUE));
        }
        int taskIndex = 0;
        try {
            taskIndex = Integer.parseInt(readInput()) -1;
        } catch (Exception e) {
            System.out.println(CliUtils.getColoredString("Invalid input, Not a integer. Setting default to 0", CliUtils.ANSI_RED));
        }
        if(taskManager.selectTask(taskIndex)) {
            log.debug("Successfully selected task: {}",taskManager.getCurrentTask().getName());
            System.out.println(CliUtils.getColoredString(taskManager.getCurrentTask().getName(), CliUtils.ANSI_BLUE) +
                    CliUtils.getColoredString(" task was selected successfully", CliUtils.ANSI_YELLOW));
            System.out.println(CliUtils.getColoredString("Task Description:", CliUtils.ANSI_GREEN));
            System.out.println(CliUtils.getColoredString(taskManager.getCurrentTask().getDescription(), CliUtils.ANSI_BLUE));
        } else {
            log.error(CliUtils.getColoredString("Task selection failed returning to home screen", CliUtils.ANSI_RED));
            loadHome();
        }
    }

    private void processParameters(List<TaskParameter> parameters) {
        if (parameters.size() == 0) {
            System.out.println(CliUtils.getColoredString("No parameters found for this task/batch. Skipping to execution", CliUtils.ANSI_YELLOW));
            return;
        }
        if (areParametersPopulated(parameters)) {
            System.out.println(CliUtils.getColoredString("Do you want use last used values for parameters?", CliUtils.ANSI_MAGENTA));
            CsvTable parameterTable = new CsvTable();
            List<String> headers = new ArrayList<>(List.of("Parameter name", "Last used value"));
            parameterTable.setHeaders(headers);
            try {
                for (TaskParameter parameter: parameters) {
                    parameterTable.addRow(List.of(parameter.getName(), parameter.getValue()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println(CsvTableUtil.getCliTable(parameterTable));
            System.out.println("Please enter y to use the above parameter values");
            if (readInput().equalsIgnoreCase("y")) {
                System.out.println("The above shown values are selected.");
                return;
            }
        }
        System.out.println(CliUtils.getColoredString("Now enter the parameters for the selected task", CliUtils.ANSI_YELLOW));
        for (TaskParameter parameter: parameters) {
            System.out.println(CliUtils.getColoredString(String.format("Enter %s: | Default value: %s", parameter.getName(), parameter.getDefaultValue()), CliUtils.ANSI_MAGENTA));
            if (parameter.getOptions().size() != 0) {
                System.out.println(CliUtils.getColoredString("Options:", CliUtils.ANSI_GREEN));
                System.out.println(CliUtils.getColoredString(CliUtils.getCliGridWithNumbers(parameter.getOptions(), 6), CliUtils.ANSI_YELLOW));
            }
            parameter.readInput(readInput());

        }
        CliSummary validationSummary = taskManager.validateParameters();
        System.out.println(CliUtils.getColoredString(String.format("Message:  %s", validationSummary.getMessage()),
                CliUtils.ANSI_GREEN));

        if (validationSummary.getStatus().equals(CliSummary.Status.FAIL)) {
            System.out.println(CliUtils.getColoredString("Setting parameters values to default", CliUtils.ANSI_YELLOW));
            for (TaskParameter parameter: taskManager.getParameters()) {
                parameter.setValueToDefault();
            }
        }
        //print input values
        CsvTable parameterTable = new CsvTable();
        List<String> headers = new ArrayList<>(List.of("Name", "Value"));
        parameterTable.setHeaders(headers);
        for (TaskParameter parameter: parameters) {
            try {
                parameterTable.addRow(new ArrayList<>(List.of(parameter.getName(), parameter.getValue())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(CliUtils.getColoredString("Parameter Values:",CliUtils.ANSI_YELLOW));
        System.out.println(CliUtils.getColoredString(CsvTableUtil.getCliTable(parameterTable), CliUtils.ANSI_MAGENTA));
    }

    private void execute() {
        System.out.println(CliUtils.getColoredString(String.format("Starting execution of %s task", taskManager.getCurrentTask().getName()), CliUtils.ANSI_GREEN));
        CliSummary executionSummary = taskManager.runCurrentTask();
        if (executionSummary == null) {
            System.out.println(CliUtils.getColoredString("execution summary is null", CliUtils.ANSI_RED));
            return;
        }
        System.out.println(CliUtils.getColoredString(String.format("Execution status: %s", executionSummary.getStatus()), CliUtils.ANSI_CYAN_BACKGROUND));
        System.out.println(CliUtils.getColoredString("Execution Summary:", CliUtils.ANSI_GREEN));
        System.out.println(executionSummary.getMessage());
    }

    private void setUpParameters() {
        /*
        1. If the size of parameter batch is null print to contact developer
        2. If the size is one:
        2.1 call processParameters(List<Parameter> parameters)
        3. else:
        3.1 for each batch in the batch list
         */
        List<ParameterBatch> batches = this.taskManager.getCurrentTask().getParameterBatches();
        if (batches == null || batches.isEmpty()) {
            System.out.println(CliUtils.getColoredString("There are no parameters in this task. Skipping to execution",
                    CliUtils.ANSI_YELLOW));
            return;
        }

        //if there is just one batch: do as before
        if (batches.size() == 1) {
            processParameters(this.taskManager.getParameters());
            return;
        }
        System.out.println(CliUtils.getColoredString("This task has multiple batches of parameters. If you want to" +
                " use the default values for that batch, just press Enter", CliUtils.ANSI_GREEN));
        for (ParameterBatch batch: batches) {
            //Ask if the user wants to use the default value for the batch
            String stringBuilder = CliUtils.getColoredString("Do you want to use to default value for parameter batch: ", CliUtils.ANSI_MAGENTA) +
                    CliUtils.getColoredString(batch.getName(), CliUtils.ANSI_BLUE) +
                    CliUtils.getColoredString("? Please enter", CliUtils.ANSI_MAGENTA) +
                    CliUtils.getColoredString(" N ", CliUtils.ANSI_RED) +
                    CliUtils.getColoredString("to enter the parameters manually", CliUtils.ANSI_MAGENTA);
            System.out.println(stringBuilder);
            System.out.println(CliUtils.getColoredString("Parameters with default values", CliUtils.ANSI_GREEN));
            try {
                System.out.println(CliUtils.getColoredString(CliUtils.getParameterDefaultValueTable(batch.getParameters()), CliUtils.ANSI_YELLOW));
            } catch (Exception e) {
                System.out.println(CliUtils.getColoredString("Something failed. Contact Developer", CliUtils.ANSI_RED));
            }
            //if user inputs n iterate through the batches and call processParameters method
            if (readInput().equalsIgnoreCase("n")) {
                String res = CliUtils.getColoredString("Set up parameters for batch: ", CliUtils.ANSI_MAGENTA)
                + CliUtils.getColoredString(batch.getName(), CliUtils.ANSI_BLUE);
                System.out.println(res);
                processParameters(batch.getParameters());
            } else {
                System.out.println(CliUtils.getColoredString("Default values are set for all parameters", CliUtils.ANSI_GREEN));
            }
        }
    }

    private boolean areParametersPopulated(List<TaskParameter> parameters) {
        for (TaskParameter parameter: parameters) {
            if (parameter.getValue() == null)
                return false;
        }
        return true;
    }

    private String readInput() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    private void printSeparators(String sectionName) {
        int paddingLength = (SECTION_HEADER_LENGTH - sectionName.length())/2;
        System.out.println(CliUtils.getColoredString("-".repeat(paddingLength) + sectionName + "-".repeat(paddingLength), CliUtils.ANSI_YELLOW_BACKGROUND));
    }

    private void printSessionHeader(int session) {
        int paddingLength = SESSION_HEADER_LENGTH - SESSION_HEADER_LABEL.length();
        System.out.println(CliUtils.getColoredString("START OF SESSION: " + session + " ".repeat(paddingLength), CliUtils.ANSI_MAGENTA_BACKGROUND));
    }




}
