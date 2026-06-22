import request from '@/utils/request'
import type { PaginatedResponse } from '@/types'

export interface FileItem {
  id: number
  filename: string
  file: string
  file_type: string
  file_size: number
  mime_type: string
  is_public: boolean
  folder_id: number | null
  created_at: string
}

export interface Folder {
  id: number
  name: string
  parent_id: number | null
  created_at: string
}

export const getMyFiles = (params?: { folder?: number; page?: number; size?: number }) =>
  request.get<PaginatedResponse<FileItem>>('/filemanager/files/', { params })

export const uploadFile = (formData: FormData) =>
  request.post<FileItem>('/filemanager/files/', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })

export const deleteFile = (id: number) =>
  request.delete(`/filemanager/files/${id}/`)

export const downloadFile = (id: number) =>
  request.get(`/filemanager/files/${id}/download/`, { responseType: 'blob' })

export const getFolders = (parent?: number) =>
  request.get<Folder[]>('/filemanager/folders/', { params: { parent } })

export const createFolder = (name: string, parentId?: number) =>
  request.post<Folder>('/filemanager/folders/', { name, parent_id: parentId })

export const deleteFolder = (id: number) =>
  request.delete(`/filemanager/folders/${id}/`)

export const createShare = (fileId: number) =>
  request.post<{ id: number; link: string }>('/filemanager/shares/', { file: fileId })
