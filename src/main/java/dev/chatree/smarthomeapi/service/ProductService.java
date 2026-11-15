package dev.chatree.smarthomeapi.service;

import dev.chatree.smarthomeapi.entity.ProductEntity;
import dev.chatree.smarthomeapi.model.product.ProductResponse;
import dev.chatree.smarthomeapi.model.product.ProductType;
import dev.chatree.smarthomeapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProductService {
    static final int MAX_RESULTS = 20;

    private final ProductRepository productRepository;

    /**
     * Searches the catalog by name (contains, case-insensitive) or barcode (prefix).
     * Returns an empty list for a blank query.
     */
    public List<ProductResponse> searchProducts(ProductType type, String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        var trimmed = query.trim();
        var escaped = escapeLike(trimmed);
        return productRepository.search(type, trimmed, escaped.toLowerCase(Locale.ROOT), escaped, PageRequest.of(0, MAX_RESULTS))
                .stream()
                .map(ProductService::toResponse)
                .toList();
    }

    static String escapeLike(String value) {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    private static ProductResponse toResponse(ProductEntity entity) {
        return ProductResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .brand(entity.getBrand())
                .barcode(entity.getBarcode())
                .unit(entity.getUnit())
                .maxQuantity(entity.getMaxQuantity())
                .type(entity.getType())
                .build();
    }
}
