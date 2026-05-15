package com.parkease.eureka;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EurekaServerApplicationTests {

    @Test
    void mainClassExists() {
        assertThat(EurekaServerApplication.class).isNotNull();
    }
}