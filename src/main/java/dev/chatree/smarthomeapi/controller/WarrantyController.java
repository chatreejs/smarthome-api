package dev.chatree.smarthomeapi.controller;

import dev.chatree.smarthomeapi.exception.BusinessException;
import dev.chatree.smarthomeapi.model.warranty.WarrantyRequest;
import dev.chatree.smarthomeapi.model.warranty.WarrantyResponse;
import dev.chatree.smarthomeapi.security.home.RequireHomeAccess;
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
@RequestMapping("/homes/{homeId}/warranties")
@RequireHomeAccess
@RequiredArgsConstructor
public class WarrantyController {

    private final WarrantyService warrantyService;

    @GetMapping
    public ResponseEntity<List<WarrantyResponse>> getAllWarranty(@PathVariable Long homeId,
                                                                 Authentication auth,
                                                                 HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var warrantyResponseList = warrantyService.getAllWarranty(homeId);
        return ResponseEntity.ok(warrantyResponseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarrantyResponse> getWarrantyById(@PathVariable Long homeId,
                                                            @PathVariable Long id,
                                                            Authentication auth,
                                                            HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var warrantyResponse = warrantyService.getWarrantyById(id, homeId);
        return ResponseEntity.ok(warrantyResponse);
    }

    @PostMapping
    public ResponseEntity<Objects> createWarranty(@PathVariable Long homeId,
                                                  @RequestBody WarrantyRequest warranty,
                                                  Authentication auth,
                                                  HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        warrantyService.createWarranty(warranty, homeId, auth.getName());
        return ResponseEntity.created(null).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Objects> updateWarranty(@PathVariable Long id,
                                                  @PathVariable Long homeId,
                                                  @RequestBody WarrantyRequest warranty,
                                                  Authentication auth,
                                                  HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        warrantyService.updateWarranty(id, warranty, homeId, auth.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Objects> deleteWarranty(@PathVariable Long id,
                                                  @PathVariable Long homeId,
                                                  Authentication auth,
                                                  HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        warrantyService.deleteWarranty(id, homeId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Objects> deleteWarranty(@PathVariable Long homeId,
                                                  String ids,
                                                  Authentication auth,
                                                  HttpServletRequest request) throws BusinessException {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        if (ids.isBlank()) {
            throw new BusinessException("ids must not be blank");
        }

        var idStringList = List.of(ids.split(","));
        var idList = idStringList.stream().map(Long::parseLong).toList();
        warrantyService.deleteMultipleWarranty(idList, homeId);
        return ResponseEntity.noContent().build();
    }
}
