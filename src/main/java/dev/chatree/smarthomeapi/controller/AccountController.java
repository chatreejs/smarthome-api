package dev.chatree.smarthomeapi.controller;

import dev.chatree.smarthomeapi.model.account.AccountRequest;
import dev.chatree.smarthomeapi.model.account.AccountResponse;
import dev.chatree.smarthomeapi.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static dev.chatree.smarthomeapi.constant.MessageConstants.LOG_USER_REQUEST_PATTERN;

@Log4j2
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/userinfo")
    public ResponseEntity<AccountResponse> getUserInfo(Authentication auth,
                                                       HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        var accountResponse = accountService.getAccountInfoBySubject(auth.getName());
        return ResponseEntity.ok(accountResponse);

    }

    @PostMapping
    public ResponseEntity<Object> createAccount(@RequestBody AccountRequest accountRequest,
                                                Authentication auth,
                                                HttpServletRequest request) {
        log.info(LOG_USER_REQUEST_PATTERN, request.getMethod(), request.getServletPath(), auth.getName());
        accountService.createAccount(accountRequest, auth.getName());
        return ResponseEntity.created(null).build();
    }
}
