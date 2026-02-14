package com.shopping.main.domain.cart.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import com.shopping.main.domain.cart.dto.CartDetailDto;
import com.shopping.main.domain.cart.dto.CartItemDto;
import com.shopping.main.domain.cart.dto.CartOrderDto;
import com.shopping.main.domain.cart.entity.Cart;
import com.shopping.main.domain.cart.entity.CartItem;
import com.shopping.main.domain.cart.repository.CartItemRepository;
import com.shopping.main.domain.cart.repository.CartRepository;
import com.shopping.main.domain.order.service.OrderService;
import com.shopping.main.domain.product.entity.Product;
import com.shopping.main.domain.product.exception.OutOfStockException;
import com.shopping.main.domain.product.repository.ProductRepository;
import com.shopping.main.domain.user.entity.SiteUser;
import com.shopping.main.domain.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    public Long addCart(CartItemDto cartItemDto, String email) {

        // 1. 상품 조회
        Product product = productRepository.findById(cartItemDto.getItemId()).orElseThrow(EntityNotFoundException::new);

        // 2. 로그인한 회원 엔티티 조회
        SiteUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        // 3. 현재 회원의 장바구니 조회
        Cart cart = cartRepository.findByUserId(user.getId());

        // 4. 장바구니가 없다면 처음 생성
        if (cart == null) {
            cart = Cart.createCart(user);
            cartRepository.save(cart);
        }

        // 5. 이미 장바구니에 있는 상품인지 조회
        CartItem savedCartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        // 재고 확인
        int currentCount = (savedCartItem == null ? 0 : savedCartItem.getCount());
        int nextCount = currentCount + cartItemDto.getCount();

        validateCartQuantity(product, nextCount);

        if (savedCartItem != null) {
            // 6.A 이미 있으면 수량만 증가
            savedCartItem.addCount(cartItemDto.getCount());
            return savedCartItem.getId();
        } else {
            // 6.B 없으면 새로운 장바구니 상품 생성 후 저장
            CartItem cartItem = CartItem.createCartItem(cart, product, cartItemDto.getCount());
            cartItemRepository.save(cartItem);
            return cartItem.getId();
        }
    }

    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartList(String email) {
        List<CartDetailDto> cartDetailDtoList = new ArrayList<>();

        // 1. 현재 로그인한 회원의 정보 조회
        SiteUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다"));

        // 2. 회원의 장바구니 조회
        Cart cart = cartRepository.findByUserId(user.getId());

        // 3. 장바구니가 없으면 빈 리스트 반환
        if (cart == null) {
            return cartDetailDtoList;
        }

        cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId());

        return cartDetailDtoList;

    }

    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, String email) {

        // 1. 현재 로그인한 사용자
        SiteUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다"));

        // 2. 장바구니 상품
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);

        // 3. 장바구니 주인
        SiteUser savedUser = cartItem.getCart().getUser();

        return StringUtils.equals(user.getEmail(), savedUser.getEmail());
    }

    public void updateCartItemCount(Long cartItemId, int count) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);
        Product product = cartItem.getProduct();

        validateCartQuantity(product, count);
        cartItem.updateCount(count);
    }

    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);

        cartItemRepository.delete(cartItem);
    }

    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email) {

        // 주문 로직 실행
        Long orderId = orderService.orders(cartOrderDtoList, email);

        return orderId;
    }

    private void validateCartQuantity(Product product, int count) {
        if (product.getStockQuantity() < count || count <= 0) {
            throw new OutOfStockException("상품의 재고가 부족합니다. (현재 재고 수량: " + product.getStockQuantity() + ")");
        }
    }

}
