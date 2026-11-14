package dev.chatree.smarthomeapi.controller;

import dev.chatree.smarthomeapi.exception.BusinessException;
import dev.chatree.smarthomeapi.model.waterworks.WaterworksRequest;
import dev.chatree.smarthomeapi.model.waterworks.WaterworksResponse;
import dev.chatree.smarthomeapi.security.home.RequireHomeAccess;
import dev.chatree.smarthomeapi.service.WaterworksService;
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
@RequestMapping("/homes/{homeId}/waterworks")
@RequireHomeAccess
@RequiredArgsConstructor
public class WaterworksController {

    private final WaterworksService waterworksService;

    @GetMapping
    public ResponseEntity<List<WaterworksResponse>> getAllWaterworks(@PathVariable Long homeId,
                                                                     Authentication auth,
                                                                     HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var waterworksResponseList = waterworksService.getAllWaterworks(homeId);
        return ResponseEntity.ok(waterworksResponseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WaterworksResponse> getWaterworksById(@PathVariable Long homeId,
                                                                @PathVariable Long id,
                                                                Authentication auth,
                                                                HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var waterworksResponse = waterworksService.getWaterworksById(id, homeId);
        return ResponseEntity.ok(waterworksResponse);
    }

    @PostMapping
    public ResponseEntity<Objects> createWaterworks(@PathVariable Long homeId,
                                                    @RequestBody WaterworksRequest waterworks,
                                                    Authentication auth,
                                                    HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        waterworksService.createWaterworks(waterworks, homeId, auth.getName());
        return ResponseEntity.created(null).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Objects> updateWaterworks(@PathVariable Long id,
                                                    @PathVariable Long homeId,
                                                    @RequestBody WaterworksRequest waterworks,
                                                    Authentication auth,
                                                    HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        waterworksService.updateWaterworks(id, waterworks, homeId, auth.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Objects> deleteWaterworks(@PathVariable Long id,
                                                    @PathVariable Long homeId,
                                                    Authentication auth,
                                                    HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        waterworksService.deleteWaterworks(id, homeId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Objects> deleteMultipleWaterworks(@PathVariable Long homeId,
                                                            String ids,
                                                            Authentication auth,
                                                            HttpServletRequest request) throws BusinessException {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        if (ids.isBlank()) {
            throw new BusinessException("ids must not be blank");
        }
        var idList = List.of(ids.split(",")).stream().map(Long::parseLong).toList();
        waterworksService.deleteMultipleWaterworks(idList, homeId);
        return ResponseEntity.noContent().build();
    }
}
