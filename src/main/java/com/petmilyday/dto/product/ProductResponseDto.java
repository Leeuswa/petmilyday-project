package com.petmilyday.dto.product;

import com.petmilyday.entity.product.Product;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponseDto {
    private Long id;
    private String name;
    private Integer price;
    private String imgUrl;

    private String category;
    private Integer stock;
    private String description;
    private String petSpecies;
    private String material;
    private String sizeInfo;
    private String origin;

    public ProductResponseDto(Product entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.price = entity.getPrice();
        this.imgUrl = entity.getImgUrl();

        this.category = entity.getCategory();
        this.stock = entity.getStock();
        this.description = entity.getDescription();
        this.petSpecies = entity.getPetSpecies();
        this.material = entity.getMaterial();
        this.sizeInfo = entity.getSizeInfo();
        this.origin = entity.getOrigin();
    }
}