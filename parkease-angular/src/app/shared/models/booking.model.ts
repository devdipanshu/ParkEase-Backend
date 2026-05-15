export interface Booking {
  bookingId?: number;
  userId: number;
  lotId: number;
  spotId: number;
  vehiclePlate: string;
  vehicleType: string;
  bookingType: string;
  startTime: string;
  endTime: string;
  status?: string;
  pricePerHour?: number;
  totalAmount?: number;
  actualCheckInTime?: string;
  actualCheckOutTime?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface BookingRequest {
  userId: number;
  lotId: number;
  spotId: number;
  vehiclePlate: string;
  vehicleType: string;
  bookingType: string;
  startTime: string;
  endTime: string;
}
