package com.heima.model.schedule.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class Task implements Serializable {

    /**
     * 任务id
     */
    private Long taskId;
    /**
     * 任务类型
     */
    private Integer taskType;

    /**
     * 任务优先级
     */
    private Integer priority;

    /**
     * 任务执行id
     */
    private long executeTime;

    /**
     * 任务执行的参数
     */
    private byte[] parameters;

}