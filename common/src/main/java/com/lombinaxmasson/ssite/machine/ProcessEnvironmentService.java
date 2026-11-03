package com.lombinaxmasson.ssite.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Server-side lookup point for the P-04 process environment.
 *
 * <p>P-04 registers the provider from its server setup. Until it exists the service returns an
 * unavailable snapshot, which keeps every advanced process stopped instead of silently ignoring
 * the environment. The provider must validate loaded chunks and room bounds itself; this service
 * only guards the missing-provider case and never accepts a client-supplied snapshot.</p>
 */
public final class ProcessEnvironmentService {
    public interface Provider {
        ProcessEnvironmentSnapshot snapshot(Level level, BlockPos pos);
    }

    private static volatile Provider provider;

    private ProcessEnvironmentService() {}

    public static void register(Provider value) {
        provider = value;
    }

    public static ProcessEnvironmentSnapshot snapshot(Level level, BlockPos pos) {
        Provider current = provider;
        if (current == null) {
            return ProcessEnvironmentSnapshot.unavailable(ProcessEnvironmentSnapshot.REASON_NO_SERVICE);
        }
        ProcessEnvironmentSnapshot snapshot = current.snapshot(level, pos);
        return snapshot == null
                ? ProcessEnvironmentSnapshot.unavailable(ProcessEnvironmentSnapshot.REASON_NO_SERVICE)
                : snapshot;
    }
}
