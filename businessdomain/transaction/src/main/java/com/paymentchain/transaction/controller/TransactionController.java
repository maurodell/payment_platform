package com.paymentchain.transaction.controller;


import com.paymentchain.transaction.entities.Transaction;
import com.paymentchain.transaction.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable(name = "id") Long idTransaction){
        Optional<Transaction> transaction = transactionRepository.findById(idTransaction);
        if(transaction.isPresent()){
            return ResponseEntity.ok(transaction.get());
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/customer/transaction")
    public List<?> getTransactionForIban(@RequestParam(name = "ibanAccount") String ibanAccount){
        List<?> transactions = transactionRepository.findByAccountIban(ibanAccount);
        if(transactions.isEmpty()){
            return new ArrayList<>();
        }
        return transactions;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Transaction transaction){
        Transaction transactionSaved = transactionRepository.save(transaction);
        return new ResponseEntity<>(transactionSaved, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long idCustomer, @RequestBody Transaction transaction){
        Optional<Transaction> findByIdCustomer = transactionRepository.findById(idCustomer);
        if(findByIdCustomer.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        return transactionRepository.findById(idCustomer)
                .map(existingtTransaction -> {
                    updateTransaction(existingtTransaction, transaction);
                    Transaction updated = transactionRepository.save(existingtTransaction);
                    return new ResponseEntity<>(updated, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long idCustomer){
        Optional<Transaction> findByIdCustomer = transactionRepository.findById(idCustomer);
        if(findByIdCustomer.isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        transactionRepository.deleteById(idCustomer);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void updateTransaction(Transaction existingTransaction, Transaction newTransaction){
        existingTransaction.setDate(newTransaction.getDate());
        existingTransaction.setAmount(newTransaction.getAmount());
        existingTransaction.setDescription(newTransaction.getDescription());
        existingTransaction.setFee(newTransaction.getFee());
        existingTransaction.setAccountIban(newTransaction.getAccountIban());
        existingTransaction.setReference(newTransaction.getReference());
    }
}
