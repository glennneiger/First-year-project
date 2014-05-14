/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import javax.swing.JMenuItem;

/**
 *
 * @author Isabella
 */
public class MenuItem extends JMenuItem {

    public final RoadPart roadPart;

    public MenuItem(RoadPart r, String text) {
        super(text);
        roadPart = r;
    }
}
