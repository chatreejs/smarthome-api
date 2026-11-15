package dev.chatree.smarthomeapi.controller;

import dev.chatree.smarthomeapi.model.product.ProductResponse;
import dev.chatree.smarthomeapi.model.product.ProductType;
import dev.chatree.smarthomeapi.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static dev.chatree.smarthomeapi.constant.MessageConstants.LOG_USER_REQUEST_PATTERN;

/**
 * Searches the product catalog. Any signed-in user can call it, and no endpoint here changes the catalog.
 */
@Log4j2
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam ProductType type,
                                                                @RequestParam(required = false) String q,
                                                                Authentication auth,
                                                                HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        return ResponseEntity.ok(productService.searchProducts(type, q));
    }
}
