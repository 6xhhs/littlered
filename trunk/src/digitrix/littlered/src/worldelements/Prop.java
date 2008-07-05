/*
 * Prop.java
 *
 * Created on 17 May 2007, 08:17
 *
 */

package digitrix.littlered.src.worldelements;

import digitrix.littlered.src.GameManager;
import digitrix.littlered.src.worldelements.GameEffect;
import java.util.Enumeration;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 *
 * @author Josh
 */
public class Prop extends Sprite{
    
    //<editor-fold defaultstate="collapsed" desc="Prop types">
    
    // Non-interatice props
    public static final int PROP_TYPE_FLOWER_1          = 0;
    public static final int PROP_TYPE_FLOWER_2          = 1;
    public static final int PROP_TYPE_GRASS_1           = 2;
    public static final int PROP_TYPE_GRASS_2           = 3;
    public static final int PROP_TYPE_MUSHROOM          = 4;
    public static final int PROP_TYPE_JUNIOR            = 5;
    
    // Interactive props i.e. POWER-UPS
    public static final int PROP_TYPE_BLOCK_YELLOW      = 20;
    public static final int PROP_TYPE_BLOCK_BLUE        = 21;
    public static final int PROP_TYPE_BLOCK_GREY        = 22;
    public static final int PROP_TYPE_GUN               = 23;
    public static final int PROP_TYPE_SHOE              = 24;
    public static final int PROP_TYPE_COIN              = 25;
    public static final int PROP_TYPE_SPRING            = 26;
    public static final int PROP_TYPE_CHECKPOINT        = 27;
    public static final int PROP_TYPE_SAVE_POINT        = 28;
    public static final int PROP_TYPE_MOVEABLE_BOX      = 29; 
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Prop details">
    
    // frame selection
    private static final int PROP_FLOWER_1_FRAME        = 0;
    private static final int PROP_FLOWER_2_FRAME        = 2;
    private static final int PROP_GRASS_1_FRAME         = 1;
    private static final int PROP_GRASS_2_FRAME         = 4;
    private static final int PROP_MUSHROOM_FRAME        = 3;
    
    private static final int PROP_BLOCK_YELLOW_FRAME     = 0;   // vertical falling block ie when the player jumps on teh block the block will fall until it hits the ground
    private static final int PROP_BLOCK_BLUE_FRAME       = 1;   // vertical falling block ie when the player jumps on teh block the block will fall until it hits the ground
    private static final int PROP_BLOCK_GREY_FRAME       = 2;   // horitontal moving block that bounces off the walls
    
    // animation frame sequences
    private static final int[] PROP_COIN_ANIM           = {0,1,2,3};
    private static final int[] PROP_SPRING_ANIM         = {0,1,2};
    
    private static final int[] PROP_JUNIOR_ANIM         = {0,1,2,3,4,5};        
    
    // states
    public static final int STATE_SHOWING              = 0;
    public static final int STATE_CONSUMED             = 1;
    public static final int STATE_HIDDEN               = 2;
    public static final int STATE_HIDDING              = 3; // in this state the object will stay hidden for a predefine time    
    
    //</editor-fold>
    
    private int _startingX = 0;
    private int _startingY = 0;
    private int _speedX = 0;
    private int _speedY = 0;
    private int _repeats = -1; // -1 indicates that the animation continues to repeat indefinitely
    private int _repeated = 0; // holds how many items the prop has repeated
    private int _frameTime = 0; // how long each frame should display before moving onto the next one
    private long _elapsedFrameTime = 0; // holds how long the current frame has been shown for
    private long _travelledTimeStamp = 0; // the time of when the prop was consumed 
    private int _hideTime = 0;
    private long _elapsedHiddenTime = 0;
    private int _currentState    = 0; // state of the prop
    private int _propType = 0; // idicates the type of prop this is
    private boolean _grounded = false; // set true when the prop collides with the ground
    private boolean _floating = false; // if true then gravity does not affect this prop
    private Actor _playerOnProp = null; // used by the blocks so we can move the player with the blocks
    private boolean _inFront = true; // determines whether prop will be placed in front of the hero or behind
    private int _travellingXTime = 0; // how long the item will travel for along the x axis
    
    /** Creates a new instance of Prop */
    public Prop( Image img ){
        super( img );
    }
    
    public Prop( Image img, int width, int height) {
        super( img, width, height );
    }
    
    public void initProp( int propType, int posX, int posY ){
        setPropType(propType);
        setPosition( posX, posY );
        
        setStartingX(posX);
        setStartingY(posY);
        setElapsedFrameTime(0);
        setState( STATE_SHOWING );
        setGrounded(false);
        
        setHideTime(1000); // used in the Hidding state when the prop dissappears for x amount of milliseconds
        
        defineReferencePixel( this.getWidth()/2, this.getHeight()/2 );
        
        switch( getPropType() ){
            case PROP_TYPE_FLOWER_1:
                setFrame( PROP_FLOWER_1_FRAME );
                break;
            case PROP_TYPE_FLOWER_2:
                setFrame( PROP_FLOWER_2_FRAME );
                break;
            case PROP_TYPE_GRASS_1:
                setFrame( PROP_GRASS_1_FRAME );
                break;
            case PROP_TYPE_GRASS_2:
                setFrame( PROP_GRASS_2_FRAME );
                break;
            case PROP_TYPE_MUSHROOM:
                setFrame( PROP_MUSHROOM_FRAME );
                break;
            case PROP_TYPE_JUNIOR:
                setFrameTime(500); // display each frame for x milli seconds
                setRepeats(-1);  // repeat indefinitely
                setFrameSequence( PROP_JUNIOR_ANIM );
                break;
            case PROP_TYPE_BLOCK_YELLOW:
                setFrame( PROP_BLOCK_YELLOW_FRAME );
                _floating = true;
                _speedY = 1;
                break;
            case PROP_TYPE_BLOCK_GREY:
                setFrame( PROP_BLOCK_GREY_FRAME );
                _speedY = 2;
                _floating = true;
                break;
            case PROP_TYPE_BLOCK_BLUE:
                setFrame( PROP_BLOCK_BLUE_FRAME );
                _travellingXTime = 5000; 
                _floating = true;
                _speedX = -2;
                break;
            case PROP_TYPE_MOVEABLE_BOX:
                _speedY = 2; 
                break;
            case PROP_TYPE_GUN:
                break;
            case PROP_TYPE_SHOE:
                break;
            case PROP_TYPE_COIN:
                _floating = true; 
                setFrameTime(300);
                setRepeats(-1);
                setFrameSequence( PROP_COIN_ANIM );
                break;
            case PROP_TYPE_SPRING:
                setFrameTime(300);
                setRepeats(3);
                setRepeated(99);
                setFrameSequence( PROP_SPRING_ANIM );
                break;
            case PROP_TYPE_CHECKPOINT:                
                setState( STATE_HIDDEN );
                break;
            case PROP_TYPE_SAVE_POINT:
                _inFront = false;                 
                
                break;
        }
        
    }
    
    public void cycle( long elapsedTime ){
        
        //<editor-fold defaultstate="collapsed" desc="movement management (includes gravity)">
        
        // movement manegement
        // - performs some basic movement for a prop either a bouncing motion for the checkpoint
        //   or slow movement along one axis for a moving block.
        
        int dy = 0;
        int dx = 0;
        
        if( !_grounded && !_floating ){
            if( _speedY == 0 )
                dy = Stage.GRAVITY;
            else
                dy = _speedY;
            
            // process moving down/up the y axis
            int[] tile = Stage.getInstance().getTileCollision( this, getX(), getY() + dy );
            
            if( tile == null ){
                setPosition( getX(), getY() + dy );
                
                // move player along with the block
                if( _playerOnProp != null && (_propType == PROP_TYPE_BLOCK_YELLOW || _propType == PROP_TYPE_BLOCK_GREY ||
                        _propType == PROP_TYPE_MOVEABLE_BOX ) ){
                    _playerOnProp.move( 0, (dy-1) );
                    _playerOnProp = null;
                }
            } else{
                // line up with tile boundary
                if ( dy > 0 ){
                    // moving down
                    setPosition( getX(), Stage.getInstance().tilesYToPixels(tile[1]) - getHeight() );
                } else if ( dy < 0 ){
                    // moving up
                    setPosition( getX(), Stage.getInstance().tilesYToPixels(tile[1] + 1 ) );
                }
                _grounded = true;
                
                // if of type block then disapear and show some sort of effect
                if( _propType == PROP_TYPE_BLOCK_GREY || _propType == PROP_TYPE_BLOCK_YELLOW ){
                    // game was a little hard when the block was removed completely (in such a way that the
                    // user was forced to commit suicide and start from the beginning again)
                    // therefore instead of removing the block completely we will re-initilize it
                    Stage.getInstance().addGameEffect( getX(), getY(), GameEffect.GAME_EFFECT_EXPLOSION );
                    initProp( _propType, _startingX, _startingY );
                }
                // if a moving box and we have landed on water or spikes then take the box off the stage
                else if( _propType == PROP_TYPE_MOVEABLE_BOX ){
                    // TODO: Implement this  
                    if ( Stage.getInstance().isTileDangerous( tile[0], tile[1] ) ){
                        Stage.getInstance().addGameEffect( getX(), getY(), GameEffect.GAME_EFFECT_EXPLOSION );
                        setState( STATE_CONSUMED ); 
                    }                    
                }
            }
        }
        
        // horizontal moving block(s)
        if( _speedX != 0 ){                        
            dx = _speedX;
            
            if( _playerOnProp != null && _propType == PROP_TYPE_BLOCK_BLUE ){                                
                int[] tile = Stage.getInstance().getTileCollision( this, getX() + dx, getY());

                // if been travelling long ensure then change _speedX 
                if( _travellingXTime != 0 ){
                    if( _travelledTimeStamp == 0 ){
                        _travelledTimeStamp = System.currentTimeMillis(); 
                    }
                    else if( _travelledTimeStamp + _travellingXTime < System.currentTimeMillis() ){
                        System.out.println("current time = " + System.currentTimeMillis() + " travellingtime = " + (_travelledTimeStamp + _travellingXTime) );
                        _travelledTimeStamp = System.currentTimeMillis(); 
                        _speedX = -1*_speedX; 
                    }
                }
                
                if( tile == null ){                                        
                    setPosition( getX() + dx, getY() );

                    // move player along with the block
                    _playerOnProp.move( dx, 0 );
                    _playerOnProp = null;
                } else{
                    // line up with tile boundary
                    if ( dx > 0 ){
                        // moving down
                        setPosition( Stage.getInstance().tilesXToPixels( tile[0] ) - (getWidth() + 1), getY() );
                    } else if ( dx < 0 ){
                        // moving up
                        setPosition( Stage.getInstance().tilesXToPixels( tile[0] + 1 ) + 1, getY() );
                    }

                    // reverse up if we have hit the wall...
                    _speedX = _speedX*-1;
                }
            }
        }
        
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="prop animation management">
        
        // is this prop associated with a animation
        if( _currentState == STATE_SHOWING && _frameTime > 0 && (_repeats == -1 || _repeats > _repeated ) ){
            _elapsedFrameTime += elapsedTime;
            
            if( _elapsedFrameTime > _frameTime ){
                
                nextFrame();
                _elapsedFrameTime = 0;
                
                // are we starting from the start? if so then we want to manage the repeats if the prop
                // has a limited number of repeats
                if( getFrame() == 0 ){
                    
                    if( _repeats != -1 && _repeats > _repeated ){
                        _repeated ++;;
                    }
                }
            }
        }
        
        //<editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="state management">
        
        // if we are in a hidding state then increment the elapsed hidden time, if lapsed over the hide time then
        // change the state back to normal/visible
        if( _currentState == STATE_HIDDING ){
            _elapsedHiddenTime += elapsedTime;
            if( _elapsedHiddenTime > _hideTime ){
                setState( STATE_SHOWING );
            }
        }
        
        // if the prop is a checkpoint and the user has collected all coins then display the checkpoint
        if( _propType == PROP_TYPE_CHECKPOINT ){
            if( Stage.getInstance().coinsLeft() == 0 )
                setState( STATE_SHOWING );
        }
        
        //</editor-fold>
    }
    
    /**
     * Called when the player collides with a prop which is a type of
     * POWER-UP!
     **/
    public void consume( Actor actor ){
        // on this game only allow the hero to consume the prop/power-up
        if( _propType < 20 || !isVisible() )
            return;       
        
        switch( _propType ){
            case PROP_TYPE_BLOCK_YELLOW:
            case PROP_TYPE_BLOCK_GREY:
                consumeMovingBlock( actor, true );
                break; 
            case PROP_TYPE_BLOCK_BLUE:
                consumeMovingBlock( actor, false );
                break;
            case PROP_TYPE_GUN:
                if( actor instanceof Hero ){
                    ((Hero)actor).setMode( Hero.MODE_GUN );
                }
                setState( STATE_CONSUMED );
                break;
            case PROP_TYPE_SHOE:
                if( actor instanceof Hero ){
                    ((Hero)actor).setMode( Hero.MODE_FAST );
                }
                setState( STATE_CONSUMED );               
                break;
            case PROP_TYPE_COIN:
                consumeCoin(); 
                break;
            case PROP_TYPE_SPRING:
                consumeSpring( actor );
                break;
            case PROP_TYPE_CHECKPOINT:
                consumeCheckpoint();
                break;
            case PROP_TYPE_SAVE_POINT:
                consumeStartingPoint(); 
                break;
            case PROP_TYPE_JUNIOR:
                consumeJunior(); 
                break;
            case PROP_TYPE_MOVEABLE_BOX:
                consumeMovingBox( actor );
                break;
        }
        
    }
    
    //<editor-fold defaultstate="collapsed" desc="comumption methods">
    
    /**
     * Method called on acquire when user collides with this prop and
     * the type of this prop is a Coin...
     * Here we will hide the coin, add an effect (points effect) and increment
     * the players score by the amount for a Coin.
     **/
    private void consumeCoin(){            
        setState( STATE_CONSUMED );
        Stage.getInstance().addGameEffect( getX(), getY(), GameEffect.GAME_EFFECT_5_POINTS );
        GameManager.getInstance().advanceScore( Stage.COIN_POINTS, true );
    }    
    
    /**
     * Called called when player consumes/collides with junior
     **/ 
    private void consumeJunior(){
        
    }
    
    /**
     * This is basically a inlevel check point; eg when the player consumes this prop then the players starting position for 
     * this level is updated to the position of the prop. 
     **/
    private void consumeStartingPoint(){
        Stage.getInstance().getHero().setStartingX( getX() );
        Stage.getInstance().getHero().setStartingY( getY() );
    }
    
    private void consumeCheckpoint(){
        setState( STATE_CONSUMED ); 
        GameManager.getInstance().levelCompleted();
    }
    
    private void consumeMovingBlock( Actor actor, boolean removeFloating ){
        
        // only activate/aquire prop if velocity is positive i.e. only when walked on top
        // of jumped on (moving down and not jumping through).
        
        // If the player is not on top of the block...
        if( actor.getVelocityY() >= 0 && actor.getY() < (this.getY()-(this.getHeight()/3) )  ){
            // re-position the actor so that he/she is standing directly on top and
            // stop the player from falling through by calling collideVertical
            actor.setPosition( actor.getX(), ((this.getY()-(this.getHeight()/2))-(actor.getHeight()/2)+4) );
            actor.collideVertical();
            
            _playerOnProp = actor;
            _playerOnProp.setOnGround( true );
        }
        
        if( _propType == PROP_TYPE_BLOCK_GREY || _propType == PROP_TYPE_BLOCK_YELLOW ){
            _floating = false;
        }
        else if( _propType == PROP_TYPE_BLOCK_BLUE ){
            // associate the direction with the block with the direction of the player
            /*if( actor.isFacingLeft() ){
                _speedX = -1*_speedX; 
            }*/
                
        }
    }
    
    private void consumeMovingBox( Actor actor ){
        int speedX = 0;
        
        // If the player is not on top of the block...
        if( actor.getVelocityY() >= 0 && actor.getY() < (this.getY()-(this.getHeight()/3) )  ){
            // re-position the actor so that he/she is standing directly on top and
            // stop the player from falling through by calling collideVertical
            actor.setPosition( actor.getX(), (this.getY()-actor.getHeight())+1 );
            actor.collideVertical();
            
            _playerOnProp = actor;           
        }
        else if( actor.getVelocityX() < 0 ){
            // moving left 
            // slow down actor and move block with actor
            speedX = -2;             
        }
        else if( actor.getVelocityX() > 0 ){
            // moving right
            // slow down actor and move block with actor
            speedX = 2;            
        }
        
        if( speedX != 0 ){
            
            int newX = getX()+speedX;  
            int oldX = getX(); 
            setPosition( newX, getY() );                        
            actor.setVelocityX( speedX );            
            
            int tile[] = Stage.getInstance().getTileCollision( this, newX, getY() );
            
            // ensure that the box has not collided with any of the other props 
            boolean collidedWithSprite = false;
            
            Enumeration worldSprites = Stage.getInstance().getWorldSprites();
            
            while( worldSprites.hasMoreElements() && !collidedWithSprite ){
                Sprite sprite = (Sprite)worldSprites.nextElement(); 

                if( sprite instanceof Prop && sprite != this ){
                    if( this.collidesWith( sprite, false ) ){
                        collidedWithSprite = true; 
                    }                
                }
            }

            if ( tile != null || collidedWithSprite ){
                setPosition( oldX, getY() );                        
                actor.collideHorizontal();            
            }
        
            // align actor to the block
            if( speedX > 0 ){
                // align to the left of the box
                actor.setPosition( getX()-actor.getWidth(), actor.getY() ); 
            }
            else{
                // align to the right of the box 
                actor.setPosition( getX()+getWidth(), actor.getY() ); 
            }
            
            // if we have ran off the edge then set gounrded to false 
            if( Stage.getInstance().getTile( Stage.getInstance().pixelsXToTiles(getX()), Stage.getInstance().pixelsXToTiles(getY())+1 ) == 0 ) 
                _grounded = false;                         
        }
        
    }
    
    private void consumeSpring( Actor actor ){
        if( actor.isJumping() && actor.getVelocityY() > 0 ){
            setState( Actor.STATE_JUMPING );
            actor.setVelocityY( Math.max( actor.getVelocityY() * -2, -20 ) );
        }
    }
    
    //</editor-fold>
    
    
    //<editor-fold defaultstate="collapsed" desc="Mutators">
    
    public int getSpeedX(){
        return _speedX;
    }
    
    public void setSpeedX( int speed ){
        _speedX = speed;
    }
    
    public int getSpeedY(){
        return _speedY;
    }
    
    public void setSpeedY( int speed ){
        _speedY = speed;
    }
    
    public boolean isPowerUp(){
        return _propType >= 20;
    }
    
    public int getStartingX() {
        return _startingX;
    }
    
    public void setStartingX(int startingX) {
        this._startingX = startingX;
    }
    
    public int getStartingY() {
        return _startingY;
    }
    
    public void setStartingY(int startingY) {
        this._startingY = startingY;
    }
    
    public int getRepeats() {
        return _repeats;
    }
    
    public void setRepeats(int repeats) {
        this._repeats = repeats;
    }
    
    public int getRepeated() {
        return _repeated;
    }
    
    public void setRepeated(int repeated) {
        this._repeated = repeated;
    }
    
    public int getFrameTime() {
        return _frameTime;
    }
    
    public void setFrameTime(int frameTime) {
        this._frameTime = frameTime;
    }
    
    public long getElapsedFrameTime() {
        return _elapsedFrameTime;
    }
    
    public void setElapsedFrameTime(long elapsedFrameTime) {
        this._elapsedFrameTime = elapsedFrameTime;
    }
    
    public int getHideTime() {
        return _hideTime;
    }
    
    public void setHideTime(int hideTime) {
        this._hideTime = hideTime;
    }
    
    public long getElapsedHiddenTime() {
        return _elapsedHiddenTime;
    }
    
    public void setElapsedHiddenTime(long elapsedHiddenTime) {
        this._elapsedHiddenTime = elapsedHiddenTime;
    }
    
    public int getCurrentState() {
        return _currentState;
    }
    
    public void setState(int newState) {
        if( newState == STATE_HIDDING ){
            _elapsedHiddenTime = 0;
            setVisible( false );
        }
        else if( newState == STATE_CONSUMED || newState == STATE_HIDDEN )
            setVisible( false );
        else if ( newState == STATE_SHOWING )
            setVisible( true );

        this._currentState = newState;               
    }
    
    public int getPropType() {
        return _propType;
    }
    
    public void setPropType(int propType) {
        this._propType = propType;
    }
    
    public boolean isGrounded() {
        return _grounded;
    }
    
    public void setGrounded(boolean grounded) {
        this._grounded = grounded;
    }
    
    public boolean isInFront() {
        return _inFront;
    }

    public void setInFront(boolean inFront) {
        this._inFront = inFront;
    }
    
    //</editor-fold> 
    
}
