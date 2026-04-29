<template>
  <SimpleLayout>
    <div class="membership-center">
      <div class="page-header">
        <h1>会员中心</h1>
      </div>

      <div v-if="loading" class="loading-section">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else class="membership-content">
        <div class="membership-status-card">
          <div class="status-header">
            <h3>我的会员</h3>
          </div>
          <div
            v-if="myMembership && myMembership.is_valid"
            class="status-content"
          >
            <div class="plan-info">
              <el-tag type="success" size="large">{{
                myMembership.plan?.name
              }}</el-tag>
              <div v-if="myMembership.is_lifetime" class="lifetime-badge">
                永久会员
              </div>
            </div>
            <div v-if="!myMembership.is_lifetime" class="expire-info">
              <span>有效期至：{{ formatDate(myMembership.end_time) }}</span>
              <span class="remaining-days"
                >剩余 {{ myMembership.remaining_days }} 天</span
              >
            </div>
          </div>
          <div v-else class="no-membership">
            <el-icon :size="64"><Crown /></el-icon>
            <p>您还不是会员</p>
            <p class="hint">开通会员，解锁更多精彩功能</p>
          </div>
        </div>

        <div class="plans-section">
          <h2>选择套餐</h2>
          <div class="plans-grid">
            <div
              v-for="plan in plans"
              :key="plan.id"
              class="plan-card"
              :class="{ popular: plan.is_popular }"
            >
              <div v-if="plan.is_popular" class="popular-badge">
                {{ plan.badge_text || "推荐" }}
              </div>
              <div class="plan-header">
                <h3>{{ plan.name }}</h3>
                <div class="price">
                  <span class="currency">¥</span>
                  <span class="amount">{{ plan.price }}</span>
                </div>
                <div class="duration">
                  {{
                    plan.duration_days === 0
                      ? "永久"
                      : `${plan.duration_days}天`
                  }}
                </div>
              </div>
              <div class="plan-features">
                <div
                  v-for="(feature, index) in plan.features"
                  :key="index"
                  class="feature-item"
                >
                  <el-icon><SuccessFilled /></el-icon>
                  <span>{{ feature }}</span>
                </div>
                <div
                  v-for="relation in plan.benefit_relations"
                  :key="relation.id"
                  class="feature-item"
                >
                  <el-icon><SuccessFilled /></el-icon>
                  <span
                    >{{ relation.benefit.name
                    }}{{ relation.value ? `：${relation.value}` : "" }}</span
                  >
                </div>
              </div>
              <el-button
                type="primary"
                class="buy-btn"
                size="large"
                @click="handleBuyPlan(plan)"
              >
                立即购买
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Crown, SuccessFilled } from "@element-plus/icons-vue";
import SimpleLayout from "@/layout/SimpleLayout.vue";
import { getMembershipPlans, getMyMembership } from "@/api/membership";
import { createOrder, payOrder } from "@/api/payments";
import type { MembershipPlan, UserMembership, Order } from "@/types";
import { useUserStore } from "@/store/user";

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const purchasing = ref(false);
const plans = ref<MembershipPlan[]>([]);
const myMembership = ref<UserMembership | null>(null);

const formatDate = (dateStr?: string) => {
  if (!dateStr) return "";
  return new Date(dateStr).toLocaleDateString("zh-CN");
};

const loadData = async () => {
  loading.value = true;
  try {
    const [plansRes, membershipRes] = await Promise.all([
      getMembershipPlans(),
      getMyMembership(),
    ]);
    plans.value = plansRes;
    myMembership.value = membershipRes;
  } catch (error) {
    console.error("Failed to load data:", error);
    ElMessage.error("加载数据失败");
  } finally {
    loading.value = false;
  }
};

const handleBuyPlan = async (plan: MembershipPlan) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning("请先登录后再购买");
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定购买「${plan.name}」套餐吗？\n价格：¥${plan.price}`,
      "确认购买",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info",
      }
    );

    purchasing.value = true;
    ElMessage.info("正在创建订单...");

    const order = await createOrder(plan.id);
    ElMessage.success("订单创建成功，正在支付...");

    await payOrder(order.id, "alipay");
    ElMessage.success("支付成功！会员已激活");

    await loadData();
  } catch (error: any) {
    if (error !== "cancel") {
      console.error("Failed to purchase plan:", error);
      ElMessage.error(error.message || "购买失败，请重试");
    }
  } finally {
    purchasing.value = false;
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.membership-center {
  max-width: 1200px;
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

.membership-status-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 48px;
  color: white;
}

.status-header h3 {
  margin: 0 0 24px 0;
  font-size: 20px;
  font-weight: 600;
}

.status-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.plan-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.lifetime-badge {
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
}

.expire-info {
  display: flex;
  align-items: center;
  gap: 24px;
  font-size: 14px;
  opacity: 0.9;
}

.remaining-days {
  font-weight: 600;
  opacity: 1;
}

.no-membership {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 24px 0;
}

.no-membership .el-icon {
  opacity: 0.8;
}

.no-membership p {
  margin: 0;
  font-size: 16px;
}

.no-membership .hint {
  font-size: 14px;
  opacity: 0.8;
}

.plans-section h2 {
  margin: 0 0 24px 0;
  font-size: 22px;
  font-weight: 600;
  color: var(--theme-text);
}

.plans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.plan-card {
  background: var(--theme-card);
  border-radius: 16px;
  padding: 32px 24px;
  position: relative;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.plan-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.plan-card.popular {
  border-color: var(--theme-primary);
}

.popular-badge {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--theme-primary);
  color: white;
  padding: 4px 16px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.plan-header {
  text-align: center;
  margin-bottom: 24px;
}

.plan-header h3 {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--theme-text);
}

.price {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
  margin-bottom: 8px;
}

.currency {
  font-size: 20px;
  font-weight: 600;
  color: var(--theme-primary);
}

.amount {
  font-size: 36px;
  font-weight: 700;
  color: var(--theme-primary);
}

.duration {
  color: var(--theme-text-secondary);
  font-size: 14px;
}

.plan-features {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--theme-text);
  font-size: 14px;
}

.feature-item .el-icon {
  color: #67c23a;
  flex-shrink: 0;
}

.buy-btn {
  width: 100%;
}
</style>
