package classes;

import interfaces.IProgressBar;
import java.awt.*;
import javax.swing.*;

/**
 *
 * @author Isabella
 */
public class ProgressBar extends JFrame implements IProgressBar {

    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private final static double updateAmount = 0.01; 
    private long target;
    private int counter; // Current counter
    private int minAdd; // How much the counter needs to be before updating
    private long t1;

    public ProgressBar() {
        super();
       // System.out.println("Creating progress bar");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        statusLabel = new JLabel("No target set :)");
        progressBar = new JProgressBar(0, 0);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        JPanel panel = new JPanel(new BorderLayout(5,5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel labelPanel = new JPanel(new GridBagLayout());
        labelPanel.add(statusLabel);
        JPanel barPanel = new JPanel(new GridBagLayout());
        barPanel.add(progressBar);
        
        panel.add(labelPanel, BorderLayout.NORTH);
        panel.add(barPanel, BorderLayout.SOUTH);
        
        add(panel);
        //setPreferredSize(new Dimension(200, panel.getPreferredSize().height));
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        paintComponents(getGraphics());
       // System.out.println("Progress bar created!");
    }
    
    //Check whether or not the loading is complete
    public boolean done() {
        return progressBar.getPercentComplete() == 1;
    }
    
    /**
     * Sets a new target for the progress bar
     * @param text The text to display for the target
     * @param target The target amount
     */
    public void setTarget(String text, long target) {
        // System.out.println("Progress: '"+text+"' ("+target+")");
        t1 = System.nanoTime();
        minAdd = (int)Math.ceil(target*updateAmount);
        counter = 0;
        this.target = target;
        progressBar.setMaximum((int) target);
        progressBar.setValue(0);
        statusLabel.setText(text);
        pack();
        setLocationRelativeTo(null);
        //repaint();
        progressBar.update(progressBar.getGraphics());
    }

    /**
     * Updates the progress bar by the given amount
     * @param addition The amount to increase by
     */
    public void update(int addition) {
        counter += addition;
        if (!done() || addition==0) {
            if (counter == minAdd || (counter+progressBar.getValue() == target)) {
                progressBar.setValue(progressBar.getValue() + counter);
                counter = 0;
                //update(getGraphics()); // Too inconsistent :( (too 'slow' to show everything)
                //System.out.println("Updating progress, "+progressBar.getValue()+" / "+target);
            }
            if (done()) {
                double time = (System.nanoTime()-t1) / 1e9;
                // System.out.println("[Progress]: Finished '"+statusLabel.getText()+"' in "+time+"s!");
            }
        } else {
            throw new RuntimeException("Update ("+addition+") attempted while already finished!");
        }
    }

    //Closes the frame if done
    public void close() {
        dispose();
    }
}
