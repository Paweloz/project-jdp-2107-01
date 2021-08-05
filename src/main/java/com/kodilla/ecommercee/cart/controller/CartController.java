package com.kodilla.ecommercee.cart.controller;

import com.kodilla.ecommercee.cart.domain.Cart;
import com.kodilla.ecommercee.cart.domain.CartDto;
import com.kodilla.ecommercee.cart.mapper.CartMapper;
import com.kodilla.ecommercee.cart.service.CartDbService;
import com.kodilla.ecommercee.order.domain.Order;
import com.kodilla.ecommercee.product.domain.Product;
import com.kodilla.ecommercee.product.domain.ProductDto;
import com.kodilla.ecommercee.order.domain.OrderDto;
import com.kodilla.ecommercee.product.mapper.ProductMapper;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/v1/cart")
@RestController
@Data
public class CartController {
    private final CartDbService cartDbService;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    @GetMapping(path = "getProducts")
    public List<ProductDto> getProducts(@RequestParam Long cartId) {
        Cart cart = getCart(cartId);
        return productMapper.mapToProductDtoList(cart.getProducts());
    }

    @PostMapping(path = "createCart")
    public void createCart(@RequestBody CartDto cartDto) {
        cartDbService.createCart(cartMapper.mapDtoToCart(cartDto));
    }

    @PostMapping(path = "addProducts")
    public void addProducts(@RequestParam List<Long> productsIds, @RequestParam Long cartId) {
        Cart cart = getCart(cartId);
        cartDbService.addProducts(productsIds, cart);
    }

    @DeleteMapping(path = "deleteProduct")
    public void deleteProduct(@RequestParam Long productId, @RequestParam Long cartId) throws Exception {
        Cart cart = getCart(cartId);
        Product product = cartDbService.getProductToDelete(productId)
                .orElseThrow(() -> new RuntimeException("Product of id'" + productId + "' not found"));

        if (!cart.getProducts().remove(product)) {
            throw new Exception("Product of id '" + productId +
                    "' not found in cart of id '" + cartId + "'");
        }
    }

    @PostMapping(path = "createOrder")
    public OrderDto createOrder(@RequestParam Long cartId) {
        Cart cart = getCart(cartId);
        Order order = new Order(LocalDate.now(), BigDecimal.valueOf(cart.getPrice().doubleValue()), cart.getUser());
        cartDbService.createOrder(order);
        return new OrderDto(order.getId(), LocalDate.of(2021, 7, 15), BigDecimal.valueOf(cart.getPrice().doubleValue()), null, order.getUser().getId());
    }

    private Cart getCart(Long cartId) {
        return cartDbService.getCart(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart of id '" + cartId + "' not found."));
    }
}
