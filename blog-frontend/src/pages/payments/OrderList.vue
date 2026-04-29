<template>
  <SimpleLayout>
    <div class="order-list-page">
      <div class="page-header">
        <h1>我的订单</h1>
      </div>

      <div v-if="loading" class="loading-section">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="orders.length === 0" class="empty-state">
        <el-icon :size="64"><Document /></el-icon>
        <p>暂无订单</p>
      </div>

      <div v-else class="orders-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-header">
            <div class="order-no">订单号：{{ order.order_no }}</div>
            <el-tag :type="getStatusType(order.status)">{{
              order.status_display
            }}</el-tag>
          </div>
          <div class="order-content">
            <div v-if="order.plan" class="plan-info">
              <el-icon><Box /></el-icon>
              <span>{{ order.plan.name }}</span>
            </div>
            <div class="order-info">
              <div class="info-item">
                <span class="label">金额</span>
                <span class="value amount">¥{{ order.amount }}</span>
              </div>
              <div v-if="order.payment_method_display" class="info-item">
                <span class="label">支付方式</span>
                <span class="value">{{ order.payment_method_display }}</span>
              </div>
              <div v-if="order.paid_time" class="info-item">
                <span class="label">支付时间</span>
                <span class="value">{{ formatDate(order.paid_time) }}</span>
              </div>
              <div class="info-item">
                <span class="label">创建时间</span>
                <span class="value">{{ formatDate(order.created_at) }}</span>
              </div>
            </div>
          </div>
          <div v-if="order.status === 'pending'" class="order-footer">
            <el-button type="primary" @click="handlePay(order)"
              >立即支付</el-button
            >
            <el-button @click="handleCancel(order)">取消订单</el-button>
          </div>
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Document, Box } from "@element-plus/icons-vue";
import SimpleLayout from "@/layout/SimpleLayout.vue";
import { getMyOrders, cancelOrder, payOrder } from "@/api/payments";
import type { Order } from "@/types";

const loading = ref(false);
const orders = ref<Order[]>([]);

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString("zh-CN");
};

const getStatusType = (status: string) => {
  const typeMap: Record<string, any> = {
    pending: "warning",
    paid: "success",
    cancelled: "info",
    refunded: "danger",
    expired: "info",
  };
  return typeMap[status] || "info";
};

const loadOrders = async () => {
  loading.value = true;
  try {
    const response = await getMyOrders();
    orders.value = response.results;
  } catch (error) {
    console.error("Failed to load orders:", error);
    ElMessage.error("加载订单失败");
  } finally {
    loading.value = false;
  }
};

const handlePay = async (order: Order) => {
  try {
    await ElMessageBox.confirm(
      `确定支付订单「${order.order_no}」吗？\n金额：¥${order.amount}`,
      "确认支付",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info",
      }
    );

    ElMessage.info("正在支付...");
    await payOrder(order.id, "alipay");
    ElMessage.success("支付成功！");
    await loadOrders();
  } catch (error: any) {
    if (error !== "cancel") {
      console.error("Failed to pay order:", error);
      ElMessage.error(error.message || "支付失败，请重试");
    }
  }
};

const handleCancel = async (order: Order) => {
  try {
    await ElMessageBox.confirm("确定要取消该订单吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    await cancelOrder(order.id);
    ElMessage.success("订单已取消");
    await loadOrders();
  } catch (error: any) {
    if (error !== "cancel") {
      console.error("Failed to cancel order:", error);
      ElMessage.error(error.message || "取消失败，请重试");
    }
  }
};

onMounted(() => {
  loadOrders();
});
</script>

<style scoped>
.order-list-page {
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

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 64px 24px;
  color: var(--theme-text-secondary);
}

.empty-state .el-icon {
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 16px;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: var(--theme-card);
  border-radius: 12px;
  padding: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--theme-border);
  margin-bottom: 16px;
}

.order-no {
  font-size: 14px;
  color: var(--theme-text-secondary);
}

.order-content {
  margin-bottom: 16px;
}

.plan-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 500;
  color: var(--theme-text);
}

.plan-info .el-icon {
  color: var(--theme-primary);
}

.order-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-item .label {
  font-size: 14px;
  color: var(--theme-text-secondary);
}

.info-item .value {
  font-size: 14px;
  color: var(--theme-text);
}

.info-item .value.amount {
  font-size: 18px;
  font-weight: 600;
  color: var(--theme-primary);
}

.order-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--theme-border);
}
</style>
