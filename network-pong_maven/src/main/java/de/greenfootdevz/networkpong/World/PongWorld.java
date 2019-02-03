package de.greenfootdevz.networkpong.World;

import de.greenfootdevz.networkpong.*;
import de.greenfootdevz.networkpong.Actor.Ball;
import de.greenfootdevz.networkpong.Actor.Bat;
import de.greenfootdevz.networkpong.Actor.Booster;
import de.greenfootdevz.networkpong.Actor.RemoteBat;
import de.greenfootdevz.networkpong.Network.PongClientConnection;
import de.greenfootdevz.networkpong.Network.PongClientData;
import de.greenfootdevz.networkpong.Network.PongHostConnection;
import de.greenfootdevz.networkpong.Network.PongHostData;
import greenfoot.Actor;
import greenfoot.Greenfoot;
import greenfoot.GreenfootSound;
import greenfoot.World;

import java.net.InetAddress;
import java.util.Random;

public class PongWorld extends World{
    public static final int BALL_SPEED_X = 5;

//    private ServerSocket host;
//    private Socket client;

//    private ObjectInputStream ois;
//    private ObjectOutputStream oos;

    private boolean isHost;

    // Actors
    private Booster booster;
    private Ball ball;
    private Bat bat;
    private RemoteBat remote;
    
    private Random r;
    
    private int dx, dy;
    private int pointsHost, pointsClient;
    
    private PongClientConnection clientConnection;
    private PongHostConnection hostConnection;
    
    private final int BALL_WALL_OFFSET = 20;
    private final int BALL_MAX_SPEED_Y = 2;
    
    public PongWorld() throws Exception{
        super(1000, 500, 1);
        
        //exception(null); //Throws exception in order to test dialog
        
        GameInitFrame frame = new GameInitFrame();
        frame.setVisible(true);
        Thread.sleep(100);
        this.isHost = frame.isHost();
        InetAddress ip = frame.getSelectedAddress();
        int port = frame.getSelectedPort();
        
        this.r = new Random();
        
        this.bat = new Bat();
        this.remote = new RemoteBat();
        this.ball = new Ball();
        addObject(ball, getWidth()/2, getHeight()/2);
        resetBall();
        
        if(isHost){
            System.out.println("Starting Host on this machine...");

            addObject(bat, BALL_WALL_OFFSET, getHeight()/2);
            addObject(remote, getWidth()-BALL_WALL_OFFSET, getHeight()/2);


            this.hostConnection = new PongHostConnection(ip, 50, port, true);
            System.out.println("Waiting for a client to connect...");
            hostConnection.waitForClientConnection();
            hostConnection.start();
//            System.out.println("Client from " + client.getInetAddress().getHostAddress() + " connected.");
//            ois = new ObjectInputStream(client.getInputStream());
//            oos = new ObjectOutputStream(client.getOutputStream());
        }else{
            System.out.println("Starting Client on this machine...");

            addObject(bat, getWidth()-BALL_WALL_OFFSET, getHeight()/2);
            addObject(remote, BALL_WALL_OFFSET, getHeight()/2);


//            client = new Socket(ip, port);
//            client.setTcpNoDelay(true);
//
//            this.oos = new ObjectOutputStream(client.getOutputStream());
//            this.ois = new ObjectInputStream(client.getInputStream());
            
            this.clientConnection = new PongClientConnection(ip, port, true);
            clientConnection.start();
        }
        
        
        
        System.out.println("Starting the Game!");
        Greenfoot.start();
    }

    public void act(){
        try{
            if(isHost){
                handleHostTasks();
            }else{
                handleClientTasks();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void handleHostTasks() throws Exception{
        if(ball.getX() < ball.getImage().getWidth()/2){ //Punkt für client
            resetBall();
            pointsClient++;
            Greenfoot.playSound("pong_8bit_scorepoint.wav");
        }else if(ball.getX() > getWidth() - ball.getImage().getWidth()/2){ //Punkt für host
            resetBall();
            pointsHost++;
            Greenfoot.playSound("pong_8bit_scorepoint.wav");
        }else if(ball.getIntesecting(Bat.class) != null){ //Host trifft ball
           handleBatHit(bat);
        }else if(ball.getIntesecting(RemoteBat.class) != null){ //Client trifft
            handleBatHit(remote);
        }else if(ball.getIntesecting(Booster.class) != null){
            removeObject(booster);
            booster = null;
            dx += 1;
            Greenfoot.playSound("sfx_sounds_powerup2.wav");
        }else{ //Ball fliegt einfach weiter
            ball.setLocation(ball.getX() + dx, ball.getY() + dy);
            if(ball.getY() <= ball.getImage().getHeight()/2 || ball.getY() >= getHeight() - ball.getImage().getHeight()/2){ // (Bounce oben / unten)
                dy = -dy;
                Greenfoot.playSound("pong_8bit_hitwall.wav");
            }
        }
        
        if(booster == null && r.nextInt(1000) == 0){ //Einen Booster auf der Karte spawnen mit Chance 0.1%
            int boosterX = randomNbrInRange(2 * BALL_WALL_OFFSET, getWidth() - 2 * BALL_WALL_OFFSET);
            int boosterY = randomNbrInRange(0, getHeight());
            
            booster = new Booster();
            addObject(booster, boosterX, boosterY);
        }
        showScoreboard(pointsHost, pointsClient);
        PongClientData data = hostConnection.getMostRecent(); // Daten vom client empfangen
        if(data == null) return;
        hostConnection.sendUpdate(new PongHostData(bat.getX(), bat.getY(), ball.getX(), ball.getY(), (booster != null ? booster.getX() : -1), (booster != null ? booster.getY() : -1),
                pointsHost, pointsClient)); // aktuelle Daten an Client senden
        remote.setLocation(data.getX(), data.getY()); // Empfangene Daten vom Client anzeigen
    }

    /*
    * Generates a random int in range [min|max] (bounds inclusive).
    * For this to work: min <= max.
     */
    private int randomNbrInRange(int min, int max){
        if(max < min) {throw new IllegalArgumentException("max is smaller than min.");}
        return r.nextInt((max - min) + 1) + min;
    }
    
    private void handleBatHit(Actor bat){
        Greenfoot.playSound("pong_8bit_hitpaddle.wav");
        // TODO: use current dy of the bat / ball to calculate new dx/dy;
        dx = -dx;
        dy = (ball.getY() - bat.getY()) / 2;
        if(dy > BALL_MAX_SPEED_Y) dy = BALL_MAX_SPEED_Y;
        if(dy < -BALL_MAX_SPEED_Y) dy = -BALL_MAX_SPEED_Y;
        
        // Ensure ball wont get stuck in the bat // TODO: this wont work when paddle is moving "into" the ball. Maybe add "Do not collide for x seconds" Flag
        do {
            ball.setLocation(ball.getX() + dx, ball.getY() + dy);
        } while (ball.getIntesecting(bat.getClass()) != null);
    }

    private void resetBall(){
       ball.setLocation(getWidth()/2, getHeight()/2);
       dx = (r.nextBoolean() ? BALL_SPEED_X : -BALL_SPEED_X); // randomly choose starting direction on x-Axis
       dy = 0;
    }

    private void handleClientTasks() throws Exception{
        clientConnection.sendUpdate(new PongClientData(bat.getX(), bat.getY()));
        PongHostData data = clientConnection.getRecentHostData();
        if(data == null) return;
        this.ball.setLocation(data.getBallPos().x, data.getBallPos().y);
        this.remote.setLocation(data.getBatPos().x, data.getBatPos().y);
        
        if(data.getBoosterPos().x == -1 && booster != null){ // Kein Booster mehr in der Welt des Hosts
            removeObject(booster);
            booster = null;
        }else if(booster == null && data.getBoosterPos().x > 0){ // Neuer Booster in der Welt des Hosts
            booster = new Booster();
            addObject(booster, data.getBoosterPos().x, data.getBoosterPos().y);
        }else if(booster != null && data.getBoosterPos().x > 0){ // Position des aktuellen Boosters hat sich geändert
            booster.setLocation(data.getBoosterPos().x, data.getBoosterPos().y);
        }
        showScoreboard(data.getHostPoints(), data.getClientPoints());
    }
    
    private void showScoreboard(int host, int client){
         showText("" + host, BALL_WALL_OFFSET + 20, 50);
        showText("" + client, getWidth() - BALL_WALL_OFFSET - 20, 50);
    }
}