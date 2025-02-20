package dev.chatree.smarthomeapi.controller;

import dev.chatree.smarthomeapi.exception.BusinessException;
import dev.chatree.smarthomeapi.model.food.FoodRequest;
import dev.chatree.smarthomeapi.model.food.FoodResponse;
import dev.chatree.smarthomeapi.service.FoodService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Objects;

import static dev.chatree.smarthomeapi.constant.MessageConstants.LOG_REQUEST_PATTERN;
import static dev.chatree.smarthomeapi.constant.MessageConstants.LOG_USER_REQUEST_PATTERN;

@Log4j2
@RestController
@RequestMapping("/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @GetMapping
    public ResponseEntity<List<FoodResponse>> getAllFood(@RequestParam("homeId") Long homeId,
                                                         Authentication auth,
                                                         HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var subject = auth.getName();
        var foodResponseList = foodService.getAllFood(homeId, subject);
        return ResponseEntity.ok(foodResponseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getFoodById(@RequestParam("homeId") Long homeId,
                                                    @PathVariable Long id,
                                                    Authentication auth,
                                                    HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var subject = auth.getName();
        var foodResponse = foodService.getFoodById(id, homeId, subject);
        return ResponseEntity.ok(foodResponse);

    }

    @PostMapping
    public ResponseEntity<Objects> createFood(@RequestParam("homeId") Long homeId,
                                              @RequestBody FoodRequest foodRequest,
                                              Authentication auth,
                                              HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());

        var subject = auth.getName();
        foodService.createFood(foodRequest, homeId, subject);
        return ResponseEntity.created(null).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Objects> updateFood(@PathVariable Long id,
                                              @RequestParam("homeId") Long homeId,
                                              @RequestBody FoodRequest foodRequest,
                                              Authentication auth,
                                              HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var subject = auth.getName();
        foodService.updateFood(id, foodRequest, homeId, subject);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Objects> deleteFood(@PathVariable Long id,
                                              @RequestParam("homeId") Long homeId,
                                              Authentication auth,
                                              HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());

        var subject = auth.getName();
        foodService.deleteFood(id, homeId, subject);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Objects> deleteMultipleFood(@RequestParam("homeId") Long homeId,
                                                      Authentication auth,
                                                      String ids,
                                                      HttpServletRequest request) throws BusinessException {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        if (ids.isBlank()) {
            throw new BusinessException("ids must not be blank");
        }
        var idStringList = List.of(ids.split(","));
        var subject = auth.getName();
        var idList = idStringList.stream().map(Long::parseLong).toList();
        foodService.deleteMultipleFood(idList, homeId, subject);
        return ResponseEntity.noContent().build();

    }
}
