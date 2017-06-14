package mygame.path.astar;

import mygame.path.GridLocation;

public class GridHeuristic
{
    public double getDistance(int x, int y, GridLocation location)
    {
        double result = 0;
        double cons = 1.2;
        result += cons * Math.abs(x - location.getX());
        result += cons * Math.abs(y - location.getY());
        return result;
    }
}
