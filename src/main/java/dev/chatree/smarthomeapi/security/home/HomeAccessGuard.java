package dev.chatree.smarthomeapi.security.home;

import dev.chatree.smarthomeapi.repository.HomeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Checks home membership for {@link RequireHomeAccess}.
 */
@Log4j2
@Component("homeAccessGuard")
@RequiredArgsConstructor
public class HomeAccessGuard {

    private final HomeRepository homeRepository;

    public boolean isMember(Long homeId, Authentication authentication) {
        if (homeId == null || authentication == null) {
            return false;
        }
        var allowed = homeRepository.existsByIdAndAccountsSubject(homeId, authentication.getName());
        if (!allowed) {
            log.info("Account {} denied access to home {}", authentication.getName(), homeId);
        }
        return allowed;
    }
}
