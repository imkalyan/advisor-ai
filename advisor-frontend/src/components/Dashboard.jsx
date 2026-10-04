import React, { useEffect, useState } from "react";
import {
  Container,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Paper,
  Typography,
  TablePagination,
  TextField,
  Box,
  IconButton,
  Collapse,
} from "@mui/material";
import { Bar, Line } from "react-chartjs-2";
import { KeyboardArrowDown, KeyboardArrowUp } from "@mui/icons-material";

import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  TimeScale,
} from "chart.js";
import "chartjs-adapter-date-fns";

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  TimeScale
);

async function fetchSignals() {
  const res = await fetch("http://localhost:8080/api/advisor/signals");
  return await res.json();
}

function parseProbabilities(raw) {
  try {
    return typeof raw === "string" ? JSON.parse(raw) : raw;
  } catch {
    return { positive: 0, neutral: 0, negative: 0 };
  }
}

function formatConfidence(value) {
  return typeof value === "number" ? `${Math.round(value * 100)}%` : "—";
}

function ProbabilityChart({ probs }) {
  const data = {
    labels: ["Positive", "Neutral", "Negative"],
    datasets: [
      {
        label: "Probabilities",
        data: [probs.positive, probs.neutral, probs.negative],
        backgroundColor: ["#4caf50", "#ffc107", "#f44336"],
      },
    ],
  };
  return <Bar data={data} options={{ responsive: true, plugins: { legend: { display: false } } }} />;
}

function HistoryChart({ history }) {
  const chartData = {
    labels: history.map((h) => new Date(h.lastUpdated)),
    datasets: [
      {
        label: "Positive",
        data: history.map((h) => parseProbabilities(h.aggregatedProbabilities).positive),
        borderColor: "#4caf50",
        fill: false,
        yAxisID: "y",
      },
      {
        label: "Neutral",
        data: history.map((h) => parseProbabilities(h.aggregatedProbabilities).neutral),
        borderColor: "#ffc107",
        fill: false,
        yAxisID: "y",
      },
      {
        label: "Negative",
        data: history.map((h) => parseProbabilities(h.aggregatedProbabilities).negative),
        borderColor: "#f44336",
        fill: false,
        yAxisID: "y",
      },
      {
        label: "Aggregated Sentiment",
        data: history.map((h) => h.avgSentiment ?? h.ewmaSentiment ?? null),
        borderColor: "#2196f3",
        borderDash: [5, 5],
        fill: false,
        yAxisID: "y2",
      },
    ],
  };

  const options = {
    responsive: true,
    plugins: { legend: { position: "top" } },
    scales: {
      x: { type: "time", title: { display: true, text: "Time" } },
      y: { min: 0, max: 1, title: { display: true, text: "Probability" } },
      y2: {
        position: "right",
        min: -1,
        max: 1,
        grid: { drawOnChartArea: false },
        title: { display: true, text: "Sentiment" },
      },
    },
  };

  return <Line data={chartData} options={options} />;
}

function SymbolRow({ symbol, latest, history }) {
  const [open, setOpen] = useState(false);
  const latestProbs = parseProbabilities(latest.aggregatedProbabilities);

  return (
    <>
      <TableRow>
        <TableCell>
          <IconButton size="small" onClick={() => setOpen(!open)}>
            {open ? <KeyboardArrowUp /> : <KeyboardArrowDown />}
          </IconButton>
          {symbol}
        </TableCell>
        <TableCell>
          <strong>{latest.action}</strong>
          <Typography variant="caption" display="block">
            Confidence: {formatConfidence(latest.decisionConfidence)}
          </Typography>
          <Typography variant="caption" display="block">
            Source: {latest.decisionSource || "fallback"}
          </Typography>
        </TableCell>
        <TableCell style={{ maxWidth: 400, whiteSpace: "pre-wrap" }}>{latest.reason}</TableCell>
        <TableCell>
          <ProbabilityChart probs={latestProbs} />
        </TableCell>
      </TableRow>

      {/* Expandable Probability Trend */}
      <TableRow>
        <TableCell colSpan={4} style={{ paddingBottom: 0, paddingTop: 0 }}>
          <Collapse in={open} timeout="auto" unmountOnExit>
            <Box margin={2}>
              <Typography variant="subtitle1">Probability Trend for {symbol}</Typography>
              <HistoryChart history={history} />
            </Box>
          </Collapse>
        </TableCell>
      </TableRow>
    </>
  );
}

export default function Dashboard() {
  const [signals, setSignals] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(5);
  const [actionFilter, setActionFilter] = useState("ALL");
  const [recencyFilter, setRecencyFilter] = useState("ALL");

  useEffect(() => {
    fetchSignals().then((data) => {
      setSignals(data);
    });
  }, []);

  const handleSearchChange = (event) => {
    setSearchQuery(event.target.value);
    setPage(0);
  };

  const grouped = signals.reduce((acc, row) => {
    if (!acc[row.symbol]) acc[row.symbol] = [];
    acc[row.symbol].push(row);
    return acc;
  }, {});

  Object.keys(grouped).forEach((sym) =>
    grouped[sym].sort((a, b) => new Date(a.lastUpdated) - new Date(b.lastUpdated))
  );

  const symbols = Object.entries(grouped).map(([symbol, rows]) => ({
    symbol,
    latest: rows[rows.length - 1],
    history: rows,
  }));

  const recencyHours = { "1H": 1, "6H": 6, "24H": 24, "7D": 24 * 7 };
  const filtered = symbols.filter((s) => {
    const ageHours = (Date.now() - new Date(s.latest.lastUpdated).getTime()) / 3600000;
    const withinRecency = recencyFilter === "ALL" ||
      (Number.isFinite(ageHours) && ageHours >= 0 && ageHours <= recencyHours[recencyFilter]);
    return s.symbol.toLowerCase().includes(searchQuery.toLowerCase()) &&
      (actionFilter === "ALL" || s.latest.action === actionFilter) &&
      withinRecency;
  });

  const paginated = filtered.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  return (
    <Container>
      <Typography variant="h4" gutterBottom>
        📊 Advisor Signals Dashboard
      </Typography>

      <Box mb={2}>
        <TextField
          label="Search by Symbol"
          variant="outlined"
          fullWidth
          value={searchQuery}
          onChange={handleSearchChange}
        />
      </Box>

      <Box mb={2} display="flex" gap={1} flexWrap="wrap">
        {["ALL", "BUY", "SELL", "HOLD"].map((action) => (
          <button
            key={action}
            type="button"
            onClick={() => { setActionFilter(action); setPage(0); }}
            style={{
              padding: "8px 16px",
              borderRadius: 18,
              border: "1px solid #bbb",
              background: actionFilter === action ? "#1976d2" : "white",
              color: actionFilter === action ? "white" : "#333",
              cursor: "pointer",
            }}
          >
            {action}
          </button>
        ))}
      </Box>

      <Box mb={2} display="flex" gap={1} flexWrap="wrap" alignItems="center">
        <Typography variant="body2">Signal recency:</Typography>
        {["ALL", "1H", "6H", "24H", "7D"].map((window) => (
          <button
            key={window}
            type="button"
            onClick={() => { setRecencyFilter(window); setPage(0); }}
            style={{
              padding: "6px 12px",
              borderRadius: 16,
              border: "1px solid #bbb",
              background: recencyFilter === window ? "#455a64" : "white",
              color: recencyFilter === window ? "white" : "#333",
              cursor: "pointer",
            }}
          >
            {window === "ALL" ? "All time" : window}
          </button>
        ))}
      </Box>

      <Paper>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Symbol</TableCell>
        <TableCell>Action / Confidence</TableCell>
              <TableCell>Reason</TableCell>
              <TableCell>Latest Probabilities</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {paginated.map(({ symbol, latest, history }) => (
              <SymbolRow key={symbol} symbol={symbol} latest={latest} history={history} />
            ))}
          </TableBody>
        </Table>
        <TablePagination
          component="div"
          count={filtered.length}
          page={page}
          onPageChange={(e, newPage) => setPage(newPage)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => {
            setRowsPerPage(parseInt(e.target.value, 10));
            setPage(0);
          }}
          rowsPerPageOptions={[5, 10, 25]}
        />
      </Paper>
    </Container>
  );
}
