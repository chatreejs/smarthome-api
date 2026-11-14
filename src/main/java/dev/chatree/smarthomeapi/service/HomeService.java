package dev.chatree.smarthomeapi.service;

import dev.chatree.smarthomeapi.entity.AccountEntity;
import dev.chatree.smarthomeapi.entity.HomeEntity;
import dev.chatree.smarthomeapi.model.home.HomeRequest;
import dev.chatree.smarthomeapi.model.home.HomeResponse;
import dev.chatree.smarthomeapi.repository.HomeRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class HomeService {

    private final HomeRepository homeRepository;

    public HomeService(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;
    }

    public List<HomeResponse> getHomeByAccount(AccountEntity account) {
        List<HomeEntity> homeEntityList = homeRepository.findByAccountsId(account.getId());
        if (homeEntityList.isEmpty()) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Home not found for this account");
        }

        List<HomeResponse> homeResponseList = new ArrayList<>();

        for (HomeEntity homeEntity : homeEntityList) {
            HomeResponse homeResponse = HomeResponse.builder()
                    .id(homeEntity.getId())
                    .name(homeEntity.getName())
                    .address(homeEntity.getAddress())
                    .latitude(homeEntity.getLatitude())
                    .longitude(homeEntity.getLongitude())
                    .isOwner(homeEntity.getOwner().equals(account.getId()))
                    .createDate(homeEntity.getCreateDate().toString())
                    .updateDate(homeEntity.getUpdateDate().toString())
                    .build();
            homeResponseList.add(homeResponse);
        }
        return homeResponseList;
    }

    @Transactional
    public HomeResponse createHome(HomeRequest homeRequest, AccountEntity account) {
        HomeEntity homeEntity = new HomeEntity();
        homeEntity.setName(homeRequest.getName());
        homeEntity.setAddress(homeRequest.getAddress());
        homeEntity.setLatitude(homeRequest.getLatitude());
        homeEntity.setLongitude(homeRequest.getLongitude());
        homeEntity.setOwner(account.getId());
        homeEntity = homeRepository.save(homeEntity);

        // Insert one membership row. Replacing account.homes would drop the account's other homes.
        homeRepository.addMember(homeEntity.getId(), account.getId());

        return HomeResponse.builder()
                .id(homeEntity.getId())
                .name(homeEntity.getName())
                .address(homeEntity.getAddress())
                .latitude(homeEntity.getLatitude())
                .longitude(homeEntity.getLongitude())
                .isOwner(true)
                .createDate(homeEntity.getCreateDate().toString())
                .updateDate(homeEntity.getUpdateDate().toString())
                .build();
    }
}
