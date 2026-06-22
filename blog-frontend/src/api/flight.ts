import request from '@/utils/request'
import type { PaginatedResponse } from '@/types'

export interface FlightRoute {
  id: number
  callsign?: string
  flightNumber?: string
  airline?: string
  airlineCode?: string
  originAirport?: string
  destinationAirport?: string
  departureTime?: string
  arrivalTime?: string
  status?: string
  changeType?: string
  altitude?: number
  velocity?: number
  latitude?: number
  longitude?: number
  country?: string
  lastSeen?: string
  createdAt: string
  updatedAt: string
}

export const getFlights = (params?: { page?: number; size?: number; airline?: string; changeType?: string }) =>
  request.get<PaginatedResponse<FlightRoute>>('/flights/', { params })

export const getFlightChanges = (params?: { size?: number }) =>
  request.get<FlightRoute[]>('/flights/changes/', { params })

export const getFlightAirlines = () =>
  request.get<string[]>('/flights/airlines/')

export const triggerFlightFetch = () =>
  request.post<number>('/flights/fetch/')
