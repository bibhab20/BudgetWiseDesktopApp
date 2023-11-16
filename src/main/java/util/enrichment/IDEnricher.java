package util.enrichment;

import lombok.extern.slf4j.Slf4j;
import model.Transaction;
import util.TransactionUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class IDEnricher implements TransactionEnricher {
    List<TransactionUtil.TransactionProperty> properties;

    TransactionUtil transactionUtil;

    public IDEnricher(TransactionUtil transactionUtil) {
        this.transactionUtil = transactionUtil;
        loadProperties();
    }

    @Override
    public List<Transaction> enrich(List<Transaction> transactions) {
        Map<String, List<Transaction>> map = generateMap(transactions);
        for (String hash: map.keySet()) {
            if (map.get(hash).size() == 1) {
                Transaction transaction = map.get(hash).get(0);
                transaction.setId(hash);
                transaction.getEnrichmentList().add(this.getClass().getName());
            } else {
                //add suffix in the transaction
                for (int i=0; i< map.get(hash).size(); i++) {
                    Transaction transaction = map.get(hash).get(i);
                    transaction.setId(hash + "-" + i);
                    transaction.getEnrichmentList().add(this.getClass().getName());
                }
            }
        }
        return transactions;
    }

    private void loadProperties() {
        this.properties = new ArrayList<>();
        properties.add(TransactionUtil.TransactionProperty.DATE);
        properties.add(TransactionUtil.TransactionProperty.SOURCE);
        properties.add(TransactionUtil.TransactionProperty.TYPE);
        properties.add(TransactionUtil.TransactionProperty.AMOUNT);
        properties.add(TransactionUtil.TransactionProperty.DESCRIPTION);
    }

    private String generateHash(Transaction transaction) {
        StringBuilder data = new StringBuilder();
        for (TransactionUtil.TransactionProperty property: this.properties) {
            data.append(transactionUtil.getTransactionPropertyValue(transaction, property));
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.toString().getBytes());

            StringBuilder hexStringBuilder = new StringBuilder();
            for (byte b : hashBytes) {
                hexStringBuilder.append(String.format("%02x", b));
            }

            return hexStringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, List<Transaction>> generateMap(List<Transaction> transactions) {
        Map<String, List<Transaction>> map = new HashMap<>();
        for (Transaction transaction: transactions) {
            String hash = generateHash(transaction);
            List<Transaction> matches = map.getOrDefault(hash, new ArrayList<>());
            matches.add(transaction);
            map.put(hash, matches);
        }
        return map;
    }
}
