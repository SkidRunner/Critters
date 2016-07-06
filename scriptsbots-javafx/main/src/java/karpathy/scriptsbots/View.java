package karpathy.scriptsbots;

import static karpathy.scriptsbots.Settings.*;
import static karpathy.scriptsbots.Helpers.*;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class View {

    private Canvas canvas;
    private GraphicsContext graphicsContext;

    private World world;
    private boolean paused;
    private boolean draw;
    private int skipdraw;
    private boolean drawfood;
    private char[] buf = new char[100];
    private char[] buf2 = new char[10];
    private  int modcounter;
    private int lastUpdate;
    private int frames;


    private float scalemult;
    private float xtranslate, ytranslate;
    private int[] downb = new int[3];
    private int mousex, mousey;

    private int following;

    public View(Canvas canvas, World world) {
        this.canvas = canvas;
        this.graphicsContext = canvas.getGraphicsContext2D();

        this.world = world;

                paused = false;
                draw = true;
                skipdraw = 1;
                drawfood = true;
                modcounter = 0;
                frames = 0;
                lastUpdate = 0;
        xtranslate= 0.0f;
        ytranslate= 0.0f;
        scalemult= 0.2f; //1.0;
        downb[0]=0;
        downb[1]=0;
        downb[2]=0;
        mousex=0;
        mousey=0;

        following = 0;
    }

    public void RenderString(float x, float y, Font font, String string, float r, float g, float b) {
        graphicsContext.setFont(font);
        graphicsContext.setFill(new Color(255, (int)(r * 255), (int)(g * 255), (int)(b * 255)));
        graphicsContext.fillText(string, x, y);
    }

    public void drawCircle(float x, float y, float r) {
        graphicsContext.fillOval(x - r, y - r, x + r, y + r);
    }

    public void changeSize(int w, int h)
    {
        canvas.setWidth(w);
        canvas.setHeight(h);
    }

    public void processMouse(int button, int state, int x, int y)
    {
        //printf("MOUSE EVENT: button=%i state=%i x=%i y=%i\n", button, state, x, y);

        //have world deal with it. First translate to world coordinates though
        if(button==0){
            int wx= (int) (((x-WWIDTH/2)/scalemult)-xtranslate);
            int wy= (int) (((y-WHEIGHT/2)/scalemult)-ytranslate);
            world.processMouse(button, state, wx, wy);
        }

        mousex=x; mousey=y;
        downb[button]=1-state; //state is backwards, ah well
    }

    public void processMouseActiveMotion(int x, int y) {
        if(downb[1]==1){
            //mouse wheel. Change scale
            scalemult -= 0.002*(y-mousey);
            if(scalemult<0.01) scalemult=0.01f;
        }

        if(downb[2]==1){
            //right mouse button. Pan around
            xtranslate += 2*(x-mousex);
            ytranslate += 2*(y-mousey);
        }

        mousex=x;
        mousey=y;
    }

    public void processNormalKeys(char key, int x, int y)
    {
        if (key=='r') {
            world.reset();
        } else if (key=='p') {
            //pause
            paused= !paused;
        } else if (key=='d') {
            //drawing
            draw= !draw;
        } else if (key==43) {
            //+
            skipdraw++;

        } else if (key==45) {
            //-
            skipdraw--;
        } else if (key=='f') {
            drawfood=!drawfood;
        } else if (key=='a') {
            for (int i=0;i<10;i++){world.addNewByCrossover();}
        } else if (key=='q') {
            for (int i=0;i<10;i++){world.addCarnivore();}
        } else if (key=='h') {
            for (int i=0;i<10;i++){world.addHerbivore();}
        } else if (key=='c') {
            world.setClosed( !world.isClosed() );
        } else if (key=='s') {
            if(following==0) following=2;
            else following=0;
        } else if(key =='o') {
            if(following==0) following = 1; //follow oldest agent: toggle
            else following =0;
        }
    }

    public void handleIdle(int time)
    {
        modcounter++;
        if (!paused) world.update();

        //TODO: look at this crap and find a better way
        /*
        //show FPS
        int currentTime = time;
        frames++;
        if ((currentTime - lastUpdate) >= 1000) {
            int[] num_herbs_carns = world.numHerbCarnivores();
            frames = 0;
            lastUpdate = currentTime;
        }
        if (skipdraw<=0 && draw) {
            float mult=-(float)(0.005*(skipdraw-1)); //ugly, ah well
            endwait = clock () + mult * CLOCKS_PER_SEC ;
            while (clock() < endwait) {}
        }

        if (draw) {
            if (skipdraw>0) {
                if (modcounter%skipdraw==0) renderScene();    //increase fps by skipping drawing
            }*/
            else renderScene(); //we will decrease fps by waiting using clocks
        /*}
        */
    }

    public void renderScene() {
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if(following==0) {
            graphicsContext.moveTo(xtranslate, ytranslate);
        } else {

            float xi=0, yi=0;
            world.positionOfInterest(following, xi, yi);
            //xi= (conf::WWIDTH/2-xi); //*scalemult;
            //yi= (conf::WHEIGHT/2-yi); //*scalemult;

            graphicsContext.moveTo(-xi, -yi);

            //reset this if there is no interest. Probably agent that was followed died
            if(xi==0 && yi==0) following = 0;
        }

        world.draw(this, drawfood);
    }

    public void drawAgent(Agent agent) {
        float n;
        float r= BOTRADIUS;
        float rp= BOTRADIUS+2;
        //handle selected agent
        if (agent.selectflag>0) {

            //draw selection
            graphicsContext.setFill(new Color(255, 255, 255, 0));
            drawCircle(agent.pos.x, agent.pos.y, BOTRADIUS + 5);

            graphicsContext.moveTo(agent.pos.x - 80, agent.pos.y + 20);

            //draw inputs, outputs
            float col;
            float yy = 15;
            float xx = 15;
            float ss = 16;
            for (int j = 0; j < INPUTSIZE; j++) {
                col = agent.in[j];
                graphicsContext.setFill(new Color(255, col, col, col));
                graphicsContext.fillRect(0 + ss * j, 0, xx + ss * j, yy);
            }
            yy += 5;
            for (int j = 0; j < OUTPUTSIZE; j++) {
                col = agent.out[j];
                graphicsContext.setFill(new Color(255, col, col, col));
                graphicsContext.fillRect(0 + ss * j, 0, xx + ss * j, yy + ss);
            }
            yy += ss * 2;

            //draw brain. Eventually move this to brain class?

            /*
            float offx=0;
            ss=8;
            xx=ss;
            for (int j=0;j<BRAINSIZE;j++) {
                col = agent.brain.boxes[j].out;

                graphicsContext.setFill(new Color(255, col,col,col));

                glColor3f(col,col,col);

                glVertex3f(offx+0+ss*j, yy, 0.0f);
                glVertex3f(offx+xx+ss*j, yy, 0.0f);
                glVertex3f(offx+xx+ss*j, yy+ss, 0.0f);
                glVertex3f(offx+ss*j, yy+ss, 0.0f);

                if ((j+1)%30==0) {
                    yy+=ss;
                    offx-=ss*30;
                }
            }
            */
        }
    }

    public void drawFood(int x, int y, float quantity) {

    }

    public void drawMisc() {

    }

}
