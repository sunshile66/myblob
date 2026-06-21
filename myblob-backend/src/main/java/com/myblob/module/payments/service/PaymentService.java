package com.myblob.module.payments.service;

import com.myblob.common.BusinessException;
import com.myblob.common.PageResponse;
import com.myblob.module.accounts.entity.User;
import com.myblob.module.accounts.repository.UserRepository;
import com.myblob.module.membership.entity.MembershipPlan;
import com.myblob.module.membership.entity.UserMembership;
import com.myblob.module.membership.repository.MembershipPlanRepository;
import com.myblob.module.membership.repository.UserMembershipRepository;
import com.myblob.module.payments.dto.OrderDTO;
import com.myblob.module.payments.dto.WalletDTO;
import com.myblob.module.payments.dto.WalletTransactionDTO;
import com.myblob.module.payments.entity.*;
import com.myblob.module.payments.repository.*;
import com.myblob.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final MembershipPlanRepository planRepository;
    private final UserMembershipRepository userMembershipRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<OrderDTO> getMyOrders(int page, int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDTO> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toOrderDTO);
        return PageResponse.of(orders);
    }

    @Transactional
    public OrderDTO createOrder(Long planId) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);

        MembershipPlan plan = planRepository.findById(planId)
                .filter(MembershipPlan::getActive)
                .orElseThrow(() -> BusinessException.notFound("会员套餐"));

        Order order = Order.builder()
                .orderNo("ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6))
                .user(user)
                .plan(plan)
                .amount(plan.getPrice())
                .status(Order.OrderStatus.PENDING)
                .expiredTime(LocalDateTime.now().plusHours(24))
                .build();

        order = orderRepository.save(order);
        return toOrderDTO(order);
    }

    @Transactional
    public OrderDTO payOrder(Long orderId, String paymentMethod) {
        Long userId = SecurityUtil.getCurrentUserId();
        // 使用悲观锁防止并发重复支付
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> BusinessException.notFound("订单"));

        if (!order.getUser().getId().equals(userId)) {
            throw BusinessException.forbidden("无权支付此订单");
        }
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BusinessException("订单状态异常，无法支付");
        }
        if (order.isExpired()) {
            order.setStatus(Order.OrderStatus.EXPIRED);
            orderRepository.save(order);
            throw new BusinessException("订单已过期，请重新下单");
        }

        Order.PaymentMethod method;
        try {
            method = Order.PaymentMethod.valueOf(paymentMethod.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("不支持的支付方式: " + paymentMethod);
        }

        Payment payment = Payment.builder()
                .order(order)
                .paymentNo("PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8))
                .amount(order.getAmount())
                .channel(paymentMethod)
                .status(Payment.PaymentStatus.SUCCESS)
                .paidTime(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        order.setStatus(Order.OrderStatus.PAID);
        order.setPaymentMethod(method);
        order.setPaidTime(LocalDateTime.now());
        orderRepository.save(order);

        activateMembership(order);
        return toOrderDTO(order);
    }

    @Transactional
    public OrderDTO cancelOrder(Long orderId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> BusinessException.notFound("订单"));

        if (!order.getUser().getId().equals(userId)) {
            throw BusinessException.forbidden("无权取消此订单");
        }
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BusinessException("只有待支付订单才能取消");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
        return toOrderDTO(order);
    }

    @Transactional
    public WalletDTO getMyWallet() {
        Long userId = SecurityUtil.getCurrentUserId();
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wallet w = Wallet.builder()
                            .user(userRepository.getReferenceById(userId))
                            .build();
                    return walletRepository.save(w);
                });
        return toWalletDTO(wallet);
    }

    @Transactional(readOnly = true)
    public PageResponse<WalletTransactionDTO> getMyWalletTransactions(int page, int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<WalletTransactionDTO> transactions = walletTransactionRepository
                .findByWalletUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toTransactionDTO);
        return PageResponse.of(transactions);
    }

    private void activateMembership(Order order) {
        if (order.getPlan() == null) return;

        UserMembership membership = userMembershipRepository.findByUserId(order.getUser().getId())
                .orElseGet(() -> UserMembership.builder()
                        .user(order.getUser())
                        .build());

        membership.setPlan(order.getPlan());

        if (order.getPlan().getDurationDays() == 0) {
            membership.setLifetime(true);
            membership.setStartTime(LocalDateTime.now());
            membership.setEndTime(null);
        } else {
            membership.setLifetime(false);
            if (membership.getEndTime() != null && membership.getEndTime().isAfter(LocalDateTime.now())) {
                membership.setEndTime(membership.getEndTime().plusDays(order.getPlan().getDurationDays()));
            } else {
                membership.setStartTime(LocalDateTime.now());
                membership.setEndTime(LocalDateTime.now().plusDays(order.getPlan().getDurationDays()));
            }
        }

        membership.setActive(true);
        userMembershipRepository.save(membership);
    }

    private OrderDTO toOrderDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUser().getId())
                .planId(order.getPlan() != null ? order.getPlan().getId() : null)
                .amount(order.getAmount())
                .status(order.getStatus().name().toLowerCase())
                .statusDisplay(getStatusDisplay(order.getStatus()))
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name().toLowerCase() : null)
                .paymentMethodDisplay(order.getPaymentMethod() != null ? getMethodDisplay(order.getPaymentMethod()) : null)
                .paidTime(order.getPaidTime())
                .expiredTime(order.getExpiredTime())
                .remark(order.getRemark())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private WalletDTO toWalletDTO(Wallet wallet) {
        return WalletDTO.builder()
                .id(wallet.getId())
                .userId(wallet.getUser().getId())
                .balance(wallet.getBalance())
                .frozenBalance(wallet.getFrozenBalance())
                .availableBalance(wallet.getAvailableBalance())
                .totalIncome(wallet.getTotalIncome())
                .totalExpense(wallet.getTotalExpense())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    private WalletTransactionDTO toTransactionDTO(WalletTransaction tx) {
        return WalletTransactionDTO.builder()
                .id(tx.getId())
                .walletId(tx.getWallet().getId())
                .transactionNo(tx.getTransactionNo())
                .transactionType(tx.getTransactionType().name().toLowerCase())
                .transactionTypeDisplay(getTxTypeDisplay(tx.getTransactionType()))
                .amount(tx.getAmount())
                .balanceBefore(tx.getBalanceBefore())
                .balanceAfter(tx.getBalanceAfter())
                .description(tx.getDescription())
                .relatedOrderId(tx.getRelatedOrder() != null ? tx.getRelatedOrder().getId() : null)
                .createdAt(tx.getCreatedAt())
                .build();
    }

    private String getStatusDisplay(Order.OrderStatus status) {
        return switch (status) {
            case PENDING -> "待支付";
            case PAID -> "已支付";
            case CANCELLED -> "已取消";
            case REFUNDED -> "已退款";
            case EXPIRED -> "已过期";
        };
    }

    private String getMethodDisplay(Order.PaymentMethod method) {
        return switch (method) {
            case ALIPAY -> "支付宝";
            case WECHAT -> "微信支付";
            case BALANCE -> "余额支付";
        };
    }

    private String getTxTypeDisplay(WalletTransaction.TransactionType type) {
        return switch (type) {
            case RECHARGE -> "充值";
            case CONSUME -> "消费";
            case REFUND -> "退款";
            case INCOME -> "收入";
        };
    }
}
