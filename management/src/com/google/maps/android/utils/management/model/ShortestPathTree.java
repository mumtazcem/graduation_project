package com.google.maps.android.utils.management.model;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ShortestPathTree{
    public List<Node> nodeTree;
    public int dist[];

    public void copyS(List<Node> input){
        this.nodeTree = new ArrayList<Node>(input.size());
        int i = 0;
        for (Iterator<Node> iter = input.iterator(); iter.hasNext();){
            Node element = iter.next();
            this.nodeTree.add(element);
            i++;
        }
    }

    public ShortestPathTree() {
    }

    public ShortestPathTree(List<Node> s, int[] dist) {
        // old copy method --> this.S = new ArrayList<>(s);
        this.copyS(s);
        this.dist = new int[dist.length];
        System.arraycopy(dist, 0, this.dist, 0, dist.length);
    }

    public ShortestPathTree(ShortestPathTree shortestPathTree) {
        // old copy method --> this.S = new ArrayList<>(shortestPathTree.S);
        this.copyS(shortestPathTree.nodeTree);
        this.dist = new int[shortestPathTree.dist.length];
        System.arraycopy(shortestPathTree.dist, 0, this.dist, 0, shortestPathTree.dist.length);
    }

    public void printPath(Node src, Node dst, List<LatLng> toEncode){
        // recursive function
        if (dst.nodeID == -1){
            Log.i("MainPoly", "ERROR next recursive call will be null");
            return;
        }
        if (dst.nodeID==src.nodeID) {
            //toEncode.add(dst.coordinate);
            return;
        }
        //Log.e("printPath", String.valueOf(dst.nodeID));
        toEncode.add(dst.coordinate);
        if (toEncode.size()>20){
            Log.i("MainPoly", "Possible error, too many recursive calls");
            return;
        }
        if (dst == null){
            Log.i("MainPoly", " ERROR : dst is null, it should not be");
            return;
        }
        printPath(src,dst.parent, toEncode);
        Log.i("MainPoly", "dst.parent is :" + String.valueOf(dst.parent.nodeID));

    }
}
