package dev.chatree.smarthomeapi.service;

import dev.chatree.smarthomeapi.entity.AccountEntity;
import dev.chatree.smarthomeapi.entity.HomeEntity;
import dev.chatree.smarthomeapi.entity.WaterworksEntity;
import dev.chatree.smarthomeapi.model.waterworks.WaterworksRequest;
import dev.chatree.smarthomeapi.repository.AccountRepository;
import dev.chatree.smarthomeapi.repository.HomeRepository;
import dev.chatree.smarthomeapi.repository.WaterworksRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaterworksServiceTest {

    private static final long HOME_ID = 1L;

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private HomeRepository homeRepository;
    @Mock
    private WaterworksRepository waterworksRepository;

    @InjectMocks
    private WaterworksService waterworksService;

    @Test
    void createWaterworksSavesTheBillAsTyped() {
        var account = new AccountEntity();
        var home = new HomeEntity();
        when(accountRepository.findBySubject("member")).thenReturn(account);
        when(homeRepository.findById(HOME_ID)).thenReturn(Optional.of(home));

        waterworksService.createWaterworks(request(), HOME_ID, "member");

        var saved = ArgumentCaptor.forClass(WaterworksEntity.class);
        verify(waterworksRepository).save(saved.capture());
        assertThat(saved.getValue().getInvoiceDate()).isEqualTo(LocalDate.of(2026, 10, 31));
        assertThat(saved.getValue().getTotal()).isEqualByComparingTo("123.45");
        assertThat(saved.getValue().getCreateBy()).isSameAs(account);
        assertThat(saved.getValue().getHome()).isSameAs(home);
    }

    @Test
    void updateWaterworksRecordsUpdateBy() {
        var account = new AccountEntity();
        var waterworks = new WaterworksEntity();
        when(accountRepository.findBySubject("member")).thenReturn(account);
        when(waterworksRepository.findByIdAndHomeId(5L, HOME_ID)).thenReturn(waterworks);

        waterworksService.updateWaterworks(5L, request(), HOME_ID, "member");

        assertThat(waterworks.getUpdateBy()).isSameAs(account);
        verify(waterworksRepository).save(waterworks);
    }

    @Test
    void getWaterworksFromAnotherHomeIsNotFound() {
        when(waterworksRepository.findByIdAndHomeId(99L, HOME_ID)).thenReturn(null);

        assertNotFound(() -> waterworksService.getWaterworksById(99L, HOME_ID));
    }

    @Test
    void updateWaterworksFromAnotherHomeIsNotFound() {
        when(waterworksRepository.findByIdAndHomeId(99L, HOME_ID)).thenReturn(null);

        assertNotFound(() -> waterworksService.updateWaterworks(99L, request(), HOME_ID, "member"));
        verify(waterworksRepository, never()).save(any());
    }

    @Test
    void deleteWaterworksFromAnotherHomeIsNotFound() {
        when(waterworksRepository.findByIdAndHomeId(99L, HOME_ID)).thenReturn(null);

        assertNotFound(() -> waterworksService.deleteWaterworks(99L, HOME_ID));
        verify(waterworksRepository, never()).delete(any());
    }

    @Test
    void bulkDeleteWithAnyForeignIdDeletesNothing() {
        // Id 99 belongs to another home, so the query finds only id 5.
        when(waterworksRepository.findAllByIdInAndHomeId(Set.of(5L, 99L), HOME_ID)).thenReturn(List.of(new WaterworksEntity()));

        assertNotFound(() -> waterworksService.deleteMultipleWaterworks(List.of(5L, 99L), HOME_ID));
        verify(waterworksRepository, never()).deleteAll(anyIterable());
    }

    @Test
    void bulkDeleteWithAllIdsInHomeDeletesThem() {
        var bills = List.of(new WaterworksEntity(), new WaterworksEntity());
        when(waterworksRepository.findAllByIdInAndHomeId(Set.of(5L, 6L), HOME_ID)).thenReturn(bills);

        waterworksService.deleteMultipleWaterworks(List.of(5L, 6L, 6L), HOME_ID);

        verify(waterworksRepository).deleteAll(bills);
    }

    private static WaterworksRequest request() {
        return new WaterworksRequest("INV-1", "2026-10-31", 20, 18,
                new BigDecimal("30.00"), new BigDecimal("8.08"), new BigDecimal("123.45"));
    }

    private static void assertNotFound(Runnable call) {
        assertThatThrownBy(call::run)
                .isInstanceOfSatisfying(HttpClientErrorException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
