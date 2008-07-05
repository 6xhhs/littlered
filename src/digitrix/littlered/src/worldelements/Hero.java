/*
 * Hero.java
 *
 * Created on 14 May 2007, 20:47
 *
 */

package digitrix.littlered.src.worldelements;

import digitrix.littlered.src.GameManager;
import digitrix.littlered.src.LittleRed;
import digitrix.littlered.src.worldelements.GameEffect;
import java.util.Enumeration;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;
import java.util.Vector;

/**
 *
 * @author Josh
 */
public class Hero extends Actor {
    
    // user attributes
    public static final int ATT_MAX_HEALTH  = 10;
    public static final int ATT_LIVES       = 3;
    public static final int ATT_SPEED_X     = 8;
    public static final int ATT_SPEED_Y     = -10; 
    
    public static final int MODE_NORMAL     = 0;
    public static final int MODE_FAST       = 1;
    public static final int MODE_GUN        = 2;
    
    private static final int ATTACK_DELAY = 150; // how long the player is invinsible after being attacked
    private static final int FLASHING_COUNTER = 2;    
    private int _flashingTicks = 0;
    
    // length of time a mode can run for    
    //public static final int MODE_FAST_TIME = 2000; 
    public static final int MODE_FAST_TIME = 15000; 
    public static final int MODE_WARNING_TIME = 1000; // this is the time in which the hud will start flashing the icon to indicate that the mode is about to change
    public static final int MODE_GUN_TIME  = 15000; 
    
    private int _currentMode = 0;
    private long _currentModeTime = 0; 
    private Vector _modes = null; 
    
    private long _lastFiredCount = 1500; 
    private long _fireDelay = 900; 
    
    private boolean _canBeDamaged = false; 
    private long _lastAttackTime = 0;    
    
    private int _orgSpeedX = 0; 
    private int _orgSpeedY = 0; 
    
    // speed up and slow down variables    
    private final int VELOCITY_TICK_REQUIRED = 6;
    private int _velocityTick = 0;
    
    // additional attributes
    private boolean _doubleJumped = false;
    private boolean _allowDoubleJump = false; 
    
    /** Creates a new instance of Hero */
    public Hero( Image img, int frameWidth, int frameHeight ) {        
        super( img, frameWidth, frameHeight );
        
        // add the inital image and it's associated frame width and height as this will be the default one
        addMode( MODE_NORMAL, img, frameWidth, frameHeight );
    }
    
    /**
     * Initilise the actor; usually called when creating a new actor or reusing an
     * existing actor
     * @param maxHealth assigns the maximum hit points this actor has
     * @param lives defines how many chances this actor has in life
     * @param hSpeed assigns the horizontal speed of the actor (jumping capability of the actor)
     * @param vSpeed assigns the vertical speed of the actor (the initial vertical velocity of a jump)
     * @param posX assigns the starting x position
     * @param posY assigns the starting y position
     **/
    public void initActor( int maxHealth, int lives, int xSpeed, int vSpeed, int posX, int posY, boolean facingLeft ) throws Exception {
        _orgSpeedX = xSpeed;
        _orgSpeedY = vSpeed; 
        _velocityTick = 0; 
        _flashingTicks = 0;
        
        _canBeDamaged = true; 
        _lastAttackTime = 0;    
        
        super.initActor( maxHealth, lives, xSpeed, vSpeed, posX, posY, facingLeft );
    }    
    
    /**
     * Because LittleRed can have multiple images associated with him (ie images with varying frame and heights) and because we 
     * want to extend the Sprite class to take advantage of any optimization that maybe supported on the phone we will store 
     * the images and their associated frame widths and heights and the actor state will refer to a animation based on its animation index
     **/
    public void addMode( int modeIndex, Image img, int frameWidth, int frameHeight ){
        if( _modes == null )
            _modes = new Vector(); 
        
        Object[] mode = new Object[3]; 
        mode[0] = img;
        mode[1] = Integer.toString( frameWidth );
        mode[2] = Integer.toString( frameHeight ); 
        
        _modes.insertElementAt( mode, modeIndex );          
    }
    
    public int getCurrentMode(){
        return _currentMode; 
    }
    
    /**
     * This will be called from the game manager/prop object when the player picks up a 'power-up'; 
     * The method is responsible for checking to ensure that the current mode exists (via its index) and 
     * the mode is currently not set - if true then change the object of the sprite (animations/state should still be 
     * the same ie the images should corrospond to each other)
     **/
    public void setMode( int modeIndex ){
        
        if( _modes.size() < modeIndex || modeIndex == _currentMode )
            return;
        
        Object[] mode = (Object[])_modes.elementAt(modeIndex); 

        try{
            int width = Integer.parseInt((String)mode[1]);
            int height = Integer.parseInt((String)mode[2]); 
            Image img = (Image)mode[0]; 
            setImage( img, width, height ); 
        }
        catch( Exception e ){
            LittleRed.getInstance().printError( this, "setCurrentMode", e.toString() );             
            return; 
        }
        
        
        _currentMode = modeIndex;        
        _currentModeTime = 0; 
        
        // modify actor attributes based on the mode
        if( modeIndex == MODE_NORMAL ){
            setSpeedX( _orgSpeedX );
            setSpeedY( _orgSpeedY );
        }
        else if( modeIndex == MODE_FAST ){
            setSpeedX( _orgSpeedX+3 );
            setSpeedY( _orgSpeedY-3 );
        }
        else if( modeIndex == MODE_GUN ){
            setSpeedX( _orgSpeedX-2 );
            setSpeedY( _orgSpeedY+2 );
            
            _lastFiredCount = 0; 
        }
        
    }

    //<editor-fold defaultstate="collapsed" desc="Actor abstract methods">
    
    /**
     * If state is dying or dead then do nothing ,
     * If left or right is pressed then either speed up or slow down (depending on the current velocity) 
     * If fire is pressed and the hero is in gun mode then fire a projectile
     **/
    public void handleInput(int keyState) {
        int currentStateID = _currentState.getStateID(); 
        
        if( currentStateID == STATE_DYING || currentStateID == STATE_DEAD )
            return; 
        
        if( !Stage.getInstance().isHeroInView() )
            return; 
        
        if ( (keyState & GameCanvas.LEFT_PRESSED) != 0 ) {
                // if running left already then increase velocity up to a maximum of the 
                // speedX variable else increase speed to one (slow the movement of left down)
                if( getVelocityX() < 0 ){
                    if( ++_velocityTick >= VELOCITY_TICK_REQUIRED ){
                        setVelocityX( getVelocityX()-1 );           
                        _velocityTick = 0;
                    }
                    else{
                        setVelocityX( getVelocityX() ); 
                    }
                }
                else{                          
                    setVelocityX( getVelocityX() -1 ); 
                    _velocityTick = 0;                                        
                }
            }
            else if ( (keyState & GameCanvas.RIGHT_PRESSED) != 0 ) {
                // if running left already then increase velocity up to a maximum of the 
                // speedX variable else increase speed to one (slow the movement of left down) 
                if( getVelocityX() > 0 ){
                    if( ++_velocityTick >= VELOCITY_TICK_REQUIRED ){
                        setVelocityX( getVelocityX()+1 );           
                        _velocityTick = 0;
                    }
                    else{
                        setVelocityX( getVelocityX() ); 
                    }
                }
                else{
                     setVelocityX( getVelocityX() +1 ); 
                    _velocityTick = 0;                                                                                
                }
            }
            else{
                // slow user down to a velocity of 0
                int dx; 
                if( getVelocityX() < 0 ){
                    dx = getVelocityX() + 1;
                    if( dx > 0 )
                        dx = 0;
                    
                    setVelocityX( dx );
                }
                else if( getVelocityX() > 0 ){
                    dx = getVelocityX() - 1;
                    if( dx < 0 )
                        dx = 0;
                    
                    setVelocityX( dx );
                }
            }
            
            // ************* HANDLE JUMPING ******************
            if ( (keyState & GameCanvas.UP_PRESSED) != 0 )
                jump(false);
        
            if( (keyState & GameCanvas.FIRE_PRESSED) != 0 && _currentMode == MODE_GUN )
                fire(); 
            else if( (keyState & GameCanvas.FIRE_PRESSED) != 0 )
                jump( false );
                        
    }        

    public void stateComplete(ActorState state) {
        
        if( state.getStateID() == STATE_DYING )
        {
            // subtract 1 life; if less than 0 then the player is dead - game over 
            // else increase the players engery levels and place back to the startx and starty positions 
            if( --_lives < 0 ){
                GameManager.getInstance().setState( GameManager.GS_GAMEOVER ); 
            }
            else{
                reset(); 
            }
        }
    }
        
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Override Actor methods">
    
    public void reset(){
        setMode( MODE_NORMAL );        
        transform( true );        
        super.reset(); 
    }
    
    public void setState( int stateID ){        
        super.setState( stateID );
    }
    
    public void displayDyingAnim( ){
        Stage.getInstance().addGameEffect( getX(), getY(), GameEffect.GAME_EFFECT_LASER_EXPLOSION ); 
    }
    
    /** override this method to ensure that the user cannot be constantly damaged ie cloud does kill him when falling through the sky **/ 
    public void takeDamage( int damage ){
        
        if( _canBeDamaged ){
            _canBeDamaged = false; 
            _flashingTicks = 0;
            _lastAttackTime = System.currentTimeMillis(); 
            
            super.takeDamage( damage );             
        }
    }
    
    public void cycle( long elapsedTime ) {
        // manage actors mode
        _currentModeTime += elapsedTime; 
        
        // manage attacking 
        if( !_canBeDamaged && _currentState.getStateID() != STATE_DYING && _currentState.getStateID() != STATE_DEAD ){
            if( System.currentTimeMillis() > (_lastAttackTime + ATTACK_DELAY) ){
                _canBeDamaged = true; 
                setVisible( true ); 
            }
        }
        
        int currentStateID = _currentState.getStateID(); 
        
        // if the player is dying or dead then cycle the actor method 
        // and exit out of thie method as we don't want to process any logic when the player is dying or dead
        if( currentStateID == STATE_DYING || currentStateID == STATE_DEAD ){
            super.cycle( elapsedTime );
            _facingLeft = true; 
            transform( true ); 
            return; 
        }
        
        // state management 
        if( _velocityY > 0 ){ // falling
            if( currentStateID != STATE_FALLING )
                setState( STATE_FALLING );
        } 
        else if( _velocityY < 0 ){ // jumping
            if( currentStateID != STATE_JUMPING )
                setState( STATE_JUMPING );
        }
        else if( _currentMode != MODE_GUN && _velocityX < 0 && _velocityX == (-1*_speedX) ){ // sprinting left
            if( currentStateID != STATE_RUNNING ){
                // add smoke effect if on the ground
                if( _onGround )
                    Stage.getInstance().addGameEffect( this.getX(), this.getY() + 10, GameEffect.GAME_EFFECT_SMOKE );              
                
                setState( STATE_RUNNING );
            }
        }
        else if( _velocityX < 0 ){ // running left
            if( currentStateID != STATE_WALKING )
                setState( STATE_WALKING );
        }
        else if( _currentMode != MODE_GUN && _velocityX > 0 && _velocityX == _speedX ){ // sprinting right
            if( currentStateID != STATE_RUNNING ){
                // if reached top speed then display the smoke effect 
                if( _onGround )
                    Stage.getInstance().addGameEffect( this.getX(), this.getY() + 10, GameEffect.GAME_EFFECT_SMOKE );              
                
                setState( STATE_RUNNING );
            }
        }
        else if( _velocityX > 0 ){ // running right
            if( currentStateID != STATE_WALKING )
                setState( STATE_WALKING );
        }
        else if( _velocityX == 0 ){
            if( currentStateID != STATE_STANDING )
                setState( STATE_STANDING );
        }
        
        // change mode if the time has ellapsed 
        if( _currentMode == MODE_FAST ){
            if( _currentModeTime >= MODE_FAST_TIME )
                setMode( MODE_NORMAL );
        }
        else if( _currentMode == MODE_GUN ){
            _lastFiredCount += elapsedTime; 
            
            if( _currentModeTime >= MODE_GUN_TIME )
                setMode( MODE_NORMAL ); 
        }      
        
        // if attacked then flash the player ot indicate that the player has been hurt and that the 
        // player is invisible for a short time 
        if( !_canBeDamaged ){
            _flashingTicks++;
            if( _flashingTicks > FLASHING_COUNTER ){
                if( isVisible() )
                    setVisible( false );
                else
                    setVisible( true ); 
                _flashingTicks = 0; 
            }
        }
                
        // call super classes cycle method (will handle moving etc) 
        super.cycle( elapsedTime ); 
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Helper methods">
    
    private void fire(){
        
        if( _fireDelay > _lastFiredCount )
            return; 
        
        _lastFiredCount = 0; 
        
        if( this._facingLeft )
            Stage.getInstance().addProjectile( getX() - 7 + _velocityX, getY()+6, Projectile.PROJECTILE_TYPE_BULLET, Projectile.PROJECTILE_DIRECTION_LEFT, this );
        else
            Stage.getInstance().addProjectile( getX() + 7 + _velocityX, getY()+6, Projectile.PROJECTILE_TYPE_BULLET, Projectile.PROJECTILE_DIRECTION_RIGHT, this );
    }
    
    public boolean canAttack(){
        return !_onGround && _velocityY > 0;
    }        
    
    /**
     *
     **/
    public void jump( boolean forceJump ){
        if( _onGround || forceJump && _velocityY == 0 ){
            _onGround = false;
            setVelocityY( _speedY );
        }
        else if( !_doubleJumped && getVelocityY() > 0 && _allowDoubleJump ){
            _doubleJumped = true; 
            setVelocityY( getVelocityY() - Math.abs(_speedY) );            
        }
    }    
    
    public void collideVertical(){
        int currentStateID = _currentState.getStateID();
        
        if( currentStateID == STATE_FALLING )
            setState( STATE_STANDING );
        
        super.collideVertical(); 
    }
    
    public long getCurrentModeTime(){
        return _currentModeTime; 
    }
    
    //</editor-fold>   
    
}
