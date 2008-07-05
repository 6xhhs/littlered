/*
 * GameManager.java
 *
 * Created on 8 May 2007, 21:10
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package digitrix.littlered.src;

//<editor-fold defaultstate="collapsed" desc="imports">
import com.sun.midp.dev.GraphicalInstaller;
import digitrix.littlered.src.datahandler.ScoreDAO;
import digitrix.littlered.src.worldelements.Actor;
import digitrix.littlered.src.worldelements.Baddie;
import digitrix.littlered.src.worldelements.Hero;
import digitrix.littlered.src.worldelements.Projectile;
import digitrix.littlered.src.worldelements.Prop;
import digitrix.littlered.src.worldelements.GameEffect;
import digitrix.littlered.src.worldelements.Stage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;
import javax.microedition.rms.*;
import java.util.*;
//</editor-fold>

/**
 *
 * @author Josh
 *
 */
public class GameManager extends GameCanvas implements Runnable, IManager {
    
    // game states    
    public static final int GS_NEWGAME          = 0;
    public static final int GS_PLAYING          = 1;
    public static final int GS_GAMEOVER         = 2;
    public static final int GS_LOADING          = 4;
    public static final int GS_PAUSED           = 5;
    public static final int GS_RESUME           = 6; 
    public static final int GS_CLOCKED          = 7; 
    public static final int GS_LEVEL_COMPLETED  = 8;   
    
    // game state times
    private static final int GS_TIME_NEWGAME            = 2000; 
    private static final int GS_TIME_GAMEOVER           = 1500;
    private static final int GS_TIME_CLOCKED            = 1500; 
    private static final int GS_TIME_LEVEL_COMPLETED    = 2500;
    private static final int GS_TIME_RESUMING           = 1500; 
    
    private static final int MAX_FPS = 25;
    
    private static GameManager _instance = null; 
    
    private Thread _gameThread = null; 
    private boolean _gameThreadRunning = false; 
    private boolean _gameManagerInitilised = false; 
    private boolean _sleeping = false; 
    
    private int _gamesScore = 0;
    private int _levelsScore = 0;
    private Stage _stage = null;
    private int _currentState = 0; 
    private int _prevState = 0; 
    
    private long _stateTime = 0;    
    private long _currTime = 0;    
    
    private CustomFont _fontManager = null; 
    private LittleRed _midlet = null; 
    private int _tick = 0; // general purpose flag
    
    /**
     * Expose this class to all classes by allow them to game reference via this static 
     * method
     **/
    public static GameManager getInstance(){
        return _instance;         
    }
    
    /** Creates a new instance of GameManager */
    public GameManager() {
        super(true);         
        _instance = this;
        _midlet = LittleRed.getInstance(); 
        _fontManager = _midlet.getFontManager();
        
        setCommandListener( _midlet ); 
        
        // start games thread
        if ( _gameThread == null || !_gameThread.isAlive() ){
            _gameThread = new Thread( this );
            _gameThreadRunning = true;
            _gameThread.start();
        }
        
    }    
    
    /**
     * Main game look; handles user input, process all of the game objects (tick) 
     * (including some game logic) and renders the game objects onto the screen
     **/
    public void run(){
        if ( !initGame() )
            return;         
        
        _currTime = System.currentTimeMillis();
        
        long endTime;
        
        while( _gameThreadRunning ){            
            long elapsedTime = System.currentTimeMillis() - _currTime;
            _currTime += elapsedTime;
            
            // sleep game thread not focused or set to sleep
            if( _sleeping || _midlet.getActivatedDisplayable() != this ){
                _midlet.printDebug( this, "run", "Going to sleep");
                _sleeping = true;
                synchronized( this ){
                    try{
                        this.wait();
                    } catch( Exception e ){
                    }
                }
                _midlet.printDebug( this, "run", "Woken up");
            }                        
            
            // process state
            _stateTime += elapsedTime;
            if( _currentState == GS_NEWGAME ){
                if( _stateTime > GS_TIME_NEWGAME )
                    setState( GS_PLAYING );
            }
            else if( _currentState == GS_LEVEL_COMPLETED ){
                if( _stateTime > GS_TIME_LEVEL_COMPLETED )
                    advanceLevel(); // progress to the next level
            }
            else if( _currentState == GS_RESUME ){
                if( _stateTime > GS_TIME_RESUMING )
                    setState( GS_PLAYING );
            }
            
            
            // process game
            if( _currentState != GS_LOADING ){
                checkUserInput( elapsedTime );
                
                if( _currentState == GS_PLAYING ){
                    cycle( elapsedTime );
                }

                render();
                
                if( LittleRed.DEBUG )
                    updateFPS(); 
            }
            else{
                render(); 
            }
            
            syncGameLoop( _currTime );                        
            
        }
        
    }        
    
    public void advanceScore( int value, boolean byCoin ){
        if( byCoin )
            Stage.getInstance().coinCollected();
    
        _gamesScore += value;
        _levelsScore += value; 
    }
    
    public void newGame(){
        setState( GS_LOADING );
        _stage.newGame();      
    }
    
    public void levelCompleted(){
        setState( GS_LEVEL_COMPLETED ); 
    }
    
    private void advanceLevel(){
        setState( GS_LOADING );
        _stage.nextLevel();         
    }
    
    public void levelLoaded( int status ){
        if( status == Stage.STAGE_STATUS_LEVEL_LOADED ){
            setState( GS_NEWGAME ); 
        }
        else if( status == Stage.STAGE_STATUS_LEVEL_NOT_FOUND ){
            // assume the player has clocked the game 
            setState( GS_CLOCKED ); 
        }
    }
    
    public int getPreviousState(){
        return _prevState; 
    }
    
    public int getState(){
        return _currentState; 
    }
    
    public void setState( int newState ){
        if( _currentState == newState )
            return; 
        
        _midlet.printDebug( this, "setState", "State changed from " + _currentState + " to " + newState );
        
        _prevState = _currentState; 
        _currentState = newState;
        _stateTime = 0;
        
        switch( _currentState ){
            case GS_NEWGAME:
                // setup time                
                _currTime = System.currentTimeMillis();   
                
                if(_sleeping )
                    wakeup(); 
                break;
            case GS_PLAYING:
                
                break; 
            case GS_PAUSED:
                // save score 
                ScoreDAO.getInstance().addScore( _stage.getCurrentLevel(), _levelsScore );
                
                _sleeping = true; 
                break;
            case GS_RESUME:
                // setup time                
                _currTime = System.currentTimeMillis();   
                
                if(_sleeping )
                    wakeup();
                break;
            case GS_LOADING:
                break;
            case GS_GAMEOVER:
            case GS_CLOCKED:
                // save score 
                ScoreDAO.getInstance().addScore( _stage.getCurrentLevel(), _levelsScore );                
                break;                 
        }
                
    }
    
    public void sleep(){
        _sleeping = true; 
    }    
    
    /**
     * Wakeup thread
     **/ 
    public void wakeup(){
        _midlet.printDebug( this, "wakeup", "waking up!" ); 
        
        _currTime = System.currentTimeMillis();
        _sleeping = false;
        synchronized( this ){
            try{
                this.notifyAll();
            } catch( Exception e ){
                _midlet.printError( this, "wakeup", e.getMessage() ); 
            }
        }
    }
    
    public void stop(){
        _gameThreadRunning = false;
        synchronized( this ){
            try{
                this.notifyAll();
            }
            catch( Exception e ){}
        }
    }
    
    public void loadSavedGame( int level, int lives, int score ){
        setState( GS_LOADING );
        
        _gamesScore = score;        

        try{
            _stage.savedGame( level, lives );             
        }
        catch( Exception e ){
            e.printStackTrace(); 
        }
        System.err.println("working here");
    }
    
    public int getLevel(){
        return _stage.getCurrentLevel(); 
    }
    
    public int getLives(){
        return _stage.getHero().getLives(); 
    }
    
    public int getScore(){
        return _gamesScore;
    }
    
    public boolean isGameManagerInitilised(){
        return _gameManagerInitilised; 
    }
    
    //<editor-fold defaultstate="collapsed" desc="private methods">
    
    private void checkUserInput( long elapsedTime ){
        // if the stage is not loaded then do nothing 
        if( !_stage.getLevelLoaded() )
            return;
        
        int keyState = getKeyStates();
    
        if( _currentState == GS_PLAYING )
            _stage.getHero().handleInput( keyState ); 
    }
    
    private void cycle( long elapsedTime ){
        // if the stage is not loaded then do nothing 
        if( !_stage.getLevelLoaded() )
            return;        
        
        Hero hero = _stage.getHero();
        
        // *** update hero ***
        hero.cycle( elapsedTime );                 
        
        // *** update actors ***            
        Enumeration worldSprites = _stage.getWorldSprites();
        while( worldSprites.hasMoreElements() ){
            Sprite sprite = (Sprite)worldSprites.nextElement(); 
            
            if( sprite instanceof Baddie ){
                Baddie baddie = (Baddie)sprite; 
                baddie.cycle( elapsedTime );
                
                if( _stage.getHero().collidesWith( baddie, true ) && baddie.getState().getStateID() == Baddie.STATE_ATTACKING )
                    hero.takeDamage( 5 ); 
                    
            }
            else if( sprite instanceof Prop ){
                Prop prop = (Prop)sprite; 
                if( hero.collidesWith( prop, true ) )
                    prop.consume( _stage.getHero() );
                
                prop.cycle( elapsedTime ); 
            }
        }
        
        // *** update game effects ***
        Enumeration worldEffects = _stage.getWorldEffects();
        while( worldEffects.hasMoreElements() ){
            GameEffect effect = (GameEffect)worldEffects.nextElement(); 
            
            effect.cycle( elapsedTime ); 
        }                
                
        // *** update game projectiles *** 
        updateProjectiles( elapsedTime ); 
        
        // *** update world ***
        _stage.cycle( elapsedTime );                
        
    }
    
    private void updateProjectiles( long elapsedTime ){
        // *** update projectiles ***
        Enumeration projectiles = _stage.getProjectiles(); 
        while( projectiles.hasMoreElements() ){
            Projectile proj = (Projectile)projectiles.nextElement(); 
            
            if( !proj.isFree() ){
                proj.cycle( elapsedTime ); 
                
                // iterate through all the live actors and test for coliision with current projectile 
                Enumeration actors = _stage.getLiveActors(); 
                while( actors.hasMoreElements() ){
                    Actor actor = (Actor)actors.nextElement();
                    if( proj.collidesWith( actor, true ) ){
                        proj.consume( actor );
                    }
                }
            }
        }
    }
    
    private void render( ){
        if( _stage == null || _midlet.getActivatedDisplayable() != this )
            return; 
        
        Graphics g = getGraphics();         
        
        // clear screen
        g.setColor( 0x000000 );
        g.fillRect( 0, 0, getWidth(), getHeight() );                
        
        if( _currentState == GS_LOADING ){
            //drawLoading( g ); 
        }
        else if ( _stage.getLevelLoaded() ){  
            g.setColor( _stage.getBackgroundColour() );
            g.fillRect( 0, 0, getWidth(), getHeight() ); 
        
            _stage.paint( g, 0, 0, getWidth(), getHeight() ); 
            
            drawHUD( g );
            
            if( _currentState == GS_NEWGAME || _currentState == GS_RESUME ){
                String msg = "LEVEL " + _stage.getCurrentLevel(); 
                _fontManager.drawString( msg, CustomFont.FONT_LARGE,  
                        (getWidth()-_fontManager.getFontsWidth(msg, CustomFont.FONT_LARGE))/2,
                        (getHeight()-_fontManager.getFontsHeight( CustomFont.FONT_LARGE ))/2, g ); 
            }
            else if( _currentState == GS_GAMEOVER ){
                String msg = "GAME OVER"; 
                int tmpY = (getHeight()-_fontManager.getFontsHeight( CustomFont.FONT_LARGE ))/2; 
                _fontManager.drawString( msg, CustomFont.FONT_LARGE,  
                        (getWidth()-_fontManager.getFontsWidth(msg, CustomFont.FONT_LARGE))/2,
                        tmpY, g ); 
                
                msg = "FINAL SCORE " + _gamesScore; 
                tmpY += _fontManager.getFontsHeight( CustomFont.FONT_LARGE ) + 20; 
                _fontManager.drawString( msg, CustomFont.FONT_LARGE,  
                    (getWidth()-_fontManager.getFontsWidth(msg, CustomFont.FONT_LARGE))/2,
                    tmpY, g ); 
            }
            else if( _currentState == GS_LEVEL_COMPLETED ){                
                // notify that the player has passed the level 
                String msg = "LEVEL COMPLETED"; 
                int tmpY = (getHeight()-_fontManager.getFontsHeight( CustomFont.FONT_LARGE ))/2; 
                _fontManager.drawString( msg, CustomFont.FONT_LARGE,  
                    (getWidth()-_fontManager.getFontsWidth(msg, CustomFont.FONT_LARGE))/2,
                    tmpY, g );                
                
                // also tell them how much they scored 
                msg = "LEVEL SCORE " + _levelsScore; 
                tmpY += _fontManager.getFontsHeight( CustomFont.FONT_LARGE ) + 20; 
                _fontManager.drawString( msg, CustomFont.FONT_LARGE,  
                    (getWidth()-_fontManager.getFontsWidth(msg, CustomFont.FONT_LARGE))/2,
                    tmpY, g ); 
                
            }
        }
        else if( _currentState == GS_CLOCKED ){
            String msg = "YOU WIN"; 
            int tmpY = (getHeight()-_fontManager.getFontsHeight( CustomFont.FONT_LARGE ))/2; 
            _fontManager.drawString( msg, CustomFont.FONT_LARGE,  
                (getWidth()-_fontManager.getFontsWidth(msg, CustomFont.FONT_LARGE))/2,
                tmpY, g ); 
            
            msg = "FINAL SCORE " + _gamesScore; 
            tmpY += _fontManager.getFontsHeight( CustomFont.FONT_LARGE ) + 20; 
            _fontManager.drawString( msg, CustomFont.FONT_LARGE,  
                (getWidth()-_fontManager.getFontsWidth(msg, CustomFont.FONT_LARGE))/2,
                tmpY, g ); 
        }        
        
        // render fps at the bottom of the screen 
        if( LittleRed.DEBUG ){
            g.setColor( 0x000000 ); 
            g.drawString( "fps " + _fpsResult, getWidth()-50, getHeight()-(Font.getDefaultFont().getHeight()+30), Graphics.TOP | Graphics.LEFT ); 
        }
        
        flushGraphics();
    }
    
    /**
     *
     **/
    private void drawLoading( Graphics g ){        
        String msg = "LOADING"; 
        if( _tick > 15 )
            _tick = 0;
        
        if( _tick < 5 ){
            msg+= ".";
            _tick++; 
        }
        else if( _tick < 10 ){
            msg+= "..";
            _tick++; 
        }
        else{
            msg+= "...";
            _tick++;             
        }
        _fontManager.drawString( msg, CustomFont.FONT_LARGE,  
            (getWidth()-_fontManager.getFontsWidth(msg, CustomFont.FONT_LARGE))/2,
            (getHeight()-_fontManager.getFontsHeight( CustomFont.FONT_LARGE ))/2, g );
    }
    
    /**
     * Draw Heads up Display (things such as coins left, lives left, player mode, etc) 
     **/
    private void drawHUD( Graphics g ){
        int tmpX = 0;
        int tmpY = 0; 
        
        // draw lifes left         
        g.drawImage( _stage._imgIcon, 5, 5, Graphics.LEFT | Graphics.TOP );
        _fontManager.drawString( String.valueOf( Math.max( 0, _stage.getHero().getLives() ) ), CustomFont.FONT_SMALL, tmpX + 10 + _stage._imgIcon.getWidth(), 8, g );                                 
        
        // draw number of coins left in right corner 
        tmpX = getWidth()-30;         
        tmpY = 5; 
        g.drawImage( _stage._imgSingleCoin, tmpX, tmpY, Graphics.LEFT | Graphics.TOP ); 
        _fontManager.drawString( String.valueOf( _stage.coinsLeft() ), CustomFont.FONT_SMALL, tmpX + _stage._imgSingleCoin.getWidth() + 3, tmpY, g );
        
        // draw player mode (ie player has gun, fast shoes) in middle 
        if( _stage.getHero().getCurrentMode() ==  Hero.MODE_FAST ){
            tmpX -= (_stage._imgShoe.getWidth()+5);            
            if( (Hero.MODE_FAST_TIME-_stage.getHero().getCurrentModeTime()) <= Hero.MODE_WARNING_TIME ){
                _tick++; 
                if( _tick <= 2 ){
                    g.drawImage( _stage._imgShoe, tmpX, tmpY, Graphics.LEFT | Graphics.TOP ); 
                    _tick=0; 
                }
            }
            else{
                g.drawImage( _stage._imgShoe, tmpX, tmpY, Graphics.LEFT | Graphics.TOP ); 
            }
        }
        else if( _stage.getHero().getCurrentMode() ==  Hero.MODE_GUN ){
            tmpX -= (_stage._imgGun.getWidth()+5);
            
            if( (Hero.MODE_GUN_TIME-_stage.getHero().getCurrentModeTime()) <= Hero.MODE_WARNING_TIME ){
                _tick++; 
                if( _tick >= 2 ){
                    g.drawImage( _stage._imgGun, tmpX, tmpY, Graphics.LEFT | Graphics.TOP ); 
                    _tick=0; 
                }
            }
            else{
                g.drawImage( _stage._imgGun, tmpX, tmpY, Graphics.LEFT | Graphics.TOP ); 
            }
        }
        
        // draw level in upper left corner 
        _fontManager.drawString( "SCORE " + _gamesScore, CustomFont.FONT_SMALL, 
                5, getHeight()-(_fontManager.getFontsHeight(CustomFont.FONT_SMALL)+10),  g ); 
        
    }
    
    private boolean initGame(){
        _gameThreadRunning = true;
        
        try{
            _stage = new Stage( this.getWidth(), this.getHeight() ); 
        }
        catch( Exception e ){
            _midlet.printError( this, "initGame", e.getMessage() ); 
            return false; 
        }                      
        
        advanceLevel();         
        _gameManagerInitilised = true; 
        
        return true; 
    }
        
    private void syncGameLoop( long currTime ){
        // sleep if necessary to make a smooth framerate
        long endTime = System.currentTimeMillis() - currTime;
        //System.out.println( "Start Time = " + startTime + " End Time = " + endTime );
        if (endTime < (1000 / MAX_FPS)) {
            try {
                // frames per second
                _gameThread.sleep((1000 / MAX_FPS) - endTime);
            } catch (Exception e) { }
        } else {
            try {
                _gameThread.yield();
            } catch (Exception e) { 
                _midlet.printError( this, "syncGameLoop", e.getMessage() );
            }
        }
    }
    
    //<editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="fps">
    private static int _fpsCounter = 0;
    private static long _fpsStartTime = 0;
    private static long _fpsTotalTime = 0; 
    private static int _fpsResult = 0;
    
    private static void updateFPS(){
        long currentTime = System.currentTimeMillis();
        long frameTime = currentTime - _fpsStartTime; 
        _fpsStartTime = currentTime;                 
        
        _fpsTotalTime += frameTime; 
        if( _fpsTotalTime >= 1000 ){
            _fpsResult = _fpsCounter + 1; 
            _fpsCounter = 0;
            _fpsTotalTime = 0; 
        }
        
        _fpsCounter++; 
    }
    
    
    //</editor-fold>
    
}
