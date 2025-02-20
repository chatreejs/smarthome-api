package dev.chatree.smarthomeapi.controller;

import dev.chatree.smarthomeapi.exception.BusinessException;
import dev.chatree.smarthomeapi.model.warranty.WarrantyRequest;
import dev.chatree.smarthomeapi.model.warranty.WarrantyResponse;
import dev.chatree.smarthomeapi.service.WarrantyService;
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
@RequestMapping("/warranties")
@RequiredArgsConstructor
public class WarrantyController {

    private final WarrantyService warrantyService;

    @GetMapping
    public ResponseEntity<List<WarrantyResponse>> getAllWarranty(Authentication auth,
                                                                 HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        return ResponseEntity.ok(warrantyService.getAllWarranty());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarrantyResponse> getWarrantyById(@PathVariable Long id,
                                                            Authentication auth,
                                                            HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var warrantyResponse = warrantyService.getWarrantyById(id);
        return ResponseEntity.ok(warrantyResponse);
    }

    @PostMapping
    public ResponseEntity<Objects> createWarranty(@RequestBody WarrantyRequest warranty,
                                                  Authentication auth,
                                                  HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        warrantyService.createWarranty(warranty);
        return ResponseEntity.created(null).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Objects> updateWarranty(@PathVariable Long id,
                                                  @RequestBody WarrantyRequest warranty,
                                                  Authentication auth,
                                                  HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        warrantyService.updateWarranty(id, warranty);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Objects> deleteWarranty(@PathVariable Long id,
                                                  Authentication auth,
                                                  HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        warrantyService.deleteWarranty(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Objects> deleteWarranty(@RequestParam String ids,
                                                  HttpServletRequest request,
                                                  Authentication auth) throws BusinessException {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        if (ids.isBlank()) {
            log.info("Error: ids must not be blank");
            throw new BusinessException("ids must not be blank");
        }

        var idStringList = List.of(ids.split(","));
        try {
            var idList = idStringList.stream().map(Long::parseLong).toList();
            warrantyService.deleteMultipleWarranty(idList);
            return ResponseEntity.noContent().build();
        } catch (NumberFormatException e) {
            throw new BusinessException("ids must be a number");
        }
    }
}
