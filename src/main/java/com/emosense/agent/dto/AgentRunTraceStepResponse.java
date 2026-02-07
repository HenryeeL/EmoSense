package com.emosense.agent.dto;

import com.emosense.agent.domain.AgentRunTraceStep;
import com.emosense.agent.service.agent.AgentAction;
import com.emosense.agent.service.agent.AgentName;
import java.time.Instant;

/**
 * 管理员查看 agent run trace 时的单步轨迹。
 */
public record AgentRunTraceStepResponse(
        int step,
        AgentName agent,
        AgentAction action,
        String observation,
        Instant createdAt
) {
    public static AgentRunTraceStepResponse from(AgentRunTraceStep step) {
        return new AgentRunTraceStepResponse(
                step.getStepNumber(),
                step.getAgent(),
                step.getAction(),
                step.getObservation(),
                step.getCreatedAt());
    }
}
