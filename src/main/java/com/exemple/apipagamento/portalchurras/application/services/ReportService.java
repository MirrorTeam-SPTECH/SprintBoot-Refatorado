package com.exemple.apipagamento.portalchurras.application.services;

import com.exemple.apipagamento.portalchurras.domain.entities.*;
import com.exemple.apipagamento.portalchurras.infrastructure.repositories.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final JpaOrderRepository orderRepository;
    private final JpaPaymentRepository paymentRepository;
    private final JpaUserRepository userRepository;

    public ReportService(JpaOrderRepository orderRepository,
                        JpaPaymentRepository paymentRepository,
                        JpaUserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    // Dashboard Metrics
    public DashboardMetrics getDashboardMetrics() {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime thisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        DashboardMetrics metrics = new DashboardMetrics();
        
        // Métricas do dia
        metrics.setTodayOrders(orderRepository.countOrdersSinceDate(today));
        metrics.setTodayRevenue(BigDecimal.valueOf(
            orderRepository.sumTotalSinceDate(today)
        ).setScale(2, RoundingMode.HALF_UP));
        
        // Métricas do mês
        metrics.setMonthOrders(orderRepository.countOrdersSinceDate(thisMonth));
        metrics.setMonthRevenue(BigDecimal.valueOf(
            orderRepository.sumTotalSinceDate(thisMonth)
        ).setScale(2, RoundingMode.HALF_UP));
        
        // Pedidos ativos
        metrics.setActiveOrders(orderRepository.findActiveOrdersOrderByCreatedAt().size());
        
        // Status dos pedidos
        metrics.setPendingOrders(orderRepository.countByStatus(OrderStatus.PENDING));
        metrics.setPreparingOrders(orderRepository.countByStatus(OrderStatus.IN_PREPARATION));
        metrics.setReadyOrders(orderRepository.countByStatus(OrderStatus.READY));
        
        // Pagamentos
        metrics.setApprovedPaymentsToday(paymentRepository.countApprovedPaymentsSince(today));
        metrics.setPaymentRevenueToday(BigDecimal.valueOf(
            paymentRepository.sumApprovedAmountsSince(today)
        ).setScale(2, RoundingMode.HALF_UP));
        
        // Calcular ticket médio
        if (metrics.getTodayOrders() > 0) {
            metrics.setAverageTicket(
                metrics.getTodayRevenue().divide(
                    BigDecimal.valueOf(metrics.getTodayOrders()), 
                    2, RoundingMode.HALF_UP
                )
            );
        } else {
            metrics.setAverageTicket(BigDecimal.ZERO);
        }
        
        return metrics;
    }

    // Relatório de Vendas
    public SalesReport getSalesReport(LocalDateTime startDate, LocalDateTime endDate) {
        SalesReport report = new SalesReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        
        // Buscar pedidos no período
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        
        // Filtrar apenas pedidos completados
        List<Order> completedOrders = orders.stream()
            .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
            .collect(Collectors.toList());
        
        report.setTotalOrders(completedOrders.size());
        report.setCancelledOrders(orders.size() - completedOrders.size());
        
        // Calcular receita total
        BigDecimal totalRevenue = completedOrders.stream()
            .map(Order::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalRevenue(totalRevenue);
        
        // Ticket médio
        if (!completedOrders.isEmpty()) {
            report.setAverageTicket(
                totalRevenue.divide(
                    BigDecimal.valueOf(completedOrders.size()), 
                    2, RoundingMode.HALF_UP
                )
            );
        }
        
        // Vendas por categoria
        Map<MenuCategory, BigDecimal> salesByCategory = new HashMap<>();
        Map<MenuCategory, Integer> quantityByCategory = new HashMap<>();
        
        for (Order order : completedOrders) {
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (item.getMenuItem() != null) {
                        MenuCategory category = item.getMenuItem().getCategory();
                        salesByCategory.merge(category, 
                            item.getTotalPrice(), 
                            BigDecimal::add);
                        quantityByCategory.merge(category, 
                            item.getQuantity(), 
                            Integer::sum);
                    }
                }
            }
        }
        
        report.setSalesByCategory(salesByCategory);
        report.setQuantityByCategory(quantityByCategory);
        
        // Top 10 produtos mais vendidos
        Map<String, Integer> productSales = new HashMap<>();
        for (Order order : completedOrders) {
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (item.getMenuItem() != null) {
                        productSales.merge(
                            item.getMenuItem().getName(),
                            item.getQuantity(),
                            Integer::sum
                        );
                    }
                }
            }
        }
        
        List<ProductSalesDTO> topProducts = productSales.entrySet().stream()
            .map(e -> new ProductSalesDTO(e.getKey(), e.getValue()))
            .sorted((a, b) -> b.getQuantity().compareTo(a.getQuantity()))
            .limit(10)
            .collect(Collectors.toList());
        
        report.setTopProducts(topProducts);
        
        // Vendas por dia da semana
        Map<String, BigDecimal> salesByDayOfWeek = completedOrders.stream()
            .collect(Collectors.groupingBy(
                o -> o.getCreatedAt().getDayOfWeek().toString(),
                Collectors.reducing(
                    BigDecimal.ZERO,
                    Order::getTotal,
                    BigDecimal::add
                )
            ));
        report.setSalesByDayOfWeek(salesByDayOfWeek);
        
        // Vendas por hora do dia
        Map<Integer, BigDecimal> salesByHour = completedOrders.stream()
            .collect(Collectors.groupingBy(
                o -> o.getCreatedAt().getHour(),
                Collectors.reducing(
                    BigDecimal.ZERO,
                    Order::getTotal,
                    BigDecimal::add
                )
            ));
        report.setSalesByHour(salesByHour);
        
        return report;
    }

    // Relatório de Clientes
    public CustomerReport getCustomerReport(LocalDateTime startDate, LocalDateTime endDate) {
        CustomerReport report = new CustomerReport();
        
        // Total de clientes
        report.setTotalCustomers(userRepository.countByRole(UserRole.CUSTOMER));
        
        // Novos clientes no período
        List<User> newCustomers = userRepository.findAll().stream()
            .filter(u -> u.getRole() == UserRole.CUSTOMER)
            .filter(u -> u.getCreatedAt().isAfter(startDate) && 
                        u.getCreatedAt().isBefore(endDate))
            .collect(Collectors.toList());
        report.setNewCustomers(newCustomers.size());
        
        // Clientes ativos (fizeram pedidos no período)
        Set<String> activeCustomerEmails = new HashSet<>();
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        
        for (Order order : orders) {
            if (order.getCustomerEmail() != null) {
                activeCustomerEmails.add(order.getCustomerEmail());
            }
        }
        report.setActiveCustomers(activeCustomerEmails.size());
        
        // Top clientes por valor gasto
        Map<String, BigDecimal> customerSpending = new HashMap<>();
        Map<String, Integer> customerOrders = new HashMap<>();
        
        for (Order order : orders) {
            if (order.getCustomerEmail() != null && 
                order.getStatus() != OrderStatus.CANCELLED) {
                customerSpending.merge(
                    order.getCustomerEmail(),
                    order.getTotal(),
                    BigDecimal::add
                );
                customerOrders.merge(
                    order.getCustomerEmail(),
                    1,
                    Integer::sum
                );
            }
        }
        
        List<TopCustomerDTO> topCustomers = customerSpending.entrySet().stream()
            .map(e -> new TopCustomerDTO(
                e.getKey(),
                e.getValue(),
                customerOrders.get(e.getKey())
            ))
            .sorted((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent()))
            .limit(20)
            .collect(Collectors.toList());
        
        report.setTopCustomers(topCustomers);
        
        // Taxa de retenção
        int returningCustomers = 0;
        for (String email : activeCustomerEmails) {
            List<Order> customerOrderList = orderRepository.findByCustomerEmail(email);
            if (customerOrderList.size() > 1) {
                returningCustomers++;
            }
        }
        
        if (!activeCustomerEmails.isEmpty()) {
            report.setRetentionRate(
                (double) returningCustomers / activeCustomerEmails.size() * 100
            );
        }
        
        return report;
    }

    // DTOs internos
    public static class DashboardMetrics {
        private Long todayOrders;
        private BigDecimal todayRevenue;
        private Long monthOrders;
        private BigDecimal monthRevenue;
        private Integer activeOrders;
        private Long pendingOrders;
        private Long preparingOrders;
        private Long readyOrders;
        private Long approvedPaymentsToday;
        private BigDecimal paymentRevenueToday;
        private BigDecimal averageTicket;

        // Getters and Setters
        public Long getTodayOrders() { return todayOrders; }
        public void setTodayOrders(Long todayOrders) { this.todayOrders = todayOrders; }
        
        public BigDecimal getTodayRevenue() { return todayRevenue; }
        public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }
        
        public Long getMonthOrders() { return monthOrders; }
        public void setMonthOrders(Long monthOrders) { this.monthOrders = monthOrders; }
        
        public BigDecimal getMonthRevenue() { return monthRevenue; }
        public void setMonthRevenue(BigDecimal monthRevenue) { this.monthRevenue = monthRevenue; }
        
        public Integer getActiveOrders() { return activeOrders; }
        public void setActiveOrders(Integer activeOrders) { this.activeOrders = activeOrders; }
        
        public Long getPendingOrders() { return pendingOrders; }
        public void setPendingOrders(Long pendingOrders) { this.pendingOrders = pendingOrders; }
        
        public Long getPreparingOrders() { return preparingOrders; }
        public void setPreparingOrders(Long preparingOrders) { this.preparingOrders = preparingOrders; }
        
        public Long getReadyOrders() { return readyOrders; }
        public void setReadyOrders(Long readyOrders) { this.readyOrders = readyOrders; }
        
        public Long getApprovedPaymentsToday() { return approvedPaymentsToday; }
        public void setApprovedPaymentsToday(Long approvedPaymentsToday) { 
            this.approvedPaymentsToday = approvedPaymentsToday; 
        }
        
        public BigDecimal getPaymentRevenueToday() { return paymentRevenueToday; }
        public void setPaymentRevenueToday(BigDecimal paymentRevenueToday) { 
            this.paymentRevenueToday = paymentRevenueToday; 
        }
        
        public BigDecimal getAverageTicket() { return averageTicket; }
        public void setAverageTicket(BigDecimal averageTicket) { this.averageTicket = averageTicket; }
    }

    public static class SalesReport {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer totalOrders;
        private Integer cancelledOrders;
        private BigDecimal totalRevenue;
        private BigDecimal averageTicket;
        private Map<MenuCategory, BigDecimal> salesByCategory;
        private Map<MenuCategory, Integer> quantityByCategory;
        private List<ProductSalesDTO> topProducts;
        private Map<String, BigDecimal> salesByDayOfWeek;
        private Map<Integer, BigDecimal> salesByHour;

        // Getters and Setters
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
        
        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
        
        public Integer getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
        
        public Integer getCancelledOrders() { return cancelledOrders; }
        public void setCancelledOrders(Integer cancelledOrders) { this.cancelledOrders = cancelledOrders; }
        
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
        
        public BigDecimal getAverageTicket() { return averageTicket; }
        public void setAverageTicket(BigDecimal averageTicket) { this.averageTicket = averageTicket; }
        
        public Map<MenuCategory, BigDecimal> getSalesByCategory() { return salesByCategory; }
        public void setSalesByCategory(Map<MenuCategory, BigDecimal> salesByCategory) { 
            this.salesByCategory = salesByCategory; 
        }
        
        public Map<MenuCategory, Integer> getQuantityByCategory() { return quantityByCategory; }
        public void setQuantityByCategory(Map<MenuCategory, Integer> quantityByCategory) { 
            this.quantityByCategory = quantityByCategory; 
        }
        
        public List<ProductSalesDTO> getTopProducts() { return topProducts; }
        public void setTopProducts(List<ProductSalesDTO> topProducts) { this.topProducts = topProducts; }
        
        public Map<String, BigDecimal> getSalesByDayOfWeek() { return salesByDayOfWeek; }
        public void setSalesByDayOfWeek(Map<String, BigDecimal> salesByDayOfWeek) { 
            this.salesByDayOfWeek = salesByDayOfWeek; 
        }
        
        public Map<Integer, BigDecimal> getSalesByHour() { return salesByHour; }
        public void setSalesByHour(Map<Integer, BigDecimal> salesByHour) { 
            this.salesByHour = salesByHour; 
        }
    }

    public static class CustomerReport {
        private Long totalCustomers;
        private Integer newCustomers;
        private Integer activeCustomers;
        private List<TopCustomerDTO> topCustomers;
        private Double retentionRate;

        // Getters and Setters
        public Long getTotalCustomers() { return totalCustomers; }
        public void setTotalCustomers(Long totalCustomers) { this.totalCustomers = totalCustomers; }
        
        public Integer getNewCustomers() { return newCustomers; }
        public void setNewCustomers(Integer newCustomers) { this.newCustomers = newCustomers; }
        
        public Integer getActiveCustomers() { return activeCustomers; }
        public void setActiveCustomers(Integer activeCustomers) { this.activeCustomers = activeCustomers; }
        
        public List<TopCustomerDTO> getTopCustomers() { return topCustomers; }
        public void setTopCustomers(List<TopCustomerDTO> topCustomers) { this.topCustomers = topCustomers; }
        
        public Double getRetentionRate() { return retentionRate; }
        public void setRetentionRate(Double retentionRate) { this.retentionRate = retentionRate; }
    }

    public static class ProductSalesDTO {
        private String productName;
        private Integer quantity;

        public ProductSalesDTO(String productName, Integer quantity) {
            this.productName = productName;
            this.quantity = quantity;
        }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class TopCustomerDTO {
        private String email;
        private BigDecimal totalSpent;
        private Integer totalOrders;
        private Long monthOrders;
        private BigDecimal monthRevenue;
        private BigDecimal todayRevenue;
        private Integer activeOrders;
        private Long pendingOrders;
        private Long preparingOrders;
        private Long readyOrders;
        private Long approvedPaymentsToday;
        private BigDecimal paymentRevenueToday;

        public TopCustomerDTO(String email, BigDecimal totalSpent, Integer totalOrders) {
            this.email = email;
            this.totalSpent = totalSpent;
            this.totalOrders = totalOrders;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public BigDecimal getTotalSpent() { return totalSpent; }
        public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }
        
        public Integer getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }

        
        public Long getMonthOrders() { return monthOrders; }
        public void setMonthOrders(Long monthOrders) { this.monthOrders = monthOrders; }
        
        public BigDecimal getMonthRevenue() { return monthRevenue; }
        public void setMonthRevenue(BigDecimal monthRevenue) { this.monthRevenue = monthRevenue; }
        
        public Integer getActiveOrders() { return activeOrders; }
        public void setActiveOrders(Integer activeOrders) { this.activeOrders = activeOrders; }
        
        public Long getPendingOrders() { return pendingOrders; }
        public void setPendingOrders(Long pendingOrders) { this.pendingOrders = pendingOrders; }
        
        public Long getPreparingOrders() { return preparingOrders; }
        public void setPreparingOrders(Long preparingOrders) { this.preparingOrders = preparingOrders; }
        
        public Long getReadyOrders() { return readyOrders; }
        public void setReadyOrders(Long readyOrders) { this.readyOrders = readyOrders; }
        
        public Long getApprovedPaymentsToday() { return approvedPaymentsToday; }
        public void setApprovedPaymentsToday(Long approvedPaymentsToday) { 
            this.approvedPaymentsToday = approvedPaymentsToday; 
        }
        
        public BigDecimal getPaymentRevenueToday() { return paymentRevenueToday; }
        public void setPaymentRevenueToday(BigDecimal paymentRevenueToday) { 
            this.paymentRevenueToday = paymentRevenueToday; 
        }
        
        public BigDecimal getTodayRevenue() { return todayRevenue; }
        public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }
    }
}