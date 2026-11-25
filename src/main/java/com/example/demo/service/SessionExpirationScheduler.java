package com.example.demo.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SessionExpirationScheduler {

    private final AdministratorSessionService sessionService;

    public SessionExpirationScheduler(
            AdministratorSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Scheduled(fixedDelay = 60000)
    public void expireSessions() {

        int expiredCount = sessionService.expireOverdueSessions();

        if (expiredCount > 0) {
            System.out.println(
                    "ThreatShare: Expired " + expiredCount
                    + " administrator session(s)."
            );
        }
    }
}