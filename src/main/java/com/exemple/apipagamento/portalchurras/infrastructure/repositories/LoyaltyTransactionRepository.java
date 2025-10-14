package com.exemple.apipagamento.portalchurras.infrastructure.repositories;

import com.exemple.apipagamento.portalchurras.domain.entities.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {
    
    List<LoyaltyTransaction> findByLoyaltyProgramIdOrderByCreatedAtDesc(Long loyaltyProgramId);
    
    List<LoyaltyTransaction> findByOrderId(Long orderId);
    
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.loyaltyProgram.id = :programId " +
           "AND lt.createdAt BETWEEN :startDate AND :endDate")
    List<LoyaltyTransaction> findByProgramAndDateRange(@Param("programId") Long programId,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);
}
