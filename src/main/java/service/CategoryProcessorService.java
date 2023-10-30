package service;

import model.Transaction;
import model.TransactionRepository;
import util.filter.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CategoryProcessorService {
    TransactionRepository repository;

    public CategoryProcessorService(TransactionRepository repository) {
        this.repository = repository;
    }

    public List<Transaction> getTransactionByDateAndCategory(Date startDate, Date endDate, String category) {
        AdvanceFilter filter = new AdvanceFilter.Builder().addFilter(new AfterDateFilter(startDate))
                .addFilter(new BeforeDateFilter(endDate))
                .addFilter(new CategoryMatchOrFilter(List.of(category)))
                .build();
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction: repository.getAll()) {
            if (filter.pass(transaction)) {
                result.add(transaction);
            }
        }
        return result;
    }
}
