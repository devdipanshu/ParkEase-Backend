export interface Payment {
  paymentId?: number;
  bookingId: number;
  userId: number;
  lotId?: number;
  amount: number;
  status?: string;
  mode: string;
  transactionId?: string;
  paidAt?: string;
  refundedAt?: string;
  refundAmount?: number;
}
