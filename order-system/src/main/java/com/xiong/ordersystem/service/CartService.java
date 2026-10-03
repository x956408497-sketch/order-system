package com.xiong.ordersystem.service;

import com.xiong.ordersystem.entity.CartItem;
import com.xiong.ordersystem.entity.Product;
import com.xiong.ordersystem.mapper.CartMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    private final CartMapper cartMapper;
    private final ProductService productService;

    public CartService(CartMapper cartMapper, ProductService productService) {
        this.cartMapper = cartMapper;
        this.productService = productService;
    }

    public List<CartItem> findByUserId(Long userId) {
        validateUser(userId);
        return cartMapper.findByUserId(userId);
    }

    @Transactional
    public List<CartItem> add(Long userId, Long productId, Integer quantity) {
        validateUser(userId);
        validateQuantity(quantity);
        Product product = productService.findPublishedById(productId);
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("商品库存不足");
        }

        CartItem existing = cartMapper.findByUserIdAndProductId(userId, productId);
        if (existing == null) {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setProductId(productId);
            item.setQuantity(quantity);
            cartMapper.insert(item);
        } else {
            long newQuantity = (long) existing.getQuantity() + quantity;
            if (newQuantity > product.getStock()) {
                throw new IllegalArgumentException("购物车商品数量超过库存");
            }
            existing.setQuantity((int) newQuantity);
            cartMapper.updateQuantity(existing);
        }
        return cartMapper.findByUserId(userId);
    }

    public List<CartItem> updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        validateUser(userId);
        validateQuantity(quantity);
        CartItem item = findOwnedItem(userId, cartItemId);
        Product product = productService.findPublishedById(item.getProductId());
        if (quantity > product.getStock()) {
            throw new IllegalArgumentException("购物车商品数量超过库存");
        }
        item.setQuantity(quantity);
        cartMapper.updateQuantity(item);
        return cartMapper.findByUserId(userId);
    }

    public List<CartItem> delete(Long userId, Long cartItemId) {
        validateUser(userId);
        findOwnedItem(userId, cartItemId);
        cartMapper.deleteByIdAndUserId(cartItemId, userId);
        return cartMapper.findByUserId(userId);
    }

    public void clear(Long userId) {
        validateUser(userId);
        cartMapper.deleteByUserId(userId);
    }

    private CartItem findOwnedItem(Long userId, Long cartItemId) {
        CartItem item = cartMapper.findByIdAndUserId(cartItemId, userId);
        if (item == null) {
            throw new IllegalArgumentException("购物车商品不存在");
        }
        return item;
    }

    private void validateUser(Long userId) {
        if (userId == null || userId <= 0 || cartMapper.countUser(userId) == 0) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("商品数量必须大于0");
        }
    }
}
