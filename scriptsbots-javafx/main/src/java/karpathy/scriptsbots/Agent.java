package karpathy.scriptsbots;

import karpathy.scriptsbots.vmath.Vector2f;

import static karpathy.scriptsbots.Settings.*;
import static karpathy.scriptsbots.Helpers.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Agent {

    public final Vector2f pos;

    public float health; //in [0,2]. I cant remember why.
    public float angle; //of the bot

    public float red;
    public float gre;
    public float blu;

    public float w1; //wheel speeds
    public float w2;
    public boolean boost; //is this agent boosting

    public float spikeLength;
    public int age;

    public boolean spiked;

    public float[] in; //input: 2 eyes, sensors for R,G,B,proximity each, then Sound, Smell, Health
    public float[] out; //output: Left, Right, R, G, B, SPIKE

    public float repcounter; //when repcounter gets to 0, this bot reproduces
    public int gencount; //generation counter
    public boolean hybrid; //is this agent result of crossover?
    public float clockf1, clockf2; //the frequencies of the two clocks of this bot
    public float soundmul; //sound multiplier of this bot. It can scream, or be very sneaky. This is actually always set to output 8

    //variables for drawing purposes
    public float indicator;
    public float ir;
    public int selectflag; //is this agent selected?
    public float dfood; //what is change in health of this agent due to giving/receiving?
    public float give;    //is this agent attempting to give food to other agent?
    public int id;
    //inhereted stuff
    public float herbivore; //is this agent a herbivore? between 0 and 1
    public float MUTRATE1; //how often do mutations occur?
    public float MUTRATE2; //how significant are they?
    public float temperature_preference; //what temperature does this agent like? [0 to 1]
    public float smellmod;
    public float soundmod;
    public float hearmod;
    public float eyesensmod;
    public float bloodmod;
    public float[] eyefov; //field of view for each eye
    public float[] eyedir; //direction of each eye
    //will store the mutations that this agent has from its parent
    //can be used to tune the mutation rate
    public ArrayList<String> mutations;
    public float ig;

    // public DWRAONBrain brain; //THE BRAIN!!!!
    public AssemblyBrain brain;
    // public MLPBrain brain;
    public  float ib; //indicator colors

    public Agent() {
        pos= new Vector2f(randf(0,WIDTH),randf(0,HEIGHT));
        angle= randf(-(float)Math.PI,(float)Math.PI);
        health= 1.0f + randf(0,0.1f);
        age=0;
        spikeLength=0;
        red= 0;
        gre= 0;
        blu= 0;
        w1=0;
        w2=0;
        soundmul=1;
        give=0;
        clockf1= randf(5,100);
        clockf2= randf(5,100);
        boost=false;
        indicator=0;
        gencount=0;
        selectflag=0;
        ir=0;
        ig=0;
        ib=0;
        temperature_preference=randf(0,1);
        hybrid= false;
        herbivore= randf(0,1);
        repcounter= herbivore*randf(REPRATEH-0.1f,REPRATEH+0.1f) + (1-herbivore)*randf(REPRATEC-0.1f,REPRATEC+0.1f);

        id=0;

        smellmod= randf(0.1f, 0.5f);
        soundmod= randf(0.2f, 0.6f);
        hearmod= randf(0.7f, 1.3f);
        eyesensmod= randf(1, 3);
        bloodmod= randf(1, 3);

        MUTRATE1= randf(0.001f, 0.005f);
        MUTRATE2= randf(0.03f, 0.07f);

        spiked= false;

        in = new float[INPUTSIZE];
        out = new float[OUTPUTSIZE];

        eyefov = new float[NUMEYES];
        eyedir = new float[NUMEYES];

        for(int i=0;i<NUMEYES;i++) {
            eyefov[i] = randf(0.5f, 2);
            eyedir[i] = randf(0, (float)(2*Math.PI));
        }

        brain = new AssemblyBrain();
    }

    public void printSelf() {
        System.out.println("Agent age=" + age + "\n");
        for(String mutation : mutations) {
            System.out.print(mutation);
        }
    }

    //for drawing purposes
    public void initEvent(float size, float r, float g, float b) {
        indicator=size;
        ir=r;
        ig=g;
        ib=b;
    }

    public void tick() {
        brain.tick(in, out);
    }

    public  Agent reproduce(float MR, float MR2) {
        Agent a2 = new Agent();

        //spawn the baby somewhere closeby behind agent
        //we want to spawn behind so that agents dont accidentally eat their young right away
        a2.pos.set(BOTRADIUS,0);
        a2.pos.rotateAroundOrigin(-a2.angle, true);
        a2.pos.addLocal(this.pos).addLocal(randf(-BOTRADIUS*2,BOTRADIUS*2), randf(-BOTRADIUS*2,BOTRADIUS*2));
        if (a2.pos.x<0) a2.pos.x= WIDTH+a2.pos.x;
        if (a2.pos.x>= WIDTH) a2.pos.x= a2.pos.x- WIDTH;
        if (a2.pos.y<0) a2.pos.y=  HEIGHT+a2.pos.y;
        if (a2.pos.y>= HEIGHT) a2.pos.y= a2.pos.y- HEIGHT;

        a2.gencount= this.gencount+1;
        a2.repcounter= a2.herbivore*randf( REPRATEH-0.1f, REPRATEH+0.1f) + (1-a2.herbivore)*randf(REPRATEC-0.1f,REPRATEC+0.1f);

        //noisy attribute passing
        a2.MUTRATE1= this.MUTRATE1;
        a2.MUTRATE2= this.MUTRATE2;
        if (randf(0,1)<0.1) a2.MUTRATE1= (float)randn(this.MUTRATE1, METAMUTRATE1);
        if (randf(0,1)<0.1) a2.MUTRATE2= (float)randn(this.MUTRATE2, METAMUTRATE2);
        if (this.MUTRATE1<0.001) this.MUTRATE1= 0.001f;
        if (this.MUTRATE2<0.02) this.MUTRATE2= 0.02f;
        a2.herbivore= cap((float)randn(this.herbivore, 0.03));
        if (randf(0,1)<MR*5) a2.clockf1= (float)randn(a2.clockf1, MR2);
        if (a2.clockf1<2) a2.clockf1= 2;
        if (randf(0,1)<MR*5) a2.clockf2= (float)randn(a2.clockf2, MR2);
        if (a2.clockf2<2) a2.clockf2= 2;

        a2.smellmod = this.smellmod;
        a2.soundmod = this.soundmod;
        a2.hearmod = this.hearmod;
        a2.eyesensmod = this.eyesensmod;
        a2.bloodmod = this.bloodmod;
        if(randf(0,1)<MR*5) {float oo = a2.smellmod; a2.smellmod = (float)randn(a2.smellmod, MR2);}
        if(randf(0,1)<MR*5) {float oo = a2.soundmod; a2.soundmod = (float)randn(a2.soundmod, MR2);}
        if(randf(0,1)<MR*5) {float oo = a2.hearmod; a2.hearmod = (float)randn(a2.hearmod, MR2);}
        if(randf(0,1)<MR*5) {float oo = a2.eyesensmod; a2.eyesensmod = (float)randn(a2.eyesensmod, MR2);}
        if(randf(0,1)<MR*5) {float oo = a2.bloodmod; a2.bloodmod = (float)randn(a2.bloodmod, MR2);}

        for(int i=0;i<NUMEYES;i++){
            if(randf(0,1)<MR*5) a2.eyefov[i] = (float)randn(a2.eyefov[i], MR2);
            if(a2.eyefov[i]<0) a2.eyefov[i] = 0;

            if(randf(0,1)<MR*5) a2.eyedir[i] = (float)randn(a2.eyedir[i], MR2);
            if(a2.eyedir[i]<0) a2.eyedir[i] = 0;
            if(a2.eyedir[i]>2*Math.PI) a2.eyedir[i] = (float)(2*Math.PI);
        }

        a2.temperature_preference= cap((float)randn(this.temperature_preference, 0.005));
        // a2.temperature_preference= this.temperature_preference;

        //mutate brain here
        a2.brain.set(this.brain);
        a2.brain.mutate(MR,MR2);

        return a2;
    }

    public Agent crossover(Agent other) {
        Agent anew = new Agent();
        anew.hybrid=true; //set this non-default flag
        anew.gencount= this.gencount;
        if (other.gencount<anew.gencount) anew.gencount= other.gencount;

        //agent heredity attributes
        anew.clockf1= randf(0,1)<0.5 ? this.clockf1 : other.clockf1;
        anew.clockf2= randf(0,1)<0.5 ? this.clockf2 : other.clockf2;
        anew.herbivore= randf(0,1)<0.5 ? this.herbivore : other.herbivore;
        anew.MUTRATE1= randf(0,1)<0.5 ? this.MUTRATE1 : other.MUTRATE1;
        anew.MUTRATE2= randf(0,1)<0.5 ? this.MUTRATE2 : other.MUTRATE2;
        anew.temperature_preference = randf(0,1)<0.5 ? this.temperature_preference : other.temperature_preference;

        anew.smellmod= randf(0,1)<0.5 ? this.smellmod : other.smellmod;
        anew.soundmod= randf(0,1)<0.5 ? this.soundmod : other.soundmod;
        anew.hearmod= randf(0,1)<0.5 ? this.hearmod : other.hearmod;
        anew.eyesensmod= randf(0,1)<0.5 ? this.eyesensmod : other.eyesensmod;
        anew.bloodmod= randf(0,1)<0.5 ? this.bloodmod : other.bloodmod;

        if(randf(0,1)<0.5 ) {
            anew.eyefov = Arrays.copyOf(this.eyefov, this.eyefov.length);
        } else {
            anew.eyefov = Arrays.copyOf(other.eyefov, other.eyefov.length);
        }

        if(randf(0,1)<0.5 ) {
            anew.eyedir = Arrays.copyOf(this.eyedir, this.eyedir.length);
        } else {
            anew.eyedir = Arrays.copyOf(other.eyedir, other.eyedir.length);
        }

        anew.brain = this.brain.crossover(other.brain);

        return anew;
    }

}
