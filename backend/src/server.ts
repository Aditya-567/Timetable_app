import cors from "cors";
import dotenv from "dotenv";
import express from "express";
import { healthRouter } from "./routes/health.js";
import { timetableRouter } from "./routes/timetable.js";

dotenv.config();

const app = express();
const port = Number(process.env.PORT || 4000);
const corsOrigin = process.env.CORS_ORIGIN || "*";

app.use(cors({ origin: corsOrigin }));
app.use(express.json());

app.get("/", (_req, res) => {
  res.json({
    service: "modile--backend",
    status: "running",
    endpoints: ["/api/health", "/api/timetable"]
  });
});

app.use("/api/health", healthRouter);
app.use("/api/timetable", timetableRouter);

app.listen(port, "0.0.0.0", () => {
  console.log(`modile--backend running at http://localhost:${port}`);
});
