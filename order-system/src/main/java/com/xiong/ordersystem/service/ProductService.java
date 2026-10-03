package com.xiong.ordersystem.service;

import com.xiong.ordersystem.entity.Product;
import com.xiong.ordersystem.mapper.CategoryMapper;
import com.xiong.ordersystem.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;

    public ProductService(ProductMapper productMapper, CategoryMapper categoryMapper) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
    }

    public List<Product> findPublished(Long categoryId, String keyword) {
        return productMapper.findPublished(categoryId, normalizeKeyword(keyword));
    }

    public List<Product> findAll() {
        return productMapper.findAll();
    }

    public Product findPublishedById(Long id) {
        Product product = findById(id);
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new IllegalArgumentException("商品不存在或已下架");
        }
        return product;
    }

    public Product findById(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        return product;
    }

    public Product create(Product product) {
        validate(product);
        productMapper.insert(product);
        return findById(product.getId());
    }

    public Product update(Long id, Product product) {
        findById(id);
        product.setId(id);
        validate(product);
        productMapper.update(product);
        return findById(id);
    }

    public void delete(Long id) {
        findById(id);
        productMapper.deleteById(id);
    }

    public Product updateStatus(Long id, Integer status) {
        findById(id);
        productMapper.updateStatus(id, requireStatus(status));
        return findById(id);
    }

    public Product updateStock(Long id, Integer stock) {
        findById(id);
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("商品库存不能为负数");
        }
        productMapper.updateStock(id, stock);
        return findById(id);
    }

    private void validate(Product product) {
        if (product == null || product.getCategoryId() == null) {
            throw new IllegalArgumentException("商品分类不能为空");
        }
        if (categoryMapper.findById(product.getCategoryId()) == null) {
            throw new IllegalArgumentException("商品分类不存在");
        }
        if (!StringUtils.hasText(product.getName())) {
            throw new IllegalArgumentException("商品名称不能为空");
        }
        product.setName(product.getName().trim());
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("商品价格不能为负数");
        }
        if (product.getStock() == null || product.getStock() < 0) {
            throw new IllegalArgumentException("商品库存不能为负数");
        }
        if (product.getStatus() == null) {
            product.setStatus(0);
        } else {
            product.setStatus(requireStatus(product.getStatus()));
        }
    }

    private Integer requireStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("商品状态只能是0或1");
        }
        return status;
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }
}
