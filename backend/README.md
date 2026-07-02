# modile--backend

Custom backend for the Android mobile app.

## Endpoints

- `GET /api/health` -> `{ status: "ok" }`
- `GET /api/timetable` -> `{ scheduleCsv, courseCsv, cancelledCellCount, cancellationTag }`

## Setup

1. Install dependencies:
   - `npm install`
2. Create env file:
   - copy `.env.example` to `.env`
3. Run server:
   - `npm run dev`

Default URL:

- `http://localhost:4000`

## Cancelled Class Detection

- Backend exports the same Google Sheet as XLSX and inspects cell fill colors.
- Cells whose fill color matches `CANCEL_RED_HEXES` are tagged in `scheduleCsv` as `[RED] <originalValue>`.
- Mobile app reads `[RED]` and marks those classes as cancelled.

Optional environment variables:

- `SCHEDULE_GID` (default `0`)
- `COURSE_GID` (default `1069732703`)
- `CANCEL_RED_HEXES` (default `FF0000,F44336,E53935,D32F2F,C62828`)

## Mobile connection

In mobile project file `app/build.gradle.kts`, set:

- `TIMETABLE_BASE_URL` to `http://10.0.2.2:4000` for Android emulator.
