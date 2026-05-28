package com.petmilyday.controller.shop;

import com.petmilyday.dto.product.ProductResponseDto;
import com.petmilyday.dto.shop.SubscriptionResponseDto; // [★추가] DTO 임포트
import com.petmilyday.service.product.ProductService;
import com.petmilyday.service.shop.SubscriptionService;   // [★추가] 서비스 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ShopController {

    private final ProductService productService;
    private final SubscriptionService subscriptionService; // [★추가] 구독 서비스 주입

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

        // 로그인 한 상태라면 실제 구독 목록을 DB에서 긁어와서 모델에 담는다!
        if (principal != null) {
            model.addAttribute("loggedInUser", principal.getName());

            // [★추가] 실제 로그인한 유저의 이메일(ID)로 ACTIVE 상태인 구독 리스트 조회
            List<SubscriptionResponseDto> subList = subscriptionService.getActiveSubscriptions(principal.getName());
            model.addAttribute("subscriptionList", subList);
        }

        return "shop";
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
    // ShopController.java 내부나 하단에 추가

    @GetMapping("/shop/subscription")
    public String showSubscriptionManagementPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/member/login";
        }

        // [★수정] 다른 화면들과 똑같이 세션 로그인 ID(username)를 모델에 확실히 꽂아줌!
        model.addAttribute("loggedInUser", principal.getName());
        model.addAttribute("activeTab", "shop");

        // DB에서 시큐리티 username 기반으로 ACTIVE 상태인 정기구독 리스트를 완벽히 긁어옴
        List<SubscriptionResponseDto> subList = subscriptionService.getActiveSubscriptions(principal.getName());
        model.addAttribute("subscriptionList", subList);

        return "shop/subscription_manage";
    }
}