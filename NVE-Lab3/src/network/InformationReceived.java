/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import com.jme3.math.Vector2f;

/**
 *
 * @author Quentin
 */
public class InformationReceived {
    private int lastseq1, seq1;
    private int lastseq2, seq2;
    private int lastseq3, seq3;
    private float time;
    
    private Vector2f position;
    private Vector2f velocity;
    private int score;
    private boolean isTime;
    
    public InformationReceived(){
        seq1 = 0;
        seq2 = 0;
        seq3 = 0;
        lastseq1 = 0;
        lastseq2 = 0;
        lastseq3 = 0;
        
        position = new Vector2f();
        velocity = new Vector2f();
        score = 0;
        time = 0;
    }
    
    
    public boolean updatePosition(){
        if(seq1 > lastseq1){
            return true;
        }
        return false;
    }
    
    public Vector2f getPosition(){
        lastseq1 = seq1;
        return position;
    }
    
    public void setPosition(int seqNb, Vector2f position){
        if(seqNb > seq1){
            this.position = position;
            seq1 = seqNb;
        }
 
    }
    
    
    public boolean updateVelocity(){
        if(seq2 > lastseq2){
            return true;
        }
        return false;
    }
    
    public Vector2f getVelocity(){
        lastseq2 = seq2;
        return velocity;
    }
    
    public void setVelocity(int seqNb, Vector2f velocity){
        if(seqNb > seq2){
            this.velocity = velocity;
            seq2 = seqNb;
        }
    }
    
    
    public boolean updateScore(){
        if(seq3 > lastseq3){
            return true;
        }
        return false;
    }
    
    public int getScore(){
        lastseq3 = seq3;
        return score;
    }
    
    public void setScore(int seqNb, int score){
        if(seqNb > seq3){
            this.score = score;
            seq3 = seqNb;
        }
    }
    
    
    public boolean updateTime(){
        if(seq1 > lastseq1){
            return true;
        }
        return false;
    }
    
    public float getTime(){
        lastseq1 = seq1;
        return time;
    }
    
    public void setTime(int seqNb, float time){
        if(seqNb > seq1){
            this.time = time;
            seq1 = seqNb;
        }
    }
    
    public int getNbId(){
        return seq2;
    }
    
}
