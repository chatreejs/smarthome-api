package dev.chatree.smarthomeapi.security.home;

import dev.chatree.smarthomeapi.controller.FoodController;
import dev.chatree.smarthomeapi.repository.HomeRepository;
import dev.chatree.smarthomeapi.security.oauth2.JWTSecurityConfig;
import dev.chatree.smarthomeapi.service.FoodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FoodController.class, properties = "API_AUTH_ISSUER_URI=http://localhost/test-issuer")
@Import({JWTSecurityConfig.class, HomeAccessGuard.class})
class RequireHomeAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FoodService foodService;

    @MockBean
    private HomeRepository homeRepository;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void memberCanAccessHomeResource() throws Exception {
        when(homeRepository.existsByIdAndAccountsSubject(1L, "member")).thenReturn(true);
        when(foodService.getAllFood(1L)).thenReturn(List.of());

        mockMvc.perform(get("/homes/1/foods").with(jwt().jwt(j -> j.subject("member"))))
                .andExpect(status().isOk());

        verify(foodService).getAllFood(1L);
    }

    @Test
    void nonMemberIsForbiddenAndServiceIsNotCalled() throws Exception {
        when(homeRepository.existsByIdAndAccountsSubject(1L, "outsider")).thenReturn(false);

        mockMvc.perform(get("/homes/1/foods").with(jwt().jwt(j -> j.subject("outsider"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/homes/1/foods/5").with(jwt().jwt(j -> j.subject("outsider"))))
                .andExpect(status().isForbidden());

        verify(foodService, never()).getAllFood(any());
        verify(foodService, never()).deleteFood(any(), any());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/homes/1/foods"))
                .andExpect(status().isUnauthorized());
    }
}
