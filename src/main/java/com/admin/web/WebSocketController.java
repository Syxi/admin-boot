package com.admin.web;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebSocketController {


    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public String handleMessage(String message) {
        System.out.println("收到前端传来的消息：" + message);
        //广播消息到 前端订阅的地址
        return "后端发送消息到前端：服务器已接收到你的消息！";
    }
}
