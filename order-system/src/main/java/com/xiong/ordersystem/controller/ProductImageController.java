package com.xiong.ordersystem.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
public class ProductImageController {
    private final Path imageDirectory;

    public ProductImageController(@Value("${product.images.directory:./uploads/products}") String directory) {
        this.imageDirectory = Path.of(directory).toAbsolutePath().normalize();
    }

    @PostMapping(path = "/api/admin/products/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getSize() > 1024 * 1024) {
            throw new IllegalArgumentException("请选择不超过 1MB 的图片");
        }
        String extension;
        try (InputStream input = file.getInputStream()) {
            BufferedImage image = ImageIO.read(input);
            if (image == null || image.getWidth() < 1 || image.getHeight() < 1) {
                throw new IllegalArgumentException("图片格式无效，请使用 JPG 或 PNG 图片");
            }
        }
        String type = file.getContentType();
        if (MediaType.IMAGE_JPEG_VALUE.equals(type)) extension = ".jpg";
        else if (MediaType.IMAGE_PNG_VALUE.equals(type)) extension = ".png";
        else throw new IllegalArgumentException("仅支持 JPG 或 PNG 图片");

        Files.createDirectories(imageDirectory);
        String name = UUID.randomUUID() + extension;
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, imageDirectory.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        }
        return Map.of("imageUrl", "/uploads/products/" + name);
    }

    @GetMapping("/uploads/products/{name}")
    public ResponseEntity<Resource> image(@PathVariable String name) throws IOException {
        if (!name.matches("[0-9a-fA-F-]{36}\\.(jpg|png)")) {
            return ResponseEntity.notFound().build();
        }
        Path path = imageDirectory.resolve(name).normalize();
        if (!path.startsWith(imageDirectory) || !Files.isRegularFile(path)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new UrlResource(path.toUri());
        MediaType type = name.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(type).body(resource);
    }
}
