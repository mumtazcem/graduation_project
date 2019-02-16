/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.utils.management;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

import com.google.maps.android.SphericalUtil;
import com.google.maps.android.utils.management.model.Node;
import com.google.maps.android.utils.management.model.Graph;
import com.google.maps.android.utils.management.model.ShortestPathTree;
import com.google.maps.android.utils.management.model.Statistics;
import com.google.maps.android.utils.management.model.Trigger;
import com.google.maps.android.utils.management.model.Truck;

import static java.lang.StrictMath.toIntExact;

public class StaticSWMActivity extends BaseDemoActivity {

    private static final String TAG = "MainPoly";
    public int cleanedContCounter=0;
    static int numOfNodes;
    static double seed;
    public int trigger_index;
    public int pathLength;
    LocationManager locationManager;
    FirebaseDatabase database;
    DatabaseReference myRef;
    public final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {

            Log.v(TAG, "Location is changed.");
            String longitude = "Longitude: " + location.getLongitude();
            Log.v(TAG, longitude);
            String latitude = "Latitude: " + location.getLatitude();
            Log.v(TAG, latitude);
            // a method
            // should be added to call startDemo() and
            // should set truck's location value as
            // changed
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
    public int destinationID=0 /*default*/;
    Truck truck;
    Handler triggerHandler = new Handler();
    Handler triggerSignalHandler = new Handler();
    int containers[];
    public boolean isFirstTime = true;

    public Node possibleClosest1, possibleClosest2;
    public List<LatLng> listofLL = new ArrayList<LatLng>(){{
        add(new LatLng(41.15952,29.01892)); // 0
        add(new LatLng(41.16011,29.01927)); // 1
        add(new LatLng(41.15975,29.01989)); // 2
        add(new LatLng(41.15944,29.01966)); // 3
        add(new LatLng(41.15894,29.01914)); // 4
        add(new LatLng(41.15871,29.01887)); // 5
        add(new LatLng(41.15949,29.02036)); // 6
        add(new LatLng(41.15933,29.02017)); // 7
        add(new LatLng(41.15875,29.01947)); // 8
        add(new LatLng(41.15869,29.01939)); // 9
        add(new LatLng(41.15832,29.019)); // 10
        add(new LatLng(41.15927,29.02075)); // 11
        add(new LatLng(41.15922,29.02085)); // 12
        add(new LatLng(41.1587,29.0204)); // 13
        add(new LatLng(41.15881,29.02012)); // 14
        add(new LatLng(41.15856,29.01979)); // 15
        add(new LatLng(41.15853,29.01976)); // 16
        add(new LatLng(41.15816,29.01936)); // 17
        add(new LatLng(41.15844,29.02017)); // 18
        add(new LatLng(41.15837,29.02011)); // 19
        add(new LatLng(41.1581,29.01983)); // 20
    }};
    public List<LatLng> twoNodesInBetween = new ArrayList<LatLng>();
    public List<LatLng> polyLinesWholeMap = new ArrayList<LatLng>();
    public List<LatLng> toEncode = new ArrayList<LatLng>();
    public List<LatLng> decodedPath = new ArrayList<LatLng>();
    public List<LatLng> allEdges = new ArrayList<LatLng>();
    public List<Node> listOfNodes = new ArrayList<Node>();
    public List<Node> updatedNodes = new ArrayList<Node>();
    public List<Trigger> triggerQueue = new ArrayList<>();

    Marker truckIcon;
    Marker dumpingArea;
    public List<Marker> markerList = new ArrayList<>();

    public double systemClock;

    private TextView distTravelled;
    private TextView timePassed;

    @Override
    protected int getLayoutId() {
        return R.layout.staticswm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        Log.d("MainPoly", "onCreate is called now.");
        systemClock = 0;
        // default speed is 20km/h ~= 5,5m/s
        truck = new Truck(new LatLng(41.15952,29.01892),35); // default location node 0
        trigger_index = 0;
        seed = 0;
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        // clear database
        myRef.child("triggers").removeValue();
        myRef.setValue("triggers");
        //myRef.child("triggers").child("firstOne").setValue(0);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        markerInitialization();

    }

    private Runnable createTriggerSignals = new Runnable() {
        @Override
        @SuppressLint("NewApi")
        public void run() {
            if (trigger_index < 500) {  // do not random number array limit is 600.
                Log.d("Handlers", "createTriggerSignals Handler");
                int randomNumbers[] = getRandomData();
                int randomNum = randomNumbers[trigger_index];
                myRef.child("triggers").child(String.valueOf(trigger_index)).setValue(containers[randomNum]);
                // Repeat this the same runnable code block again another 5 seconds
                triggerSignalHandler.postDelayed(createTriggerSignals, 2000);
                trigger_index++;
            }
        }
    };

    private Runnable controllerMechanism = new Runnable() {
        @Override
        @SuppressLint("NewApi")
        public void run() {
            Log.d("Handlers", "controllerMechanism Handler");
            if (cleanedContCounter == 500){  // target is reached. target = 600 triggers.
                closeApplication();
            }
            if (!triggerQueue.isEmpty()){
                long timePassed;
                ReentrantLock lock = new ReentrantLock();
                if (truck.isTruckFree){
                    lock.lock();
                    try {
                        truck.isTruckFree = false;
                        boolean isDestinationDumpingArea = false;
                        // set destination
                        // check truck's capacity first
                        if (truck.isFull()){
                            destinationID = 0; // which is dumping area
                            isDestinationDumpingArea = true;
                        }
                        else{  // get first element in triggerQueue
                            destinationID = triggerQueue.get(0).triggeredContainer;
                            // remove the first element
                            triggerQueue.remove(0);
                        }
                        // clear map
                        markerList.clear();
                        getMap().clear();
                        markerInitialization();
                        containerUpdater();
                        // start demo: set pathLength and graph etc.
                        startDemo();
                        if (isDestinationDumpingArea){
                            truck.currentWeight = 0; // reset
                        }
                        else{
                            cleanedContCounter++;
                            Log.d(TAG, "cleanedContCounter: " + String.valueOf(cleanedContCounter));
                        }
                        // set duration and clock
                        timePassed = (long) (pathLength / truck.speed);
                        Log.d(TAG, "Time needed to be passed: " + String.valueOf(timePassed));
                        systemClock += timePassed;
                        Log.d(TAG, "SystemClock is now : " + String.valueOf(systemClock));
                        triggerHandler.postDelayed(controllerMechanism, timePassed*1000);
                    }
                    finally {
                        lock.unlock();
                        truck.isTruckFree = true;
                        // make container green
                        updatedNodes.get(destinationID).isAlerted = false;
                        //containerUpdater();

                    }
                }
            }
            else{
                // wait for 2 seconds
                triggerHandler.postDelayed(controllerMechanism, 2000);
            }
            // Repeat this the same runnable code block again another 5 seconds

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        triggerSignalHandler.post(createTriggerSignals);
        myRef.child("triggers").addChildEventListener(new ChildEventListener(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Long value = dataSnapshot.getValue(Long.class);
                String Key = dataSnapshot.getKey();
                destinationID = toIntExact(value);
                Trigger triggerInput = new Trigger(Key, destinationID);
                triggerQueue.add(triggerInput);
                // update the alerted containers
                updatedNodes.get(destinationID).isAlerted = true;
                containerAlertedUpdater();
                // set container red
                // update end
                Log.d(TAG, "triggeredQueue size is: " + String.valueOf(triggerQueue.size()));

                Log.d(TAG, "Destination is: " + value);
                Log.d(TAG, "onChildAdded");

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildMoved");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled");
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildChanged");
            }
        });
        triggerHandler.post(controllerMechanism);
        containerUpdater();
    }

    @Override
    protected void startDemo() {
        distTravelled = (TextView) findViewById(R.id.distTravelled);
        timePassed = (TextView) findViewById(R.id.timePassed);
        resetAll();
        // containers' ID: 3, 5, 7, 9, 10, 11, 14, 16, 17, 18, 20
        containers = new int[] {3, 5, 7, 9, 10, 11, 14, 16, 17, 18, 20};
        // node initialization
        numOfNodes = 21;
        int[][] graph = getGraph();
        int toSend[] = new int[numOfNodes];
        // node initialization
        for (int i=0;i<numOfNodes;i++){
            System.arraycopy(graph[i], 0, toSend, 0, numOfNodes);
            Node temp = new Node(listofLL.get(i), false, false, toSend, i, numOfNodes);
            listOfNodes.add(temp);
        } // node initialization ends

        for (int i=0; i<containers.length; i++){
            listOfNodes.get(containers[i]).isContainer = true;
        }
        if (isFirstTime){
            isFirstTime=false;
            updatedNodes = new ArrayList<>(listOfNodes);
            markerInitialization();
            return;
        }

        containerUpdater();  // TODO FIX THIS
        // inputs and
        // input truck locations (0,1)(41.159700, 29.019007), (1,2)(41.159952, 29.019566),
        // (4,8)41.158865, 29.019308
        Graph wholeGraph = new Graph(listOfNodes,numOfNodes);
        polyLinesWholeMap = wholeGraph.createWholeMap();
        //polyLinesWholeMap = PolyUtil.decode(PolyUtil.encode(allEdges));

        twoNodesInBetween = findThePath(truck, polyLinesWholeMap);
        if (twoNodesInBetween.size()==0){
            // ERROR BELOW:
            Log.d(TAG, "***   ERROR  ***     THERE IS NO TWO NODES");
            return;
        }
        Graph g = new Graph(listOfNodes,numOfNodes);
        possibleClosest1=g.findNode(twoNodesInBetween.get(0));
        possibleClosest2=g.findNode(twoNodesInBetween.get(1));
        // inputs end
        Log.d(TAG, "Source Node is : " + String.valueOf(g.findNode(truck.coordinate).nodeID));
        // two possibilities: either first node or second node
        // will be used to get shortest path from truck location.
        ShortestPathTree t = new ShortestPathTree(g.shortestPath(possibleClosest1));

        Graph g2 = new Graph(listOfNodes,numOfNodes);
        ShortestPathTree t2 = new ShortestPathTree(g2.shortestPath(possibleClosest2));

        double distanceToTruck1 = SphericalUtil.computeDistanceBetween(truck.coordinate, possibleClosest1.coordinate);
        double distanceToTruck2 = SphericalUtil.computeDistanceBetween(truck.coordinate, possibleClosest2.coordinate);

        distTravelled.setText("Distance Travelled:  "+ String.valueOf(truck.distanceTravelled));
        myRef.child("Distance Travelled Statistic(Static)").child(String.valueOf((int) systemClock)).
                setValue(String.valueOf(truck.distanceTravelled));

        myRef.child("Queue Size Statistic(Static)").child(String.valueOf((int) systemClock)).setValue(triggerQueue.size());
        toEncode =  new ArrayList<LatLng>();
        if (t.dist[destinationID] < t2.dist[destinationID]) {
            possibleClosest1.parent = new Node();
            t.printPath(possibleClosest1, g.nodeList.get(destinationID), toEncode);
            toEncode.add(possibleClosest1.coordinate); // add source as well
            toEncode.add(truck.coordinate);
            truck.distanceTravelled += distanceToTruck1;
            truck.distanceTravelled += t.dist[destinationID];

            // add 125 kg to truck's current weight
            // that is the container capacity.
            if (destinationID != 0) // if destination is not dumping area
                truck.currentWeight += 125;
            Log.d(TAG, "Weight added. Currently : " + String.valueOf(truck.currentWeight));
            pathLength = t.dist[destinationID];
        }
        else{
            possibleClosest2.parent = new Node();
            t2.printPath(possibleClosest2, g2.nodeList.get(destinationID), toEncode);
            toEncode.add(possibleClosest2.coordinate); // add source as well
            toEncode.add(truck.coordinate);
            truck.distanceTravelled += distanceToTruck2;
            truck.distanceTravelled += t2.dist[destinationID];
            // add 125 kg to truck's current weight
            // that is the container capacity.
            if (destinationID != 0) // if destination is not dumping area
                truck.currentWeight += 125;
            Log.d(TAG, "Weight added. Currently : " + String.valueOf(truck.currentWeight));
            pathLength = t2.dist[destinationID];

        }
        Log.d(TAG, "Distance Travelled: " + String.valueOf(truck.distanceTravelled));

        //distTravelled.setText("Distance Travelled:  "+ String.valueOf(truck.distanceTravelled));

        decodedPath = PolyUtil.decode(PolyUtil.encode(toEncode));
        getMap().addPolyline(new PolylineOptions().addAll(decodedPath));
        truck.coordinate = g.nodeList.get(destinationID).coordinate;
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.159, 29.01972), 17));
        timePassed.setText("System Clock is now:  "+ String.valueOf(systemClock));
    }

    // finds the edge that truck is currently located.
    // returns nodeID of nodes of the current edge
    public List<LatLng> findThePath(Truck truck, List<LatLng> polyline){
        int result;
        final List<LatLng> twoNodes =  new ArrayList<LatLng>();
        //double distance = SphericalUtil.computeDistanceBetween(mMarkerA.getPosition(), mMarkerB.getPosition());
        result = PolyUtil.locationIndexOnPath(truck.coordinate, polyline, true, 0.1);
        if (result == -1){
            Log.d(TAG, "locationIndexOnPath error: Given coordinate does not lie on or near the polyline");
        }
        else{
            // add two nodes that truck is in between
            twoNodes.add(polyline.get(result));
            result++;
            twoNodes.add(polyline.get(result));
        }
        return twoNodes;
    }

    public int[][] getGraph(){
        int graph[][] = new int [][]{
                // 0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18 19  20
                {0, 71, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // 0
                {71, 0, 63, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},  // 1
                {0, 63, 0, 35, 0, 0, 47, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},  // 2
                {0, 0, 35, 0, 67, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},  // 3
                {0, 0, 0, 67, 0, 33, 0, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},  // 4
                {0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},  // 5
                {0, 0, 47, 0, 0, 0, 0, 24, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0},  // 6
                {0, 0, 0, 0, 0, 0, 24, 0, 87, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},  // 7
                {0, 0, 0, 0, 35, 0, 0, 87, 0, 3, 0, 0, 0, 0, 0, 35, 0, 0, 0, 0, 0},  // 8
                {0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 59, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},  // 9
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 59, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},  // 10
                {0, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0},  // 11
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 67, 0, 0, 0, 0, 0, 0, 0},  // 12
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 67, 0, 21, 0, 0, 0, 32, 0, 0},  // 13
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 35, 0, 0, 0, 0, 0},  // 14
                {0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 0, 0, 35, 0, 3, 0, 0, 30, 0},  // 15
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 53, 0, 0, 0},  // 16
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 0, 0, 0, 0},  // 17
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 0, 6, 0},  // 18
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 0, 0, 6, 0, 36},  // 19
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 36, 0}  // 20
        };
        return graph;
    }

    public int[] getRandomData(){
        // 500 random numbers
        // randomness come from atmospheric noise
        // generated from : https://www.random.org/
        int randomData[] = new int []{
                2,3,8,5,4,4,2,3,0,10,3,3,6,6,4,5,0,0,3,9,
                5,9,10,5,10,9,8,5,5,9,9,4,4,9,8,2,0,6,6,6,
                4,10,5,9,4,10,9,5,5,4,9,1,0,2,7,8,1,8,9,9,
                6,5,10,6,9,7,4,9,4,6,5,5,2,2,5,6,9,0,7,4,1,
                1,5,5,1,3,0,5,0,0,9,10,9,3,9,9,3,5,6,2,2,7,
                0,1,10,9,5,3,6,10,10,2,3,7,0,2,9,8,8,1,5,2,
                7,2,0,4,6,6,10,8,6,8,6,9,3,2,9,8,1,2,9,9,6,
                6,7,9,8,1,7,6,4,2,0,6,10,0,4,1,3,8,1,10,8,3,
                4,2,1,0,3,5,4,6,2,5,6,0,7,7,3,1,2,1,3,0,5,2,
                9,9,10,9,2,0,4,8,1,3,8,6,6,4,8,2,5,5,7,3,10,
                0,2,2,8,9,4,8,0,3,7,2,8,9,7,1,6,8,9,1,10,8,6,
                7,1,5,4,1,0,6,5,0,3,5,4,5,4,3,3,1,7,2,6,8,3,2,
                6,3,1,7,8,2,2,10,7,0,3,9,8,2,10,1,9,10,6,1,9,
                9,2,6,10,6,8,8,5,2,8,1,1,3,10,2,2,0,3,1,7,0,
                10,4,5,4,3,4,0,4,9,9,0,3,5,4,5,3,0,10,1,6,5,3,
                7,4,8,7,7,8,3,4,2,6,10,9,2,4,7,5,7,9,3,6,10,6,
                10,10,6,6,0,7,8,4,8,7,1,4,7,9,1,6,9,1,4,4,7,4,
                6,6,0,5,4,7,2,4,9,7,8,4,5,2,7,2,1,1,9,4,8,9,6,
                3,3,5,7,8,7,9,8,6,4,1,1,0,10,5,4,4,7,2,1,0,1,
                1,5,1,7,9,2,7,10,8,7,6,1,1,2,8,1,5,4,2,5,9,0,
                5,6,4,8,9,1,7,10,8,0,1,0,9,10,8,9,0,2,6,7,1,0,
                2,8,8,4,2,2,4,4,3,3,5,10,9,10,2,1,5,9,9,9,1,10,
                7,3,10,1,10,10,4,2,3,0,4,3,1,1,10,9,0,0,5,6,3,3,
                9,4,8,3,5,7,9
        };
        return randomData;
    }

    public void resetAll(){
        polyLinesWholeMap.clear();
        twoNodesInBetween.clear();
        allEdges.clear();
        listOfNodes.clear();
    }


    public void markerInitialization(){
        // marker initialization
        truckIcon = getMap().addMarker(new MarkerOptions()
                .position(truck.coordinate)
                .title("Truck")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck))
        );
        dumpingArea = getMap().addMarker(new MarkerOptions()
                .position(new LatLng(41.15918, 29.01847))
                .title("dumpingArea")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.dumpingarea_icon))
        );
        // container markers
        for (int i =0; i<containers.length; i++) {
            Marker container = getMap().addMarker(new MarkerOptions()
                    .position(listofLL.get(containers[i]))
                    .title(String.valueOf(containers[i]))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.container_green))
            );
            markerList.add(container);

        }
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.159, 29.01972), 17));
        // marker initialization ends
    }

    public void containerAlertedUpdater(){
        int index = -1;
        for (Iterator<Node> iter = updatedNodes.iterator(); iter.hasNext();){
            Node element = iter.next();
            if (element.isContainer){
                for (int i = 0; i<containers.length; i++){
                    if (element.nodeID == containers[i])
                        index = i;
                }
                if (element.isAlerted){
                    markerList.get(index).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.container_red));
                }
                /*else{
                    markerList.get(index).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.container_green));
                }*/
            }
        }
    }

    public void containerUpdater(){
        int index = -1;
        for (Iterator<Node> iter = updatedNodes.iterator(); iter.hasNext();){
            Node element = iter.next();
            if (element.isContainer){
                for (int i = 0; i<containers.length; i++){
                    if (element.nodeID == containers[i])
                        index = i;
                }
                if (element.isAlerted){
                    markerList.get(index).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.container_red));
                }
                else{
                    markerList.get(index).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.container_green));
                }
            }
        }
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void closeApplication(){
        Log.d(TAG, "Application is finished at -> " + String.valueOf(systemClock));
        triggerHandler.removeCallbacks(controllerMechanism);
        triggerSignalHandler.removeCallbacks(createTriggerSignals);
        this.finish();
        onDestroy();
    }
}
