package dev.chatree.smarthomeapi.controller;

import dev.chatree.smarthomeapi.model.waterworks.WaterworksResponse;
import dev.chatree.smarthomeapi.security.home.RequireHomeAccess;
import dev.chatree.smarthomeapi.service.WaterworksService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
