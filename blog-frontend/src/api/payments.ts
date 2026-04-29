import request from '@/utils/request'
import type { Order, Wallet, WalletTransaction, PaginatedResponse } from '@/types'

export const getMyOrders = (params?: any) => {
  return request.get<PaginatedResponse<Order>>('/payments/orders/', { params })
}

export const createOrder = (planId: number) => {
  return request.post<Order>('/payments/orders/', { plan_id: planId })
}

export const cancelOrder = (orderId: number) => {
  return request.post<Order>(`/payments/orders/${orderId}/cancel/`)
}

export const payOrder = (orderId: number, paymentMethod: string = 'alipay') => {
  return request.post<Order>(`/payments/orders/${orderId}/pay/`, { payment_method: paymentMethod })
}

export const getMyWallet = () => {
  return request.get<Wallet>('/payments/wallet/my_wallet/')
}

export const getMyTransactions = (params?: any) => {
  return request.get<PaginatedResponse<WalletTransaction>>('/payments/transactions/', { params })
}
