package com.petmilyday.controller.shop;

import com.petmilyday.config.jwt.JwtTokenProvider;
import com.petmilyday.dto.product.ProductResponseDto;
import com.petmilyday.dto.shop.SubscriptionResponseDto;
import com.petmilyday.service.product.ProductService;
import com.petmilyday.service.shop.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ShopController {

    private final ProductService productService;
    private final SubscriptionService subscriptionService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/shop")
    public String showShopPage(@RequestParam(required = false) String category,
                               Model model,
                               Principal principal) {

        List<ProductResponseDto> products;

        if (category == null || category.isEmpty() || category.equals("ALL")) {
            products = productService.getAllProducts();
        } else {
            products = productService.getProductsByCategory(category);
        }

        model.addAttribute("productList", products);
        model.addAttribute("activeTab", "shop");

        if (principal != null) {
            model.addAttribute("loggedInUser", principal.getName());
            List<SubscriptionResponseDto> subList = subscriptionService.getActiveSubscriptions(principal.getName());
            model.addAttribute("subscriptionList", subList);
        }

        return "shop/shop";
    }

    @GetMapping("/shop/detail/{id}")
    public String showProductDetail(@PathVariable("id") Long id,
                                    Model model,
                                    Principal principal) {

        ProductResponseDto product = productService.getProductById(id);
        model.addAttribute("product", product);

        if (principal != null) {
            model.addAttribute("loggedInUser", principal.getName());
        }

        return "shop/detail";
    }

    @GetMapping("/shop/subscription")
    public String showSubscriptionManagementPage(Model model, Principal principal) {

        if (principal == null) {
            model.addAttribute("loggedInUser", null);
            model.addAttribute("activeTab", "shop");
            model.addAttribute("subscriptionList", List.of()); // 빈 리스트 넘겨서 에러 방지
        }
        else {
            model.addAttribute("loggedInUser", principal.getName());
            model.addAttribute("activeTab", "shop");

            List<SubscriptionResponseDto> subList = subscriptionService.getActiveSubscriptions(principal.getName());
            model.addAttribute("subscriptionList", subList);
        }

        return "shop/subscription_manage";
    }

    @GetMapping("/shop/subscription-checkout")
    public String subscriptionCheckoutPage(@RequestParam("productId") Long productId,
                                           @RequestParam("quantity") int quantity,
                                           @RequestParam("cycleDays") int cycleDays,
                                           @RequestParam("name") String name,
                                           @RequestParam("price") int price,
                                           Model model) {
        int totalPrice = price * quantity;

        model.addAttribute("productId", productId);
        model.addAttribute("quantity", quantity);
        model.addAttribute("cycleDays", cycleDays);
        model.addAttribute("name", name);
        model.addAttribute("price", price);
        model.addAttribute("totalPrice", totalPrice);

        return "shop/subscription_checkout";
    }
}