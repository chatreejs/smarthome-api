package dev.chatree.smarthomeapi.controller;

import dev.chatree.smarthomeapi.exception.BusinessException;
import dev.chatree.smarthomeapi.model.inventory.InventoryRequest;
import dev.chatree.smarthomeapi.model.inventory.InventoryResponse;
import dev.chatree.smarthomeapi.service.InventoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static dev.chatree.smarthomeapi.constant.MessageConstants.LOG_USER_REQUEST_PATTERN;

@Log4j2
@RestController
@RequestMapping("/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventory(
            @RequestParam("homeId") Long homeId,
            Authentication auth,
            HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var subject = auth.getName();
        var inventoryResponseList = inventoryService.getAllInventory(homeId, subject);
        return ResponseEntity.ok(inventoryResponseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> getInventoryById(@PathVariable Long id,
                                                              @RequestParam("homeId") Long homeId,
                                                              Authentication auth,
                                                              HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var subject = auth.getName();
        var inventoryResponse = inventoryService.getInventoryById(id, homeId, subject);
        return ResponseEntity.ok(inventoryResponse);
    }

    @PostMapping
    public ResponseEntity<Objects> createInventory(@RequestParam("homeId") Long homeId,
                                                   @RequestBody InventoryRequest inventory,
                                                   Authentication auth,
                                                   HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var subject = auth.getName();
        inventoryService.createInventory(inventory, homeId, subject);
        return ResponseEntity.created(null).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Objects> updateInventory(@PathVariable Long id,
                                                   @RequestParam("homeId") Long homeId,
                                                   @RequestBody InventoryRequest inventory,
                                                   Authentication auth,
                                                   HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var subject = auth.getName();
        inventoryService.updateInventory(id, inventory, homeId, subject);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Objects> deleteInventory(@PathVariable Long id,
                                                   @RequestParam("homeId") Long homeId,
                                                   Authentication auth,
                                                   HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var subject = auth.getName();
        inventoryService.deleteInventory(id, homeId, subject);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Objects> deleteInventory(@RequestParam("homeId") Long homeId,
                                                   String ids,
                                                   Authentication auth,
                                                   HttpServletRequest request) throws BusinessException {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        if (ids.isBlank()) {
            throw new BusinessException("ids must not be blank");
        }

        var idStringList = List.of(ids.split(","));
        var subject = auth.getName();
        var idList = idStringList.stream().map(Long::parseLong).toList();
        inventoryService.deleteMultipleInventory(idList, homeId, subject);
        return ResponseEntity.noContent().build();
    }
}
