package util.enrichment;

import model.Transaction;

import java.util.List;

public interface TransactionEnricher {

    List<Transaction> enrich(List<Transaction> transactions);
}
