import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSocket } from '../SocketContext';
import './ChatList.css';

function ChatList() {
  const socket = useSocket();
  const [rooms, setRooms] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchRooms = () => {
      if (!socket) return;

      const onSocketReady = () => {
        socket.send(JSON.stringify({ type: 'LIST' }));
      };

      if (socket.readyState === WebSocket.OPEN) {
        onSocketReady();
      } else {
        socket.addEventListener('open', onSocketReady);
      }

      const handleRoomList = (event) => {
        try {
          const response = JSON.parse(event.data);
          if (response.type === 'CHATROOM_LIST') {
            const roomIds = JSON.parse(response.data);
            setRooms(roomIds.map((id) => ({ id, name: `Room ${id}` })));
          }
        } catch (error) {
          setRooms([]);
        }
      };

      socket.addEventListener('message', handleRoomList);

      return () => {
        socket.removeEventListener('message', handleRoomList);
        socket.removeEventListener('open', onSocketReady);
      };
    };

    fetchRooms();
  }, [socket]);

  const handleRoomClick = (roomId) => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({ type: 'JOIN', data: roomId }));
      navigate(`/chat/${roomId}`);
    } else {
      alert('WebSocket is not connected.');
    }
  };

  const createRoom = () => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({ type: 'CREATE', data: '' }));
    }
  };

  return (
    <div className='chat-list-container'>
      <h2 className='chat-list-title'>채팅 목록</h2>
      <button onClick={createRoom} className='chat-room-button'>
        방 생성
      </button>
      <ul className='chat-room-list'>
        {rooms.map((room) => (
          <li
            key={room.id}
            onClick={() => handleRoomClick(room.id)}
            className='chat-room-item'
          >
            <span>{room.name}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default ChatList;
