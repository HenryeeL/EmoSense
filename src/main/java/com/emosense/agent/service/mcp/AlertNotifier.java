package com.emosense.agent.service.mcp;

import com.emosense.agent.domain.AlertRecord;
import com.emosense.agent.domain.PsychologicalReport;

/**
 * 高风险预警通知接口。
 *
 * <p>具体实现可以是日志、SMTP 邮件或 HTTP MCP 服务。</p>
 */
public interface AlertNotifier {

    void notify(AlertRecord alertRecord, PsychologicalReport report);
}
