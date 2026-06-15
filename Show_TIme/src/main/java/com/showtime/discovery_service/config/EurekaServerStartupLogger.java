package com.showtime.discovery_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

/**
 * Eureka Server configuration and startup event listener.
 * Logs useful info when the server is fully started.
 */
@Component
public class EurekaServerStartupLogger {

    private static final Logger log =
            LoggerFactory.getLogger(EurekaServerStartupLogger.class);

    @Value("${server.port:8761}")
    private int serverPort;

    @Value("${spring.security.user.name:eureka}")
    private String username;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("╔══════════════════════════════════════════════╗");
        log.info("║     ShowTime - Discovery Service Ready       ║");
        log.info("╠══════════════════════════════════════════════╣");
        log.info("║  Dashboard : http://localhost:{}/            ║", serverPort);
        log.info("║  Username  : {}                              ║", username);
        log.info("║  Password  : eureka123                       ║");
        log.info("║  Status    : RUNNING                         ║");
        log.info("╚══════════════════════════════════════════════╝");
    }
}