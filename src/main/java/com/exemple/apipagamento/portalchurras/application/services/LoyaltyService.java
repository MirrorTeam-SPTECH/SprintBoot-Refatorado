package com.exemple.apipagamento.portalchurras.application.services;

import com.exemple.apipagamento.portalchurras.domain.entities.*;
import com.exemple.apipagamento.portalchurras.domain.ports.UserRepository;
import com.exemple.apipagamento.portalchurras.infrastructure.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LoyaltyService {

    private final LoyaltyProgramRepository loyaltyProgramRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final UserRepository userRepository;
    
    // Configuração: 1 real = 10 pontos
    private static final BigDecimal POINTS_PER_REAL = new BigDecimal("10");
    
    public LoyaltyService(LoyaltyProgramRepository loyaltyProgramRepository,
                         LoyaltyTransactionRepository loyaltyTransactionRepository,
                         UserRepository userRepository) {
        this.loyaltyProgramRepository = loyaltyProgramRepository;
        this.loyaltyTransactionRepository = loyaltyTransactionRepository;
        this.userRepository = userRepository;
    }

    public LoyaltyProgram createLoyaltyProgram(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
            
        if (loyaltyProgramRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("Usuário já possui programa de fidelidade");
        }
        
        LoyaltyProgram program = new LoyaltyProgram(user);
        return loyaltyProgramRepository.save(program);
    }

    public LoyaltyProgram earnPoints(Long userId, Order order) {
        LoyaltyProgram program = loyaltyProgramRepository.findByUserId(userId)
            .orElseGet(() -> createLoyaltyProgram(userId));
        
        // Calcular pontos baseados no valor do pedido
        Integer basePoints = order.getTotal()
            .multiply(POINTS_PER_REAL)
            .intValue();
        
        // Aplicar multiplicador baseado no tier
        Integer pointsToAdd = basePoints * program.getPointsMultiplier();
        
        // Adicionar pontos
        program.addPoints(pointsToAdd, order.getTotal());
        
        // Criar transação
        String description = String.format("Pedido #%d - %d pontos (x%d)", 
            order.getId(), pointsToAdd, program.getPointsMultiplier());
        LoyaltyTransaction transaction = new LoyaltyTransaction(
            program, order, LoyaltyTransaction.TransactionType.EARNED, 
            pointsToAdd, description
        );
        
        loyaltyTransactionRepository.save(transaction);
        return loyaltyProgramRepository.save(program);
    }

    public LoyaltyProgram redeemPoints(Long userId, Integer points, String reason) {
        LoyaltyProgram program = loyaltyProgramRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Programa de fidelidade não encontrado"));
        
        program.usePoints(points);
        
        // Criar transação
        LoyaltyTransaction transaction = new LoyaltyTransaction(
            program, null, LoyaltyTransaction.TransactionType.REDEEMED, 
            points, reason
        );
        
        loyaltyTransactionRepository.save(transaction);
        return loyaltyProgramRepository.save(program);
    }

    public Optional<LoyaltyProgram> findByUserId(Long userId) {
        return loyaltyProgramRepository.findByUserId(userId);
    }

    public List<LoyaltyTransaction> getUserTransactions(Long userId) {
        LoyaltyProgram program = loyaltyProgramRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Programa de fidelidade não encontrado"));
        
        return loyaltyTransactionRepository.findByLoyaltyProgramIdOrderByCreatedAtDesc(program.getId());
    }

    public BigDecimal calculateDiscount(Long userId, BigDecimal orderTotal) {
        Optional<LoyaltyProgram> program = loyaltyProgramRepository.findByUserId(userId);
        
        if (program.isPresent()) {
            BigDecimal discountPercentage = program.get().getDiscountPercentage();
            return orderTotal.multiply(discountPercentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        
        return BigDecimal.ZERO;
    }

    public void addBonusPoints(Long userId, Integer points, String reason) {
        LoyaltyProgram program = loyaltyProgramRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Programa de fidelidade não encontrado"));
        
        program.addPoints(points, BigDecimal.ZERO);
        
        LoyaltyTransaction transaction = new LoyaltyTransaction(
            program, null, LoyaltyTransaction.TransactionType.BONUS, 
            points, reason
        );
        
        loyaltyTransactionRepository.save(transaction);
        loyaltyProgramRepository.save(program);
    }
}