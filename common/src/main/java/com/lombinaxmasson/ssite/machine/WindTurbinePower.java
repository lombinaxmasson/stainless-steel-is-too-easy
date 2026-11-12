package com.lombinaxmasson.ssite.machine;

/** Power curve for the fiberglass rotor. Wind is relative to the rated wind speed. */
final class WindTurbinePower {
    static final int RATED_RU = 8192;
    static final int BUFFER_RU = 2 * RATED_RU - 1;
    private static final double CUT_IN = 0.35;
    private static final double CUT_IN_CUBED = CUT_IN * CUT_IN * CUT_IN;
    private static final double CUT_OUT = 1.45;
    private static final double RESTART = 1.25;
    private static final int BRAKE_TICKS = 200;
    private static final int RESTART_TICKS = 600;

    private WindTurbinePower() {}

    static int availableRu(double wind) {
        if (!(wind > CUT_IN)) return 0;
        if (wind >= 1.0) return RATED_RU;
        double fraction = (wind * wind * wind - CUT_IN_CUBED) / (1 - CUT_IN_CUBED);
        return (int) Math.round(RATED_RU * fraction);
    }

    static final class StormBrake {
        private boolean engaged;
        private int consecutive;

        StormBrake(boolean engaged) { this.engaged = engaged; }

        boolean sample(double wind) {
            boolean qualifies = engaged ? wind < RESTART : wind > CUT_OUT;
            if (!qualifies) {
                interrupt();
            } else if (++consecutive >= (engaged ? RESTART_TICKS : BRAKE_TICKS)) {
                engaged = !engaged;
                interrupt();
            }
            return engaged;
        }

        void interrupt() { consecutive = 0; }
    }
}
