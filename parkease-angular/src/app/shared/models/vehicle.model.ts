export interface Vehicle {
  vehicleId?: number;
  ownerId: number;
  licensePlate: string;
  make: string;
  model: string;
  color: string;
  vehicleType: string;
  isEV: boolean;
  registeredAt?: string;
  isActive?: boolean;
}
