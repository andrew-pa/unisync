package com.lightspeed.unisync.core.interfaces;

import java.util.UUID;

public interface IdentityService {
    boolean checkSession(UUID sessionId);
}
