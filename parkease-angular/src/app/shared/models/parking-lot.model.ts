export interface ParkingLot {
  lotId?: number;
  name: string;
  address: string;
  city: string;
  latitude: number;
  longitude: number;
  totalSpots: number;
  availableSpots?: number;
  managerId: number;
  isOpen?: boolean;
  isApproved?: boolean;
  openTime?: string;
  closeTime?: string;
  imageUrl?: string;
}
