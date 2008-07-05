/*
 * Actor.java
 *
 * Created on 14 May 2007, 20:15
 *
 */

package digitrix.littlered.src.worldelements;

import digitrix.littlered.src.LittleRed;
import java.util.Enumeration;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;
import java.util.Vector;

/**
 *
 * @author Josh
 *
 * Managers a specific sprite/game actor and is also responsible for 
 * state management 
 */
public abstract class Actor extends Sprite {
    
    //<editor-fold defaultstate="collapsed" desc="Actor States">
    
    public static final int STATE_STANDING      = 0;
    public static final int STATE_WALKING       = 1;
    public static final int STATE_RUNNING       = 2;
    public static final int STATE_ATTACKING     = 3;
    public static final int STATE_DYING         = 4;
    public static final int STATE_DEAD          = 5;
    public static final int STATE_JUMPING       = 6;
    public static final int STATE_FALLING       = 7; 
    public static final int STATE_ATTACKED      = 8;
    
    //</editor-fold>
    
    // attributes of the actor 
    protected int _maxHealth;
    protected int _speedX;
    protected int _speedY;
    protected int _startingX;
    protected int _startingY;    
    
    // current state of the actor 
    protected int _health;
    protected int _lives;
    protected int _velocityX;
    protected int _velocityY;    
    protected boolean _facingLeft = false;
    protected boolean _allowTransform = true; 
    protected boolean _canFly = false;          // set true if the actor can fly; if this is the case then gravity will not affect the actor
    protected boolean _onGround = false; 
    protected boolean _updateWhenInView = false; // if set to true then the cycle method will only be called when the actor is visible on stage to the user
    protected boolean _initialInitilisationDone = false; 
    protected int _blinkTimmer = -1;
    
    protected Vector _states;
    protected ActorState _currentState;
    
    /** Creates a new instance of Actor */
    public Actor( Image img, int frameWidth, int frameHeight) {
        super( img, frameWidth, frameHeight );                 
        
        defineReferencePixel( this.getWidth()/2, this.getHeight()/2 );
        setVisible( false );
        
        _states = new Vector();
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
        _maxHealth = maxHealth;
        _lives = lives;
        _speedX = xSpeed;
        _speedY = vSpeed;
        _startingX = posX;
        _startingY = posY;
        setFacingLeft(facingLeft);
        _onGround = true; 
        
        // ensure we have a standing state; otherwise throw an exception
        if( _states.size() <= 0 )
            throw new Exception( "Actor States have not been initiated" );
        
        _initialInitilisationDone = true; 
        reset();
    }
    
    /** only called when re-initilising the actor - ie if we are wanting to reuse a instance of the actor **/ 
    public void initActor( int posX, int posY, boolean facingLeft ) throws Exception {
        // if the actor has not be formally initilised (ie max health, speed etc has not been set) 
        // then throw an exception as this method should only be called when re-initilising the 
        // actor 
        if( !_initialInitilisationDone )
            throw new Exception( "Actor has not been formally initilised" ); 
        
        _startingX = posX; 
        _startingY = posY;
        setFacingLeft(facingLeft);        
        
        setState( STATE_STANDING ); 
        
        reset(); 
    }
    
    /**
     * Reset the Actor back to its initial state
     **/
    public void reset() {
        setHealth(getMaxHealth());
        setVelocityX(0);
        setVelocityY(0);
                
        setState( STATE_STANDING );
        
        setPosition( getStartingX(), getStartingY());
        setVisible( true );
    }
    
    public void cycle( long elapsedTime ){
        
        // cycle the current state
        _currentState.cycle( elapsedTime );
        
        if( _currentState.getStateID() == STATE_DEAD )
            return;
        
        transform( false ); // transform image depending on the actors velocity
        
        // apply gravity if creature if not flying
        if ( !getCanFly() ){
            setVelocityY( getVelocityY() + Stage.GRAVITY);
        }
        
        // change x
        int dx = getVelocityX();
        int oldX = getX();
        int newX = oldX + dx;
        
        int tile[] = Stage.getInstance().getTileCollision( this, newX, getY() );
        
        if (tile == null ){
            setPosition( newX, getY() );
        }
        else{
            // line up with tile boundary                        
            if( dx > 0 ){ // running right               
                setPosition( Stage.getInstance().tilesXToPixels( tile[0] ) - (getWidth() + 1), getY() );
            }
            else if ( dx < 0 ){
                setPosition( Stage.getInstance().tilesXToPixels( tile[0] + 1 ) + 1, getY() );                
            }            
            
            collideHorizontal();
            
        }                         
        
        // change y
        int dy = getVelocityY();
        int oldY = getY(); 
        int newY = oldY + dy; 
        tile = Stage.getInstance().getTileCollision( this, getX(), newY );
        
        if ( tile == null ){
            setPosition( getX(), newY );
        }
        else{
            int selTile = Stage.getInstance().getTile( tile[0], tile[1] ); 
            
            if( selTile == Stage.TILE_SPIKE ){
                takeDamage( _maxHealth ); 
            }
            else if( selTile == Stage.TILE_WATER_1 || selTile == Stage.TILE_WATER_2 ){
                takeDamage( _maxHealth ); 
            }
            else{
                // line up with tile boundary
            if ( dy > 0 ){
                // moving down
                setPosition( getX(), Stage.getInstance().tilesYToPixels(tile[1]) - getHeight() );
            }
            else if ( dy < 0 ){
                // moving up
                setPosition( getX(), Stage.getInstance().tilesYToPixels(tile[1] + 1 ) );
            }
            collideVertical();            
            }
        }        
    }
    
    /**
     * Only right direction based sprites are stored in the image, therefore to make
     * our actor face left we must transform the image
     **/
    public void transform( boolean forced ){
        //setRefPixelPosition( getWidth()/2, getHeight()/2 );
        
        if ( (_velocityX > 0 || forced ) && _facingLeft ){
            _facingLeft = false;
            
            if( _allowTransform ){
                move( 1, 0 );  // step back a little so we don't collide with any objects we might be standing next to
                setTransform( this.TRANS_NONE );                         
            }
        } else if ( (_velocityX < 0  || forced ) && !_facingLeft ){
            _facingLeft = true; 
            
            if( _allowTransform ){
                move( -1, 0 );  // step back a little so we don't collide with any objects we might be standing next to
                setTransform( this.TRANS_MIRROR );
            }
        }
    }
    
    /**
     * Called before update() if the actor collided with a
     * tile horizontally.
     */
    public void collideHorizontal() {
        setVelocityX( 0 );
    }
    
    /**
     * Called before update() if the actor collided with a
     * tile vertically (ie landed on the ground)
     **/
    public void collideVertical(){
        
        if( _velocityY > 20 ){
            takeDamage( 2 );
        }
        
        _velocityY = 0;
        _onGround = true; 
    }
    
    /**
     * Called when there is a conflict between two sprites or dangerous tile
     * @param damage is the value of the attackers strength; this value will be deducted from the actors health
     **/
    public void takeDamage( int damage ){
        
        // only affect if not in attacked state - poor fella, got to give him a little chance
        if( _currentState.getStateID() == STATE_ATTACKED || _currentState.getStateID() == STATE_DYING || _currentState.getStateID() == STATE_DEAD )
            return;         
        
        _health -= damage;
        if( _health <= 0 ){
            setState( STATE_DYING );
        }
        
        LittleRed.getInstance().printDebug( this, "takeDamage", "actors remaing health is " + _health );
    }
    
    /*
     * Only move the fighter when their state is either standing or walking (ie cannot work when
     * attacking, falling, or dying).
     */
    public void moveLeft(){
        int stateID = getState().getStateID();
        if(  stateID == STATE_STANDING || stateID == STATE_WALKING )
            setVelocityX( -_speedX );
    }
    
    public void moveRight(){
        int stateID = getState().getStateID();
        if(  stateID == STATE_STANDING || stateID == STATE_WALKING )
            setVelocityX( _speedX );
    }        
    
    //<editor-fold defaultstate="collapsed" desc="Mutators">
    
    public boolean isJumping(){
        return !_onGround;
    }
    
    public void setOnGround( boolean onGround ){
        _onGround = onGround; 
    }
    
    public boolean isUpdateWhenInView(){
        return _updateWhenInView; 
    } 
    
    public boolean isAlive(){
        if( _currentState == null )
            return true; 
        else
            return _currentState.getStateID() != STATE_DEAD;  
    }
    
    public void setUpdateWhenInView( boolean updateWhenInView ){
        _updateWhenInView  = updateWhenInView;
    }
    
    public int getMaxHealth() {
        return _maxHealth;
    }
    
    public void setMaxHealth( int maxHealth ){
        _maxHealth = maxHealth; 
    }
    
    public int getHealth() {
        return _health;
    }
    
    public void setHealth(int health) {
        this._health = health;
    }
    
    public int getLives() {
        return _lives;
    }
    
    public void setLives(int lives) {
        this._lives = lives;
    }
    
    public int getVelocityX() {
        return _velocityX;
    }
    
    public void setVelocityX(int velocityX) {
        if( velocityX < 0 ){            
            _velocityX = Math.max( -1*_speedX, velocityX );
        }
        else{
            _velocityX = Math.min( _speedX, velocityX );        
        }
    }
    
    public int getVelocityY() {
        return _velocityY;
    }
    
    public void setVelocityY(int velocityY) {
        this._velocityY = velocityY;
    }
    
    public int getSpeedX() {
        return _speedX;
    }
    
    public void setSpeedX(int speedX) {
        this._speedX = speedX;
    }
    
    public int getSpeedY() {
        return _speedY;
    }
    
    public void setSpeedY(int speedY) {
        this._speedY = speedY;
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
        
    public boolean isFacingLeft() {
        return _facingLeft;
    }
    
    public void setFacingLeft(boolean facingLeft) {
        this._facingLeft = facingLeft;
        
        // transform if facing right
        if( _facingLeft )
            transform( false );
    }
    
    public void setCanFly( boolean canFly ){
        _canFly = canFly; 
    }
    
    public boolean getCanFly(){
        return _canFly; 
    }
    
    public ActorState getCurrentState() {
        return _currentState;
    }
    
    public void setCurrentState(ActorState currentState) {
        this._currentState = currentState;
    }        
    
    private ActorState hasState( int stateID ){
        if( _states == null )
            return null;
        
        Enumeration enumStates = _states.elements();
        while( enumStates.hasMoreElements() ){
            ActorState state = (ActorState)enumStates.nextElement(); 
            
            if( state.getStateID() == stateID )
                return state;
        }
        
        return null;
    }
    
    public void setState( int stateID ) {
       
        if( _states == null )            
            return;
        
        Enumeration enumStates = _states.elements();
        while( enumStates.hasMoreElements() ){
            ActorState state = (ActorState)enumStates.nextElement();
            if( state.getStateID() == stateID )
                setState( state );
        }
        
    }
    
    public void setState( ActorState newState ) {
        if( _currentState == newState )
            return; 
        
        try{                        
            newState.activateState();                        
            
            // if dying then add the game effect
            if( _currentState.getStateID() == STATE_DYING )
                displayDyingAnim(); 
            
        }
        catch( Exception e ){ LittleRed.getInstance().printError( this, "setState", e.toString() ); }
    }
    
    public ActorState getState(){
        return _currentState; 
    }
    
    public void addState( ActorState state, int index ){
        _states.insertElementAt( state, index );
    }
    
    public void addState( ActorState state ){
        _states.addElement( state );
    }
    
    public void setStates( Vector states ){
        _states = states;
    }    
    
    //</editor-fold>
    
    public abstract void handleInput( int keyState );
    public abstract void stateComplete( ActorState state );
    public abstract void displayDyingAnim(); 
}
