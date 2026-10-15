export interface BookingSelection {
  roomId: string;
  quantity: number;
}

export interface BookingSummary {
  checkIn: string;
  checkOut: string;
  nights: number;
  selections: BookingSelection[];
}
