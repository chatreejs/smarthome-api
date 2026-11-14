package dev.chatree.smarthomeapi.controller;

import dev.chatree.smarthomeapi.exception.BusinessException;
import dev.chatree.smarthomeapi.model.electricity.ElectricityRequest;
import dev.chatree.smarthomeapi.model.electricity.ElectricityResponse;
import dev.chatree.smarthomeapi.security.home.RequireHomeAccess;
import dev.chatree.smarthomeapi.service.ElectricityService;
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
@RequestMapping("/homes/{homeId}/electricity")
@RequireHomeAccess
@RequiredArgsConstructor
public class ElectricityController {

    private final ElectricityService electricityService;

    @GetMapping
    public ResponseEntity<List<ElectricityResponse>> getAllElectricity(@PathVariable Long homeId,
                                                                     Authentication auth,
                                                                     HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var electricityResponseList = electricityService.getAllElectricity(homeId);
        return ResponseEntity.ok(electricityResponseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ElectricityResponse> getElectricityById(@PathVariable Long homeId,
                                                                @PathVariable Long id,
                                                                Authentication auth,
                                                                HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var electricityResponse = electricityService.getElectricityById(id, homeId);
        return ResponseEntity.ok(electricityResponse);
    }

    @PostMapping
    public ResponseEntity<Objects> createElectricity(@PathVariable Long homeId,
                                                    @RequestBody ElectricityRequest electricity,
                                                    Authentication auth,
                                                    HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        electricityService.createElectricity(electricity, homeId, auth.getName());
        return ResponseEntity.created(null).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Objects> updateElectricity(@PathVariable Long id,
                                                    @PathVariable Long homeId,
                                                    @RequestBody ElectricityRequest electricity,
                                                    Authentication auth,
                                                    HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        electricityService.updateElectricity(id, electricity, homeId, auth.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Objects> deleteElectricity(@PathVariable Long id,
                                                    @PathVariable Long homeId,
                                                    Authentication auth,
                                                    HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        electricityService.deleteElectricity(id, homeId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Objects> deleteMultipleElectricity(@PathVariable Long homeId,
                                                            String ids,
                                                            Authentication auth,
                                                            HttpServletRequest request) throws BusinessException {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        if (ids.isBlank()) {
            throw new BusinessException("ids must not be blank");
        }
        var idList = List.of(ids.split(",")).stream().map(Long::parseLong).toList();
        electricityService.deleteMultipleElectricity(idList, homeId);
        return ResponseEntity.noContent().build();
    }
}
