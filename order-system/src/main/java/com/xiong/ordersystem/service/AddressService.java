package com.xiong.ordersystem.service;

import com.xiong.ordersystem.entity.Address;
import com.xiong.ordersystem.mapper.AddressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AddressService {

    private final AddressMapper addressMapper;

    public AddressService(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public List<Address> findByUserId(Long userId) {
        validateUser(userId);
        return addressMapper.findByUserId(userId);
    }

    @Transactional
    public Address create(Long userId, Address address) {
        validateUser(userId);
        validate(address);
        address.setUserId(userId);
        boolean makeDefault = address.getIsDefault() != null && address.getIsDefault() == 1;
        if (makeDefault || addressMapper.findByUserId(userId).isEmpty()) {
            addressMapper.clearDefault(userId);
            address.setIsDefault(1);
        } else {
            address.setIsDefault(0);
        }
        addressMapper.insert(address);
        return addressMapper.findByIdAndUserId(address.getId(), userId);
    }

    public Address update(Long userId, Long id, Address address) {
        validateUser(userId);
        findOwnedAddress(userId, id);
        validate(address);
        address.setId(id);
        address.setUserId(userId);
        addressMapper.update(address);
        return addressMapper.findByIdAndUserId(id, userId);
    }

    @Transactional
    public Address setDefault(Long userId, Long id) {
        validateUser(userId);
        findOwnedAddress(userId, id);
        addressMapper.clearDefault(userId);
        addressMapper.setDefault(id, userId);
        return addressMapper.findByIdAndUserId(id, userId);
    }

    public void delete(Long userId, Long id) {
        validateUser(userId);
        findOwnedAddress(userId, id);
        addressMapper.deleteByIdAndUserId(id, userId);
    }

    private Address findOwnedAddress(Long userId, Long id) {
        Address address = addressMapper.findByIdAndUserId(id, userId);
        if (address == null) {
            throw new IllegalArgumentException("收货地址不存在");
        }
        return address;
    }

    private void validateUser(Long userId) {
        if (userId == null || userId <= 0 || addressMapper.countUser(userId) == 0) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    private void validate(Address address) {
        if (address == null || !StringUtils.hasText(address.getReceiverName())) {
            throw new IllegalArgumentException("收货人姓名不能为空");
        }
        if (!StringUtils.hasText(address.getPhone())) {
            throw new IllegalArgumentException("联系电话不能为空");
        }
        if (!StringUtils.hasText(address.getDetailAddress())) {
            throw new IllegalArgumentException("详细地址不能为空");
        }
        address.setReceiverName(address.getReceiverName().trim());
        address.setPhone(address.getPhone().trim());
        address.setDetailAddress(address.getDetailAddress().trim());
        if (address.getIsDefault() != null && address.getIsDefault() != 0 && address.getIsDefault() != 1) {
            throw new IllegalArgumentException("默认地址状态只能是0或1");
        }
    }
}
