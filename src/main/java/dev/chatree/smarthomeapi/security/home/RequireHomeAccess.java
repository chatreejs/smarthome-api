package dev.chatree.smarthomeapi.security.home;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows a call only if the caller belongs to the home in the method's {@code homeId} parameter.
 * <p>
 * Put it on a controller class to guard every endpoint, or on one method.
 * If the method has no {@code homeId} parameter, the guard rejects every call.
 * A rejected call throws {@link org.springframework.security.access.AccessDeniedException},
 * which the API returns as HTTP 403.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("@homeAccessGuard.isMember(#homeId, authentication)")
public @interface RequireHomeAccess {
}
