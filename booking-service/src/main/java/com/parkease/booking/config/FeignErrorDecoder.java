package com.parkease.booking.config;

import com.parkease.booking.exception.ExternalServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FeignErrorDecoder {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    public static class CustomErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultDecoder = new Default();

        @Override
        public Exception decode(String methodKey, Response response) {
            int status = response.status();
            log.error("Feign call failed: method={}, status={}", methodKey, status);

            if (status >= 400 && status < 500) {
                return new ExternalServiceException(
                    "External service client error [" + status + "] for " + methodKey);
            }
            if (status >= 500) {
                return new ExternalServiceException(
                    "External service server error [" + status + "] for " + methodKey);
            }
            return defaultDecoder.decode(methodKey, response);
        }
    }
}
