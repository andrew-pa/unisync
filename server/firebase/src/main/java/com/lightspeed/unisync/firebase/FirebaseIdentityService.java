package com.lightspeed.unisync.firebase;

import com.lightspeed.unisync.core.interfaces.IdentityService;

import java.util.Optional;
import java.util.UUID;

public class FirebaseIdentityService implements IdentityService {
    @Override
    public Optional<UUID> checkSession(UUID sessionId) {
        return Optional.empty();
    }
}
