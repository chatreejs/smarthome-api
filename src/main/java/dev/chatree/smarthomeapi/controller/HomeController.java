package dev.chatree.smarthomeapi.controller;

import dev.chatree.smarthomeapi.model.home.HomeRequest;
import dev.chatree.smarthomeapi.model.home.HomeResponse;
import dev.chatree.smarthomeapi.service.AccountService;
import dev.chatree.smarthomeapi.service.HomeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static dev.chatree.smarthomeapi.constant.MessageConstants.LOG_USER_REQUEST_PATTERN;

@Log4j2
@RestController
@RequestMapping("/homes")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<HomeResponse>> getHomeByAccountId(Authentication auth,
                                                                 HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var subject = auth.getName();
        var accountEntity = accountService.getAccountBySubject(subject);
        var homeEntityList = homeService.getHomeByAccount(accountEntity);
        return ResponseEntity.ok(homeEntityList);

    }

    @PostMapping
    public ResponseEntity<HomeResponse> createHome(@RequestBody HomeRequest homeRequest,
                                                   Authentication auth,
                                                   HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var subject = auth.getName();
        var accountEntity = accountService.getAccountBySubject(subject);
        var homeResponse = homeService.createHome(homeRequest, accountEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(homeResponse);
    }
}
