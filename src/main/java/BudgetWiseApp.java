import config.CategoryConfigSupplier;
import config.VendorConfigSupplier;
import config.VendorTypeConfigSupplier;
import controller.CLIController;
import controller.CsvOutController;
import controller.JsonOutController;
import controller.VendorController;
import lombok.extern.slf4j.Slf4j;
import model.Transaction;
import model.TransactionRepository;
import service.DiscoverTransactionProcessor;
import service.MidFirstTransactionProcessor;
import service.VendorProcessorService;
import util.AppConfig;
import util.CsvWriter;
import util.TransactionUtil;
import util.cli.CliTaskManager;
import util.cli.GetVendorTransactionSummaryTask;
import util.cli.SimpleCliTask;
import util.enrichment.EnrichmentManager;
import util.enrichment.SimpleCategoryEnricher;
import util.enrichment.VendorEnricher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BudgetWiseApp {
    private static final String SIMPLE_DATE_FORMAT_CONFIG = "default.date.format";

    //Config
    AppConfig appConfig;

    //Processors
    DiscoverTransactionProcessor discoverProcessor;
    MidFirstTransactionProcessor midFirstProcessor;
    VendorProcessorService vendorProcessorService;

    //utils
    TransactionUtil transactionUtil;
    CsvWriter writer;
    SimpleDateFormat dateFormat;
    TransactionRepository repository;

    //suppliers
    VendorConfigSupplier vendorConfigSupplier;
    VendorTypeConfigSupplier vendorTypeConfigSupplier;
    CategoryConfigSupplier categoryConfigSupplier;

    //Enrichment
    EnrichmentManager enrichmentManager;
    VendorEnricher vendorEnricher;
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

    public BudgetWiseApp()  {
        loadObjects();
    }
    private void loadObjects() {
        this.appConfig = new AppConfig("Default");

        //processors
        discoverProcessor = new DiscoverTransactionProcessor(appConfig);
        midFirstProcessor = new MidFirstTransactionProcessor(appConfig);


        //utils
        repository = new TransactionRepository();
        transactionUtil = new TransactionUtil(appConfig);
        writer = new CsvWriter(appConfig);
        dateFormat = new SimpleDateFormat(appConfig.getProperties().getProperty(SIMPLE_DATE_FORMAT_CONFIG));


        //suppliers
        vendorConfigSupplier = new VendorConfigSupplier(appConfig);
        vendorTypeConfigSupplier = new VendorTypeConfigSupplier(appConfig, vendorConfigSupplier);
        categoryConfigSupplier = new CategoryConfigSupplier(appConfig);

        //enrichers
        vendorEnricher = new VendorEnricher(appConfig, vendorConfigSupplier);
        categoryEnricher = new SimpleCategoryEnricher(categoryConfigSupplier, appConfig);
        enrichmentManager = new EnrichmentManager(List.of(vendorEnricher, categoryEnricher));

        //services
        vendorProcessorService = new VendorProcessorService(repository);

        //cli tasks
        simpleCliTask = new SimpleCliTask("Simple Adder");
        getVendorTransactionSummaryTask = new GetVendorTransactionSummaryTask("Vendor Transaction Summary Task",
                appConfig, transactionUtil, vendorConfigSupplier, vendorProcessorService);
        taskManager = new CliTaskManager(List.of(simpleCliTask, getVendorTransactionSummaryTask));


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
            /*
            csvOutController.getTransactionsWithVendorEnrichmentFailures(dateFormat.parse("1/1/22"), dateFormat.parse("6/30/23"));
            vendorController.writeAllVendorTypesToCsv();
            jsonOutController.getAllTransactions();
             */
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
