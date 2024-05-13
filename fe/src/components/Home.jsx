import React from 'react';
import { Link } from 'react-router-dom';

const Home = () => (
  <div>
    <h1>채팅 잘 되게 해주세요.</h1>
    <Link to='/chat'>채팅 목록 보기</Link>
  </div>
);

export default Home;
