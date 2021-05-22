package be.uantwerpen.fti.ei.spaceinvaders.gamelogic;

import be.uantwerpen.fti.ei.spaceinvaders.gamelogic.entities.EnemyBullet;
import be.uantwerpen.fti.ei.spaceinvaders.gamelogic.entities.EnemyShip;
import be.uantwerpen.fti.ei.spaceinvaders.gamelogic.entities.Playership;
import be.uantwerpen.fti.ei.spaceinvaders.graphics.Graphics;

import javax.swing.*;
import java.util.ArrayList;

public class Game extends JPanel implements Runnable {
    AbstractFactory factory;

    private int playingfield=16;
    private int rows=3;
    private int columns=8;
    private int moveForward;
    boolean running;
    private int slowcount=0;

    private Thread thread;
    //KeyHandler key;


    //Entitys
    private ArrayList<EnemyShip> wave=new ArrayList<EnemyShip>();
    private Playership playership;
    private EnemyBullet enemyBullet;

    //input
    private AbstractInput input;
    public Game(AbstractFactory f){
        this.factory=f;

    }
    public void init(){
        running=true;
        input= factory.createInput();
        for(int j=0; j<rows;j++)
        {
            for(int i=0;i<columns;i++)
            {
                wave.add(factory.newEnemyShip());
                wave.get(wave.size()-1).setDx(1);
                wave.get(wave.size()-1).setX(1+i);
                wave.get(wave.size()-1).setY(j);
            }
        }

        playership= factory.newPlayership();
        playership.setX(8);
        playership.setY(16);

        enemyBullet=factory.newEnemyBullet();
        enemyBullet.setX(10);
        enemyBullet.setY(10);
    }

    public void addNotify(){
        super.addNotify();
        if(thread==null)
        {
            thread =new Thread(this,"gamethread");
            thread.start();
        }
    }
    @Override
    public void run() {
        final double GAME_HERTZ=4;
        final double TBU= 2_00_000_000 /GAME_HERTZ;//time before update

        final int MUBR=1; //most updates before render

        double lastUpdateTime=System.nanoTime();
        double lastRenderTime;

        final double TARGET_FPS=30;
        final double TTBR=1000000000/TARGET_FPS;//total time before render

        int frameCount=0;
        int lastSecondTime=(int) (lastUpdateTime/1000000000);
        int oldFrameCount=0;

        while(running){

            double now=System.nanoTime();
            int updateCount=0;


            if(input.inputAvailable())
            {
                switch (input.getInput())
                {
                    case LEFT:
                        if(playership.getX()>0)
                        {
                            playership.setX(playership.getX()-1);
                        }
                        //else do nothing
                    break;
                    case SPACE:
                        //shoot

                    break;
                    case RIGHT:
                        if(playership.getX()<=playingfield)
                        {
                            playership.setX(playership.getX()+1);
                        }
                        System.out.println("The right key is pressed");
                        break;
                    case ESCAPE:
                        //pause the game
                        System.out.println("The escape button is pressed");
                        break;
                }



            }

            while((now-lastUpdateTime)>TBU&&(updateCount<MUBR)){
                //update();

                if(slowcount==10)
                {


                    if((wave.get(wave.size()-1).getX()>playingfield)&&moveForward!=-2)//if the outer right wall is hit
                    {
                        System.out.println("if the outer right wall is hit");
                        for(int i = 0; i < wave.size();i++)
                        {
                            wave.get(i).setDx(0);
                            moveForward=-1;
                            wave.get(i).setY(wave.get(i).getY()+1);
                        }
                    }

                    if((wave.get(0).getX()==0)&&moveForward!=2)
                    {
                        System.out.println("/if the outer left wall is hit");
                        for (int i = 0;i<wave.size();i++)
                        {
                            wave.get(i).setDx(0);
                            wave.get(i).setY(wave.get(i).getY()+1);
                            moveForward=1;

                        }
                    }
                    for(int i=0;i< wave.size();i++)
                    {
                        //if(wave.get(i).getDx()==1)
                        wave.get(i).setX(wave.get(i).getX()+wave.get(i).getDx());
                        if (moveForward==-1)
                        {

                            wave.get(i).setDx(-1);

                        }
                        if (moveForward==1)
                        {
                            wave.get(i).setDx(1);

                        }
                    }
                    if(moveForward==-1)
                    {
                        moveForward=-2;
                    }
                    if (moveForward==1)
                    {
                        moveForward=2;
                    }

                    slowcount=0;


                }
                else
                {
                    slowcount=slowcount+1;
                }



                //input(key);
                lastUpdateTime+=TBU;
                updateCount++;
            }
            if(now-lastUpdateTime>TBU){
                lastUpdateTime=now-TBU;
            }




            //draw();
            lastRenderTime=now;
            frameCount++;


            for(int i=0;i<wave.size();i++)
            {
                if (wave.get(i).isVisible()==true)
                {
                    wave.get(i).visualize();
                }

            }

            playership.visualize();
            enemyBullet.visualize();

            factory.update();

            int thisSecond=(int) (lastUpdateTime/1000000000);
            if(thisSecond>lastSecondTime){
                if(frameCount!=oldFrameCount){
                    System.out.println("NEW SECOND "+thisSecond+" "+frameCount);
                    oldFrameCount=frameCount;
                }
                frameCount=0;
                lastSecondTime=thisSecond;
            }

            while(now-lastRenderTime<TTBR&& now-lastUpdateTime<TBU){
                Thread.yield();
                try{
                    Thread.sleep(1);
                }catch(Exception e){
                    System.out.println("Error yielding thread");
                }
                now=System.nanoTime();

            }
        }
    }
}
