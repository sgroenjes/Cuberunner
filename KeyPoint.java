/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.math.Vector3f;

/**
 *
 * @author bricks
 */
class KeyPoint {
    
    Vector3f location;
    
    boolean hit = false;

    KeyPoint(float x, float y, float z) {
       location = new Vector3f(x, y, z);
       
    }
    
}
