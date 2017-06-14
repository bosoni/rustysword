package mygame.path.astar;

import java.util.ArrayList;

import mygame.path.GridLocation;

public class GridSortedLocationList
{
    private ArrayList<GridLocationAstar> locationList;

    public GridSortedLocationList()
    {
        locationList = new ArrayList<GridLocationAstar>();
    }

    public boolean hasNext()
    {
        return locationList.size() > 0;
    }

    public void add(GridLocation loc)
    {
        GridLocationAstar location = (GridLocationAstar) loc;
        addInOrder(location);
    }

    public GridLocationAstar getNext()
    {
        if (locationList.size() > 0)
        {
            return locationList.remove(0);
        }
        return null; //TODO throw end of list exception
    }

    private void addInOrder(GridLocationAstar location)
    {
        GridLocationAstar tempLocation;
        if (locationList.size() == 0)
        {
            locationList.add(location);
            return;
        }
        for (int i = 0; i < locationList.size(); i++)
        {
            tempLocation = locationList.get(i);
            if (location.getTotalDistance() < tempLocation.getTotalDistance())
            {
                locationList.add(i, location);
                return;
            }
        }
        locationList.add(location);
    }

    public String toString()
    {
        String result = locationList.size() + "";
        return result;
    }
}
