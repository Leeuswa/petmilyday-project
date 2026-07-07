package com.petmilyday.dto.product;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductRequestDto {
    private String name;
    private Integer price;
    private Integer stock;
    private String category;
    private String description;
    private MultipartFile imageFile;
    private String petSpecies;
    private String material;
    private String sizeInfo;
    private String origin;
}