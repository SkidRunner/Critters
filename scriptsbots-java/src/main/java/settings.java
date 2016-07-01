public class settings {

    public static final int INPUTSIZE = 25;
    public static final int OUTPUTSIZE = 9;
    public static final int NUMEYES = 4;
    public static final int BRAINSIZE = 200;
    public static final int CONNS = 4;

    public static final int WIDTH = 6000;  //width and height of simulation
    public static final int HEIGHT = 3000;
    public static final int WWIDTH = 1600;  //window width and height
    public static final int WHEIGHT = 900;

    public static final int CZ = 50; //cell size in pixels, for food squares. Should divide well into Width Height

    public static final int NUMBOTS = 70; //initially, and minimally
    public static final float BOTRADIUS = 10; //for drawing
    public static final float BOTSPEED = 0.3f;
    public static final float SPIKESPEED = 0.005f; //how quickly can attack spike go up?
    public static final float SPIKEMULT = 1; //essentially the strength of every spike impact
    public static final int BABIES = 2; //number of babies per agent when they reproduce
    public static final float BOOSTSIZEMULT = 2; //how much boost do agents get? when boost neuron is on
    public static final float REPRATEH = 7; //reproduction rate for herbivors
    public static final float REPRATEC = 7; //reproduction rate for carnivors

    public static final float DIST = 150;        //how far can the eyes see on each bot?
    public static final float METAMUTRATE1 = 0.002f; //what is the change in MUTRATE1 and 2 on reproduction? lol
    public static final float METAMUTRATE2 = 0.05f;

    public static final float FOODINTAKE = 0.002f; //how much does every agent consume?
    public static final float FOODWASTE = 0.001f; //how much food disapears if agent eats?
    public static final float FOODMAX = 0.5f; //how much food per cell can there be at max?
    public static final int FOODADDFREQ = 15; //how often does random square get to full food?

    public static final float FOODTRANSFER = 0.001f; //how much is transfered between two agents trading food? per iteration
    public static final float FOOD_SHARING_DISTANCE = 50; //how far away is food shared between bots?

    public static final float TEMPERATURE_DISCOMFORT = 0; //how quickly does health drain in nonpreferred temperatures (0= disabled. 0.005 is decent value)

    public static final float FOOD_DISTRIBUTION_RADIUS = 100; //when bot is killed, how far is its body distributed?

    public static final float REPMULT = 5; //when a body of dead animal is distributed, how much of it goes toward increasing birth counter for surrounding bots?

}
