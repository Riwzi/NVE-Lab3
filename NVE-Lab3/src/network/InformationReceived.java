/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

/**
 *
 * @author Quentin
 */
public class InformationReceived {
    private int lastseq1, seq1;
    private int lastseq2, seq2;
    private int lastseq3, seq3;
    
    private float position;
    private float velocity;
    private int score;
    
    public InformationReceived(){
        seq1 = 0;
        seq2 = 0;
        seq3 = 0;
        lastseq1 = 0;
        lastseq2 = 0;
        lastseq3 = 0;
        
        position = 0;
        velocity = 0;
        score = 0;
    }
    
    
    public boolean updatePosition(){
        if(seq1 > lastseq1){
            return true;
        }
        return false;
    }
    
    public float getPosition(){
        seq1 = lastseq1;
        return position;
    }
    
    public void setPosition(int seqNb, float position){
        this.position = position;
        seq1 = seqNb;
    }
    
    
    public boolean updateVelocity(){
        if(seq2 > lastseq2){
            return true;
        }
        return false;
    }
    
    public float getVelocity(){
        seq2 = lastseq2;
        return position;
    }
    
    public void setVelocity(int seqNb, float velocity){
        this.velocity = velocity;
        seq2 = seqNb;
    }
    
}
