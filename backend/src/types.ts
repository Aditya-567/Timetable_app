export interface TimetablePayload {
  scheduleCsv: string;
  courseCsv: string;
  cancelledCellCount?: number;
  cancellationTag?: string;
  
  // The newly requested grouped property containing everything
  allData?: {
    events: TimetableEventBlock[];
    classes: TimetableCellEntry[];
    blockedClasses: TimetableCellEntry[];
    cancelledClasses: TimetableCellEntry[];
    lunchBreaks: TimetableCellEntry[];
  };

  // Specific top-level properties requested
  events?: TimetableEventBlock[];
  cancelledClasses?: TimetableCellEntry[];

  // Keeping these at the root level for any existing frontend dependencies
  classes?: TimetableCellEntry[];
  blockedClasses?: TimetableCellEntry[];
  lunchBreaks?: TimetableCellEntry[];
  weather?: WeatherData;
}

export interface TimetableEventBlock {
  title: string;
  startDate: string;
  endDate: string;
  dates: string[];
}

export interface TimetableCellEntry {
  date: string;
  time: string;
  classroom: string;
  value: string;
}

// Add this interface to types.ts
export interface WeatherData {
  temp: string;
  rainChance: string;
  condition: string;
  message: string;
}
