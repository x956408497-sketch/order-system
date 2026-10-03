package com.xiong.ordersystem.service;

import com.xiong.ordersystem.mapper.StatisticsMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class SparkAnalysisService {

    private final StatisticsMapper statisticsMapper;
    private final Map<String, AnalysisTask> tasks = new ConcurrentHashMap<>();
    private volatile String lastSuccessfulFingerprint;
    private volatile String runningTaskId;

    @Value("${spark.analysis.script:../order-analysis/RunSparkAnalysis.ps1}")
    private String scriptPath;

    @Value("${spark.analysis.timeout-seconds:180}")
    private long timeoutSeconds;

    public SparkAnalysisService(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    public synchronized AnalysisTask submit() {
        if (runningTaskId != null) {
            AnalysisTask running = tasks.get(runningTaskId);
            if (running != null && "RUNNING".equals(running.status())) return running;
        }
        String fingerprint = fingerprint(statisticsMapper.analysisSourceRows());
        if (fingerprint.equals(lastSuccessfulFingerprint)) {
            return new AnalysisTask(UUID.randomUUID().toString(), "COMPLETED", "订单数据未变化，已跳过 Spark 分析");
        }

        String taskId = UUID.randomUUID().toString();
        tasks.put(taskId, new AnalysisTask(taskId, "RUNNING", "Spark 正在重新计算销售统计"));
        runningTaskId = taskId;
        CompletableFuture.runAsync(() -> execute(taskId, fingerprint));
        return tasks.get(taskId);
    }

    public AnalysisTask findTask(String taskId) {
        AnalysisTask task = tasks.get(taskId);
        if (task == null) throw new IllegalArgumentException("分析任务不存在或已过期");
        return task;
    }

    private void execute(String taskId, String fingerprint) {
        Path output = null;
        try {
            List<String> command = List.of("powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass",
                    "-File", scriptPath, "-NoPause");
            output = Files.createTempFile("spark-analysis-", ".log");
            Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output.toFile()).start();
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IllegalStateException("Spark 分析超时，请稍后重试");
            }
            String log = Files.readString(output, StandardCharsets.UTF_8);
            if (process.exitValue() != 0) throw new IllegalStateException(lastLines(log));
            synchronized (this) {
                lastSuccessfulFingerprint = fingerprint;
                tasks.put(taskId, new AnalysisTask(taskId, "COMPLETED", "Spark 分析完成"));
                runningTaskId = null;
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            synchronized (this) {
                tasks.put(taskId, new AnalysisTask(taskId, "FAILED", "Spark 分析被中断"));
                runningTaskId = null;
            }
        } catch (Exception exception) {
            String message = exception.getMessage() == null ? "无法启动 Spark 分析" : exception.getMessage();
            synchronized (this) {
                tasks.put(taskId, new AnalysisTask(taskId, "FAILED", message));
                runningTaskId = null;
            }
        } finally {
            if (output != null) {
                try { Files.deleteIfExists(output); } catch (IOException ignored) { }
            }
        }
    }

    private String fingerprint(List<String> rows) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String row : rows) {
                digest.update(row.getBytes(StandardCharsets.UTF_8));
                digest.update((byte) '\n');
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception exception) {
            throw new IllegalStateException("无法计算订单数据指纹", exception);
        }
    }

    private String lastLines(String output) {
        String[] lines = output.strip().split("\\R");
        int start = Math.max(0, lines.length - 8);
        return String.join(" | ", java.util.Arrays.copyOfRange(lines, start, lines.length));
    }

    public record AnalysisTask(String taskId, String status, String message) { }
}
