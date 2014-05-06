package classes;

import java.awt.Dimension;

/**
 * The Rect class <More docs goes here>
 * @author Jakob Lautrup Nysom (jaln@itu.dk)
 * @version 10-Mar-2014
 */
public class Rect {
    
    public final double x;
    public final double y;
    public final double width;
    public final double height;
    public final double left;
    public final double right;
    public final double top;
    public final double bottom;
    
    /**
     * Constructor for the Rect class
     * @param x The position of the rectangle on the x-axis
     * @param y The position of the rectangle on the y-axis
     * @param width The width of the rectangle
     * @param height The height of the rectangle
     */
    public Rect (double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.left = x;
        this.right = x+width;
        this.top = y+height;
        this.bottom = y;
    }
    
    public Rect(Dimension dim) {
        this(0, 0, dim.width, dim.height);
    }
    
    /**
     * Returns whether this rectangle collides with another rectangle
     * @param other The other rectangle
     * @return Whether the rectangles overlap
     */
    public boolean collidesWith(Rect other) {
        return this.right > other.left && this.left < other.right && this.top > other.bottom && this.bottom < other.top;
    }
    
    /**
     * Checks whether the other rect is fully contained by this rect
     * @param other The other rect
     * @return whether the other rect is fully contained by this rect
     */
    public boolean contains(Rect other) {
        return !((other.top > this.top)||(other.bottom < this.bottom)||(other.left < this.left)||(other.right > this.right));
    }
    
    @Override
    public String toString() {
        return "Rect("+x+", "+y+", "+width+", "+height+")";
    }
    
    /**
     * Shifts the rect by the given amounts
     * @param x
     * @param y
     * @return A new rect shifted by the given amount
     */
    public Rect shift(double x, double y) {
        return new Rect(this.x+x, this.y+y, this.width, this.height);
    }
    
    /**
     * Returns the rect scaled by a given positive factor
     * @param factor The factor to scale by
     * @return A new rect scaled by the factor.
     */
    public Rect getScaled(double factor) {
        if (factor < 0) {
            throw new RuntimeException("The scale factor may not be negative! ("+factor+")");
        }
        double rw = width * factor;
        double rh = height * factor;
        double hdw = (width - rw) / 2; // half delta width
        double hdh = (height - rh) / 2; // ~ height
        double rx = x + hdw;
        double ry = y + hdh;
        return new Rect(rx, ry, rw, rh);
    }
    
    /**
     * Returns the rect moved to the new position
     * @param x
     * @param y
     * @return 
     */
    public Rect shiftTo(double x, double y) {
        return new Rect(x, y, width, height);
    }
}
