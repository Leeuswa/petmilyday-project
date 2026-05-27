package com.petmilyday.controller.shop;

import com.petmilyday.dto.product.ProductResponseDto;
import com.petmilyday.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class ShopController {
    private final ProductService productService;
    @GetMapping("/shop")
    public String showShopPage(@RequestParam(required = false) String category, Model model) {
        List<ProductResponseDto> products;

        if (category == null || category.isEmpty() || category.equals("ALL")) {
            products = productService.getAllProducts();
        } else {
            products = productService.getProductsByCategory(category);
        }

        model.addAttribute("productList", products);
        model.addAttribute("activeTab","shop");

        return "shop";
    }
    @GetMapping("/shop/detail/{id}")
    public String showProductDetail(@PathVariable("id") Long id, Model model) {
        // 1. 서비스에서 DTO로 깔끔하게 받아옴
        ProductResponseDto product = productService.getProductById(id);

        // 2. 모델에 "product"라는 이름으로 담아줌
        model.addAttribute("product", product);

        // 3. templates/shop/detail.html 화면을 띄움
        return "shop/detail";
    }


}
