package com.google.maps.android.utils.management.model;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.utils.management.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Node{
    public LatLng coordinate;
    public boolean isContainer;
    public boolean isAlerted;
    public int distanceArray[];

    public int nodeID; // its id starting from 0

    // tree structure
    public Node parent;

    public Node() {
        this.nodeID = -1; // for checking
    }

    public Node(LatLng coordinate, boolean isContainer, boolean isAlerted, int[] distanceArray, int nodeID, int numOfNodes) {
        this.coordinate = coordinate;
        this.isContainer = isContainer;
        this.isAlerted = isAlerted;
        this.distanceArray = new int[distanceArray.length];
        System.arraycopy(distanceArray, 0, this.distanceArray, 0, distanceArray.length);
        this.nodeID = nodeID;
        // tree
        if (this.nodeID == 0) {  // root node
            parent = new Node();
            parent.nodeID = -5;  // root node parent id
        } else
            parent = new Node();   // not yet assigned node id
    }

    // copy constructor
    public Node(Node n, Node parent){
        this.coordinate = n.coordinate;
        this.isContainer = n.isContainer;
        this.isAlerted = n.isAlerted;
        this.nodeID = n.nodeID;
        this.distanceArray = new int[n.distanceArray.length];
        System.arraycopy(n.distanceArray, 0, this.distanceArray, 0, n.distanceArray.length);

        // copy tree structure
        this.parent = parent;
        /*if (n.parent!=null)
            this.parent.assign(n.parent);
        if (n.children!=null)
            this.assignChildren(n.children);*/
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Node)) return false;
        Node o = (Node) obj;
        return o.nodeID == this.nodeID;
    }

    public void assign(Node input, Node parent){
        this.coordinate = input.coordinate;
        this.isContainer = input.isContainer;
        this.isAlerted = input.isAlerted;
        this.distanceArray = new int[input.distanceArray.length];
        System.arraycopy(input.distanceArray, 0, this.distanceArray, 0, input.distanceArray.length);
        this.nodeID = input.nodeID;

        // assign tree structure
        this.parent = parent;
        /*
            this.parent.assign(input.parent);
        if (input.children!=null)
            this.assignChildren(input.children);*/

    }

}
