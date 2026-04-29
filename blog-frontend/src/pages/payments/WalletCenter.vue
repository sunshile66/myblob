<template>
  <SimpleLayout>
    <div class="wallet-center">
      <div class="page-header">
        <h1>我的钱包</h1>
      </div>

      <div v-if="loading" class="loading-section">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else class="wallet-content">
        <div class="wallet-card">
          <div class="balance-section">
            <div class="balance-label">可用余额</div>
            <div class="balance-amount">¥{{ wallet?.available_balance || '0.00' }}</div>
            <div class="balance-info">
              <span>余额：¥{{ wallet?.balance || '0.00' }}</span>
              <span>冻结：¥{{ wallet?.frozen_balance || '0.00' }}</span>
            </div>
          </div>
          <div class="wallet-actions">
            <el-button type="primary" size="large" @click="handleRecharge">充值</el-button>
            <el-button size="large" @click="handleWithdraw">提现</el-button>
          </div>
        </div>

        <div class="wallet-stats">
          <div class="stat-item">
            <div class="stat-label">累计收入</div>
            <div class="stat-value">¥{{ wallet?.total_income || '0.00' }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">累计支出</div>
            <div class="stat-value">¥{{ wallet?.total_expense || '0.00' }}</div>
          </div>
        </div>

        <div class="transactions-section">
          <h2>交易记录</h2>
          <div v-if="transactionsLoading" class="loading-section">
            <el-skeleton :rows="5" animated />
          </div>
          <div v-else-if="transactions.length === 0" class="empty-state">
            <el-icon :size="48"><Tickets /></el-icon>
            <p>暂无交易记录</p>
          </div>
          <div v-else class="transactions-list">
            <div v-for="transaction in transactions" :key="transaction.id" class="transaction-item">
              <div class="transaction-icon" :class="getTransactionTypeClass(transaction.transaction_type)">
                <el-icon><component :is="getTransactionIcon(transaction.transaction_type)" /></el-icon>
              </div>
              <div class="transaction-info">
                <div class="transaction-type">{{ transaction.transaction_type_display }}</div>
                <div class="transaction-time">{{ formatDate(transaction.created_at) }}</div>
                <div v-if="transaction.description" class="transaction-desc">{{ transaction.description }}</div>
              </div>
              <div class="transaction-amount" :class="getAmountClass(transaction.transaction_type)">
                {{ getAmountPrefix(transaction.transaction_type) }}¥{{ transaction.amount }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Tickets, Plus, Minus, RefreshLeft, Money } from '@element-plus/icons-vue'
import SimpleLayout from '@/layout/SimpleLayout.vue'
import { getMyWallet, getMyTransactions } from '@/api/payments'
import type { Wallet, WalletTransaction } from '@/types'

const loading = ref(false)
const transactionsLoading = ref(false)
const wallet = ref<Wallet | null>(null)
const transactions = ref<WalletTransaction[]>([])

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

const getTransactionTypeClass = (type: string) => {
  const classMap: Record<string, string> = {
    recharge: 'income',
    consume: 'expense',
    refund: 'refund',
    income: 'income',
  }
  return classMap[type] || ''
}

const getTransactionIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    recharge: Plus,
    consume: Minus,
    refund: RefreshLeft,
    income: Money,
  }
  return iconMap[type] || Money
}

const getAmountClass = (type: string) => {
  const classMap: Record<string, string> = {
    recharge: 'income',
    consume: 'expense',
    refund: 'income',
    income: 'income',
  }
  return classMap[type] || ''
}

const getAmountPrefix = (type: string) => {
  const prefixMap: Record<string, string> = {
    recharge: '+',
    consume: '-',
    refund: '+',
    income: '+',
  }
  return prefixMap[type] || ''
}

const loadWallet = async () => {
  loading.value = true
  try {
    wallet.value = await getMyWallet()
  } catch (error) {
    console.error('Failed to load wallet:', error)
    ElMessage.error('加载钱包失败')
  } finally {
    loading.value = false
  }
}

const loadTransactions = async () => {
  transactionsLoading.value = true
  try {
    const response = await getMyTransactions()
    transactions.value = response.results
  } catch (error) {
    console.error('Failed to load transactions:', error)
    ElMessage.error('加载交易记录失败')
  } finally {
    transactionsLoading.value = false
  }
}

const handleRecharge = () => {
  ElMessage.info('充值功能开发中')
}

const handleWithdraw = () => {
  ElMessage.info('提现功能开发中')
}

onMounted(() => {
  loadWallet()
  loadTransactions()
})
</script>

<style scoped>
.wallet-center {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 24px;
}

.page-header {
  margin-bottom: 32px;
}

.page-header h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: var(--theme-text);
}

.loading-section {
  padding: 24px;
}

.wallet-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 24px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.balance-section {
  flex: 1;
}

.balance-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.balance-amount {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 12px;
}

.balance-info {
  display: flex;
  gap: 24px;
  font-size: 14px;
  opacity: 0.9;
}

.wallet-actions {
  display: flex;
  gap: 12px;
}

.wallet-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.stat-item {
  background: var(--theme-card);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: var(--theme-text-secondary);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--theme-text);
}

.transactions-section h2 {
  margin: 0 0 24px 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--theme-text);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 24px;
  color: var(--theme-text-secondary);
}

.empty-state .el-icon {
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

.transactions-list {
  background: var(--theme-card);
  border-radius: 12px;
  overflow: hidden;
}

.transaction-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--theme-border);
}

.transaction-item:last-child {
  border-bottom: none;
}

.transaction-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.transaction-icon.income {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.transaction-icon.expense {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.transaction-icon.refund {
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
}

.transaction-icon .el-icon {
  font-size: 24px;
}

.transaction-info {
  flex: 1;
  min-width: 0;
}

.transaction-type {
  font-size: 15px;
  font-weight: 500;
  color: var(--theme-text);
  margin-bottom: 4px;
}

.transaction-time {
  font-size: 13px;
  color: var(--theme-text-secondary);
  margin-bottom: 2px;
}

.transaction-desc {
  font-size: 13px;
  color: var(--theme-text-secondary);
}

.transaction-amount {
  font-size: 18px;
  font-weight: 600;
}

.transaction-amount.income {
  color: #67c23a;
}

.transaction-amount.expense {
  color: #f56c6c;
}
</style>
