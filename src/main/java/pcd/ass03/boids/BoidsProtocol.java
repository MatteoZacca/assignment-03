package pcd.ass03.boids;

import java.util.List;

public interface BoidsProtocol {
    public static record BootMsg(BoidsModel model) {};

    public static record StartSimulationMsg() {};

    public static record PauseSimulationMsg() {};

    public static record ResetSimulationMsg(int nStartingBoids) {};

    public static record CalculateVelocityMsg(List<Boid> boids) {};

    public static record AfterCalculateVelocityMsg() {};

    public static record BeforeUpdateBoidMsg() {};

    public static record AfterUpdateBoidMsg(Boid updatedBoid) {};

    public static record ContinueUpdatingSimulationMsg() {};

    public static record UpdateSeparationWeightMsg(double weight) {};

    public static record UpdateAlignmentWeightMsg(double weight) {};

    public static record UpdateCohesionWeightMsg(double weight) {};

    public static record Tick() {};



}
