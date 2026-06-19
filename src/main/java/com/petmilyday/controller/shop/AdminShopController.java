package com.petmilyday.controller.shop;

import com.petmilyday.dto.product.ProductRequestDto;
import com.petmilyday.entity.product.Product;
import com.petmilyday.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/shop")
@RequiredArgsConstructor
public class AdminShopController {

    private final ProductService productService;

    // 관리자 상품 목록 조회
    @GetMapping("/list")
    public String adminShopList(Model model) {
        List<Product> productList = productService.findAll();
        model.addAttribute("productList", productList);

        return "shop/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id) {
        productService.softDelete(id);
        return "redirect:/admin/shop/list";
    }

    @GetMapping("/new")
    public String showRegisterForm(Model model) {
        model.addAttribute("productRequestDto", new ProductRequestDto());
        return "shop/admin_product_register";
    }

    @PostMapping("/new")
    public String registerProduct(@ModelAttribute ProductRequestDto dto) throws IOException {
        productService.registerProduct(dto);
        return "redirect:/admin/shop/list";
    }

}