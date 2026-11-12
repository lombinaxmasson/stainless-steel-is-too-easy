package com.lombinaxmasson.ssite.machine;

import com.lombinaxmasson.ssite.client.WindTurbineRenderTests;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class WindTurbineTests {
    public static void main(String[] args) {
        checkPowerCurve();
        checkStormBrake();
        checkWindAndClearance();
        WindTurbineRenderTests.main(args);
        System.out.println("Wind turbine power, storm brake and wind field checks passed");
    }

    private static void checkPowerCurve() {
        assert WindTurbinePower.availableRu(0.35) == 0;
        assert WindTurbinePower.availableRu(0.8) == 4015;
        assert WindTurbinePower.availableRu(0.9) == 5873;
        assert WindTurbinePower.availableRu(1.0) == 8192;
        assert WindTurbinePower.availableRu(1.4) == 8192;
    }

    private static void checkStormBrake() {
        var brake = new WindTurbinePower.StormBrake(false);
        for (int i = 0; i < 199; i++) assert !brake.sample(1.46);
        assert !brake.sample(1.44);
        for (int i = 0; i < 199; i++) assert !brake.sample(1.46);
        assert brake.sample(1.46);
        for (int i = 0; i < 599; i++) assert brake.sample(1.24);
        brake.interrupt();
        for (int i = 0; i < 599; i++) assert brake.sample(1.24);
        assert !brake.sample(1.24);
    }

    private static void checkWindAndClearance() {
        double atBoundary = WindTurbineEnvironment.regionalWind(42, 3600, -256, 128);
        assert atBoundary >= 0.72 && atBoundary <= 1.15;
        assert Math.abs(atBoundary - WindTurbineEnvironment.regionalWind(42, 3599, -256.01, 128)) < 0.001;
        assert Math.abs(atBoundary - WindTurbineEnvironment.regionalWind(42, 3601, -255.99, 128)) < 0.001;
        BlockPos head = BlockPos.ZERO;
        assert WindTurbineEnvironment.sweepsOverlap(head, Direction.NORTH, head.east(10), Direction.NORTH);
        assert !WindTurbineEnvironment.sweepsOverlap(head, Direction.NORTH, head.east(11), Direction.NORTH);
        assert !WindTurbineEnvironment.sweepsOverlap(head, Direction.NORTH, head.south(), Direction.NORTH);
        BlockPos crossing = head.east().north();
        assert WindTurbineEnvironment.sweepsOverlap(head, Direction.NORTH, crossing, Direction.WEST);
        assert !WindTurbineEnvironment.sweepsOverlap(head, Direction.NORTH, crossing.above(11), Direction.WEST);
    }
}
