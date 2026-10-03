package com.xiong.ordersystem.service;

import com.xiong.ordersystem.entity.CategorySalesStatistics;
import com.xiong.ordersystem.entity.ProductSalesStatistics;
import com.xiong.ordersystem.entity.SalesStatistics;
import com.xiong.ordersystem.entity.StatisticsOverview;
import com.xiong.ordersystem.mapper.StatisticsMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsService {

    private final StatisticsMapper statisticsMapper;

    public StatisticsService(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    public StatisticsOverview overview() {
        return statisticsMapper.overview();
    }

    public List<SalesStatistics> salesTrend() {
        return statisticsMapper.salesTrend();
    }

    public List<ProductSalesStatistics> topProducts() {
        return statisticsMapper.topProducts();
    }

    public List<CategorySalesStatistics> categories() {
        return statisticsMapper.categories();
    }
}
