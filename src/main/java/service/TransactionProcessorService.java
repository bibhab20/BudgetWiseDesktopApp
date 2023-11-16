package service;

import lombok.extern.slf4j.Slf4j;
import model.Transaction;
import model.TransactionRepository;
import util.TransactionUtil;
import util.filter.AdvanceFilter;
import util.filter.AfterDateFilter;
import util.filter.BeforeDateFilter;

import java.util.*;

@Slf4j
public class TransactionProcessorService{
    TransactionUtil transactionUtil;
    TransactionRepository repository;

    public TransactionProcessorService(TransactionUtil transactionUtil, TransactionRepository repository) {
        this.transactionUtil = transactionUtil;
        this.repository = repository;
    }

    public List<Transaction> getTransactionsByDateRange(Date startDate, Date endDate, int sortingAlgo) {
        List<Transaction> result = new ArrayList<>();
        AdvanceFilter filter = new AdvanceFilter.Builder().addFilter(new AfterDateFilter(startDate))
                .addFilter(new BeforeDateFilter(endDate)).build();
        for (Transaction transaction: repository.getAll()) {
            if (filter.pass(transaction)) {
                result.add(transaction);
            }
        }
        result.sort(transactionUtil.getComparator(sortingAlgo));
        return result;
    }

    public static Map<String, Date[]> getDateRangeMap(Date startDate, Date endDate) {
        Map<String, Date[]> dateRangeMap = new LinkedHashMap<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        while (calendar.getTime().before(endDate) || calendar.getTime().equals(endDate)) {
            String monthYear = String.format("%1$tB %1$ty", calendar);
            Date[] dateRange = {calendar.getTime(), null};
            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            dateRange[1] = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            dateRangeMap.put(monthYear, dateRange);
        }

        return dateRangeMap;
    }



}
