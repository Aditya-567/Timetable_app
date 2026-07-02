import ExcelJS from "exceljs";
import { Router } from "express";
import { TimetableCellEntry, TimetableEventBlock, TimetablePayload, WeatherData } from "../types.js";

export const timetableRouter = Router();

async function getWeatherData(location: string, apiKey: string): Promise<WeatherData | null> {
  if (!apiKey) return null;
  try {
    const url = `https://api.openweathermap.org/data/2.5/weather?q=${encodeURIComponent(location)}&appid=${apiKey}&units=metric`;
    const response = await fetch(url);
    if (!response.ok) return null;
    
    const data = await response.json();
    const temp = `${Math.round(data.main.temp)}°C`;
    const condition = data.weather[0].main;
    const isRaining = condition.toLowerCase().includes("rain") || data.clouds.all > 80;
    
    return {
      temp: temp,
      rainChance: isRaining ? "High chance of rain" : "Low chance of rain",
      condition: condition,
      message: isRaining 
        ? "Weather looks rainy. Carry an umbrella!" 
        : "Weather looks clear. Have a great day!"
    };
  } catch (e) {
    console.error("Weather API error:", e);
    return null;
  }
}

function resolveCsvLinks(
  scheduleUrl: string,
  courseUrl: string,
  scheduleGid: number,
  courseGid: number
): { scheduleFetchUrl: string; courseFetchUrl: string; scheduleXlsxUrl?: string } {
  const googleSheetMatch = scheduleUrl.match(/\/d\/([a-zA-Z0-9-_]+)/);
  if (googleSheetMatch) {
    const spreadsheetId = googleSheetMatch[1];
    return {
      scheduleFetchUrl: `https://docs.google.com/spreadsheets/d/${spreadsheetId}/export?format=csv&gid=${scheduleGid}`,
      courseFetchUrl: `https://docs.google.com/spreadsheets/d/${spreadsheetId}/export?format=csv&gid=${courseGid}`,
      scheduleXlsxUrl: `https://docs.google.com/spreadsheets/d/${spreadsheetId}/export?format=xlsx`
    };
  }

  if (!courseUrl) {
    throw new Error(
      "A custom non-Google Sheet schedule URL was provided, but COURSE_URL is empty. Configure both SCHEDULE_URL and COURSE_URL."
    );
  }

  return { scheduleFetchUrl: scheduleUrl, courseFetchUrl: courseUrl };
}

function parseCsvLine(line: string): string[] {
  const values: string[] = [];
  let current = "";
  let inQuotes = false;

  for (let i = 0; i < line.length; i += 1) {
    const ch = line[i];
    const next = i + 1 < line.length ? line[i + 1] : "";

    if (ch === '"') {
      if (inQuotes && next === '"') {
        current += '"';
        i += 1;
      } else {
        inQuotes = !inQuotes;
      }
      continue;
    }

    if (ch === "," && !inQuotes) {
      values.push(current);
      current = "";
      continue;
    }

    current += ch;
  }

  values.push(current);
  return values;
}

function parseCsv(csv: string): string[][] {
  return csv
    .split(/\r?\n/)
    .filter((line) => line.length > 0)
    .map((line) => parseCsvLine(line));
}

function toCsvValue(value: string): string {
  if (value.includes(",") || value.includes('"') || value.includes("\n")) {
    return `"${value.replace(/"/g, '""')}"`;
  }
  return value;
}

function toCsv(rows: string[][]): string {
  return rows.map((row) => row.map((v) => toCsvValue(v)).join(",")).join("\n");
}

function normalizeKey(value: string): string {
  return value.trim().replace(/\s+/g, " ").toUpperCase();
}

function normalizeDateKey(value: string): string {
  const raw = value.trim();
  if (!raw) return "";
  return normalizeKey(raw);
}

function normalizeTimeKey(value: string): string {
  return normalizeKey(value.replace(/[–—]/g, "-").replace(/\s*-\s*/g, " - "));
}

function isSkippableCancellationValue(value: string): boolean {
  const normalized = normalizeKey(value);
  return normalized === "LUNCH BREAK" || normalized === "BLOCKED";
}

function isLunchValue(value: string): boolean {
  return normalizeKey(value) === "LUNCH BREAK";
}

function isBlockedValue(value: string): boolean {
  return normalizeKey(value) === "BLOCKED";
}

function isClassLikeValue(value: string): boolean {
  return /^[A-Za-z]{1,8}\s*-\s*[A-Za-z0-9]{1,12}$/.test(value.trim());
}

function parseSheetDateTimestamp(value: string): number | null {
  const raw = value.trim();
  if (!raw) return null;

  const parsed = Date.parse(raw);
  return Number.isNaN(parsed) ? null : parsed;
}

function createCellEntry(date: string, time: string, classroom: string, value: string): TimetableCellEntry {
  return { date, time, classroom, value };
}

function extractEventTitleFromRow(row: ExcelJS.Row, dateCol: number, timeCol: number): string | null {
  let bestCandidate = "";

  row.eachCell({ includeEmpty: true }, (cell, colNumber) => {
    if (colNumber === dateCol || colNumber === timeCol) return;

    const raw = String(cell.text ?? "").trim();
    if (!raw || isSkippableCancellationValue(raw) || isClassLikeValue(raw)) return;

    if (raw.length > bestCandidate.length) {
      bestCandidate = raw;
    }
  });

  return bestCandidate || null;
}

function extractEventTitleFromCsvRow(row: string[], header: string[], dateIdx: number, timeIdx: number): string | null {
  let bestCandidate = "";

  row.forEach((value, index) => {
    if (index === dateIdx || index === timeIdx) return;

    const raw = value.trim();
    if (!raw || isSkippableCancellationValue(raw) || isClassLikeValue(raw)) return;

    const headerValue = normalizeKey(header[index] ?? "");
    if (headerValue === "DATE" || headerValue === "TIME") return;

    if (raw.length > bestCandidate.length) {
      bestCandidate = raw;
    }
  });

  return bestCandidate || null;
}

function buildEventBlocksFromScheduleCsv(scheduleCsv: string): TimetableEventBlock[] {
  const rows = parseCsv(scheduleCsv);
  if (rows.length === 0) return [];

  const headerRowIndex = rows.findIndex((row) => {
    const keys = row.map((v) => normalizeKey(v));
    return keys.includes("DATE") && keys.includes("TIME");
  });
  if (headerRowIndex < 0) return [];

  const header = rows[headerRowIndex];
  const headerIndex = new Map<string, number>();
  header.forEach((h, i) => headerIndex.set(normalizeKey(h), i));
  const dateIdx = headerIndex.get("DATE");
  const timeIdx = headerIndex.get("TIME");
  if (dateIdx == null || timeIdx == null) return [];

  const blocks: TimetableEventBlock[] = [];
  let activeBlock: TimetableEventBlock | null = null;

  const flushBlock = () => {
    if (activeBlock) {
      const uniqueDates = activeBlock.dates.filter((date, index, values) => values.indexOf(date) === index);
      blocks.push({
        title: activeBlock.title,
        startDate: uniqueDates[0] ?? activeBlock.startDate,
        endDate: uniqueDates[uniqueDates.length - 1] ?? activeBlock.endDate,
        dates: uniqueDates
      });
      activeBlock = null;
    }
  };

  for (let i = headerRowIndex + 1; i < rows.length; i += 1) {
    const row = rows[i];
    const date = row[dateIdx]?.trim() ?? "";
    const time = row[timeIdx]?.trim() ?? "";
    if (!date) continue;

    if (!time) {
      const eventTitle = extractEventTitleFromCsvRow(row, header, dateIdx, timeIdx);
      if (eventTitle) {
        if (!activeBlock || activeBlock.title !== eventTitle) {
          flushBlock();
          activeBlock = { title: eventTitle, startDate: date, endDate: date, dates: [date] };
        } else {
          activeBlock.endDate = date;
          activeBlock.dates.push(date);
        }
      } else if (activeBlock) {
        activeBlock.endDate = date;
        activeBlock.dates.push(date);
      }
    } else {
      flushBlock();
    }
  }

  flushBlock();
  return blocks;
}

function buildClassificationFromScheduleCsv(scheduleCsv: string): {
  events: TimetableEventBlock[];
  classes: TimetableCellEntry[];
  blockedClasses: TimetableCellEntry[];
  cancelledClasses: TimetableCellEntry[];
  lunchBreaks: TimetableCellEntry[];
} {
  const rows = parseCsv(scheduleCsv);
  if (rows.length === 0) {
    return { events: [], classes: [], blockedClasses: [], cancelledClasses: [], lunchBreaks: [] };
  }

  const headerRowIndex = rows.findIndex((row) => {
    const keys = row.map((v) => normalizeKey(v));
    return keys.includes("DATE") && keys.includes("TIME");
  });
  if (headerRowIndex < 0) {
    return { events: [], classes: [], blockedClasses: [], cancelledClasses: [], lunchBreaks: [] };
  }

  const header = rows[headerRowIndex];
  const headerIndex = new Map<string, number>();
  header.forEach((h, i) => headerIndex.set(normalizeKey(h), i));
  const dateIdx = headerIndex.get("DATE");
  const timeIdx = headerIndex.get("TIME");
  if (dateIdx == null || timeIdx == null) {
    return { events: [], classes: [], blockedClasses: [], cancelledClasses: [], lunchBreaks: [] };
  }

  const events: TimetableEventBlock[] = [];
  const classes: TimetableCellEntry[] = [];
  const blockedClasses: TimetableCellEntry[] = [];
  const cancelledClasses: TimetableCellEntry[] = [];
  const lunchBreaks: TimetableCellEntry[] = [];
  let activeBlock: TimetableEventBlock | null = null;

  const flushBlock = () => {
    if (activeBlock) {
      const uniqueDates = activeBlock.dates.filter((date, index, values) => values.indexOf(date) === index);
      events.push({
        title: activeBlock.title,
        startDate: uniqueDates[0] ?? activeBlock.startDate,
        endDate: uniqueDates[uniqueDates.length - 1] ?? activeBlock.endDate,
        dates: uniqueDates
      });
      activeBlock = null;
    }
  };

  for (let i = headerRowIndex + 1; i < rows.length; i += 1) {
    const row = rows[i];
    const date = row[dateIdx]?.trim() ?? "";
    const time = row[timeIdx]?.trim() ?? "";
    if (!date) continue;

    if (!time) {
      const eventTitle = extractEventTitleFromCsvRow(row, header, dateIdx, timeIdx);
      if (eventTitle) {
        if (!activeBlock || activeBlock.title !== eventTitle) {
          flushBlock();
          activeBlock = { title: eventTitle, startDate: date, endDate: date, dates: [date] };
        } else {
          activeBlock.endDate = date;
          activeBlock.dates.push(date);
        }
      } else if (activeBlock) {
        activeBlock.endDate = date;
        activeBlock.dates.push(date);
      }
      continue;
    }

    flushBlock();

    header.forEach((columnName, columnIndex) => {
      if (columnIndex === dateIdx || columnIndex === timeIdx) return;

      const raw = (row[columnIndex] ?? "").trim();
      if (!raw) return;

      const entry = createCellEntry(date, time, columnName, raw);
      if (isLunchValue(raw)) {
        lunchBreaks.push(entry);
        return;
      }

      if (isBlockedValue(raw)) {
        blockedClasses.push(entry);
        return;
      }

      if (normalizeKey(raw).includes("[RED]")) {
        cancelledClasses.push(entry);
        return;
      }

      classes.push(entry);
    });
  }

  flushBlock();
  return { events, classes, blockedClasses, cancelledClasses, lunchBreaks };
}

function isRedArgb(argb: string, redHexSet: Set<string>): boolean {
  const clean = argb.replace(/^#/, "").toUpperCase();
  const rgb = clean.length >= 6 ? clean.slice(-6) : clean;
  return redHexSet.has(rgb);
}

function isRedCell(cell: ExcelJS.Cell, redHexSet: Set<string>): boolean {
  const fill = cell.fill;
  if (!fill || fill.type !== "pattern") return false;

  const patternFill = fill as ExcelJS.FillPattern;
  if (patternFill.pattern !== "solid") return false;

  const fgArgb = patternFill.fgColor?.argb;
  if (fgArgb && isRedArgb(fgArgb, redHexSet)) return true;

  const bgArgb = patternFill.bgColor?.argb;
  if (bgArgb && isRedArgb(bgArgb, redHexSet)) return true;

  return false;
}

async function scanScheduleFromXlsx(
  scheduleCsv: string,
  xlsxBytes: ArrayBuffer,
  redHexSet: Set<string>
): Promise<{
  taggedScheduleCsv: string;
  taggedCellsCount: number;
  events: TimetableEventBlock[];
  classes: TimetableCellEntry[];
  blockedClasses: TimetableCellEntry[];
  cancelledClasses: TimetableCellEntry[];
  lunchBreaks: TimetableCellEntry[];
}> {
  const workbook = new ExcelJS.Workbook();
  await workbook.xlsx.load(Buffer.from(new Uint8Array(xlsxBytes)) as any);

  const scheduleSheet = workbook.worksheets.find((ws) => {
    let foundHeader = false;
    ws.eachRow({ includeEmpty: false }, (row) => {
      if (foundHeader) return;
      const rowValues = Array.isArray(row.values) ? row.values : [];
      const cells = rowValues
        .slice(1)
        .map((v: ExcelJS.CellValue | undefined) => String(v ?? "").trim().toUpperCase());
      if (cells.includes("DATE") && cells.includes("TIME")) {
        foundHeader = true;
      }
    });
    return foundHeader;
  });

  if (!scheduleSheet) {
    return {
      taggedScheduleCsv: scheduleCsv,
      taggedCellsCount: 0,
      events: [],
      classes: [],
      blockedClasses: [],
      cancelledClasses: [],
      lunchBreaks: []
    };
  }

  let headerRowNo = 0;
  const sheetHeaderMap = new Map<number, string>();
  scheduleSheet.eachRow({ includeEmpty: false }, (row, rowNumber) => {
    if (headerRowNo > 0) return;
    row.eachCell({ includeEmpty: true }, (cell, colNumber) => {
      const text = String(cell.text ?? "").trim();
      if (text) {
        sheetHeaderMap.set(colNumber, text);
      }
    });
    const values = Array.from(sheetHeaderMap.values()).map((v) => normalizeKey(v));
    if (values.includes("DATE") && values.includes("TIME")) {
      headerRowNo = rowNumber;
    } else {
      sheetHeaderMap.clear();
    }
  });

  if (headerRowNo === 0) {
    return {
      taggedScheduleCsv: scheduleCsv,
      taggedCellsCount: 0,
      events: [],
      classes: [],
      blockedClasses: [],
      cancelledClasses: [],
      lunchBreaks: []
    };
  }

  const dateCol = Array.from(sheetHeaderMap.entries()).find(([, h]) => normalizeKey(h) === "DATE")?.[0];
  const timeCol = Array.from(sheetHeaderMap.entries()).find(([, h]) => normalizeKey(h) === "TIME")?.[0];
  if (!dateCol || !timeCol) {
    return {
      taggedScheduleCsv: scheduleCsv,
      taggedCellsCount: 0,
      events: [],
      classes: [],
      blockedClasses: [],
      cancelledClasses: [],
      lunchBreaks: []
    };
  }

  const redKeys = new Set<string>();
  const events: TimetableEventBlock[] = [];
  const classes: TimetableCellEntry[] = [];
  const blockedClasses: TimetableCellEntry[] = [];
  const cancelledClasses: TimetableCellEntry[] = [];
  const lunchBreaks: TimetableCellEntry[] = [];
  let activeEvent: TimetableEventBlock | null = null;

  const flushEvent = () => {
    if (activeEvent) {
      events.push(activeEvent);
      activeEvent = null;
    }
  };

  for (let r = headerRowNo + 1; r <= scheduleSheet.rowCount; r += 1) {
    const row = scheduleSheet.getRow(r);
    const date = normalizeDateKey(String(row.getCell(dateCol).text ?? ""));
    const time = normalizeTimeKey(String(row.getCell(timeCol).text ?? ""));
    if (!date) continue;

    if (!time || time === "") {
      const eventTitle = extractEventTitleFromRow(row, dateCol, timeCol);
      if (eventTitle) {
        if (!activeEvent || activeEvent.title !== eventTitle) {
          flushEvent();
          activeEvent = {
            title: eventTitle,
            startDate: date,
            endDate: date,
            dates: [date]
          };
        } else {
          activeEvent.endDate = date;
          activeEvent.dates.push(date);
        }
      } else if (activeEvent) {
        activeEvent.endDate = date;
        activeEvent.dates.push(date);
      }

      continue;
    }

    flushEvent();

    sheetHeaderMap.forEach((header, colNo) => {
      if (colNo === dateCol || colNo === timeCol) return;

      const cell = row.getCell(colNo);
      const raw = String(cell.text ?? "").trim();

      if (!raw) return;

      const entry = createCellEntry(date, time, header, raw);
      if (isLunchValue(raw)) {
        lunchBreaks.push(entry);
        return;
      }

      const redCell = isRedCell(cell, redHexSet);
      if (isBlockedValue(raw)) {
        blockedClasses.push(entry);
        if (redCell) {
          redKeys.add(`${date}|${time}|${normalizeKey(header)}`);
        }
        return;
      }

      if (redCell) {
        cancelledClasses.push(entry);
        redKeys.add(`${date}|${time}|${normalizeKey(header)}`);
        return;
      }

      if (isSkippableCancellationValue(raw)) return;

      classes.push(entry);
    });
  }

  flushEvent();

  if (redKeys.size === 0) {
    // keep going; tagging is optional, but classification still matters
  }

  const rows = parseCsv(scheduleCsv);
  if (rows.length === 0) {
    return {
      taggedScheduleCsv: scheduleCsv,
      taggedCellsCount: 0,
      events,
      classes,
      blockedClasses,
      cancelledClasses,
      lunchBreaks
    };
  }

  const headerRowIndex = rows.findIndex((row) => {
    const keys = row.map((v) => normalizeKey(v));
    return keys.includes("DATE") && keys.includes("TIME");
  });
  if (headerRowIndex < 0) {
    return {
      taggedScheduleCsv: scheduleCsv,
      taggedCellsCount: 0,
      events,
      classes,
      blockedClasses,
      cancelledClasses,
      lunchBreaks
    };
  }

  const header = rows[headerRowIndex];
  const headerIndex = new Map<string, number>();
  header.forEach((h, i) => headerIndex.set(normalizeKey(h), i));

  const dateIdx = headerIndex.get("DATE");
  const timeIdx = headerIndex.get("TIME");
  if (dateIdx == null || timeIdx == null) {
    return {
      taggedScheduleCsv: scheduleCsv,
      taggedCellsCount: 0,
      events,
      classes,
      blockedClasses,
      cancelledClasses,
      lunchBreaks
    };
  }

  let taggedCellsCount = 0;
  for (let i = headerRowIndex + 1; i < rows.length; i += 1) {
    const row = rows[i];
    const date = normalizeDateKey(String(row[dateIdx] ?? ""));
    const time = normalizeTimeKey(String(row[timeIdx] ?? ""));
    if (!date || !time) continue;

    header.forEach((h, idx) => {
      if (idx === dateIdx || idx === timeIdx) return;
      const current = (row[idx] ?? "").trim();
      if (!current || isSkippableCancellationValue(current)) return;

      const key = `${date}|${time}|${normalizeKey(h)}`;
      if (!redKeys.has(key)) return;

      if (!normalizeKey(current).includes("[RED]")) {
        row[idx] = `[RED] ${current}`;
        taggedCellsCount += 1;
      }
    });
  }

  return {
    taggedScheduleCsv: toCsv(rows),
    taggedCellsCount,
    events,
    classes,
    blockedClasses,
    cancelledClasses,
    lunchBreaks
  };
}

timetableRouter.get("/", async (_req, res) => {
  try {
    const scheduleUrl = process.env.SCHEDULE_URL || "";
    const courseUrl = process.env.COURSE_URL || "";
    const scheduleGid = Number(process.env.SCHEDULE_GID || "0");
    const courseGid = Number(process.env.COURSE_GID || "1069732703");
    const apiKey = process.env.WEATHER_API_KEY || "";
const location = process.env.WEATHER_LOCATION || "Kozhikode";

    const redHexSet = new Set(
      (process.env.CANCEL_RED_HEXES || "FF0000,F44336,E53935,D32F2F,C62828")
        .split(",")
        .map((v) => v.trim().toUpperCase())
        .filter((v) => v.length > 0)
    );

    if (!scheduleUrl) {
      return res.status(400).json({
        error: "SCHEDULE_URL is not configured in backend environment variables."
      });
    }

    const { scheduleFetchUrl, courseFetchUrl, scheduleXlsxUrl } = resolveCsvLinks(
      scheduleUrl,
      courseUrl,
      scheduleGid,
      courseGid
    );

    const [scheduleRes, courseRes] = await Promise.all([
      fetch(scheduleFetchUrl),
      fetch(courseFetchUrl)
    ]);

    
    const weatherData = await getWeatherData(location, apiKey);


    if (!scheduleRes.ok) {
      throw new Error(`Schedule source returned status ${scheduleRes.status}`);
    }
    if (!courseRes.ok) {
      throw new Error(`Course source returned status ${courseRes.status}`);
    }

    let scheduleCsv = await scheduleRes.text();
    const courseCsv = await courseRes.text();
    let cancelledCellCount = 0;

    if (scheduleXlsxUrl) {
      try {
        const xlsxRes = await fetch(scheduleXlsxUrl);
        if (xlsxRes.ok) {
          const xlsxBytes = await xlsxRes.arrayBuffer();
          const tagged = await scanScheduleFromXlsx(scheduleCsv, xlsxBytes, redHexSet);
          scheduleCsv = tagged.taggedScheduleCsv;
          cancelledCellCount = tagged.taggedCellsCount;
          const csvClassification = buildClassificationFromScheduleCsv(scheduleCsv);

          const payload: TimetablePayload = {
            scheduleCsv,
            courseCsv,
            cancelledCellCount,
            cancellationTag: "[RED]",
            events: csvClassification.events,
            classes: csvClassification.classes,
            blockedClasses: csvClassification.blockedClasses,
            cancelledClasses: csvClassification.cancelledClasses,
            lunchBreaks: csvClassification.lunchBreaks,
          };

          return res.status(200).json(payload);
        }
      } catch {
        // Keep plain CSV fallback if XLSX style extraction fails.
      }
    }

    const payload: TimetablePayload = {
      scheduleCsv,
      courseCsv,
      cancelledCellCount,
      cancellationTag: "[RED]",
      weather: weatherData ?? undefined,
      ...buildClassificationFromScheduleCsv(scheduleCsv)
    };

    return res.status(200).json(payload);
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unexpected backend error";
    return res.status(500).json({ error: message });
  }
});
