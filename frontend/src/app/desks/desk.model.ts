export type DeskType = 'STANDARD' | 'STANDING' | 'CONFERENCE';

export interface Floor {
  id: number;
  name: string;
}

export interface Room {
  id: number;
  name: string;
  floor: Floor;
}

export interface OccupiedBy {
  employeeName: string;
  checkedInAt: string;
}

export interface Desk {
  id: number;
  name: string;
  type: DeskType;
  occupiedBy: OccupiedBy | null;
  room: Room;
}
