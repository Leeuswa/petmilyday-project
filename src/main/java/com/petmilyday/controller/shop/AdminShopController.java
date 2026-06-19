package com.petmilyday.controller.shop;

import com.petmilyday.dto.product.ProductRequestDto;
import com.petmilyday.entity.product.Product;
import com.petmilyday.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    // 상품 삭제 (Soft Delete)
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id) {
        productService.softDelete(id);
        return "redirect:/admin/shop/list";
    }

    // 상품 등록 폼 이동
    @GetMapping("/new")
    public String showRegisterForm(Model model) {
        model.addAttribute("productRequestDto", new ProductRequestDto());
        return "shop/admin_product_register";
    }

    // 상품 등록 처리
    @PostMapping("/new")
    public String registerProduct(@ModelAttribute ProductRequestDto dto) throws IOException {
        productService.registerProduct(dto);
        return "redirect:/admin/shop/list";
    }

    // 상품 수정 폼 이동 (기존 데이터 조회 및 폼 세팅)
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.findById(id); // 기존 상품 정보 가져오기
        model.addAttribute("product", product);        // HTML에 "product" 이름으로 전달

        return "shop/product_edit";
    }

    // 상품 수정 처리 (DB 반영 후 리스트 리다이렉트)
    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable("id") Long id,
                                @ModelAttribute ProductRequestDto dto,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {

        productService.updateProduct(id, dto, imageFile);

        return "redirect:/admin/shop/list";
    }
}