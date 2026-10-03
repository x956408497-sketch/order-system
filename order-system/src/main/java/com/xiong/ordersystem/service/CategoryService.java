package com.xiong.ordersystem.service;

import com.xiong.ordersystem.entity.Category;
import com.xiong.ordersystem.mapper.CategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<Category> findAll() {
        return categoryMapper.findAll();
    }

    public Category create(Category category) {
        validate(category, null);
        categoryMapper.insert(category);
        return findById(category.getId());
    }

    public Category update(Long id, Category category) {
        findById(id);
        category.setId(id);
        validate(category, id);
        categoryMapper.update(category);
        return findById(id);
    }

    public void delete(Long id) {
        findById(id);
        if (categoryMapper.countProducts(id) > 0) {
            throw new IllegalStateException("分类下仍有商品，不能删除");
        }
        categoryMapper.deleteById(id);
    }

    private Category findById(Long id) {
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new IllegalArgumentException("分类不存在");
        }
        return category;
    }

    private void validate(Category category, Long currentId) {
        if (category == null || !StringUtils.hasText(category.getName())) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        category.setName(category.getName().trim());
        if (category.getName().length() > 64) {
            throw new IllegalArgumentException("分类名称不能超过64个字符");
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getSort() < 0) {
            throw new IllegalArgumentException("分类排序不能为负数");
        }
        Category existing = categoryMapper.findByName(category.getName());
        if (existing != null && (currentId == null || !existing.getId().equals(currentId))) {
            throw new IllegalArgumentException("分类名称已存在");
        }
    }
}
