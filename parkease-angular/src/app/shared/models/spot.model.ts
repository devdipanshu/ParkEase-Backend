export interface Spot {
  spotId?: number;
  lotId: number;
  spotNumber: string;
  floor: number;
  spotType: string;
  vehicleType: string;
  status?: string;
  pricePerHour: number;
  isHandicapped?: boolean;
  isEVCharging?: boolean;
}

export interface BulkSpotRequest {
  spots: Spot[];
}
