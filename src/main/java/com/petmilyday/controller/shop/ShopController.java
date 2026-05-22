package com.petmilyday.controller.shop;

import com.petmilyday.dto.product.ProductResponseDto;
import com.petmilyday.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class ShopController {


    private final ProductService productService;

    @GetMapping("/shop")
    public String showShopPage(Model model) {
        List<ProductResponseDto> products = productService.getAllProducts();

        model.addAttribute("productList", products);
    return "shop";
    }


}
