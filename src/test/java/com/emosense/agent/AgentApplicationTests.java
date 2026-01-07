package com.emosense.agent;

import static org.assertj.core.api.Assertions.assertThat;

import com.emosense.agent.domain.ChatMessage;
import com.emosense.agent.domain.ChatSession;
import com.emosense.agent.domain.IntentType;
import com.emosense.agent.domain.MessageRole;
import com.emosense.agent.domain.RiskLevel;
import com.emosense.agent.domain.UserAccount;
import com.emosense.agent.dto.AgentRunTraceResponse;
import com.emosense.agent.repository.ChatMessageRepository;
import com.emosense.agent.repository.ChatSessionRepository;
import com.emosense.agent.repository.UserAccountRepository;
import com.emosense.agent.service.AgentRunTraceService;
import com.emosense.agent.service.agent.AgentAction;
import com.emosense.agent.service.agent.AgentName;
import com.emosense.agent.service.agent.AgentRunResult;
import com.emosense.agent.service.agent.AgentStep;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:emosense-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE"
})
class AgentApplicationTests {

    @Autowired
    private AgentRunTraceService agentRunTraceService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void persistsAndReadsAgentRunTrace() {
        UserAccount user = new UserAccount();
        user.setUsername("trace-student");
        user.setDisplayName("Trace Student");
        user.setPassword("encoded");
        user.setRoles(Set.of("ROLE_USER"));
        user = userAccountRepository.save(user);

        ChatSession session = new ChatSession();
        session.setPublicId("trace-session");
        session.setTitle("Trace session");
        session.setUser(user);
        session = chatSessionRepository.save(session);

        ChatMessage message = new ChatMessage();
        message.setUser(user);
        message.setSession(session);
        message.setRole(MessageRole.USER);
        message.setContent("hello trace");
        message = chatMessageRepository.save(message);

        Instant startedAt = Instant.now();
        AgentRunResult result = new AgentRunResult(
                IntentType.CHAT,
                RiskLevel.LOW,
                null,
                List.of(),
                List.of(),
                List.of(),
                "memory loaded",
                null,
                "answer naturally",
                AgentName.COMPANION_AGENT,
                List.of(new AgentStep(
                        1,
                        AgentName.MEMORY_AGENT,
                        AgentAction.READ_MEMORY,
                        "loaded short-term memory",
                        startedAt))
        );

        String traceId = agentRunTraceService
                .saveRun(user, session, message, "hello trace", result, startedAt, Instant.now())
                .getTraceId();

        AgentRunTraceResponse trace = agentRunTraceService.trace(traceId);
        assertThat(trace.sessionId()).isEqualTo("trace-session");
        assertThat(trace.stepCount()).isEqualTo(1);
        assertThat(trace.steps()).singleElement()
                .satisfies(step -> {
                    assertThat(step.agent()).isEqualTo(AgentName.MEMORY_AGENT);
                    assertThat(step.action()).isEqualTo(AgentAction.READ_MEMORY);
                    assertThat(step.observation()).isEqualTo("loaded short-term memory");
                });
        assertThat(agentRunTraceService.tracesForSession("trace-session"))
                .extracting(AgentRunTraceResponse::traceId)
                .contains(traceId);
    }
}
