export interface Notification {
  notificationId?: number;
  recipientId: number;
  title: string;
  message: string;
  type: string;
  channel: string;
  isRead?: boolean;
  isDelivered?: boolean;
  createdAt?: string;
}

export interface UnreadCountResponse {
  recipientId: number;
  unreadCount: number;
}
