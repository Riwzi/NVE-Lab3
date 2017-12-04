/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disk;

import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import com.jme3.scene.Geometry;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.shape.Cylinder;
/**
 *  Abstract class for disks
 * 
 * @author Rickard
 * Implemented the class
 * 
 */
public abstract class Disk extends Node implements Comparable<Disk>{
    protected AssetManager assetManager;
    protected Vector2f velocity;
    protected Vector2f position;
    protected float radius;
    protected float mass;
    protected int id;
    protected int score;
    protected int lastUpdate;
    
    public Disk(AssetManager assetManager, float radius, int id) {
        this.assetManager = assetManager;
        this.velocity = new Vector2f();
        this.position = new Vector2f();
        this.radius = radius;
        this.mass = (float)(Math.PI) * radius * radius;
        this.id = id;
        this.score = 0;
        this.lastUpdate = 0;
    }
    
    public Geometry createGeometry(float radius, float height, ColorRGBA color) {
        Cylinder c = new Cylinder(20, 20, radius, height, true);
        Geometry disk = new Geometry("Disk", c);
        Material matCylinder = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matCylinder.setColor("Color", color);
        disk.setMaterial(matCylinder);
        
        return disk;
    }
    
    // Additional effects of being hit
    public abstract int reward(Disk otherDisk);
    
    public void addToScore(int score){
        this.score += score;
    }
    
    // Handle collision with the frame. If a collision is detected, calculate the time passed since collision and move the disk back
    // Then once the new velocity has been calculated, move the disk the remainder of the distance
    public boolean frameCollision(float minX, float maxX, float minY, float maxY, float tpf) {
        if (position.getX()-radius <= minX) {
            // We hit the west wall
            float distance = minX - (position.getX()-radius);
            float time_since_collision = timeSinceCollision(minX, false, tpf);
            
            move(velocity.mult(time_since_collision).negate());
            velocity = velocity.multLocal(new Vector2f(-1,1));
            move(velocity.mult(time_since_collision));
            return true;
        }
        if (position.getX()+radius >= maxX) {
            // We hit the east wall
            float distance = (position.getX()+radius) - maxX;
            float time_since_collision = timeSinceCollision(maxX, false, tpf);
            
            move(velocity.mult(time_since_collision).negate());
            velocity = velocity.multLocal(new Vector2f(-1,1));
            move(velocity.mult(time_since_collision));
            return true;
        }
        if (position.getY()-radius <= minY) {
            // We hit the south wall
            float distance = minY - (position.getY()-radius);
            float time_since_collision = timeSinceCollision(minY, true, tpf);
            
            move(velocity.mult(time_since_collision).negate());
            velocity = velocity.multLocal(new Vector2f(1,-1));
            move(velocity.mult(time_since_collision));
            return true;
        }
        if (position.getY()+radius >= maxY) {
            // We hit the north wall
            float distance = (position.getY()+radius) - maxY;
            float time_since_collision = timeSinceCollision(maxY, true, tpf);
            
            move(velocity.mult(time_since_collision).negate());
            velocity = velocity.multLocal(new Vector2f(1,-1));
            move(velocity.mult(time_since_collision));
            return true;
        } else {
            return false;
        }
    }
    
    public boolean diskCollision(Disk otherDisk, float tpf) {
        Vector2f v_1 = this.velocity;
        Vector2f p_1 = this.position;
        float r_1 = this.radius;
        float m_1 = this.mass;
        
        Vector2f v_2 = otherDisk.getVelocity();
        Vector2f p_2 = otherDisk.getPosition();
        float r_2 = otherDisk.getRadius();
        float m_2 = otherDisk.getMass();
        if (p_1.distance(p_2) <= r_1 + r_2) {
            // There was a collision between the disks
            // First calculate the exact point of collision and the time at which it occurred
            float time_since_collision = this.timeSinceDiskCollision(otherDisk, tpf);
            this.move(v_1.mult(time_since_collision).negate());
            otherDisk.move(v_2.mult(time_since_collision).negate()); //Negate the velocity vectors to move in the opposite direction
            p_1 = this.position;
            p_2 = otherDisk.getPosition();
            
            // 1. Unit normal and tangent
            Vector2f normal = p_1.subtract(p_2);
            Vector2f unit_normal = normal.normalize();
            Vector2f unit_tangent = new Vector2f(-unit_normal.getY(), unit_normal.getX());
            
            // 2. Not needed, already have the initial velocities
            
            // 3. Scalar normal velocities
            float v_1n = unit_normal.dot(v_1);
            float v_1t = unit_tangent.dot(v_1);
            float v_2n = unit_normal.dot(v_2);
            float v_2t = unit_tangent.dot(v_2);
            
            // 4. Scalar tangental velocities
            float s_1t_prime = v_1t;
            float s_2t_prime = v_2t;
            
            // 5. Normal velocities
            float s_1n_prime = (v_1n * (m_1 - m_2) + 2 * m_2 * v_2n) / (m_1 + m_2);
            float s_2n_prime = (v_2n * (m_2 - m_1) + 2 * m_1 * v_1n) / (m_1 + m_2);
            
            // 6. Scalars to vectors
            Vector2f v_1n_prime = unit_normal.mult(s_1n_prime);
            Vector2f v_1t_prime = unit_tangent.mult(s_1t_prime);
            Vector2f v_2n_prime = unit_normal.mult(s_2n_prime);
            Vector2f v_2t_prime = unit_tangent.mult(s_2t_prime);
            
            // 7. Final velocities
            Vector2f v_1_prime = v_1n_prime.add(v_1t_prime);
            Vector2f v_2_prime = v_2n_prime.add(v_2t_prime);
            
            this.move(v_1_prime.mult(time_since_collision));
            this.setVelocity(v_1_prime);
            
            otherDisk.move(v_2_prime.mult(time_since_collision));
            otherDisk.setVelocity(v_2_prime);
            
            this.addToScore(otherDisk.reward(this));
            otherDisk.addToScore(this.reward(otherDisk));
            return true;
        } else {
            return false;
        }
    }
    
    // Returns the time of the collision if the disk has traveled 'distance' since the collision
    public float timeSinceCollision(float distance) {
        float magnitude = this.velocity.length();
        return distance/magnitude;
    }
    
    //The boolean vertical is true if we hit the floor or roof of the frame
    public float timeSinceCollision(float frame_coord, boolean vertical, float tpf) {
        if (vertical) {
            Vector2f v_1 = this.velocity;
            Vector2f p_1 = this.position.subtract(v_1.mult(tpf)); //Move the disk back tpf time
            float p_1_y = p_1.getY();
            float v_1_y = v_1.getY();
            float r_1 = this.radius;
            
            float a = (float) (Math.pow((v_1_y),2));
            float b = 2f * ((p_1_y - frame_coord)*(v_1_y));
            float c = (float) (Math.pow(p_1_y - frame_coord,2) - Math.pow(r_1, 2));
            float det = (b*b - 4 * a * c);
            float t = (float) ((-b - Math.sqrt(det))/(2f*a));

            return tpf-t;
        } else {
            Vector2f v_1 = this.velocity;
            Vector2f p_1 = this.position.subtract(v_1.mult(tpf)); //Move the disk back tpf time
            float p_1_x = p_1.getX();
            float v_1_x = v_1.getX();
            float r_1 = this.radius;
            
            float a = (float) (Math.pow((v_1_x),2));
            float b = 2f * ((p_1_x - frame_coord)*(v_1_x));
            float c = (float) (Math.pow(p_1_x - frame_coord,2) - Math.pow(r_1, 2));
            float det = (b*b - 4 * a * c);
            float t = (float) ((-b - Math.sqrt(det))/(2f*a));

            return tpf-t;
        }
    }
    
    // Returns the time the two disks collided, this function assumes a collision has occurred
    public float timeSinceDiskCollision(Disk otherDisk, float tpf) {
        Vector2f v_1 = this.velocity;
        Vector2f p_1 = this.position.subtract(v_1.mult(tpf)); //Move the disks back tpf time
        float p_1_x = p_1.getX();
        float p_1_y = p_1.getY();
        float v_1_x = v_1.getX();
        float v_1_y = v_1.getY();
        float r_1 = this.radius;
        
        Vector2f v_2 = otherDisk.getVelocity();
        Vector2f p_2 = otherDisk.getPosition().subtract(v_1.mult(tpf));
        float p_2_x = p_2.getX();
        float p_2_y = p_2.getY();
        float v_2_x = v_2.getX();
        float v_2_y = v_2.getY();
        float r_2 = otherDisk.getRadius();
        
        float a = (float) (Math.pow((v_2_x - v_1_x),2) + Math.pow(v_2_y - v_1_y,2));
        float b = 2f * ((p_2_x - p_1_x)*(v_2_x-v_1_x) + (p_2_y - p_1_y)*(v_2_y-v_1_y));
        float c = (float) (Math.pow((p_1_x - p_2_x),2) + Math.pow(p_1_y - p_2_y,2) - Math.pow(r_1 + r_2, 2));
        float det = (b*b - 4 * a * c);
        float t = (float) ((-b - Math.sqrt(det))/(2f*a));
        
        return tpf-t;
    }
    
    public Vector2f getVelocity() {
        return this.velocity;
    }
    
    public void addVelocity(Vector2f velocity) {
        this.velocity = this.velocity.add(velocity);
    }
    
    public void setVelocity(Vector2f velocity) {
        this.velocity = velocity;
    }
    
    public Vector2f getPosition() {
        return this.position;
    }
    
    public float getRadius() {
        return this.radius;
    }
    
    public void move(Vector2f velocity) {
        super.move(velocity.getX(), velocity.getY(), 0); //move the node
        this.position = position.add(velocity); //move the internal position
    }
    
    public float getMass() {
        return this.mass;
    }
    
    public int getId() {
        return this.id;
    }
    
    public int getScore() {
        return this.score;
    }
    
    public void setScore(int score){
        this.score = score;
    }
    
    @Override
    public int compareTo(Disk comparePlayer) {
        return this.score - comparePlayer.getScore();
    }
    
    public void setPosition(Vector2f position){
        super.setLocalTranslation(position.getX(), position.getY(), -24f);
        this.position = position;
    }
}






