package classes;

import enums.RoadType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.HashMap;

/* OSM
x: [52.691433 : 62.0079024]
y: [-20.071433 : 28.0741667]
*/

/**
 * The NewLoader class <More docs goes here>
 * @author Jakob Lautrup Nysom (jaln@itu.dk)
 * @version 15-May-2014
 */
public class NewLoader {
    
    public static Datafile krakdata = new Datafile("resources/new_krak_roads.txt", 812301, 
            "Loading new Krak roads", 
            new Rect(
                442254.35659f, 
                6049914.43018f, 
                892658.21706f - 442254.35659f, 
                6402050.98297f - 6049914.43018f)
    );
   
    /*public static Datafile osmdata = new Datafile("resources/new_osm_roads.txt", 
            1452532, "Loading new OSM roads...");
    
    public static Rect OSMBounds = new Rect(
            52.691433f, 
            -20.071433f,
            62.0079024f - 52.691433f, 
            28.0741667f - (-20.071433f)
    );*/
    //[x: 314455.05500143045 : 588445.5270339495] [y: 6086420.049592097 : 6231795.69263988]
    //'maxY': 7567986.8759869505, 'maxX': 1410980.5570572452, 'minY': 7312234.822616017, 'minX': 927436.897410232}
    public static Datafile osmtestfile = new Datafile("resources/converted_test_roads.txt", 
            1000, "Loading OSM test roads", 
            new Rect(
                927436.897410232f,
                7312234.822616017f,
                1410980.5570572452f - 927436.897410232f,
                7567986.8759869505f - 7312234.822616017f)
    );
    
    //[x: 271984.88412645145 : 415199.71181185555] [y: 921203.957443526 : 1401614.2412508868]
    public static Datafile osmtesttwo = new Datafile("resources/converted_test_roads.txt",
        1000, "Testing roads",
        new Rect(
            271984.88412645145f,
            921203.957443526f,
            415199.71181185555f - 271984.88412645145f,
            1401614.2412508868f - 921203.957443526f
        )
    );
    
    //[x: 150955.1544851401 : 695916.7463075386] [y: 5838350.401229404 : 6877073.128247903]
    //'minX': -2234341.7010513074, 'minY': 6892115.032515525, 'maxX': 3125201.9414894776, 'maxY': 8823248.457253834
    // Full(broken) datafile: 1452532
    public static Datafile osmdata = new Datafile("resources/osm_roads.txt",
        527924, "Loading OSM roads",
        new Rect(
            883750f,
            7235451f,
            1700000f - 883750f,
            7888838f - 7235451f
        )
    );
    
    public static HashMap<Long, Road.Node> loaded = new HashMap<>();
        
    public static final char sepchar = '@';
    public static Road loadRoad(String line) {
        // Split the road into metadata, nodes and drive times
        int firstSplit = line.indexOf(sepchar);
        int secondSplit = line.indexOf(sepchar, firstSplit+1);
        if ((firstSplit == -1) || (secondSplit == -1)) {
            throw new RuntimeException("Could not split at '"+sepchar+"' in the line:\n"+line);
        }
        String meta = line.substring(0, firstSplit);
        String nodestring = line.substring(firstSplit+1, secondSplit);
        String drivetimestring = line.substring(secondSplit+1);
                
        Utils.Tokenizer.setLine(meta);
        String      name        = Utils.Tokenizer.getString();
        RoadType    type        = RoadType.fromValue(Utils.Tokenizer.getInt());
        short       zip         = Utils.Tokenizer.getShort();
        short       speedLimit  = Utils.Tokenizer.getShort();
        boolean     oneway      = Utils.Tokenizer.getBool();
        
        Utils.Tokenizer.setLine(nodestring);
        ArrayList<Road.Node> nodes = new ArrayList<>();
        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float minY = minX;
        float maxY = maxX;
        while (Utils.Tokenizer.hasNext()) {
            long id = Utils.Tokenizer.getLong(); // Buffered node loading
            Road.Node node;
            if (!loaded.containsKey(id)) {
                float x = Utils.Tokenizer.getFloat();
                float y = Utils.Tokenizer.getFloat();
                node = new Road.Node(id, x, y);
                loaded.put(id, node);
            } else {
                Utils.Tokenizer.discard();
                Utils.Tokenizer.discard();
                node = loaded.get(id);
            }
            minX = (node.x < minX)? node.x: minX;
            maxX = (node.x > maxX)? node.x: maxX;
            minY = (node.y < minY)? node.y: minY;
            maxY = (node.y > maxY)? node.y: maxY;
            nodes.add(node);
        }
        Rect bounds = new Rect(minX, minY, maxX-minX, maxY-minY);
        
        float[] driveTimes;
        if (drivetimestring.length() != 0) {
            driveTimes = new float[nodes.size()-1];
            Utils.Tokenizer.setLine(drivetimestring);
            for (int i = 0; i < driveTimes.length; i++) {
                driveTimes[i] = Utils.Tokenizer.getFloat();
            }
        } else {
            driveTimes = new float[0];
        }
        
        return new Road(name, type, zip, speedLimit, oneway, 
                nodes.toArray(new Road.Node[nodes.size()]), driveTimes, bounds);
    }
    
    public static Model loadData(final Datafile file) {
        Rect bounds = new Rect(0,0,0,0);
        final Model model = new Model(file.bounds);
        
        ProgressBar progbar = new ProgressBar();
        progbar.setTarget(file.progressDescription, file.lines);
        
        model.startStream(progbar);
        try (InputStream stream = Utils.getFileStream(file.filename);
            InputStreamReader is = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(is)) {
            String line;
            while ((line = br.readLine()) != null) {
                //loadRoad(line);
                model.add(loadRoad(line));
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not load road data from '" + file.filename + "'");
        } catch (Utils.LoadFileException ex) {
            System.out.println("Could not load the file. Error: "+ex);
        }
        model.endStream();

        
        MemoryMXBean mxbean = ManagementFactory.getMemoryMXBean();
        System.out.printf("Heap memory usage: %d MB%n",
                mxbean.getHeapMemoryUsage().getUsed() / (1000000));
        progbar.close();
        return model;
    }
    
    public static void main(String[] args) {
        System.out.println("Loading model...");
        long t1 = System.nanoTime();
        Model model = loadData(krakdata);
        double delta = (System.nanoTime()-t1)/1e9;
        System.out.println("Loaded!");
        System.out.println("The loading took "+delta+" seconds!");
    }
    
}
