package dev.chatree.smarthomeapi.service;

import dev.chatree.smarthomeapi.entity.AccountEntity;
import dev.chatree.smarthomeapi.entity.HomeEntity;
import dev.chatree.smarthomeapi.entity.ElectricityEntity;
import dev.chatree.smarthomeapi.model.electricity.ElectricityRequest;
import dev.chatree.smarthomeapi.repository.AccountRepository;
import dev.chatree.smarthomeapi.repository.HomeRepository;
import dev.chatree.smarthomeapi.repository.ElectricityRepository;
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
class ElectricityServiceTest {

    private static final long HOME_ID = 1L;

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private HomeRepository homeRepository;
    @Mock
    private ElectricityRepository electricityRepository;

    @InjectMocks
    private ElectricityService electricityService;

    @Test
    void createElectricitySavesTheBillAsTyped() {
        var account = new AccountEntity();
        var home = new HomeEntity();
        when(accountRepository.findBySubject("member")).thenReturn(account);
        when(homeRepository.findById(HOME_ID)).thenReturn(Optional.of(home));

        electricityService.createElectricity(request(), HOME_ID, "member");

        var saved = ArgumentCaptor.forClass(ElectricityEntity.class);
        verify(electricityRepository).save(saved.capture());
        assertThat(saved.getValue().getInvoiceDate()).isEqualTo(LocalDate.of(2026, 10, 31));
        assertThat(saved.getValue().getTotal()).isEqualByComparingTo("123.45");
        assertThat(saved.getValue().getFtRate()).isEqualByComparingTo("0.3972");
        assertThat(saved.getValue().getCreateBy()).isSameAs(account);
        assertThat(saved.getValue().getHome()).isSameAs(home);
    }

    @Test
    void updateElectricityRecordsUpdateBy() {
        var account = new AccountEntity();
        var electricity = new ElectricityEntity();
        when(accountRepository.findBySubject("member")).thenReturn(account);
        when(electricityRepository.findByIdAndHomeId(5L, HOME_ID)).thenReturn(electricity);

        electricityService.updateElectricity(5L, request(), HOME_ID, "member");

        assertThat(electricity.getUpdateBy()).isSameAs(account);
        verify(electricityRepository).save(electricity);
    }

    @Test
    void getElectricityFromAnotherHomeIsNotFound() {
        when(electricityRepository.findByIdAndHomeId(99L, HOME_ID)).thenReturn(null);

        assertNotFound(() -> electricityService.getElectricityById(99L, HOME_ID));
    }

    @Test
    void updateElectricityFromAnotherHomeIsNotFound() {
        when(electricityRepository.findByIdAndHomeId(99L, HOME_ID)).thenReturn(null);

        assertNotFound(() -> electricityService.updateElectricity(99L, request(), HOME_ID, "member"));
        verify(electricityRepository, never()).save(any());
    }

    @Test
    void deleteElectricityFromAnotherHomeIsNotFound() {
        when(electricityRepository.findByIdAndHomeId(99L, HOME_ID)).thenReturn(null);

        assertNotFound(() -> electricityService.deleteElectricity(99L, HOME_ID));
        verify(electricityRepository, never()).delete(any());
    }

    @Test
    void bulkDeleteWithAnyForeignIdDeletesNothing() {
        // Id 99 belongs to another home, so the query finds only id 5.
        when(electricityRepository.findAllByIdInAndHomeId(Set.of(5L, 99L), HOME_ID)).thenReturn(List.of(new ElectricityEntity()));

        assertNotFound(() -> electricityService.deleteMultipleElectricity(List.of(5L, 99L), HOME_ID));
        verify(electricityRepository, never()).deleteAll(anyIterable());
    }

    @Test
    void bulkDeleteWithAllIdsInHomeDeletesThem() {
        var bills = List.of(new ElectricityEntity(), new ElectricityEntity());
        when(electricityRepository.findAllByIdInAndHomeId(Set.of(5L, 6L), HOME_ID)).thenReturn(bills);

        electricityService.deleteMultipleElectricity(List.of(5L, 6L, 6L), HOME_ID);

        verify(electricityRepository).deleteAll(bills);
    }

    private static ElectricityRequest request() {
        return new ElectricityRequest("INV-1", "2026-10-31", 250, new BigDecimal("0.3972"),
                new BigDecimal("950.00"), new BigDecimal("38.22"), new BigDecimal("99.30"),
                new BigDecimal("76.08"), new BigDecimal("123.45"));
    }

    private static void assertNotFound(Runnable call) {
        assertThatThrownBy(call::run)
                .isInstanceOfSatisfying(HttpClientErrorException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
