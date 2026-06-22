import request from '@/utils/request'

export interface MediaAsset {
  id: number
  file: string
  media_type: string
  title?: string
  alt_text?: string
  created_at: string
}

export const uploadMedia = (formData: FormData) =>
  request.post<MediaAsset>('/media/assets/', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
