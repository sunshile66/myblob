import request from '@/utils/request'
import type { MembershipPlan, UserMembership } from '@/types'

export const getMembershipPlans = () => {
  return request.get<MembershipPlan[]>('/membership/plans/')
}

export const getMyMembership = () => {
  return request.get<UserMembership>('/membership/user/my_membership/')
}
