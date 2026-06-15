package com.petmilyday.controller.shop;

import com.petmilyday.config.jwt.JwtTokenProvider;
import com.petmilyday.dto.product.ProductResponseDto;
import com.petmilyday.dto.shop.SubscriptionResponseDto;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.member.PetProfile;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.member.PetProfileRepository;
import com.petmilyday.repository.shop.OrdersRepository;
import com.petmilyday.service.product.ProductService;
import com.petmilyday.service.shop.SubscriptionService;
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
    private final SubscriptionService subscriptionService;
    private final JwtTokenProvider jwtTokenProvider;
    private final OrdersRepository ordersRepository;
    private final PetProfileRepository petProfileRepository;
    private final MemberRepository memberRepository;

    // 쇼핑몰 메인 페이지 및 카테고리별 상품/구독/반려동물 프로필 연동
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
            String username = principal.getName();
            model.addAttribute("loggedInUser", username);

            List<SubscriptionResponseDto> subList = subscriptionService.getActiveSubscriptions(username);
            model.addAttribute("subscriptionList", subList);

            Member member = memberRepository.findByUsername(username).orElse(null);
            if (member != null) {
                List<PetProfile> petList = petProfileRepository.findByMember(member);
                model.addAttribute("petList", petList);
            } else {
                model.addAttribute("petList", List.of());
            }
        } else {
            model.addAttribute("petList", List.of());
        }

        return "shop/shop";
    }

    // 상품 상세 정보 조회 및 구매 여부 검증
    @GetMapping("/shop/detail/{id}")
    public String showProductDetail(@PathVariable("id") Long id,
                                    Model model,
                                    Principal principal) {

        ProductResponseDto product = productService.getProductById(id);
        model.addAttribute("product", product);

        boolean isBuyer = false;

        if (principal != null) {
            model.addAttribute("loggedInUser", principal.getName());
            isBuyer = ordersRepository.existsByUsernameAndProductId(principal.getName(), id);
        }

        model.addAttribute("isBuyer", isBuyer);

        return "shop/detail";
    }

    // 회원의 활성화된 정기구독 목록 관리 페이지
    @GetMapping("/shop/subscription")
    public String showSubscriptionManagementPage(Model model, Principal principal) {

        if (principal == null) {
            model.addAttribute("loggedInUser", null);
            model.addAttribute("activeTab", "shop");
            model.addAttribute("subscriptionList", List.of());
        }
        else {
            model.addAttribute("loggedInUser", principal.getName());
            model.addAttribute("activeTab", "shop");

            List<SubscriptionResponseDto> subList = subscriptionService.getActiveSubscriptions(principal.getName());
            model.addAttribute("subscriptionList", subList);
        }

        return "shop/subscription_manage";
    }

    // 선택한 상품 및 주기 기준 정기구독 결제 주문서 페이지
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