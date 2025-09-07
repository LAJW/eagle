package org.example.eagle.repository;

import org.example.eagle.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByAccount_AccountNumber(Integer accountNumber);
    Transaction findByIdAndAccount_AccountNumber(Long id, Integer accountNumber);
}

