package service;

import model.Transaction;
import util.filter.*;

import java.util.Date;
import java.util.List;

public class CategoryProcessorService {
    public List<Transaction> getTransactionByDateAndCategory(Date startDate, Date endDate, String category) {
        AdvanceFilter filter = new AdvanceFilter.Builder().addFilter(new AfterDateFilter(startDate))
                .addFilter(new BeforeDateFilter(endDate))
                .addFilter(new CategoryMatchOrFilter(List.of(category)))
                .build();
        return null;
    }
}
