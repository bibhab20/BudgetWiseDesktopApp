package controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import model.Transaction;
import model.TransactionRepository;
import util.AppConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
public class JsonOutController {
    private static final String  OUTPUT_FILE_PATH = "json.output.file.path";

    TransactionRepository repository;
    AppConfig appConfig;

    public JsonOutController(TransactionRepository repository, AppConfig appConfig) {
        this.repository = repository;
        this.appConfig = appConfig;
    }

    public void getAllTransactions() {
        List<Transaction> allTransactions = repository.getAll();
        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = new File((String) appConfig.getProperties().get(OUTPUT_FILE_PATH));
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            String jsonArray = mapper.writeValueAsString(allTransactions);
            mapper.writeValue(jsonFile, jsonArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
