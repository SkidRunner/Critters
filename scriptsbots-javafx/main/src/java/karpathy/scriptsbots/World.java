package karpathy.scriptsbots;

import karpathy.scriptsbots.vmath.Vector2f;

import java.util.Iterator;
import java.util.Vector;

import static karpathy.scriptsbots.Settings.*;
import static karpathy.scriptsbots.Helpers.*;

public class World {

    public Vector<Integer> numCarnivore;
    public Vector<Integer> numHerbivore;
    public int ptr;
    private int modcounter = 0;
    private int current_epoch = 0;
    private int idcounter = 0;
    private Vector<Agent> agents = new Vector<>();
    // food
    private int FW = WIDTH / CZ;
    private int FH = HEIGHT / CZ;
    private int fx;
    private int fy;
    private float[][] food = new float[WIDTH / CZ][HEIGHT / CZ];
    private boolean CLOSED = false; //if environment is closed, then no random bots are added per time interval

    public World() {
        addRandomBots(NUMBOTS);
        //inititalize food layer

        for (int x = 0; x < FW; x++) {
            for (int y = 0; y < FH; y++) {
                food[x][y] = 0;
            }
        }

        numCarnivore = new Vector<>();
        numHerbivore = new Vector<>();
        ptr = 0;
    }

    public void update() {
        modcounter++;

        //Process periodic events
        //Age goes up!
        if (modcounter%100==0) {
            for(Agent agent : agents) {
                agent.age++;
            }
        }

        if(modcounter%1000==0){
            int[] num_herbs_carns = numHerbCarnivores();
            numHerbivore.set(ptr, num_herbs_carns[0]);
            numCarnivore.set(ptr, num_herbs_carns[1]);
            ptr++;
            if(ptr == numHerbivore.size()) ptr = 0;
        }
        if (modcounter%1000==0) writeReport();
        if (modcounter>=10000) {
            modcounter=0;
            current_epoch++;
        }
        if (modcounter%FOODADDFREQ==0) {
            fx=randi(0,FW);
            fy=randi(0,FH);
            food[fx][fy]= FOODMAX;
        }

        //reset any counter variables per agent
        for(Agent agent : agents) {
            agent.spiked= false;
        }

        //give input to every agent. Sets in[] array
        setInputs();

        //brains tick. computes in[] -> out[]
        brainsTick();

        //read output and process consequences of bots on environment. requires out[]
        processOutputs();

        //process bots: health and deaths
        for(Agent agent : agents) {
            float baseloss = 0.0002f; // + 0.0001*(abs(agents[i].w1) + abs(agents[i].w2))/2;
            //if (agents[i].w1<0.1 && agents[i].w2<0.1) baseloss=0.0001; //hibernation :p
            //baseloss += 0.00005*agents[i].soundmul; //shouting costs energy. just a tiny bit

            if (agent.boost) {
                //boost carries its price, and it's pretty heavy!
                agent.health -= baseloss * BOOSTSIZEMULT * 1.3f;
            } else {
                agent.health -= baseloss;
            }
        }

        //process temperature preferences
        for(Agent agent : agents) {

            //calculate temperature at the agents spot. (based on distance from equator)
            float dd= (float)(2.0*Math.abs(agent.pos.x/WIDTH - 0.5));
            float discomfort= Math.abs(dd-agent.temperature_preference);
            discomfort= discomfort*discomfort;
            if (discomfort<0.08) discomfort=0;
            agent.health -= TEMPERATURE_DISCOMFORT*discomfort;
        }

        //process indicator (used in drawing)
        for(Agent agent : agents) {
            if(agent.indicator>0) agent.indicator -= 1;
        }

        //remove dead agents.
        //first distribute foods
        for(Agent agent : agents) {
            //if this agent was spiked this round as well (i.e. killed). This will make it so that
            //natural deaths can't be capitalized on. I feel I must do this or otherwise agents
            //will sit on spot and wait for things to die around them. They must do work!
            if (agent.health<=0 && agent.spiked) {

                //distribute its food. It will be erased soon
                //first figure out how many are around, to distribute this evenly
                int numaround=0;
                for(Agent otherAgent : agents) {
                    if (otherAgent.health>0) {
                        float d= (agent.pos.subtract(otherAgent.pos)).length();
                        if (d<FOOD_DISTRIBUTION_RADIUS) {
                            numaround++;
                        }
                    }
                }

                //young killed agents should give very little resources
                //at age 5, they mature and give full. This can also help prevent
                //agents eating their young right away
                float agemult= 1.0f;
                if(agent.age<5) agemult= agent.age*0.2f;

                if (numaround>0) {
                    //distribute its food evenly
                    for(Agent otherAgent : agents) {
                    //for (int j=0;j<agents.size();j++) {

                        if (otherAgent.health>0) {
                            float d= (agent.pos.subtract(otherAgent.pos)).length();
                            if (d<FOOD_DISTRIBUTION_RADIUS) {
                                otherAgent.health += 5*(1-otherAgent.herbivore)*(1-otherAgent.herbivore)/Math.pow(numaround,1.25)*agemult;
                                otherAgent.repcounter -= REPMULT*(1-otherAgent.herbivore)*(1-otherAgent.herbivore)/Math.pow(numaround,1.25)*agemult; //good job, can use spare parts to make copies
                                if (otherAgent.health>2) otherAgent.health=2; //cap it!
                                otherAgent.initEvent(30,1,1,1); //white means they ate! nice
                            }
                        }
                    }
                }
            }
        }

        //TODO: Look to see if this will crash.
        Iterator<Agent> iter = agents.iterator();
        while(iter.hasNext()) {
            Agent agent = iter.next();
            if(agent.health <= 0) {
                iter.remove();
            }
        }


        //handle reproduction
        for (int i=0;i<agents.size();i++) {
            Agent agent = agents.get(i);
            if (agent.repcounter<0 && agent.health>0.65 && modcounter%15==0 && randf(0,1)<0.1) { //agent is healthy and is ready to reproduce. Also inject a bit non-determinism
                //agents[i].health= 0.8; //the agent is left vulnerable and weak, a bit
                reproduce(i, agent.MUTRATE1, agent.MUTRATE2); //this adds conf::BABIES new agents to agents[]
                agent.repcounter= agent.herbivore*randf(REPRATEH-0.1f,REPRATEH+0.1f) + (1-agent.herbivore)*randf(REPRATEC-0.1f,REPRATEC+0.1f);
            }
        }

        //add new agents, if environment isn't closed
        if (!CLOSED) {
            //make sure environment is always populated with at least NUMBOTS bots
            if (agents.size()<NUMBOTS
                    ) {
                //add new agent
                addRandomBots(1);
            }
            if (modcounter%100==0) {
                if (randf(0,1)<0.5){
                    addRandomBots(1); //every now and then add random bots in
                }else
                    addNewByCrossover(); //or by crossover
            }
        }
    }

    public void reset() {
        agents.clear();
        addRandomBots(NUMBOTS);
    }

    public void draw(View view, boolean drawfood) {
        //draw food
        if(drawfood) {
            for(int i=0;i<FW;i++) {
                for(int j=0;j<FH;j++) {
                    float f= (float)(0.5*food[i][j]/FOODMAX);
                    view.drawFood(i,j,f);
                }
            }
        }

        //draw all agents
        for(Agent a : agents) {
            view.drawAgent(a);
        }

        view.drawMisc();
    }

    public boolean isClosed() {
        return CLOSED;
    }

    public void setClosed(boolean close) {
        CLOSED = close;
    }

    /**
     * Returns the number of herbivores and
     * carnivores in the world.
     * first : num herbs
     * second : num carns
     */
    public int[] numHerbCarnivores() {
        int numherb=0;
        int numcarn=0;
        for(Agent a : agents) {
            if (a.herbivore>0.5) numherb++;
            else numcarn++;
        }

        return new int[]{numherb,numcarn};
    }

    public int numAgents() {
        return agents.size();
    }

    public int epoch() {
        return current_epoch;
    }

    //mouse interaction
    public void processMouse(int button, int state, int x, int y) {
        if (state==0) {
            float mind=1e10f;
            float mini=-1;
            float d;

            for (int i=0;i<agents.size();i++) {
                d= (float)(Math.pow(x-agents.get(i).pos.x,2)+Math.pow(y-agents.get(i).pos.y,2));
                if (d<mind) {
                    mind=d;
                    mini=i;
                }
            }
            //toggle selection of this agent
            for (int i=0;i<agents.size();i++) agents.get(i).selectflag= 0;
            agents.get((int)mini).selectflag= 1;
            agents.get((int)mini).printSelf();
        }
    }

    public void addNewByCrossover() {
        //find two success cases
        int i1= randi(0, agents.size());
        int i2= randi(0, agents.size());
        for (int i=0;i<agents.size();i++) {
            if (agents.get(i).age > agents.get(i1).age && randf(0,1)<0.1) {
                i1= i;
            }
            if (agents.get(i).age > agents.get(i2).age && randf(0,1)<0.1 && i!=i1) {
                i2= i;
            }
        }

        Agent a1= agents.get(i1);
        Agent a2= agents.get(i2);


        //cross brains
        Agent anew = a1.crossover(a2);


        //maybe do mutation here? I dont know. So far its only crossover
        anew.id= idcounter;
        idcounter++;
        agents.add(anew);
    }

    public void addRandomBots(int num) {
        for (int i=0;i<num;i++) {
            Agent a = new Agent();
            a.id= idcounter;
            idcounter++;
            agents.add(a);
        }
    }

    public void addCarnivore() {
        Agent a = new Agent();
        a.id= idcounter;
        idcounter++;
        a.herbivore= randf(0, 0.1f);
        agents.add(a);
    }

    public void addHerbivore() {
        Agent a = new Agent();
        a.id= idcounter;
        idcounter++;
        a.herbivore= randf(0.9f, 1);
        agents.add(a);
    }

    public void positionOfInterest(int type, float xi, float yi) {
        if(type==1){
            //the interest of type 1 is the oldest agent
            int maxage=-1;
            int maxi=-1;
            for(int i=0;i<agents.size();i++){
                if(agents.get(i).age>maxage) { maxage = agents.get(i).age; maxi=i; }
            }
            if(maxi!=-1) {
                xi = agents.get(maxi).pos.x;
                yi = agents.get(maxi).pos.y;
            }
        } else if(type==2){
            //interest of type 2 is the selected agent
            int maxi=-1;
            for(int i=0;i<agents.size();i++){
                if(agents.get(i).selectflag==1) {maxi=i; break; }
            }
            if(maxi!=-1) {
                xi = agents.get(maxi).pos.x;
                yi = agents.get(maxi).pos.y;
            }
        }
    }

    private void setInputs() {
//P1 R1 G1 B1 FOOD P2 R2 G2 B2 SOUND SMELL HEALTH P3 R3 G3 B3 CLOCK1 CLOCK 2 HEARING     BLOOD_SENSOR   TEMPERATURE_SENSOR
        //0   1  2  3  4   5   6  7 8   9     10     11   12 13 14 15 16       17      18           19                 20

        float PI8=(float)(Math.PI/8/2); //pi/8/2
        float PI38= 3*PI8; //3pi/8/2
        float PI4= (float)(Math.PI/4);

        for (Agent a : agents) {

            //HEALTH
            a.in[11]= cap(a.health/2); //divide by 2 since health is in [0,2]

            //FOOD
            int cx= (int) a.pos.x/CZ;
            int cy= (int) a.pos.y/CZ;
            a.in[4]= food[cx][cy]/FOODMAX;

            //SOUND SMELL EYES
            float[] p = new float[NUMEYES];
            float[] r = new float[NUMEYES];
            float[] g = new float[NUMEYES];
            float[] b = new float[NUMEYES];

            float soaccum=0;
            float smaccum=0;
            float hearaccum=0;

            //BLOOD ESTIMATOR
            float blood= 0;

            for(Agent a2 : agents) {
                if(a2.equals(a)) {
                    continue;
                }

                if (a.pos.x < a2.pos.x-DIST || a.pos.x>a2.pos.x+DIST
                        || a.pos.y>a2.pos.y+DIST || a.pos.y<a2.pos.y-DIST) continue;

                float d= a.pos.subtract(a2.pos).length();

                if (d<DIST) {

                    //smell
                    smaccum+= (DIST-d)/DIST;

                    //sound
                    soaccum+= (DIST-d)/DIST*(Math.max(Math.abs(a2.w1),Math.abs(a2.w2)));

                    //hearing. Listening to other agents
                    hearaccum+= a2.soundmul*(DIST-d)/DIST;

                    float ang= a2.pos.subtract(a.pos).getAngle(); //current angle between bots

                    for(int q=0;q<NUMEYES;q++){
                        float aa = a.angle + a.eyedir[q];
                        if (aa<-Math.PI) aa += 2*Math.PI;
                        if (aa>Math.PI) aa -= 2*Math.PI;

                        float diff1= aa- ang;
                        if (Math.abs(diff1)>Math.PI) diff1= (float)(2*Math.PI- Math.abs(diff1));
                        diff1= Math.abs(diff1);

                        float fov = a.eyefov[q];
                        if (diff1<fov) {
                            //we see a2 with this eye. Accumulate stats
                            float mul1= a.eyesensmod*(Math.abs(fov-diff1)/fov)*((DIST-d)/DIST);
                            p[q] += mul1*(d/DIST);
                            r[q] += mul1*a2.red;
                            g[q] += mul1*a2.gre;
                            b[q] += mul1*a2.blu;
                        }
                    }

                    //blood sensor
                    float forwangle= a.angle;
                    float diff4= forwangle- ang;
                    if (Math.abs(forwangle)>Math.PI) diff4= (float)(2*Math.PI- Math.abs(forwangle));
                    diff4= Math.abs(diff4);
                    if (diff4<PI38) {
                        float mul4= ((PI38-diff4)/PI38)*((DIST-d)/DIST);
                        //if we can see an agent close with both eyes in front of us
                        blood+= mul4*(1-a2.health/2); //remember: health is in [0 2]
                        //agents with high life dont bleed. low life makes them bleed more
                    }
                }
            }

            smaccum *= a.smellmod;
            soaccum *= a.soundmod;
            hearaccum *= a.hearmod;
            blood *= a.bloodmod;

            a.in[0]= cap(p[0]);
            a.in[1]= cap(r[0]);
            a.in[2]= cap(g[0]);
            a.in[3]= cap(b[0]);

            a.in[5]= cap(p[1]);
            a.in[6]= cap(r[1]);
            a.in[7]= cap(g[1]);
            a.in[8]= cap(b[1]);
            a.in[9]= cap(soaccum);
            a.in[10]= cap(smaccum);

            a.in[12]= cap(p[2]);
            a.in[13]= cap(r[2]);
            a.in[14]= cap(g[2]);
            a.in[15]= cap(b[2]);
            a.in[16]= (float)Math.abs(Math.sin(modcounter/a.clockf1));
            a.in[17]= (float)Math.abs(Math.sin(modcounter/a.clockf2));
            a.in[18]= cap(hearaccum);
            a.in[19]= cap(blood);

            //temperature varies from 0 to 1 across screen.
            //it is 0 at equator (in middle), and 1 on edges. Agents can sense discomfort
            float dd= (float)(2.0*Math.abs(a.pos.x/WIDTH - 0.5));
            float discomfort= Math.abs(dd - a.temperature_preference);
            a.in[20]= discomfort;

            a.in[21]= cap(p[3]);
            a.in[22]= cap(r[3]);
            a.in[23]= cap(g[3]);
            a.in[24]= cap(b[3]);

        }
    }

    private void processOutputs() {
//assign meaning
        //LEFT RIGHT R G B SPIKE BOOST SOUND_MULTIPLIER GIVING
        // 0    1    2 3 4   5     6         7             8
        for(Agent a : agents) {

            a.red= a.out[2];
            a.gre= a.out[3];
            a.blu= a.out[4];
            a.w1= a.out[0]; //-(2*a->out[0]-1);
            a.w2= a.out[1]; //-(2*a->out[1]-1);
            a.boost= a.out[6]>0.5;
            a.soundmul= a.out[7];
            a.give= a.out[8];

            //spike length should slowly tend towards out[5]
            float g= a.out[5];
            if (a.spikeLength<g)
                a.spikeLength+=SPIKESPEED;
            else if (a.spikeLength>g)
                a.spikeLength= g; //its easy to retract spike, just hard to put it up
        }

        //move bots
        //#pragma omp parallel for
        for(Agent a : agents) {

            Vector2f v = new Vector2f(BOTRADIUS/2, 0);
            v.rotateAroundOrigin(a.angle + (float)(Math.PI/2), true);

            Vector2f w1p= a.pos.add(v); //wheel positions
            Vector2f w2p= a.pos.subtract(v);

            float BW1= BOTSPEED*a.w1;
            float BW2= BOTSPEED*a.w2;
            if (a.boost) {
                BW1=BW1*BOOSTSIZEMULT;
            }
            if (a.boost) {
                BW2=BW2*BOOSTSIZEMULT;
            }

            //move bots
            Vector2f vv= w2p.subtract(a.pos);
            vv.rotateAroundOrigin(BW1, false);
            a.pos.set(w2p.subtract(vv));
            a.angle -= BW1;
            if (a.angle<-Math.PI) a.angle= (float)(Math.PI - (-Math.PI-a.angle));
            vv= a.pos.subtract(w1p);
            vv.rotateAroundOrigin(BW2, true);
            a.pos.set(w1p.add(vv));
            a.angle += BW2;
            if (a.angle>Math.PI) a.angle= -(float)(Math.PI + (a.angle-(Math.PI)));

            //wrap around the map
            if (a.pos.x<0) a.pos.x= WIDTH+a.pos.x;
            if (a.pos.x>=WIDTH) a.pos.x= a.pos.x-WIDTH;
            if (a.pos.y<0) a.pos.y= HEIGHT+a.pos.y;
            if (a.pos.y>=HEIGHT) a.pos.y= a.pos.y-HEIGHT;
        }

        //process food intake for herbivors
        for(Agent a : agents) {

            int cx= (int) a.pos.x/CZ;
            int cy= (int) a.pos.y/CZ;
            float f= food[cx][cy];
            if (f>0 && a.health<2) {
                //agent eats the food
                float itk=Math.min(f,FOODINTAKE);
                float speedmul= (float)((1-(Math.abs(a.w1)+Math.abs(a.w2))/2)*0.7 + 0.3);
                itk= itk*a.herbivore*speedmul; //herbivores gain more from ground food
                a.health+= itk;
                a.repcounter -= 3*itk;
                food[cx][cy]-= Math.min(f,FOODWASTE);
            }
        }

        //process giving and receiving of food
        for(Agent a : agents) {
            a.dfood=0;
        }

        for(Agent a : agents) {
            if (a.give>0.5) {
                for(Agent a2 : agents) {
                    float d= a.pos.subtract(a2.pos).length();
                    if (d<FOOD_SHARING_DISTANCE) {
                        //initiate transfer
                        if (a2.health<2) a2.health += FOODTRANSFER;
                        a.health -= FOODTRANSFER;
                        a2.dfood += FOODTRANSFER; //only for drawing
                        a.dfood -= FOODTRANSFER;
                    }
                }
            }
        }

        //process spike dynamics for carnivors
        if (modcounter%2==0) { //we dont need to do this TOO often. can save efficiency here since this is n^2 op in #agents
            for(Agent a : agents) {

                //NOTE: herbivore cant attack. TODO: hmmmmm
                //fot now ok: I want herbivores to run away from carnivores, not kill them back
                if(a.herbivore>0.8 || a.spikeLength<0.2 || a.w1<0.5 ||a.w2<0.5) continue;

                for(Agent a2 : agents) {
                    if(a2.equals(a)) {
                        continue;
                    }
                    float d= a.pos.subtract(a2.pos).length();

                    if (d<2*BOTRADIUS) {
                        //these two are in collision and agent i has extended spike and is going decent fast!
                        Vector2f v =new Vector2f(1,0);
                        v.rotateAroundOrigin(a.angle, true);
                        float diff= v.angleBetween(a2.pos.subtract(a.pos));
                        if (Math.abs(diff)<Math.PI/8) {
                            //bot i is also properly aligned!!! that's a hit
                            float mult=1;
                            if (a.boost) mult= BOOSTSIZEMULT;
                            float DMG= SPIKEMULT*a.spikeLength*Math.max(Math.abs(a.w1),Math.abs(a.w2))*BOOSTSIZEMULT;

                            a2.health-= DMG;

                            if (a.health>2) a.health=2; //cap health at 2
                            a.spikeLength= 0; //retract spike back down

                            a.initEvent(40*DMG,1,1,0); //yellow event means bot has spiked other bot. nice!

                            Vector2f v2 =new Vector2f(1,0);
                            v2.rotateAroundOrigin(a2.angle, true);
                            float adiff= v.angleBetween(v2);
                            if (Math.abs(adiff)<Math.PI/2) {
                                //this was attack from the back. Retract spike of the other agent (startle!)
                                //this is done so that the other agent cant right away "by accident" attack this agent
                                a2.spikeLength= 0;
                            }

                            a2.spiked= true; //set a flag saying that this agent was hit this turn
                        }
                    }
                }
            }
        }
    }

    private void brainsTick() {
        //takes in[] to out[] for every agent
        for(Agent a : agents) {
            a.tick();
        }
    }

    private void writeReport() {

    }

    private void reproduce(int ai, float MR, float MR2) {
        if (randf(0,1)<0.04) MR= MR*randf(1, 10);
        if (randf(0,1)<0.04) MR2= MR2*randf(1, 10);

        agents.get(ai).initEvent(30,0,0.8f,0); //green event means agent reproduced.
        for (int i=0;i<BABIES;i++) {

            Agent a2 = agents.get(ai).reproduce(MR,MR2);
            a2.id= idcounter;
            idcounter++;
            agents.add(a2);

            //TODO fix recording
            //record this
            //FILE* fp = fopen("log.txt", "a");
            //fprintf(fp, "%i %i %i\n", 1, this->id, a2.id); //1 marks the event: child is born
            //fclose(fp);
        }
    }

}
