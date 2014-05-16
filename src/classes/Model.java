/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import enums.RoadType;
import interfaces.IProgressBar;
import interfaces.StreamedContainer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Model is the model handling the tree. It used to handle the retrieval of lines.
 * @author Jakob
 */

public class Model implements StreamedContainer<Road> {
    public final Rect bounds;
    private HashMap<RoadType, QuadTree> trees = new HashMap<>();
    public ArrayList<RoadType> priorities;
    
    /* Krak boundaries
    x = [442254.35659 : 892658.21706]
    y = [6049914.43018 : 6402050.98297]
    */
    public Model(Rect boundingBox) {
        bounds = boundingBox;
        priorities = new ArrayList<>();
        priorities.add(RoadType.Other);
        priorities.add(RoadType.Path);
        priorities.add(RoadType.Ferry);
        priorities.add(RoadType.PrimeRoute);
        priorities.add(RoadType.HighwayExit);
        priorities.add(RoadType.Highway);
    }

    /**
     * Streams the road of the given projection to a target
     * @param target Where to stream the roads
     * @param p The projection to use as the source
     */
    public void getRoads(StreamedContainer<Road> target, Viewport.Projection p) {
        if (p.equals(Viewport.Projection.Empty)) { 
            target.startStream();
            target.endStream();
        } else {
            for (RoadType type : priorities) {
                trees.get(type).getIn(p.source, target);
            }
        }
    }

    /**
     * Streams all roads in the model to the target
     * @param target The target to stream roads to
     */
    public void getAllRoads(StreamedContainer<Road> target) {
        for (RoadType type : priorities) {
            trees.get(type).getIn(bounds, target);
        }
    }

    @Override
    public void startStream() {
        // Find the bounding area of the intersections
        System.out.println("Populating the Quad Tree...");
        for (RoadType type : RoadType.values()) {
            trees.put(type, new QuadTree(bounds, (short)400, (short)30));
        }
    }

    @Override
    public void endStream() {
        System.out.println("Finished populating the Quad Tree!");
    }

    @Override
    public void startStream(IProgressBar bar) {
        throw new UnsupportedOperationException("Progress bar unsupported");
    }

    @Override
    public void add(Road obj) {
        trees.get(obj.type).add(obj);
    }

}
