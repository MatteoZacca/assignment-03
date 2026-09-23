package pcd.ass03.boids;

import java.util.List;

public interface BoidsProtocol {

    // --- Configuration & Data Structures ---

    public static record BoidsConfig(
            double width,
            double height,
            double maxSpeed,
            double perceptionRadius,
            double avoidRadius
    ) {}

    public static record BoidState(
            int id,
            P2d pos,
            V2d vel
    ) {}

    // --- Lifecycle Messages ---

    public static record BootMsg(BoidsModel model) {};

    public static record StartSimulationMsg() {};

    public static record PauseSimulationMsg() {};

    public static record ResetSimulationMsg(int nStartingBoids) {};

    public static record Tick() {}

    // --- Simulation Step Messages ---

    public static record ComputeStepMsg(
            List<BoidState> flockState,
            double separationWeight,
            double alignmentWeight,
            double cohesionWeight

    ) {}

    public static record StepDoneMsg(BoidState updatedBoid) {}

    // --- UI Update Messages ---

    public static record UpdateSeparationWeightMsg(double weight) {}
    public static record UpdateAlignmentWeightMsg(double weight) {}
    public static record UpdateCohesionWeightMsg(double weight) {}
}
