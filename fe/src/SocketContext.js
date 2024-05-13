import React, { createContext, useContext, useEffect, useState } from 'react';

const SocketContext = createContext(null);

export const SocketProvider = ({ children }) => {
  const [socket, setSocket] = useState(null);

  useEffect(() => {
    const connectWebSocket = () => {
      const ws = new WebSocket('ws://localhost:8080/chat');

      ws.onopen = () => {
        console.log('Connected to WebSocket');
      };

      ws.onmessage = (event) => {
        console.log('Message from WebSocket:', event.data);
      };

      ws.onerror = (error) => {
        console.error('WebSocket error:', error);
      };

      ws.onclose = () => {
        console.log('WebSocket disconnected');
        setTimeout(connectWebSocket, 1000);
      };

      setSocket(ws);
    };

    connectWebSocket();
  }, []);

  return (
    <SocketContext.Provider value={socket}>{children}</SocketContext.Provider>
  );
};

export const useSocket = () => {
  return useContext(SocketContext);
};
