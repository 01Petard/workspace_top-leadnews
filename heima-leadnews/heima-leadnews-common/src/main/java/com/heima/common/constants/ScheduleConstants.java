package com.heima.common.constants;

public class ScheduleConstants {

    //task状态
    public static final int SCHEDULED = 0;   //初始化状态

    public static final int EXECUTED = 1;       //已执行状态

    public static final int CANCELLED = 2;   //已取消状态

    public static String FUTURE = "future_";   //未来数据key前缀

    public static int FUTURE_AMOUNT = 5;   //未来任务执行的时间，单位分钟。大于这个时间的不会记录到redis中

    public static String TOPIC = "topic_";     //当前数据key前缀
}