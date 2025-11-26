package net.exylia.exyliaEntityLimiter.config;

import lombok.Value;

@Value
public class EntityLimitConfig {
    int limit;

    public boolean hasLimit() {
        return limit >= 0;
    }

    public boolean isUnlimited() {
        return limit < 0;
    }
}
