import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useSocket } from '../SocketContext';
import './ChatRoom.css';

function ChatRoom() {
  const { chatId } = useParams();
  const socket = useSocket();
  const [message, setMessage] = useState('');
  const [messages, setMessages] = useState([]);

  useEffect(() => {
    if (!socket) return;

    const handleOpen = () => {
      if (socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify({ type: 'JOIN', data: chatId }));
      }
    };

    const handleMessage = (event) => {
      const response = JSON.parse(event.data);
      if (response.type === 'CHAT_MESSAGE') {
        setMessages((prev) => [
          ...prev,
          { sender: response.sender, text: response.data },
        ]);
      }
    };

    socket.addEventListener('open', handleOpen);
    socket.addEventListener('message', handleMessage);

    return () => {
      socket.removeEventListener('open', handleOpen);
      socket.removeEventListener('message', handleMessage);
    };
  }, [socket, chatId]);

  const sendMessage = () => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({ type: 'SEND', data: message }));
      setMessage('');
    }
  };

  return (
    <div className='chat-container'>
      <h2 className='chat-title'>채팅방 {chatId}</h2>
      <div className='messages-container'>
        <h2>Received Messages</h2>
        <div className='messages'>
          {messages.map((msg, index) => (
            <div key={index} className='message'>
              <strong>{msg.sender}</strong>: {msg.text}
            </div>
          ))}
        </div>
      </div>
      <div className='input-container'>
        <input
          type='text'
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
          placeholder='메세지를 입력하세요.'
        />
        <button onClick={sendMessage}>Send</button>
      </div>
    </div>
  );
}

export default ChatRoom;
