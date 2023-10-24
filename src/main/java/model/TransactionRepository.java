package model;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class TransactionRepository {
    List<Transaction> credits = new ArrayList<>();
    List<Transaction> debits = new ArrayList<>();


    public TransactionRepository(List<Transaction> allTransactions) {
        setTransactions(allTransactions);
    }
    public TransactionRepository() {

    }

    public void setTransactions(List<Transaction> allTransactions) {
        for (Transaction transaction: allTransactions){
            if (transaction.getType().equals(TransactionType.DEBIT)){
                this.debits.add(transaction);
            }
            else if (transaction.getType().equals(TransactionType.CREDIT)) {
                this.credits.add(transaction);
            }
            else {
                log.error("Transaction found with invalid type {}", transaction);
            }
        }
    }

    public List<Transaction> getCredits() {
        return credits;
    }

    public void setCredits(List<Transaction> credits) {
        this.credits = credits;
    }

    public List<Transaction> getDebits() {
        return debits;
    }

    public void setDebits(List<Transaction> debits) {
        this.debits = debits;
    }

    public List<Transaction> getAll() {
        List<Transaction> all = new ArrayList<>(credits);
        all.addAll(debits);
        Collections.sort(all, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                return t1.getTransactionDate().compareTo(t2.getTransactionDate());
            }
        });
        return all;
    }
}
