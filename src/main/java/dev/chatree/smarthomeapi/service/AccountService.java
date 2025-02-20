package dev.chatree.smarthomeapi.service;

import dev.chatree.smarthomeapi.entity.AccountEntity;
import dev.chatree.smarthomeapi.entity.HomeEntity;
import dev.chatree.smarthomeapi.model.account.AccountRequest;
import dev.chatree.smarthomeapi.model.account.AccountResponse;
import dev.chatree.smarthomeapi.repository.AccountRepository;
import dev.chatree.smarthomeapi.repository.HomeRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Log4j2
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    private final HomeRepository homeRepository;

    public AccountService(AccountRepository accountRepository, HomeRepository homeRepository) {
        this.accountRepository = accountRepository;
        this.homeRepository = homeRepository;
    }

    public AccountResponse getAccountInfoBySubject(String subject) {
        AccountEntity accountEntity = accountRepository.findBySubject(subject);
        if (accountEntity == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Account not found");
        }

        List<HomeEntity> homeEntities = homeRepository.findByAccountsId(accountEntity.getId());

        return AccountResponse.builder()
                .id(accountEntity.getId())
                .username(accountEntity.getUsername())
                .firstName(accountEntity.getFirstName())
                .lastName(accountEntity.getLastName())
                .email(accountEntity.getEmail())
                .hasHome(!homeEntities.isEmpty())
                .build();
    }

    public AccountEntity getAccountBySubject(String subject) {
        AccountEntity accountEntity = accountRepository.findBySubject(subject);
        if (accountEntity == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Account not found");
        }
        return accountEntity;
    }

    public void createAccount(AccountRequest accountRequest, String subject) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setSubject(subject);
        accountEntity.setUsername(accountRequest.getUsername());
        accountEntity.setFirstName(accountRequest.getFirstName());
        accountEntity.setLastName(accountRequest.getLastName());
        accountEntity.setEmail(accountRequest.getEmail());

        accountRepository.save(accountEntity);
    }
}
