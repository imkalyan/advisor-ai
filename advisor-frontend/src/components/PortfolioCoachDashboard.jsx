import { createContext, useState } from "react";

export const PortfolioCoachDashboard = createContext();

export const PortfolioCoachDashboardProvider = ({ children }) => {
  const [user, setUser] = useState({
    id: "8ce8c098-ccaa-4cdb-a6c0-7e32e7315454",
    name: "Pawan Kalyan",
    riskProfile: "Moderate",
  });

  const [portfolio, setPortfolio] = useState([]);
  const [riskScore, setRiskScore] = useState(null);
  const [llmExplanation, setLlmExplanation] = useState("");
  const [insights, setInsights] = useState([]);

  const [settings, setSettings] = useState({
    theme: "light",
    showCharts: true,
    apiBaseUrl: "http://localhost:8080/api",
  });

  return (
    <PortfolioCoachDashboard.Provider
      value={{
        user,
        setUser,
        portfolio,
        setPortfolio,
        riskScore,
        setRiskScore,
        llmExplanation,
        setLlmExplanation,
        insights,
        setInsights,
        settings,
        setSettings
      }}
    >
      {children}
    </PortfolioCoachDashboard.Provider>
  );
};