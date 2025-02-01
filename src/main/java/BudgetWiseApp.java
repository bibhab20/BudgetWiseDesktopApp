import config.CategoryConfigSupplier;
import config.VendorConfigSupplier;
import config.VendorOverrideConfigSupplier;
import config.VendorTypeConfigSupplier;
import controller.CLIController;
import controller.CsvOutController;
import controller.JsonOutController;
import controller.VendorController;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import model.Transaction;
import model.TransactionRepository;
import service.*;
import service.reader.DiscoverTransactionReader;
import service.reader.MidFirstTransactionReader;
import util.AppConfig;
import util.CsvWriter;
import util.TransactionUtil;
import util.cli.*;
import util.enrichment.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BudgetWiseApp {
    private static final String SIMPLE_DATE_FORMAT_CONFIG = "default.date.format";

    //Config
    AppConfig appConfig;

    //Processors
    DiscoverTransactionReader discoverProcessor;
    MidFirstTransactionReader midFirstProcessor;
    VendorProcessorService vendorProcessorService;
    CategoryProcessorService categoryProcessorService;
    TransactionSummarizationService transactionSummarizationService;
    TransactionProcessorService transactionProcessorService;
    PersistenceService persistenceService;

    //utils
    TransactionUtil transactionUtil;
    CsvWriter writer;
    SimpleDateFormat dateFormat;
    TransactionRepository repository;

    //suppliers
    VendorConfigSupplier vendorConfigSupplier;
    VendorTypeConfigSupplier vendorTypeConfigSupplier;
    CategoryConfigSupplier categoryConfigSupplier;
    VendorOverrideConfigSupplier vendorOverrideConfigSupplier;

    //Enrichment
    EnrichmentManager enrichmentManager;
    VendorEnricher vendorEnricher;
    IDEnricher idEnricher;
    VendorOverrideEnricher vendorOverrideEnricher;
    SimpleCategoryEnricher categoryEnricher;

    //Controller
    CsvOutController csvOutController;
    VendorController vendorController;
    JsonOutController jsonOutController;
    CLIController cliController;

    //Tasks
    SimpleCliTask simpleCliTask;
    CliTaskManager taskManager;
    GetVendorTransactionSummaryTask getVendorTransactionSummaryTask;
    GetCategoryTransactionSummary getCategoryTransactionSummary;
    GetTransactionsWithMissingVendorConfig getTransactionsWithMissingVendorConfig;
    GetCategoryTree getCategoryTree;
    GetMonthlySummary getMonthlySummary;
    GetMonthlyTrend getMonthlyTrend;
    AddOrUpdateVendorConfig addOrUpdateVendorConfig;
    GenericTransactionQueryTask genericTransactionQueryTask;
    GetTransactionByDateTask getTransactionByDateTask;
    GetMonthlyTrendTableByCategoryTask getMonthlyTrendTableByCategoryTask;
    GetMonthlyCategoryWiseChartTask getMonthlyCategoryWiseChartTask;

    public BudgetWiseApp()  {
        loadObjects();
    }
    private void loadObjects() {
        this.appConfig = new AppConfig("Default");

        //processors
        discoverProcessor = new DiscoverTransactionReader(appConfig);
        midFirstProcessor = new MidFirstTransactionReader(appConfig);


        //utils
        repository = new TransactionRepository();
        transactionUtil = new TransactionUtil(appConfig);
        writer = new CsvWriter(appConfig);
        dateFormat = new SimpleDateFormat(appConfig.getProperties().getProperty(SIMPLE_DATE_FORMAT_CONFIG));


        //suppliers
        vendorConfigSupplier = new VendorConfigSupplier(appConfig);
        vendorTypeConfigSupplier = new VendorTypeConfigSupplier(appConfig, vendorConfigSupplier);
        categoryConfigSupplier = new CategoryConfigSupplier(appConfig, vendorTypeConfigSupplier);
        vendorOverrideConfigSupplier = new VendorOverrideConfigSupplier(appConfig);

        //enrichers
        vendorEnricher = new VendorEnricher(appConfig, vendorConfigSupplier);
        categoryEnricher = new SimpleCategoryEnricher(categoryConfigSupplier, appConfig);
        idEnricher = new IDEnricher(transactionUtil);
        vendorOverrideEnricher = new VendorOverrideEnricher(vendorOverrideConfigSupplier);
        enrichmentManager = new EnrichmentManager(List.of(idEnricher, vendorEnricher, categoryEnricher, vendorOverrideEnricher));

        //services
        vendorProcessorService = new VendorProcessorService(repository, vendorConfigSupplier);
        categoryProcessorService = new CategoryProcessorService(repository);
        transactionSummarizationService = new TransactionSummarizationService(categoryConfigSupplier,
                vendorTypeConfigSupplier, transactionUtil, repository);
        transactionProcessorService = new TransactionProcessorService(transactionUtil, repository);
        persistenceService = new PersistenceService(repository);

        //cli tasks
        simpleCliTask = new SimpleCliTask("Simple Adder");
        getVendorTransactionSummaryTask = new GetVendorTransactionSummaryTask("Vendor Transaction Summary",
                appConfig, transactionUtil, vendorConfigSupplier, vendorProcessorService);
        getCategoryTransactionSummary = new GetCategoryTransactionSummary("Category wise transaction summary",
                categoryProcessorService, transactionUtil, categoryConfigSupplier);
        getTransactionsWithMissingVendorConfig = new GetTransactionsWithMissingVendorConfig("Find transactions with missing vendor config",
                vendorProcessorService, transactionUtil);
        addOrUpdateVendorConfig = new AddOrUpdateVendorConfig("Test Vendor Config",
                vendorTypeConfigSupplier, vendorProcessorService, transactionUtil);
        getCategoryTree = new GetCategoryTree("Get the category tree", categoryConfigSupplier,
                vendorTypeConfigSupplier, persistenceService, transactionUtil);
        getMonthlySummary = new GetMonthlySummary("Get Monthly summary tree", repository, transactionSummarizationService);
        getMonthlyTrend = new GetMonthlyTrend("Get Monthly Trend Tree", transactionSummarizationService);
        getTransactionByDateTask = new GetTransactionByDateTask("Get transactions for a given date range",
                transactionProcessorService, transactionUtil);
        genericTransactionQueryTask = new GenericTransactionQueryTask("Generic Transaction query", categoryConfigSupplier, vendorConfigSupplier,
                vendorTypeConfigSupplier, repository, transactionUtil);
        getMonthlyTrendTableByCategoryTask = new GetMonthlyTrendTableByCategoryTask("Get Monthly trend table by category",
                transactionSummarizationService);
        getMonthlyCategoryWiseChartTask = new GetMonthlyCategoryWiseChartTask("Get bar chart for a given category and date range",
                categoryConfigSupplier, transactionSummarizationService);
        taskManager = new CliTaskManager(List.of(getVendorTransactionSummaryTask,
                getCategoryTransactionSummary, getTransactionsWithMissingVendorConfig,
                addOrUpdateVendorConfig, getCategoryTree, getMonthlySummary, getMonthlyTrend, getTransactionByDateTask,
                genericTransactionQueryTask, getMonthlyTrendTableByCategoryTask, getMonthlyCategoryWiseChartTask));


        //controllers
        csvOutController = new CsvOutController(repository,appConfig,transactionUtil,writer);
        vendorController = new VendorController(vendorTypeConfigSupplier, appConfig, writer);
        jsonOutController = new JsonOutController(repository, appConfig);
        cliController = new CLIController(appConfig, taskManager);


    }

    public void processAndLoad() {
        try {
            List<Transaction> discoverTransactions = discoverProcessor.readAndProcessRecords();
            List<Transaction> midFirstTransactions = midFirstProcessor.readAndProcessRecords();
            List<Transaction> allTransaction = new ArrayList<>();
            allTransaction.addAll(discoverTransactions);
            allTransaction.addAll(midFirstTransactions);
            log.info("Total no of transactions found: {}", allTransaction.size());

            repository.setTransactions(allTransaction);
            log.info("Loaded all transactions to repository");
            log.info("Starting enrichment process");
            enrichmentManager.enrich(allTransaction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeControllers() {
        try {
            cliController.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        try {
            BudgetWiseApp app = new BudgetWiseApp();
            app.processAndLoad();
            app.executeControllers();



        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
