package dev.chatree.smarthomeapi.service;

import dev.chatree.smarthomeapi.constant.ErrorMessage;
import dev.chatree.smarthomeapi.entity.AccountEntity;
import dev.chatree.smarthomeapi.entity.FoodEntity;
import dev.chatree.smarthomeapi.model.food.FoodRequest;
import dev.chatree.smarthomeapi.model.food.FoodResponse;
import dev.chatree.smarthomeapi.model.food.FoodStatus;
import dev.chatree.smarthomeapi.repository.AccountRepository;
import dev.chatree.smarthomeapi.repository.FoodRepository;
import dev.chatree.smarthomeapi.repository.HomeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class FoodService {
    private final AccountRepository accountRepository;
    private final HomeRepository homeRepository;
    private final FoodRepository foodRepository;

    public List<FoodResponse> getAllFood(Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);
        var isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

        var foodEntityList = foodRepository.findAllByHomeId(homeId);
        var foodResponseList = new ArrayList<FoodResponse>();

        for (var foodEntity : foodEntityList) {
            var updateBy = foodEntity.getUpdateBy();
            if (updateBy == null) {
                updateBy = foodEntity.getCreateBy();
            }
            var foodResponse = generateFoodResponse(foodEntity, updateBy);
            foodResponseList.add(foodResponse);
        }
        return foodResponseList;
    }

    public FoodResponse getFoodById(Long id, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);
        var isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

        var foodEntity = foodRepository.findByIdAndHomeId(id, homeId);
        if (foodEntity == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Food not found");
        }

        var updateBy = foodEntity.getUpdateBy();
        if (updateBy == null) {
            updateBy = foodEntity.getCreateBy();
        }
        return generateFoodResponse(foodEntity, updateBy);
    }

    public void createFood(FoodRequest foodRequest, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);
        var isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

        var homeEntity = homeRepository.findById(homeId).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "Home not found"));
        var foodEntity = new FoodEntity();
        foodEntity.setName(foodRequest.getName());
        foodEntity.setBrand(foodRequest.getBrand());
        foodEntity.setQuantity(foodRequest.getQuantity());
        foodEntity.setUnit(foodRequest.getUnit());
        foodEntity.setBuyDate(LocalDate.parse(foodRequest.getBuyDate(), DateTimeFormatter.ISO_DATE));
        foodEntity.setExpiryDate(LocalDate.parse(foodRequest.getExpiryDate(), DateTimeFormatter.ISO_DATE));
        foodEntity.setCreateBy(account);
        foodEntity.setHome(homeEntity);

        foodRepository.save(foodEntity);
    }

    public void updateFood(Long id, FoodRequest foodRequest, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);
        var isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

        var foodEntity = foodRepository.findById(id).orElse(null);
        if (foodEntity == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Food not found");
        }

        foodEntity.setName(foodRequest.getName());
        foodEntity.setBrand(foodRequest.getBrand());
        foodEntity.setQuantity(foodRequest.getQuantity());
        foodEntity.setUnit(foodRequest.getUnit());
        foodEntity.setBuyDate(LocalDate.parse(foodRequest.getBuyDate(), DateTimeFormatter.ISO_DATE));
        foodEntity.setUpdateBy(account);
        foodEntity.setExpiryDate(LocalDate.parse(foodRequest.getExpiryDate(), DateTimeFormatter.ISO_DATE));

        foodRepository.save(foodEntity);
    }

    public void deleteFood(Long id, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);
        var isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

        var foodEntity = foodRepository.findById(id).orElse(null);
        if (foodEntity == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Food not found");
        }

        foodRepository.delete(foodEntity);
    }

    public void deleteMultipleFood(List<Long> ids, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);
        boolean isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

        foodRepository.deleteAllById(ids);
    }

    private FoodResponse generateFoodResponse(FoodEntity foodEntity, AccountEntity updateBy) {
        return FoodResponse.builder()
                .id(foodEntity.getId())
                .name(foodEntity.getName())
                .brand(foodEntity.getBrand())
                .quantity(foodEntity.getQuantity())
                .unit(foodEntity.getUnit())
                .buyDate(foodEntity.getBuyDate().toString())
                .expiryDate(foodEntity.getExpiryDate().toString())
                .status(checkFoodStatus(foodEntity.getExpiryDate()))
                .updateBy(updateBy.getUsername())
                .updateDate(foodEntity.getUpdateDate().toString())
                .build();
    }

    private FoodStatus checkFoodStatus(LocalDate expiryDate) {
        if (expiryDate.isBefore(LocalDate.now())) {
            return FoodStatus.EXPIRED;
        } else {
            return FoodStatus.FRESH;
        }
    }
}
