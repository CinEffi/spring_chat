import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

// 간단한 홈 페이지 컴포넌트
const Home = () => (
  <div>
    <h1>Welcome to the React Home Page!</h1>
  </div>
);

// 채팅 및 웹소켓 관리 컴포넌트
class Chat extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      message: '',
      receivedMessages: [],
      isConnected: false
    };
    this.websocket = null;
  }

  componentDidMount() {
    this.connectWebSocket();
  }

  componentWillUnmount() {
    this.disconnectWebSocket();
  }

  connectWebSocket = () => {
    this.websocket = new WebSocket('ws://localhost:8080/chat');
    this.websocket.onopen = () => {
      console.log("WebSocket Connected");
      this.setState({ isConnected: true });
    };
    this.websocket.onmessage = (event) => {
      this.setState(prevState => ({
        receivedMessages: [...prevState.receivedMessages, event.data]
      }));
    };
    this.websocket.onclose = () => {
      console.log("WebSocket Disconnected");
      this.setState({ isConnected: false });
    };
    this.websocket.onerror = (error) => {
      console.error("WebSocket error: ", error);
    };
  };

  disconnectWebSocket = () => {
    if (this.websocket) {
      this.websocket.close();
    }
  };

  handleMessageChange = (event) => {
    this.setState({ message: event.target.value });
  };

  sendMessage = () => {
    if (this.websocket && this.state.isConnected) {
      this.websocket.send(this.state.message);
    } else {
      alert("WebSocket is not connected.");
    }
  };

  render() {
    return (
      <div>
        <h1>Chat Room</h1>
        <textarea
          placeholder="Enter message"
          value={this.state.message}
          onChange={this.handleMessageChange}
          rows="4"
          style={{ width: '100%' }}
        />
        <button onClick={this.sendMessage}>Send</button>
        <div>
          <h2>Received Messages</h2>
          {this.state.receivedMessages.map((msg, index) => (
            <p key={index}>{msg}</p>
          ))}
        </div>
      </div>
    );
  }
}

// 라우터 설정
function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/chat" element={<Chat />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;