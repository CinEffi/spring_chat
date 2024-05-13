import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './components/Home';
import ChatList from './components/ChatList';
import ChatRoom from './components/ChatRoom';
import { SocketProvider } from './SocketContext';

function App() {
  return (
    <Router>
      <SocketProvider>
        <Routes>
          <Route path='/' element={<Home />} />
          <Route path='/chat' element={<ChatList />} />
          <Route path='/chat/:chatId' element={<ChatRoom />} />
        </Routes>
      </SocketProvider>
    </Router>
  );
}

export default App;
