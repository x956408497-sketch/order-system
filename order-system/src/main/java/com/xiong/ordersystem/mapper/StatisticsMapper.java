package com.xiong.ordersystem.mapper;

import com.xiong.ordersystem.entity.CategorySalesStatistics;
import com.xiong.ordersystem.entity.ProductSalesStatistics;
import com.xiong.ordersystem.entity.SalesStatistics;
import com.xiong.ordersystem.entity.StatisticsOverview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StatisticsMapper {

    @Select("""
            SELECT CONCAT_WS('|', o.id, o.order_status, o.pay_status, o.update_time,
                             oi.id, oi.product_id, oi.product_name, oi.quantity, oi.subtotal,
                             p.category_id, p.name, c.name)
            FROM orders o
            JOIN order_item oi ON oi.order_id = o.id
            JOIN product p ON p.id = oi.product_id
            JOIN category c ON c.id = p.category_id
            WHERE o.order_status IN ('PAID', 'TO_SHIP', 'SHIPPED', 'COMPLETED')
            ORDER BY o.id, oi.id
            """)
    List<String> analysisSourceRows();

    @Select("""
            SELECT COALESCE(SUM(order_count), 0) AS orderCount,
                   COALESCE(SUM(product_count), 0) AS productCount,
                   COALESCE(SUM(sales_amount), 0) AS salesAmount,
                   (
                       SELECT product_name
                       FROM product_sales_statistics
                       GROUP BY product_id, product_name
                       ORDER BY SUM(sales_count) DESC, SUM(sales_amount) DESC
                       LIMIT 1
                   ) AS topProductName
            FROM sales_statistics
            """)
    StatisticsOverview overview();

    @Select("""
            SELECT id, stat_date AS statDate, order_count AS orderCount,
                   product_count AS productCount, sales_amount AS salesAmount,
                   create_time AS createTime
            FROM sales_statistics
            ORDER BY stat_date ASC
            """)
    List<SalesStatistics> salesTrend();

    @Select("""
            SELECT product_id AS productId, product_name AS productName,
                   SUM(sales_count) AS salesCount, SUM(sales_amount) AS salesAmount,
                   MAX(stat_date) AS latestStatDate
            FROM product_sales_statistics
            GROUP BY product_id, product_name
            ORDER BY salesCount DESC, salesAmount DESC
            LIMIT 20
            """)
    List<ProductSalesStatistics> topProducts();

    @Select("""
            SELECT category_id AS categoryId, category_name AS categoryName,
                   SUM(sales_count) AS salesCount, SUM(sales_amount) AS salesAmount,
                   MAX(stat_date) AS latestStatDate
            FROM category_sales_statistics
            GROUP BY category_id, category_name
            ORDER BY salesAmount DESC, salesCount DESC
            LIMIT 20
            """)
    List<CategorySalesStatistics> categories();
}
