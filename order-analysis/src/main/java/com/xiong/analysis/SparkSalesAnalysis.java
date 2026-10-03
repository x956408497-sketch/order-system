package com.xiong.analysis;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.LocalDate;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.countDistinct;
import static org.apache.spark.sql.functions.sum;

public class SparkSalesAnalysis {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/order_system"
            + "?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8"
            + "&connectionCollation=utf8mb4_unicode_ci";
    private static final String JDBC_USER = env("MYSQL_USER", "root");
    private static final String JDBC_PASSWORD = env("MYSQL_PASSWORD", "YOUR_MYSQL_PASSWORD");
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    public static void main(String[] args) throws Exception {
        System.setProperty("HADOOP_HOME", "C:/hadoop");
        LocalDate statDate = LocalDate.now();

        SparkSession spark = SparkSession.builder()
                .appName("OrderSalesAnalysis")
                .master("local[*]")
                .config("spark.ui.enabled", "false")
                .getOrCreate();
        spark.sparkContext().setLogLevel("WARN");

        try {
            Dataset<Row> orderDetails = spark.read()
                    .format("jdbc")
                    .option("url", JDBC_URL)
                    .option("dbtable", "(" + validOrderDetailSql() + ") AS order_details")
                    .option("user", JDBC_USER)
                    .option("password", JDBC_PASSWORD)
                    .option("driver", JDBC_DRIVER)
                    .load();

            orderDetails.createOrReplaceTempView("valid_order_details");

            Dataset<Row> daily = spark.sql("""
                    SELECT stat_date,
                           COUNT(DISTINCT order_id) AS order_count,
                           CAST(SUM(quantity) AS INT) AS product_count,
                           CAST(SUM(subtotal) AS DECIMAL(12, 2)) AS sales_amount
                    FROM valid_order_details
                    GROUP BY stat_date
                    """);

            Dataset<Row> products = spark.sql("""
                    SELECT stat_date, product_id, product_name,
                           CAST(SUM(quantity) AS INT) AS sales_count,
                           CAST(SUM(subtotal) AS DECIMAL(12, 2)) AS sales_amount
                    FROM valid_order_details
                    GROUP BY stat_date, product_id, product_name
                    """);

            Dataset<Row> categories = spark.sql("""
                    SELECT stat_date, category_id, category_name,
                           CAST(SUM(quantity) AS INT) AS sales_count,
                           CAST(SUM(subtotal) AS DECIMAL(12, 2)) AS sales_amount
                    FROM valid_order_details
                    GROUP BY stat_date, category_id, category_name
                    """);

            deleteAllStatistics();
            write(daily, "sales_statistics", "append");
            write(products, "product_sales_statistics", "append");
            write(categories, "category_sales_statistics", "append");

            System.out.println("Spark sales analysis completed for " + statDate);
            daily.orderBy(col("sales_amount").desc()).show(false);
            products.orderBy(col("sales_count").desc()).show(10, false);
            categories.orderBy(col("sales_amount").desc()).show(false);
        } finally {
            spark.stop();
        }
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static String validOrderDetailSql() {
        return "SELECT o.id AS order_id, DATE(o.create_time) AS stat_date, "
                + "oi.product_id, oi.product_name, oi.quantity, oi.subtotal, "
                + "p.category_id, c.name AS category_name "
                + "FROM orders o "
                + "JOIN order_item oi ON oi.order_id = o.id "
                + "JOIN product p ON p.id = oi.product_id "
                + "JOIN category c ON c.id = p.category_id "
                + "WHERE o.order_status IN ('PAID', 'TO_SHIP', 'SHIPPED', 'COMPLETED')";
    }

    private static void write(Dataset<Row> dataset, String table, String mode) {
        dataset.write()
                .format("jdbc")
                .option("url", JDBC_URL)
                .option("dbtable", table)
                .option("user", JDBC_USER)
                .option("password", JDBC_PASSWORD)
                .option("driver", JDBC_DRIVER)
                .mode(mode)
                .save();
    }

    private static void deleteAllStatistics() throws Exception {
        Class.forName(JDBC_DRIVER);
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            for (String table : new String[]{
                    "sales_statistics", "product_sales_statistics", "category_sales_statistics"}) {
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table)) {
                    statement.executeUpdate();
                }
            }
        }
    }
}

