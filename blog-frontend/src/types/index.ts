export interface User {
  id: number
  username: string
  email: string
  nickname?: string
  avatar?: string
  bio?: string
  website?: string
  company?: string
  profession?: string
  location?: string
  phone?: string
  wechat?: string
  qq?: string
  weibo?: string
  profile?: UserProfile
  is_following?: boolean
  follower_count?: number
  following_count?: number
  post_count?: number
}

export interface UserProfile {
  id: number
  user: User
  bio?: string
  website?: string
  company?: string
  profession?: string
  location?: string
  phone?: string
  wechat?: string
  qq?: string
  weibo?: string
}

export interface Category {
  id: number
  name: string
  slug: string
  description?: string
  sort: number
  is_active: boolean
}

export interface Tag {
  id: number
  name: string
  slug: string
}

export interface MediaAsset {
  id: number
  user: User
  file: string
  media_type: string
  title?: string
  alt_text?: string
  created_at: string
}

export interface Post {
  id: number
  title: string
  slug: string
  summary?: string
  content: string
  cover?: string
  video?: MediaAsset
  post_type: string
  status: string
  is_top: boolean
  allow_comment: boolean
  is_original: boolean
  view_count: number
  comment_count: number
  like_count: number
  published_at?: string
  created_at: string
  updated_at: string
  author: User
  category?: Category
  tags: Tag[]
  is_liked?: boolean
  is_favorited?: boolean
}

export interface Comment {
  id: number
  post: Post
  user?: User
  author?: User
  parent?: Comment
  reply_to?: User
  content: string
  nickname?: string
  email?: string
  like_count?: number
  is_approved: boolean
  is_deleted: boolean
  created_at: string
  children?: Comment[]
}

export interface PostLike {
  id: number
  user: User
  post: Post
  created_at: string
}

export interface Favorite {
  id: number
  user: User
  post: Post
  created_at: string
}

export interface Notification {
  id: number
  user: User
  type: string
  content: string
  is_read: boolean
  created_at: string
}

export interface ApiResponse<T> {
  count?: number
  next?: string
  previous?: string
  results: T[]
}

export interface PaginatedResponse<T> {
  results: T[]
  count: number
  next: string | null
  previous: string | null
}

export interface Announcement {
  id: number
  title: string
  content: string
  announcement_type: 'bar' | 'modal'
  is_active: boolean
  is_pinned: boolean
  sort: number
  publish_time: string
  show_delay: number
  auto_close: boolean
  auto_close_time: number
  created_at: string
  updated_at: string
}

export interface Ad {
  id: number
  title: string
  image?: string
  link?: string
  position: string
  is_active: boolean
  sort: number
  start_time?: string
  end_time?: string
  click_count: number
  created_at: string
  updated_at: string
}

export interface MembershipBenefit {
  id: number
  name: string
  code: string
  benefit_type: string
  description?: string
  icon?: string
  is_active: boolean
}

export interface PlanBenefitRelation {
  id: number
  benefit: MembershipBenefit
  value?: string
}

export interface MembershipPlan {
  id: number
  name: string
  description?: string
  price: string
  duration_days: number
  features: string[]
  is_active: boolean
  sort: number
  is_popular: boolean
  badge_text?: string
  benefit_relations: PlanBenefitRelation[]
  created_at: string
  updated_at: string
}

export interface UserMembership {
  id: number
  user: number
  plan?: MembershipPlan
  start_time?: string
  end_time?: string
  is_active: boolean
  is_lifetime: boolean
  is_valid: boolean
  remaining_days: number
  created_at: string
  updated_at: string
}

export interface Order {
  id: number
  order_no: string
  user: number
  plan?: MembershipPlan
  amount: string
  status: string
  status_display: string
  payment_method?: string
  payment_method_display?: string
  paid_time?: string
  expired_time?: string
  remark?: string
  created_at: string
  updated_at: string
}

export interface Payment {
  id: number
  order: number
  payment_no: string
  third_party_no?: string
  amount: string
  channel: string
  channel_display: string
  status: string
  status_display: string
  paid_time?: string
  created_at: string
  updated_at: string
}

export interface Wallet {
  id: number
  user: number
  balance: string
  frozen_balance: string
  available_balance: string
  total_income: string
  total_expense: string
  created_at: string
  updated_at: string
}

export interface WalletTransaction {
  id: number
  wallet: number
  transaction_no: string
  transaction_type: string
  transaction_type_display: string
  amount: string
  balance_before: string
  balance_after: string
  description?: string
  related_order?: number
  created_at: string
}

export interface OAuthApp {
  id: number
  provider: string
  provider_display: string
  app_id: string
  redirect_uri?: string
  scope?: string
  is_active: boolean
  created_at: string
  updated_at: string
}

export interface SocialAccount {
  id: number
  user: number
  provider: string
  provider_display: string
  openid: string
  unionid?: string
  nickname?: string
  avatar?: string
  created_at: string
  updated_at: string
}
