package com.emosense.agent.config;

import com.emosense.agent.service.mcp.AlertNotifier;
import com.emosense.agent.service.mcp.ExcelReportWriter;
import com.emosense.agent.service.mcp.HttpAlertNotifier;
import com.emosense.agent.service.mcp.HttpExcelReportWriter;
import com.emosense.agent.service.mcp.LocalExcelReportWriter;
import com.emosense.agent.service.mcp.LogAlertNotifier;
import com.emosense.agent.service.mcp.McpAlertNotifier;
import com.emosense.agent.service.mcp.McpExcelReportWriter;
import com.emosense.agent.service.mcp.McpToolClient;
import com.emosense.agent.service.mcp.EmoSenseMcpTools;
import com.emosense.agent.service.mcp.SmtpAlertNotifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
/**
 * MCP 工具链配置。
 *
 * <p>这里按配置选择 Excel 写入方式和预警通知方式，同时提供独立线程池，
 * 避免后台工具调用阻塞学生端聊天。</p>
 */
public class McpToolConfig {

    @Bean
    public TaskExecutor mcpTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("emosense-mcp-");
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(200);
        executor.initialize();
        return executor;
    }

    @Bean
    public ToolCallbackProvider emoSenseMcpToolProvider(EmoSenseMcpTools emoSenseMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(emoSenseMcpTools)
                .build();
    }

    @Bean
    public McpToolClient mcpToolClient(
            ObjectProvider<SyncMcpToolCallbackProvider> toolCallbackProvider,
            ObjectMapper objectMapper
    ) {
        return new McpToolClient(toolCallbackProvider, objectMapper);
    }

    @Bean
    public ExcelReportWriter excelReportWriter(
            EmoSenseProperties properties,
            WebClient.Builder webClientBuilder,
            McpToolClient mcpToolClient
    ) {
        String mode = properties.getMcp().getExcel().getMode();
        if ("mcp".equalsIgnoreCase(mode)) {
            return new McpExcelReportWriter(mcpToolClient);
        }
        if ("http".equalsIgnoreCase(mode)) {
            return new HttpExcelReportWriter(webClientBuilder, properties);
        }
        return new LocalExcelReportWriter(properties);
    }

    @Bean
    public AlertNotifier alertNotifier(
            EmoSenseProperties properties,
            WebClient.Builder webClientBuilder,
            JavaMailSender mailSender,
            McpToolClient mcpToolClient
    ) {
        String mode = properties.getMcp().getEmail().getMode();
        if ("mcp".equalsIgnoreCase(mode)) {
            return new McpAlertNotifier(mcpToolClient);
        }
        if ("http".equalsIgnoreCase(mode)) {
            return new HttpAlertNotifier(webClientBuilder, properties);
        }
        if ("smtp".equalsIgnoreCase(mode)) {
            return new SmtpAlertNotifier(mailSender, properties);
        }
        return new LogAlertNotifier();
    }
}
