package experiments;

import classes.Line;
import classes.Rect;
import classes.Utils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * The OptimizedView class <More docs goes here>
 * @author Jakob Lautrup Nysom (jaln@itu.dk)
 * @version 10-Mar-2014
 */
public class OptimizedView extends JPanel  {
    private BufferedImage image;
    private BufferedImage scaleSource;
    public static final Color clearColor = Color.WHITE;
    GraphicsConfiguration gfx_config = GraphicsEnvironment.
		getLocalGraphicsEnvironment().getDefaultScreenDevice().
		getDefaultConfiguration(); // Voodoo
    Rect markerRect = null;
    public final static double wperh = 450403.8604700001 / 352136.5527900001; // map ratio
    
    /**
     * Constructor for the OptimizedView class
     * @param dimension
     */
    public OptimizedView (Dimension dimension) {
        setPreferredSize(dimension);
        setFocusTraversalKeysEnabled(false);
        setFocusable(true);
    }
    
    /**
     * Fills the given image with the set clear color
     * @param img
     */
    public void clear(BufferedImage img) {
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(clearColor);
        g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
    }
    
    /**
     * Moves the current image based on the offset, then draws the given array
     * of lines. Used for panning.
     * @param x The Eastward offset 
     * @param y The Nortward offset 
     * @param newLines The new lines to patch up 
     */
    public void offsetImage(int x, int y, Line[] newLines) { // Takes roughly 0.0016 secs at worst
        long t1 = System.nanoTime();
        BufferedImage newImage = gfx_config.createCompatibleImage(getWidth(), getHeight());
        clear(newImage); // Clear the whole image
        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(image, x, -y, this); // Draw the old image offset
        long t2 = System.nanoTime();
        for (Line line : newLines) {
            g2d.setColor(line.color);
            g2d.drawLine(line.x1, line.y1, line.x2, line.y2);
        }
        g2d.dispose();
        System.out.println("Offsetting by "+x+", "+y+" ("+newLines.length+" lines)");
        long t3 = System.nanoTime();
        double nFac = 1000000000.0;
        double stampTime = (t2-t1)/nFac;
        double drawTime = (t3-t2)/nFac;
        double total = (t3-t1)/nFac;
        System.out.println("Offsetting took "+total+" secs (image: "+stampTime+" secs, lines: "+drawTime+" secs)");
        image = newImage;
        repaint();
    }
    
    /**
     * Creates a scaling source for the view from the given lines
     * @param lines The lines to draw on the scale source
     * @param dim How big the source should be in pixels
     */
    public void createScaleSource(Line[] lines, Dimension dim) {
        System.out.println("Creating a scale source with the size "+dim+" from "+lines.length+" lines");
        scaleSource = createImage(lines, dim);
    }
    
    /**
     * Resizes the map somewhat naïvely
     * @param newSize 
     */
    public void resizeMap(Dimension newSize) {
        if (scaleSource != null) {
            // Calculate the dimensions of the new image
            Dimension size = Utils.convertDimension(newSize);
            System.out.println("Resizing to "+size);
            int width = size.width; int height = size.height;
            Image scaledImage = scaleSource.getScaledInstance(width, height, Image.SCALE_FAST);
            BufferedImage imageBuff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = imageBuff.createGraphics();
            g.drawImage(scaledImage, 0, 0, clearColor, null);
            image = imageBuff;
            g.dispose();
        }
    }
    
    /**
     * Creates a buffered image from the given array of lines
     * @param lineArr The lines to draw
     * @return A buffered image containing the drawn lines
     */
    private BufferedImage createImage(Line[] lineArr, Dimension dim) {
        BufferedImage img = gfx_config.createCompatibleImage(dim.width, dim.height);
        clear(img);
        Graphics2D g2d = img.createGraphics();
        for (Line line : lineArr) {
            g2d.setColor(line.color);
            g2d.drawLine(line.x1, line.y1, line.x2, line.y2);
        }
        g2d.dispose();
        return img;
    }
    
    public void renewImage(Line[] lineArr) {
        image = createImage(lineArr, getSize());
        repaint();
    }
    
    /**
     * Set the marker rect to be drawn
     * @param rect The rect
     */
    public void setMarkerRect(Rect rect) {
        markerRect = rect;
    }
    
    /**
     * What happens at the default render (on resize etc.)
     * @param g 
     */
    @Override
    public void paintComponent(Graphics g) {
        if (image != null) {
            long t1 = System.nanoTime();
            g.setColor(clearColor);
            g.fillRect(0,0,getWidth(),getHeight());
            g.drawImage(image, 0, 0, this);
            double delay = (System.nanoTime()-t1)/1000000000.0;
            System.out.println("Drawing the Optimized View took "+delay+" secs");
            if (markerRect != null) { // Draw the rect used for marking 
                BasicStroke str = new BasicStroke(2, BasicStroke.CAP_BUTT, 
                        BasicStroke.JOIN_BEVEL, 0, new float[] {3,2}, 0);
                Graphics2D g2d = (Graphics2D)g;
                g2d.setColor(new Color(200,200,255,90));
                g2d.fillRect((int)Math.round(markerRect.x), (int)Math.round(markerRect.y-markerRect.height), 
                        (int)Math.round(markerRect.width), (int)Math.round(markerRect.height));
                g2d.setColor(Color.BLUE);
                g2d.setStroke(str);
                g2d.drawRect((int)Math.round(markerRect.x), (int)Math.round(markerRect.y-markerRect.height), 
                        (int)Math.round(markerRect.width), (int)Math.round(markerRect.height));
            }
            
        } else {
            System.out.println("No image set yet, so nothing to draw...");
        }
    } 
}
