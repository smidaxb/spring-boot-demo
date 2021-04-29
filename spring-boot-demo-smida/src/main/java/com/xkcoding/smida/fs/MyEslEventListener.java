package com.xkcoding.smida.fs;

import lombok.extern.slf4j.Slf4j;
import org.freeswitch.esl.client.IEslEventListener;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.springframework.stereotype.Service;

/**
 * 事件监听器
 * des:目前来看单例足以解决所有通话的事件
 *
 * @author yusong
 */
@Slf4j
//@Service
public class MyEslEventListener implements IEslEventListener {

    @Override
    public void eventReceived(EslEvent event) {
        String callId = event.getEventHeaders().get("variable_call_uuid");
        String eventName = event.getEventName();
        if ((!FsEventConstants.CHANNEL_HANGUP_COMPLETE.equals(eventName)) &&
            (!FsEventConstants.CUSTOM_EVENT.equals(eventName)) &&
            (!FsEventConstants.SYSTEM_PLAYBACK_STOP.equals(eventName)) &&
            (!FsEventConstants.CHANNEL_ANSWER.equals(eventName)) &&
            (!FsEventConstants.CHANNEL_DESTROY.equals(eventName)) &&
            (!FsEventConstants.RECV_RTCP_MESSAGE.equals(eventName)) &&
            (!FsEventConstants.SYSTEM_PLAYBACK_START.equals(eventName))
        ) {
            log.warn("eventReceived|无需处理事件|eventName:{}", eventName);
            return;
        }
        log.info("eventReceived|开始处理事件event:{}, callId:{}", event, callId);
    }

    @Override
    public void backgroundJobResultReceived(EslEvent eslEvent) {
        String callId = eslEvent.getEventHeaders().get("variable_call_uuid");
        String eventName = eslEvent.getEventName();
        log.info("backgroundJobResultReceived|callId:{}|eventName:{}", callId, eventName);
    }
}
