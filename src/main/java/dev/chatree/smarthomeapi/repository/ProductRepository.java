package dev.chatree.smarthomeapi.repository;

import dev.chatree.smarthomeapi.entity.ProductEntity;
import dev.chatree.smarthomeapi.model.product.ProductType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    /**
     * Finds products of a type whose name contains the pattern (case-insensitive) or whose barcode starts with it.
     * Exact barcode matches come first, then names that start with the pattern, then the rest by name.
     *
     * @param query    the raw search text, already trimmed
     * @param pattern  the search text, lower-cased, with LIKE wildcards escaped by {@code !}
     */
    @Query("""
            SELECT p FROM ProductEntity p
            WHERE p.type = :type
              AND (LOWER(p.name) LIKE CONCAT('%', :pattern, '%') ESCAPE '!'
                   OR p.barcode LIKE CONCAT(:barcodePattern, '%') ESCAPE '!')
            ORDER BY
              CASE WHEN p.barcode = :query THEN 0
                   WHEN LOWER(p.name) LIKE CONCAT(:pattern, '%') ESCAPE '!' THEN 1
                   ELSE 2 END,
              p.name ASC
            """)
    List<ProductEntity> search(ProductType type, String query, String pattern, String barcodePattern, Pageable pageable);
}
