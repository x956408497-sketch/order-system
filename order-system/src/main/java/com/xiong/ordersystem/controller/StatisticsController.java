package com.xiong.ordersystem.controller;

import com.xiong.ordersystem.entity.CategorySalesStatistics;
import com.xiong.ordersystem.entity.ProductSalesStatistics;
import com.xiong.ordersystem.entity.SalesStatistics;
import com.xiong.ordersystem.entity.StatisticsOverview;
import com.xiong.ordersystem.service.SparkAnalysisService;
import com.xiong.ordersystem.service.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final SparkAnalysisService sparkAnalysisService;

    public StatisticsController(StatisticsService statisticsService, SparkAnalysisService sparkAnalysisService) {
        this.statisticsService = statisticsService;
        this.sparkAnalysisService = sparkAnalysisService;
    }

    @PostMapping("/refresh")
    public SparkAnalysisService.AnalysisTask refresh() {
        return sparkAnalysisService.submit();
    }

    @GetMapping("/refresh/{taskId}")
    public SparkAnalysisService.AnalysisTask refreshStatus(@PathVariable String taskId) {
        return sparkAnalysisService.findTask(taskId);
    }

    @GetMapping("/overview")
    public StatisticsOverview overview() {
        return statisticsService.overview();
    }

    @GetMapping("/sales-trend")
    public List<SalesStatistics> salesTrend() {
        return statisticsService.salesTrend();
    }

    @GetMapping("/top-products")
    public List<ProductSalesStatistics> topProducts() {
        return statisticsService.topProducts();
    }

    @GetMapping("/categories")
    public List<CategorySalesStatistics> categories() {
        return statisticsService.categories();
    }
}
