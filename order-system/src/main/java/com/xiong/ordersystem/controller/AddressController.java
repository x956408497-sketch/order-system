package com.xiong.ordersystem.controller;

import com.xiong.ordersystem.entity.Address;
import com.xiong.ordersystem.service.AddressService;
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
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public List<Address> findByUserId(@RequestParam Long userId) {
        return addressService.findByUserId(userId);
    }

    @PostMapping
    public Address create(@RequestParam Long userId, @RequestBody Address address) {
        return addressService.create(userId, address);
    }

    @PutMapping("/{id}")
    public Address update(@RequestParam Long userId, @PathVariable Long id, @RequestBody Address address) {
        return addressService.update(userId, id, address);
    }

    @PutMapping("/{id}/default")
    public Address setDefault(@RequestParam Long userId, @PathVariable Long id) {
        return addressService.setDefault(userId, id);
    }

    @DeleteMapping("/{id}")
    public void delete(@RequestParam Long userId, @PathVariable Long id) {
        addressService.delete(userId, id);
    }
}
