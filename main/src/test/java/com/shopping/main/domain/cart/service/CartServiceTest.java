package com.shopping.main.domain.cart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.shopping.main.domain.cart.dto.CartItemDto;
import com.shopping.main.domain.cart.entity.CartItem;
import com.shopping.main.domain.cart.repository.CartItemRepository;
import com.shopping.main.domain.product.constant.ProductSellStatus;
import com.shopping.main.domain.product.entity.Product;
import com.shopping.main.domain.product.repository.ProductRepository;
import com.shopping.main.domain.user.constant.UserRole;
import com.shopping.main.domain.user.entity.SiteUser;
import com.shopping.main.domain.user.repository.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
public class CartServiceTest {
    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    SiteUser user, otherUser;
    Product product;

    @BeforeEach
    void setUp() {
        user = SiteUser.builder()
                .email("cart@test.com")
                .nickname("carttest")
                .password("testPassowrd")
                .role(UserRole.USER)
                .provider("local")
                .providerId(null)
                .build();
        userRepository.save(user);

        otherUser = SiteUser.builder()
                .email("cart2@test.com")
                .nickname("carttest2")
                .password("testPassowrd")
                .role(UserRole.USER)
                .provider("local")
                .providerId(null)
                .build();

        userRepository.save(otherUser);

        product = new Product();
        product.setName("장바구니 테스트 상품");
        product.setDescription("테스트 상품입니다");
        product.setPrice(10000);
        product.setStockQuantity(10);
        product.setProductSellStatus(ProductSellStatus.SELL);

        productRepository.save(product);
    }

    @Test
    @DisplayName("같은 상품을 장바구니에 다시 담으면 수량이 증가한다")
    void addCart_sameProduct_increaseCount() {

        Long firstCartItemId = cartService.addCart(creatCartItemDto(product.getId(), 2), user.getEmail());
        Long secondCartItemId = cartService.addCart(creatCartItemDto(product.getId(), 3), user.getEmail());

        CartItem cartItem = cartItemRepository.findById(firstCartItemId).orElseThrow();

        assertEquals(firstCartItemId, secondCartItemId);
        assertEquals(cartItem.getCount(), 5);
    }

    @Test
    @DisplayName("장바구니 수량 수정 후 삭제가 된다")
    void updateAndDeleteCartItem() {
        Long cartItemId = cartService.addCart(creatCartItemDto(product.getId(), 1), user.getEmail());

        cartService.updateCartItemCount(cartItemId, 3);
        CartItem updated = cartItemRepository.findById(cartItemId).orElseThrow();

        assertEquals(updated.getCount(), 3);

        cartService.deleteCartItem(cartItemId);

        assertFalse(cartItemRepository.findById(cartItemId).isPresent());
    }

    @Test
    @DisplayName("장바구니 권한 검증: 본인 true, 타인 false")
    void validateCartItem_ownerTrue_otherFalse() {
        Long cartItemId = cartService.addCart(creatCartItemDto(product.getId(), 1), user.getEmail());

        boolean owenerResult = cartService.validateCartItem(cartItemId, user.getEmail());
        boolean otherResult = cartService.validateCartItem(cartItemId, otherUser.getEmail());

        assertTrue(owenerResult);
        assertFalse(otherResult);

    }

    private CartItemDto creatCartItemDto(Long itemId, int count) {
        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setItemId(itemId);
        cartItemDto.setCount(count);

        return cartItemDto;
    }
}
