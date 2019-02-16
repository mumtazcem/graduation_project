package com.google.maps.android.utils.management.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Truck {
    public LatLng coordinate;
    public int distanceTravelled;
    public boolean isTruckFree;
    public double speed;  // (m/s)
    public int weightLimit;
    public int currentWeight;


    public Truck(LatLng coordinate, double speed) {
        this.coordinate = coordinate;
        distanceTravelled=0;
        isTruckFree = true;
        this.speed = speed;
        weightLimit = 2500; // kg, referenced from The effect.. paper
        currentWeight = 0;  // kg
    }

    public boolean isFull(){
        return currentWeight >= weightLimit;
    }

}
