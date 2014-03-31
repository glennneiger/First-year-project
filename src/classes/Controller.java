package classes;

import enums.RoadType;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 * The Controller class <More docs goes here>
 * @author Jakob Lautrup Nysom (jaln@itu.dk)
 * @version 10-Mar-2014
 */
public class Controller extends JFrame {
    private final OptimizedView view;
    private final Model model;
    private final CMouseHandler mouseHandler;
    private final CKeyHandler keyHandler;
    private final CResizeHandler resizeHandler;
    public final double wperh = 450403.8604700001 / 352136.5527900001; // map ratio
    private ArrayList<RoadType> prioritized;
    
    // Dynamic fields
    private Rect activeRect;
    private RenderInstructions ins;
    
    /**
     * Constructor for the TestController class
     * @param view The view to manage
     * @param model The model to manage
     */
    public Controller(OptimizedView view, Model model) {
        super();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.model = model;
        activeRect = model.getBoundingArea();
        this.ins = Model.defaultInstructions;
        
        prioritized = new ArrayList<>();
        prioritized.add(RoadType.Highway);
        prioritized.add(RoadType.HighwayExit);
        prioritized.add(RoadType.PrimeRoute);
        
        this.view = view;
        
        // Set the insets / padding of the window
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
        // Padding for the view (and unobstructing borders)
        JPanel viewPanel = new JPanel(new GridLayout(1,1));
        Border padding = BorderFactory.createEmptyBorder(10,0,10,10);
        Border bevel = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
        viewPanel.setBorder(BorderFactory.createCompoundBorder(padding, bevel));
        viewPanel.add(view);
        
        // Key handling
        setFocusTraversalKeysEnabled(false);
        setFocusable(true);
        keyHandler = new CKeyHandler(this, view);
        mouseHandler = new CMouseHandler(this, view);
        resizeHandler = new CResizeHandler(this, view);
        
        contentPanel.add(new RenderPanel(ins, this), BorderLayout.NORTH);
        contentPanel.add(new ZoomButtonsGUI(resizeHandler, this), BorderLayout.EAST);
        contentPanel.add(viewPanel);
        contentPanel.add(new FindRoadPanel(this, view), BorderLayout.SOUTH);
        
        setTitle("First-year Project - Visualization of Denmark");
        
        // Pack the window
        this.setContentPane(contentPanel);
    }
    
    /**
     * Returns the lines of the given area mapped to the target
     * @param area The area to get roads from
     * @param target The target to map them to as lines
     * @return A list of lines to render the area
     */
    public Line[] getLines(Rect area, Rect target) {
        return model.getLines(area, target, view.getHeight(), ins, prioritized);
    }
    
    /**
     * Returns the controller's active rect
     * @return the controller's active rect
     */
    public Rect getActiveRect() {
        return activeRect;
    }
    
    /**
     * Sets the marker rect of the view to the given rect
     * @param markerRect The rect to mark
     */
    public void setMarkerRect(Rect markerRect) {
        view.setMarkerRect(markerRect);
    }
    
    /**
     * Refreshes the view
     */
    public void refresh() {
        view.repaint();
    }
    
    /**
     * Sets the active rect of the controller
     * @param rect The new active rect of the controller
     */
    public void setActiveRect(Rect rect) {
        activeRect = rect;
    }
    
    public RoadPart[] getRoads(Rect area){
        return model.getRoads(area, ins);
    }
    
    /**
     * Returns the size of the controller's view
     * @return the size of the controller's view
     */
    public Dimension getViewSize() {
        return view.getSize();
    }
    
    public void resizeActiveArea(Dimension dim) {
        double height = activeRect.height;
        double width = (dim.width/(double)dim.height) * height;
        Rect newArea = new Rect(activeRect.x, activeRect.y, width, height);
        System.out.println("Resizing active area from "+activeRect+" to "+newArea);
        activeRect = newArea;
    }
    
    public void resetView() {
        setActiveRect(model.getBoundingArea());
        redraw();
    }
    
    /**
     * Zooms the view out
     */
    public void zoomOut() {
        System.out.println("Zoomin' out!");
        resizeHandler.zoomOut();
    }
    
    /**
     * Zoom the view in
     */
    public void zoomIn() {
        System.out.println("Zoomin' in!");
        resizeHandler.zoomIn();
    }
    
    /**
     * Tells the model to redraw based on the activeRect
     */
    public void redraw() {
        long t1 = System.nanoTime();
        
        // Change the active Rect so that it fits the screen
        resizeActiveArea(view.getSize());
        resizeHandler.setLastRect(activeRect);
        
        System.out.println("Preparing the image...");
        view.renewImage(model.getLines(activeRect, new Rect(0, 0, 
                view.getWidth(), view.getHeight()), ins, prioritized));
        System.out.println("Finished! ("+(System.nanoTime()-t1)/1000000000.0+" sec)");
    }
    
    /**
     * Entry point
     * @param args 
     */
    public static void main(String[] args) throws InterruptedException {
        ProgressBar progbar = new ProgressBar(); // Create the progress bar
        OptimizedView view = new OptimizedView(new Dimension(600,400));
        
        // Load everything with the optional progressbar on :U
        Intersection[] intersections = Loader.loadIntersections("resources/intersections.txt", progbar);
        if (intersections.length == 0) {
            Thread.sleep(4000); // Show the message for 4 seconds
            progbar.close();
            return;
        }    
        
        RoadPart[] roads = Loader.loadRoads("resources/roads.txt", progbar);
        if (roads.length == 0) {
            Thread.sleep(4000); // Show the message for 4 seconds
            progbar.close();
            return;
        }
                
        Model model = new Model(intersections, roads, progbar);
        
        Controller controller = new Controller(view, model); 
        controller.setMinimumSize(new Dimension(600,500));
        controller.pack();
        controller.redraw();
        progbar.close();
        controller.setVisible(true);
    }
}
