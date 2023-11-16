package service;

import model.Transaction;
import model.TransactionRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PersistenceService {
    TransactionRepository repository;

    public PersistenceService(TransactionRepository repository) {
        this.repository = repository;
    }

    public Map<String, List<Transaction>> getHashCollisions() {
        Map<String, List<Transaction>> map = new HashMap<>();
        for (Transaction transaction: repository.getAll()) {
            String hash = generateHash(transaction);
            List<Transaction> transactions = map.getOrDefault(hash, new ArrayList<>());
            transactions.add(transaction);
            map.put(hash, transactions);
        }
        Map<String, List<Transaction>> result = new HashMap<>();
        for (String hashKey: map.keySet()) {
            System.out.println(hashKey);
            if (map.get(hashKey).size() > 1) {
                System.out.println("Found it: " +hashKey);
                result.put(hashKey, map.get(hashKey));
            }
        }
        return result;
    }

    private String generateHash(Transaction transaction) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy");
        String data = simpleDateFormat.format(transaction.getTransactionDate()) +
                transaction.getType() +
                transaction.getDescription() +
                transaction.getAmount();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes());

            StringBuilder hexStringBuilder = new StringBuilder();
            for (byte b : hashBytes) {
                hexStringBuilder.append(String.format("%02x", b));
            }

            return hexStringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            // Handle exception (e.g., log it or throw a runtime exception)
            e.printStackTrace();
            return null;
        }
    }
}
