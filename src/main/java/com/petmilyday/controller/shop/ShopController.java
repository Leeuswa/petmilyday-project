package com.petmilyday.controller.shop;

import com.petmilyday.dto.product.ProductResponseDto;
import com.petmilyday.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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


}
