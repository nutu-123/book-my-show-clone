package com.showtime.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.cloud.gateway.enabled=false"
})
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}
