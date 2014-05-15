/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

/**
 *
 * @author Alekxander
 * @author Jakob
 */
public class QuadTree extends Quad {
    boolean frozen = false;
    
    public QuadTree(Rect area, short maxNodes, short maxDepth) {
        super(area, maxNodes, maxDepth, (short)1);
    }
    
    /**
     * Assure that the tree is frozen :)
     */
    private void assureFrozen() {
        if (!frozen) { 
            freeze();
        }
    }
    
    @Override
    public void freeze() {
        System.out.println("Freezing the Quad Tree :)");
        super.freeze();
        frozen = true;
    }
    
    public RoadPart[] getIn(Rect rect) {
        assureFrozen();
        long t1 = System.nanoTime();
        FastArList<RoadPart> result = new FastArList<>();
        super.getIn(rect, result);
        RoadPart[] resArr = result.toArray(new RoadPart[result.size()]);
        double s = (System.nanoTime()-t1)/1e9;
        //System.out.println("'getIn' returned "+result.size()+" roads from the QuadTree in "+s+"sec");
        return resArr;
    }
}
