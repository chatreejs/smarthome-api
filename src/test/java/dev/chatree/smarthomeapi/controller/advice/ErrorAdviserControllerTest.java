package dev.chatree.smarthomeapi.controller.advice;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorAdviserControllerTest {

    private final ErrorAdviserController adviser = new ErrorAdviserController();

    @Test
    void notFoundKeepsItsStatus() {
        var response = adviser.handleHttpErrorException(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Food not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    void forbiddenKeepsItsStatus() {
        var response = adviser.handleHttpErrorException(new HttpClientErrorException(HttpStatus.FORBIDDEN, "nope"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void unreachableHostIsServiceUnavailable() {
        var response = adviser.handleHttpErrorException(new ResourceAccessException("down"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
