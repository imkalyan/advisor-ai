import React from 'react';
import { useQuery } from '@tanstack/react-query';
import axios from 'axios';
import SignalCard from './components/SignalCard';

function App() {
  const { data, isLoading, error } = useQuery(['signals'], async () => {
    const res = await axios.get('http://localhost:8080/api/signals'); // Replace with your API endpoint
    console.log(res.data);
    
    return res.data;
  }, { refetchInterval: 5000 }); // Refresh every 5 seconds

  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>Error loading signals</div>;

  return (
    <div style={{ maxWidth: '900px', margin: '0 auto', padding: '20px' }}>
      <h1>Advisor Signals Dashboard</h1>
      {data.map(signal => (
        <SignalCard key={signal.symbol} signal={signal} />
      ))}
    </div>
  );
}

export default App;