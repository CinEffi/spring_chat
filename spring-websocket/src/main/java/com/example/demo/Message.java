package com.example.demo;

import lombok.*;

// {"type":"", "sender":"me", "receiver":"b6268d02-ac43-fc6d-39bb-6e09151eb735", "data":"test..."}

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String type;
    private String sender;
    //    private String receiver;
    private Object data;

    public void setSender(String sender) {
        this.sender = sender;
    }
}