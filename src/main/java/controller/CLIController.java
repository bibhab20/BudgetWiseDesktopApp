package controller;

import lombok.extern.slf4j.Slf4j;
import model.CliSummary;
import model.TaskParameter;
import util.AppConfig;
import util.cli.CliTaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Slf4j
public class CLIController {
    private static final String HOME_KEY="H";
    AppConfig config;
    CliTaskManager taskManager;

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
        System.out.println("Welcome to Budgetwise app");
        System.out.println("Please enter x to exit the app else enter anything else");
        if (readInput().equals("x")) {
            return;
        }
        runTaskManager();
    }

    public void runTaskManager() {
        System.out.println("Starting Task Manager...");
        //print All tasks
        List<String> tasks = taskManager.getAllTaskNames();
        for (int i=0; i<tasks.size(); i++) {
            System.out.println(String.format("%d : %s", i, tasks.get(i)));
        }
        int taskIndex = Integer.parseInt(readInput());
        if(taskManager.selectTask(taskIndex)) {
            log.info("Successfully selected task: {}",taskManager.getCurrentTask().getName());
            System.out.printf("%s task was selected successfully%n", taskManager.getCurrentTask().getName());
            System.out.println(String.format("Task Description: \n %s", taskManager.getCurrentTask().getDescription()));
        } else {
            log.error("Task selection failed returning to home screen");
            loadHome();
            return;
        }
        //Map<String, String> parameterMap = taskManager.getParameterMap();
        List<TaskParameter> parameters = taskManager.getParameters();
        //read and store parameters
        if (parameters != null && parameters.size() != 0) {
            System.out.println("Now enter the parameters for the selected task");
            for (TaskParameter parameter: parameters) {
                System.out.println(String.format("Enter %s: | Default value: %s", parameter.getName(), parameter.getDefaultValue()));
                parameter.setValue(readInput());
            }
            CliSummary validationSummary = taskManager.validateParameters();
            System.out.println(String.format("validation %s", validationSummary.getStatus()));
            System.out.println(String.format("Message:  %s", validationSummary.getMessage()));
            if (validationSummary.getStatus().equals(CliSummary.Status.FAIL)) {
                System.out.println("Returning to home...");
                loadHome();
            }
        }
        else {
            System.out.println("No parameters found for this task. Skipping to execution");
        }

        System.out.println(String.format("Starting execution of %s task", taskManager.getCurrentTask().getName()));
        CliSummary executionSummary = taskManager.runCurrentTask();
        if (executionSummary == null) {
            System.out.println("execution summary is null");
        }
        System.out.println(String.format("Execution status: %s", executionSummary.getStatus()));
        System.out.println("Execution Summary-");
        System.out.println(executionSummary.getMessage());
        loadHome();

    }

    public String readInput() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

}
