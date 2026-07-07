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

@Controller
@RequestMapping("/admin/shop")
@RequiredArgsConstructor
public class AdminShopController {

    private final ProductService productService;

    @GetMapping("/list")
    public String adminShopList(@RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "sort", defaultValue = "idDesc") String sort,
                                Model model) {

        org.springframework.data.domain.Page<Product> productList = productService.getAdminProductPage(page, 10, sort);

        model.addAttribute("productList", productList);
        model.addAttribute("sort", sort);

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
    public String registerProduct(@ModelAttribute ProductRequestDto dto,
                                  @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                  @RequestParam(value = "descImgFile", required = false) MultipartFile descFile) throws IOException {
        productService.registerProduct(dto, imageFile, descFile);
        return "redirect:/admin/shop/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);

        return "shop/product_edit";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable("id") Long id,
                                @ModelAttribute ProductRequestDto dto,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                @RequestParam(value = "descImgFile", required = false) MultipartFile descFile) throws IOException {

        productService.updateProduct(id, dto, imageFile, descFile);

        return "redirect:/admin/shop/list";
    }
}