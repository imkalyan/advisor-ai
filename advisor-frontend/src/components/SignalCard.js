import React from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';

const SignalCard = ({ signal }) => {
  const { symbol, action, aggregatedProbabilities } = signal;

  const data = [
    { name: 'Positive', value: aggregatedProbabilities.positive * 100 },
    { name: 'Neutral', value: aggregatedProbabilities.neutral * 100 },
    { name: 'Negative', value: aggregatedProbabilities.negative * 100 },
  ];

  const actionColor = {
    BUY: '#4caf50',
    HOLD: '#ff9800',
    SELL: '#f44336',
  }[action] || '#9e9e9e';

  return (
    <div style={{ border: `2px solid ${actionColor}`, borderRadius: '8px', padding: '16px', marginBottom: '16px' }}>
      <h2>{symbol} → {action}</h2>
      <ResponsiveContainer width="100%" height={50}>
        <BarChart data={data}>
          <XAxis dataKey="name" hide />
          <YAxis hide />
          <Tooltip />
          <Bar dataKey="value" fill={actionColor} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};

export default SignalCard;