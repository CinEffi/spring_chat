package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> chatroom_sessions = new ConcurrentHashMap<>();
    private final Map<String, Long> session_chatroom = new ConcurrentHashMap<>();

    //웹소켓 연결 시

    public boolean isSessionOK(WebSocketSession session) { return session != null && session.isOpen(); }
    public void sessionDelete(WebSocketSession session) {
        var sessionId = session.getId();
        Long chatroomId = session_chatroom.get(sessionId);
        if (chatroomId != 0L) { chatroom_sessions.get(chatroomId).remove(sessionId); }
        session_chatroom.remove(sessionId);
        sessions.remove(sessionId);
    }
    public void sendToSession(WebSocketSession session, Message message) throws Exception {
        if (isSessionOK(session)) {session.sendMessage(new TextMessage(Utils.getString(message)));}
        else {sessionDelete(session);}
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        var sessionId = session.getId();
        System.out.println("WebSocketHandler.afterConnectionEstablished/session : " + sessionId);
        ////////////////////////////////////////////////////////////////////////

        sessions.put(sessionId, session);
        session_chatroom.put(sessionId, 0L);
        System.out.println("session connected : " + sessionId);
//        if (isSessionOK(session))
//            session.sendMessage(new TextMessage(chatroomIdSet.isEmpty() ? "no chatroom" : chatroomIdSet.toString()));
//        else {
//            sessionDelete(session);
//        }

//        sessions.values().forEach(s -> {
//            try {
//                if(!s.getId().equals(sessionId)) {
//                    s.sendMessage(new TextMessage(Utils.getString(message)));
//                }
//            }
//            catch (Exception e) {
//                //TODO: throw
//            }
//        });

        ////////////////////////////////////////////////////////////////////////

        System.out.println("WebSocketHandler.afterConnectionEstablished - done / session : " + sessionId);
    }

    //양방향 데이터 통신
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        System.out.println("WebSocketHandler.handleTextMessage");
        System.out.println("session = " + session);
        System.out.println("textMessage = " + textMessage);
        System.out.println("textMessage.getPayload() = " + textMessage.getPayload());
        ////////////////////////////////////////////////////////////////////////

        Message message = null;
        try { message= Utils.getObject(textMessage.getPayload());}
        catch (Exception e) {
            System.out.println("getObject() Exception occur = " + textMessage.getPayload());
            System.out.println("e = " + e); return;
        }

        String type = message.getType();
        switch(type) {
            case "LIST" :
                chatroomList(session);
                break;
            case "CREATE" :
                chatroomCreate(session);//session, textMessage);
                break;
            case "JOIN" :
                chatroomJoin(session, message.getData().toString());
                break;
            case "SEND" :
                sendMessageToChatroom(session, message.getData().toString());
                break;
            default :
                System.out.println("Unknown type : " + type);
        }//        message.setSender(session.getId());//        WebSocketSession receiver = sessions.get(message.getReceiver());//        if (receiver != null && receiver.isOpen()) {//            receiver.sendMessage(new TextMessage(Utils.getString(message)));//        }

        ////////////////////////////////////////////////////////////////////////
        System.out.println("WebSocketHandler.handleTextMessage : done");
    }

    private void chatroomList(WebSocketSession session) throws Exception {
        Set<Long> chatroomIdSet = chatroom_sessions.keySet();
        sendToSession(session, Message.builder()
                .type("CHATROOM_LIST")
                .sender("SERVER")
                .data(chatroomIdSet.isEmpty() ?
                        "no chatroom" :
                        chatroomIdSet.toString()
                ).build()
        );
    }

    private void chatroomCreate(WebSocketSession session // 사실 오히려 이쪽이 더 필요가 없긴 한데
//        , TextMessage textMessage //뭐 방 제목을 전달해줘야한다거나 할때 쓸수 있어.
    )
            throws Exception
    {
        System.out.println("WebSocketHandler.chatroomCreate");
        ////////////////////////////////////////////////////////////////////////

        Long chatroomId = chatroom_sessions.size() + 1L;
        Set<String> sessionsInChatroom = new HashSet<>();
        chatroom_sessions.put(chatroomId, sessionsInChatroom);
        chatroomList(session);
        ////////////////////////////////////////////////////////////////////////
        System.out.println("WebSocketHandler.chatroomCreate : done");
    }

    private void chatroomJoin(WebSocketSession session, String messageData) throws Exception {
        System.out.println("WebSocketHandler.chatroomJoin");
        System.out.println("session = " + session);
        System.out.println("messageData = " + messageData);
        ////////////////////////////////////////////////////////////////////////
        String sessionId = session.getId();
        String responseResult = "JOIN_FAILURE";
        try {
            Long chatroomId = Long.parseLong(messageData);// 실패시 exception인데, 어떻게 되나 한번 보자. 웹소켓에 잘 전달이 되는지

            if (chatroom_sessions.containsKey(chatroomId)) {
                responseResult = "JOIN_SUCCESS";
                Long exChatroomId = session_chatroom.get(sessionId);
                if (exChatroomId != 0L) {
                    chatroom_sessions.get(exChatroomId).remove(sessionId);
                }
                session_chatroom.put(sessionId, chatroomId);
                chatroom_sessions.get(chatroomId).add(session.getId());
            }
        }
        catch (NumberFormatException e) {
            responseResult = "JOIN_FAILURE";
            System.out.println(e);
            System.out.println("[Invalid chatroomId] : " + messageData);
        }
        try {
            sendToSession(session, Message.builder().sender("SERVER").type("JOIN_STATUS").data(responseResult).build());
            if (responseResult.equals("JOIN_SUCCESS")) { sendMessageToChatroom(session, "내 왔다."); }
        }
        catch (Exception e) {
            System.out.println(e);
            System.out.println("Utils.getString Exception occur");
        }
        ////////////////////////////////////////////////////////////////////////
        System.out.println("WebSocketHandler.chatroomJoin : done");
    }

    private void sendMessageToChatroom(WebSocketSession session, String messageData) throws Exception  {

        System.out.println("WebSocketHandler.sendMessageToChatroom");
        System.out.println("session = " + session);
        System.out.println("messageData = " + messageData);
        ////////////////////////////////////////////////////////////////////////
        String senderId = session.getId();

        Long chatroomId = session_chatroom.get(senderId);
        if (chatroomId == 0L)
        {
            System.out.println("chatroomId(0), session : " + session);
            System.out.println("messageData = " + messageData);
            System.out.println("WebSocketHandler.sendMessageToChatroom : done");
            return;
        }


        for (String sessionId : chatroom_sessions.get(chatroomId)) {
            sendToSession(sessions.get(sessionId), Message.builder().sender(senderId).type("CHAT_MESSAGE").data(messageData).build());
        }

        ////////////////////////////////////////////////////////////////////////
        System.out.println("WebSocketHandler.sendMessageToChatroom : done");
    }

    //소켓 연결 종료
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();

        System.out.println("WebSocketHandler.afterConnectionClosed/session : " + sessionId);
        System.out.println("status = " + status);
        ////////////////////////////////////////////////////////////////////////
        Long exChatroomId = session_chatroom.get(sessionId);
        if (exChatroomId != 0L) {
            sendMessageToChatroom(session, "내 간데이");
            chatroom_sessions.get(exChatroomId).remove(sessionId);
        }
        session_chatroom.remove(session.getId());
        sessions.remove(sessionId);
        System.out.println("session disconnected : " + sessionId);

        ////////////////////////////////////////////////////////////////////////
        System.out.println("WebSocketHandler.afterConnectionClosed done/session : " + sessionId);

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        //TODO:
    }
}
