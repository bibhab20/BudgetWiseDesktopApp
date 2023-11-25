package util.enrichment;

import exception.EnrichmentFailureException;
import lombok.extern.slf4j.Slf4j;
import model.Transaction;

import java.util.List;

@Slf4j
public class EnrichmentManager {
    private List<TransactionEnricher> enrichers;

    public EnrichmentManager(List<TransactionEnricher> enrichers) {
        this.enrichers = enrichers;
    }

    public List<Transaction> enrich(List<Transaction> transactions) throws EnrichmentFailureException {
        if (this.enrichers == null || this.enrichers.size() == 0) {
            log.error("No enrichers found in Enrichment manager. Exiting Enrichment");
            return transactions;
        }
        for (TransactionEnricher enricher: enrichers) {
            enricher.enrich(transactions);
        }
        return transactions;
    }
}
