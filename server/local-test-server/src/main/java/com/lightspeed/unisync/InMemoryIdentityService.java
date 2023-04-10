package com.lightspeed.unisync;

import com.lightspeed.unisync.core.interfaces.IdentityService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryIdentityService implements IdentityService {
    @Override
    public Optional<UUID> checkSession(UUID sessionId) {
        return Optional.of(sessionId);
    }
}
