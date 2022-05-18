package com.bid.bidmanage.controler;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.common.model.UserInfo;
import one.yiran.dashboard.common.util.UserCacheUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/message/notice/{token}")
@Slf4j
public class NoticeController {

    private static ConcurrentHashMap<String, NoticeController> webSocketPools = new ConcurrentHashMap<>();

    private Session session;
    private String token;
    private UserInfo userInfo;
    //private static ConcurrentHashMap<String, Session> sessionPools = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        UserInfo userInfo = UserCacheUtil.getSessionInfo(token);
        if (userInfo == null) {
            log.info("token={}没有登陆，不建立session", token);
            return;
        }
        this.session = session;
        this.token = token;
        this.userInfo = userInfo;

        if (webSocketPools.containsKey(token)) {
            webSocketPools.remove(token);
        }
        webSocketPools.put(token, this);
        //sessionPools.put(token,session);

        log.info("用户连接:token={} userId={} loginName={}", token, userInfo.getUserId(), userInfo.getLoginName());

        try {
            sendMessage(token,"连接成功");
        } catch (IOException e) {
            log.error("用户userId={}网络异常!!!!!!", userInfo.getUserId());
        }
    }

    @OnClose
    public void onClose() {
        if(this.token != null) {
            if (webSocketPools.containsKey(this.token)){
                webSocketPools.remove(this.token);
            }
            log.info("用户退出:token={} userId={} loginName={}", token, userInfo.getUserId(), userInfo.getLoginName());
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到消息:{}={},报文:{}", this.token, this.userInfo.getLoginName(), message);
        if(StringUtils.equals("ping",message)) {
            try {
                session.getBasicRemote().sendText("pong");
            } catch (IOException e) {
                log.error("pong err",e);
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("ws异常,{}={} ", this.token, this.userInfo.getLoginName(), error);
    }

    public void sendMessage(String tokenId,String message) throws IOException {
        NoticeController conn = webSocketPools.get(tokenId);
        if(conn == null)
            return;
        UserInfo u = conn.userInfo;
        log.info("发送给客户端消息:{}={},报文:{}", tokenId, u.getLoginName(), message);
        conn.session.getBasicRemote().sendText(message);
    }

    public void sendJobMessage(String tokenId, Object message) throws IOException {
        if(tokenId == null)
            return;
        Map<String, Object> map = new HashMap<>();
        map.put("type", "job-collect");
        map.put("data", message);
        sendMessage(tokenId, JSON.toJSONString(map));
    }


}
