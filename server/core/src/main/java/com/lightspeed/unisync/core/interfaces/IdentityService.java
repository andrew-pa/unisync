package com.lightspeed.unisync.core.interfaces;

import java.util.Optional;
import java.util.UUID;

public interface IdentityService {

    /**
     * Check to see if a session token is still valid
     *
     * @param sessionId the identifier of the session
     * @return if the session token is valid, the user ID associated with the session
     */
    Optional<UUID> checkSession(UUID sessionId);
}
