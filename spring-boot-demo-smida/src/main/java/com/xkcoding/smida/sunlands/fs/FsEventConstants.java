package com.xkcoding.smida.sunlands.fs;

/**
 * FreeSwitch 事件列表
 * 通常5类：Channel events/System events/Undocumented events/Other events/Custom events
 * <p>
 * https://freeswitch.org/confluence/display/FREESWITCH/Event+List
 *
 * @author Created by hetao on 2019/7/19
 */
public class FsEventConstants {

    //******Channel events**********//

    public final static String CHANNEL_CREATE = "CHANNEL_CREATE";
    public final static String CHANNEL_DESTROY = "CHANNEL_DESTROY";
    public final static String CHANNEL_STATE = "CHANNEL_STATE";
    public final static String CHANNEL_ANSWER = "CHANNEL_ANSWER";
    public final static String CHANNEL_HANGUP = "CHANNEL_HANGUP";
    public final static String CHANNEL_HANGUP_COMPLETE = "CHANNEL_HANGUP_COMPLETE";
    public final static String CHANNEL_EXECUTE_COMPLETE = "CHANNEL_EXECUTE_COMPLETE";
    public final static String CHANNEL_BRIDGE = "CHANNEL_BRIDGE";
    public final static String CHANNEL_UNBRIDGE = "CHANNEL_UNBRIDGE";
    public final static String CHANNEL_PROGRESS = "CHANNEL_PROGRESS";
    public final static String CHANNEL_PROGRESS_MEDIA = "CHANNEL_PROGRESS_MEDIA";
    public final static String CHANNEL_OUTGOING = "CHANNEL_OUTGOING";
    public final static String CHANNEL_PARK = "CHANNEL_PARK";
    public final static String CHANNEL_UNPARK = "CHANNEL_UNPARK";
    public final static String CHANNEL_APPLICATION = "CHANNEL_APPLICATION";
    public final static String CHANNEL_ORIGINATE = "CHANNEL_ORIGINATE";
    public final static String CHANNEL_UUID = "CHANNEL_UUID";
    public final static String CHANNEL_DATA = "CHANNEL_DATA";

    public final static String RECV_RTCP_MESSAGE = "RECV_RTCP_MESSAGE";

    //******System events**********//

    public final static String SYSTEM_PLAYBACK_START = "PLAYBACK_START";
    public final static String SYSTEM_PLAYBACK_STOP = "PLAYBACK_STOP";

    //******other events**********//

    //Custom is just a place holder for other events.
    public final static String CUSTOM_EVENT = "CUSTOM";
    public final static String CUSTOM_SUBCLASS = "Event-Subclass";
    public final static String MRCP_UPLOAD_SUBCLASS ="mrcp-upload";
    public final static String MRCP_BIND_SUBCLASS ="mrcp-bind";
    public final static String MRCP_DATA_HEADER ="MRCP-Data";

}
