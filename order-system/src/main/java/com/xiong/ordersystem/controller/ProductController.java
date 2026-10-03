package com.xiong.ordersystem.controller;

import com.xiong.ordersystem.entity.Product;
import com.xiong.ordersystem.service.ProductService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<Product> findPublished(@RequestParam(required = false) Long categoryId,
                                       @RequestParam(required = false) String keyword) {
        return productService.findPublished(categoryId, keyword);
    }

    @GetMapping("/products/{id}")
    public Product findPublishedById(@PathVariable Long id) {
        return productService.findPublishedById(id);
    }

    @GetMapping("/admin/products")
    public List<Product> findAll() {
        return productService.findAll();
    }

    @GetMapping("/admin/products/{id}")
    public Product findById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping("/admin/products")
    public Product create(@RequestBody Product product) {
        return productService.create(product);
    }

    @PutMapping("/admin/products/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product product) {
        return productService.update(id, product);
    }

    @DeleteMapping("/admin/products/{id}")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

    @PutMapping("/admin/products/{id}/status")
    public Product updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return productService.updateStatus(id, status);
    }

    @PutMapping("/admin/products/{id}/stock")
    public Product updateStock(@PathVariable Long id, @RequestParam Integer stock) {
        return productService.updateStock(id, stock);
    }
}
