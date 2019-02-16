package com.google.maps.android.utils.management.model;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Graph{
    private static final String TAG = "Graph";
    public List<Node> nodeList, alertedContainerList, neighbourVertices, Q, S;
    public int numOfVertices;

    public Graph(List<Node> nodeList, int numOfVertices) {
        this.nodeList = new ArrayList<Node>(nodeList);
        this.numOfVertices = numOfVertices;
        this.alertedContainerList = new ArrayList<Node>();
        this.neighbourVertices = new ArrayList<Node>();
        this.Q = null;
        this.S = new ArrayList<Node>();
    }

    // copy method of Q
    public void copyQ(List<Node> input){
        this.Q = new ArrayList<Node>(input.size());
        for (Iterator<Node> iter = input.iterator(); iter.hasNext();){
            Node element = iter.next();
            this.Q.add(element);
        }
    }

    public Node minimumDistance(List<Node> Q, int dist[], List<Node> S) {

        int min = Integer.MAX_VALUE, index;
        Node to_return = new Node();
        for (Iterator<Node> iter = Q.iterator(); iter.hasNext();){
            Node element = iter.next();
            index = element.nodeID;

            if (!S.contains(element) && dist[index] <= min) {
                min = dist[index];
                to_return.assign(element, element.parent);
                // assign tree structure if needed
                /*if (to_return.parent!=null)
                    to_return.parent.assign(to_return.parent);
                if (to_return.children!=null)
                    to_return.assignChildren(to_return.children);*/
            }
        }
        return to_return;
    }
    public ShortestPathTree shortestPath(Node src){
        // initialization
        if (neighbourVertices != null)
            if (!neighbourVertices.isEmpty()){
                neighbourVertices.clear();
                Log.d(TAG, "n.vertices cleared");
            }

        if (Q != null)
            if (!Q.isEmpty()){
                Q.clear();
                Log.d(TAG, "Q cleared");
            }

        if (S != null)
            if (!S.isEmpty()){
                S.clear();
                Log.d(TAG, "S cleared");
            }
        Node u = new Node();
        int[] dist = new int[src.distanceArray.length];
        System.arraycopy(src.distanceArray, 0, dist, 0, src.distanceArray.length);
        int index;

        dist[src.nodeID] = 0; // distance to itself is zero

        for (int i=0; i<numOfVertices; i++){
            if (i!=src.nodeID){
                dist[i]=Integer.MAX_VALUE;
            }
        }


        this.copyQ(nodeList); // Q <- V, nodelist is copied to Q
        // old copy method --> Q = new ArrayList<Node>(nodeList);

        // initialization ends
        while (!Q.isEmpty()){
            u = minimumDistance(Q, dist, S);
            S.add(u);
            Q.remove(u);

            // find neighbours, create neighbourVertices
            neighbourVertices = findNeighbourVertices(u, src);

            for (Iterator<Node> iter = neighbourVertices.iterator(); iter.hasNext();){
                Node element = iter.next();
                index = element.nodeID;
                // dist[i] > dist[u] + w(u,v)
                if (dist[index] > dist[u.nodeID] + u.distanceArray[index]){
                    dist[index] = dist[u.nodeID] + u.distanceArray[index];
                    Log.d(TAG, "new shortest path"+ String.valueOf(dist[index]));
                    // new shortest path
                    if (nodeList.get(u.nodeID).parent.parent != null){ // -1 or -5
                        if (nodeList.get(u.nodeID).parent.parent.nodeID != nodeList.get(u.nodeID).nodeID)
                            // TODO End nodes' parent assignments are problematic such as: 9-10, 16-17, 19-20, 18-19
                            nodeList.get(index).parent.assign(nodeList.get(u.nodeID), nodeList.get(u.nodeID).parent);
                    }
                    else{
                        nodeList.get(index).parent.assign(nodeList.get(u.nodeID), nodeList.get(u.nodeID).parent);
                    }
                }
            }
            neighbourVertices.clear();
        }
        ShortestPathTree to_return = new ShortestPathTree(nodeList, dist);
        return to_return;
        // or you can return list S
        // since it includes all the nodes
        // that are visited along the way.
    }

    public List<Node> findNeighbourVertices(Node minDist, Node source){
        List<Node> neighbourVertices = new ArrayList<Node>();
        for (int i=0; i<numOfVertices; i++) {
            if (minDist.distanceArray[i]!=0 &&
                    nodeList.get(i).nodeID != source.nodeID){
                // means that there is vertice and the neighbour
                // is not source node
                neighbourVertices.add(nodeList.get(i));
                //neighbourVertices.add(minDist); // to avoid wrong edges
            }
        }
        return neighbourVertices;
    }

    public List<LatLng> createWholeMap(){
        List<String> edges = new ArrayList<>();
        List<LatLng> wholeMap = new ArrayList<>();

        // it is not important, just empty node
        // no need to exclude source node from neighbour vertices
        //Node empty = new Node();

        // find all edges
        for (Iterator<Node> iter = nodeList.iterator(); iter.hasNext();) {
            Node element = iter.next();
            List<Node> neighbours = findNeighbourVertices(element, element);

            for (int i = 0; i < neighbours.size(); i++){
                List<LatLng> path = new ArrayList<LatLng>();
                path.add(element.coordinate);
                path.add(neighbours.get(i).coordinate);
                edges.add(PolyUtil.encode(path));
            }
        }
        for (Iterator<String> iter = edges.iterator(); iter.hasNext();) {
            String element = iter.next();
            List<LatLng> sequence = PolyUtil.decode(element);
            wholeMap.addAll(sequence);
        }

        return wholeMap;
    }

    public Node findNode(LatLng input){
        Node notFound = new Node();
        double lat, lnt;
        for (Iterator<Node> iter = nodeList.iterator(); iter.hasNext();) {
            Node element = iter.next();
            lat = element.coordinate.latitude - input.latitude;
            lnt = element.coordinate.longitude - input.longitude;
            if ((lat<0.0001 && lat>-0.0001) && (lnt<0.0001 && lnt>-0.0001))
                return element;
        }
        return notFound;
    }
}
