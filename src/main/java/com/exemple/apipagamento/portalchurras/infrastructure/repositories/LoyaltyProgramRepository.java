package com.exemple.apipagamento.portalchurras.infrastructure.repositories;

import com.exemple.apipagamento.portalchurras.domain.entities.LoyaltyProgram;
import com.exemple.apipagamento.portalchurras.domain.entities.LoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyProgramRepository extends JpaRepository<LoyaltyProgram, Long> {
    
    Optional<LoyaltyProgram> findByUserId(Long userId);
    
    List<LoyaltyProgram> findByTier(LoyaltyTier tier);
    
    @Query("SELECT lp FROM LoyaltyProgram lp WHERE lp.availablePoints > 0 ORDER BY lp.availablePoints DESC")
    List<LoyaltyProgram> findActivePrograms();
    
    @Query("SELECT COUNT(lp) FROM LoyaltyProgram lp WHERE lp.tier = :tier")
    Long countByTier(LoyaltyTier tier);
}
