package dev.chatree.smarthomeapi.controller;

import dev.chatree.smarthomeapi.model.Food;
import dev.chatree.smarthomeapi.entity.FoodEntity;
import dev.chatree.smarthomeapi.service.FoodService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/foods")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping
    public ResponseEntity<List<FoodEntity>> getAllFood(HttpServletRequest request) {
        log.info("API {}: {}", request.getMethod(), request.getServletPath());
        return ResponseEntity.ok(foodService.getAllFood());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getFoodById(@PathVariable Long id, HttpServletRequest request) {
        log.info("API {}: {}", request.getMethod(), request.getServletPath());
        FoodEntity food = foodService.getFoodById(id);
        if (food == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(food);
    }

    @PostMapping
    public ResponseEntity<Object> createFood(@RequestBody Food request) {
        foodService.createFood(request);
        return ResponseEntity.created(null).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateFood(@PathVariable Long id, @RequestBody Food request) {
        FoodEntity food = foodService.getFoodById(id);
        if (food == null) {
            return ResponseEntity.notFound().build();
        }
        foodService.updateFood(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteFood(@PathVariable Long id) {
        FoodEntity food = foodService.getFoodById(id);
        if (food == null) {
            return ResponseEntity.notFound().build();
        }
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Object> deleteMultipleFood(String ids) {
        String[] idList = ids.split(",");
        for (String id : idList) {
            FoodEntity food = foodService.getFoodById(Long.parseLong(id));
            if (food == null) {
                return ResponseEntity.notFound().build();
            }
            foodService.deleteFood(Long.parseLong(id));
        }
        return ResponseEntity.noContent().build();
    }
}