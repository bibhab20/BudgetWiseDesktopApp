package util.filter;

import lombok.extern.slf4j.Slf4j;
import model.Transaction;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AdvanceFilter {
    private final List<TransactionFilter> filters;

    public AdvanceFilter(List<TransactionFilter> filters) {
        this.filters = filters;
    }

    public List<Transaction> filter(List<Transaction> transactions) {
        log.debug("Starting filtering with {} transactions", transactions.size());
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction: transactions) {
            if (this.pass(transaction)) {
                result.add(transaction);
            }

        }
        return result;
    }

    public boolean pass(Transaction transaction) {
        for (TransactionFilter filter: filters) {
            if (!filter.pass(transaction)) {
                return false;
            }
        }
        return true;
    }

    public boolean orPass(Transaction transaction) {
        for (TransactionFilter filter: filters) {
            if (filter.pass(transaction))
                return true;
        }
        return false;
    }

    public static class Builder {
        private List<TransactionFilter> filters = new ArrayList<>();

        public Builder addFilter(TransactionFilter filter) {
            this.filters.add(filter);
            return this;
        }

        public AdvanceFilter build() {
            return new AdvanceFilter(filters);
        }
    }
}
