package dev.chatree.smarthomeapi.repository;

import dev.chatree.smarthomeapi.entity.ProductEntity;
import dev.chatree.smarthomeapi.model.product.ProductResponse;
import dev.chatree.smarthomeapi.model.product.ProductType;
import dev.chatree.smarthomeapi.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the product catalog search against an in-memory H2 database.
 */
@DataJpaTest
@Import(ProductService.class)
class ProductSearchRepositoryTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private ProductService productService;

    @BeforeEach
    void seed() {
        product("Fresh Milk", "Meiji", "8851234567890", ProductType.FOOD);
        product("Chocolate Milk", "Foremost", "8850000000011", ProductType.FOOD);
        product("Milk Powder", "Dumex", "8851234500000", ProductType.FOOD);
        product("Dish Soap 100%", "Sunlight", "8859999999999", ProductType.INVENTORY);
        product("Milk Frother", "Generic", "1111111111111", ProductType.INVENTORY);
        em.flush();
        em.clear();
    }

    @Test
    void nameSearchIsCaseInsensitiveAndFilteredByType() {
        var result = productService.searchProducts(ProductType.FOOD, "  MILK ");

        assertThat(result).extracting(ProductResponse::getName)
                // Names that start with the query come first. Each group is sorted by name.
                .containsExactly("Milk Powder", "Chocolate Milk", "Fresh Milk");
    }

    @Test
    void barcodeMatchesByPrefixWithExactMatchFirst() {
        var result = productService.searchProducts(ProductType.FOOD, "8851234567890");
        assertThat(result).extracting(ProductResponse::getName).containsExactly("Fresh Milk");

        var prefix = productService.searchProducts(ProductType.FOOD, "88512345");
        assertThat(prefix).extracting(ProductResponse::getName).containsExactlyInAnyOrder("Fresh Milk", "Milk Powder");
    }

    @Test
    void likeWildcardsInTheQueryAreLiteral() {
        assertThat(productService.searchProducts(ProductType.INVENTORY, "%")).extracting(ProductResponse::getName)
                .containsExactly("Dish Soap 100%");
        assertThat(productService.searchProducts(ProductType.FOOD, "_")).isEmpty();
    }

    @Test
    void blankQueryReturnsNothing() {
        assertThat(productService.searchProducts(ProductType.FOOD, "")).isEmpty();
        assertThat(productService.searchProducts(ProductType.FOOD, "   ")).isEmpty();
        assertThat(productService.searchProducts(ProductType.FOOD, null)).isEmpty();
    }

    @Test
    void resultsAreCapped() {
        for (int i = 0; i < 30; i++) {
            product("Bulk item " + i, null, null, ProductType.INVENTORY);
        }
        em.flush();

        assertThat(productService.searchProducts(ProductType.INVENTORY, "bulk")).hasSize(20);
    }

    private void product(String name, String brand, String barcode, ProductType type) {
        var p = new ProductEntity();
        p.setName(name);
        p.setBrand(brand);
        p.setBarcode(barcode);
        p.setType(type);
        em.persist(p);
    }
}
