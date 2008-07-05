/*
 * Baddie.java
 *
 * Created on 21 May 2007, 22:50
 *
 */

package digitrix.littlered.src.worldelements;

import digitrix.littlered.src.GameManager;
import digitrix.littlered.src.LittleRed;
import java.util.Enumeration;
import java.util.Random;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;
import java.util.Vector;

/**
 *
 * @author Josh
 */
public class Baddie extends Actor {
    
    public static Random _rand = new Random();
    
    //<editor-fold defaultstate="collapsed" desc="Baddie types">
    
    public static final int BADDIE_FLYING_ALEIN_1 = 0;
    public static final int BADDIE_FLYING_ALEIN_2 = 1;
    public static final int BADDIE_THUNDER_CLOUD  = 2;
    
    //</editor-fold>
    
    // baddie state info
    private int _baddieType = -1;
    private long _travelledTime = 0; // current amount of time we have travelled
    
    // baddie characteristics
    private int _travellingTime  = 6000; // travel x amount of milli seconds before turning around
    private int _attackRange = 100; // the distance that little red needs to be until we attack
    private int _vision = 300; // distance the emeny is able to see littlered (to allow the baddie to move towards littlered when he has been seen)
    private int _freqToFire = 40; // fire frequency (measured in  %) when in range
    private int _freqToMove = 70; // progress
    private int _freqToMoveToTarget = 40; // % of times we will move towards littlered if he is in range regardless if we have travelled our designated distance
    private long _fireTimeStamp = 0;
    private int _fireDelay = 1000;
    
    
    /** Creates a new instance of Baddie */
    public Baddie( int baddieType, Image img, int frameWidth, int frameHeight ) {
        super( img, frameWidth, frameHeight );
        _baddieType = baddieType;
    }
    
    /** set up actor states then call the parent initActor method **/
    public void initActor( int posX, int posY, boolean facingLeft ){
        
        int[] frames = null;
        
        if( facingLeft )
            transform( true );
        
        // get the current level so we can adjust the behaviour of the baddies depending on
        // what level the player is currently at
        int currentLevel = Stage.getInstance().getCurrentLevel();
        
        switch( _baddieType ){
            case BADDIE_FLYING_ALEIN_1:
                //<editor-fold defaultstate="collapsed" desc="create baddie states">
                
                // state - standing (hovering, floating)
                frames = new int[]{1};
                this.addState( new ActorState( Actor.STATE_STANDING, this, frames, 0, -1 ) );
                
                // state - walking (moving, flying)
                frames = new int[]{1};
                this.addState( new ActorState( Actor.STATE_WALKING, this, frames, 600, -1 ) );
                
                // state - attacking
                frames = new int[]{0};
                this.addState( new ActorState( Actor.STATE_ATTACKING, this, frames, 400, 0 ) );
                
                // state - dying (drop out of the sky and explode on the ground)
                frames = new int[]{0};
                this.addState( new ActorState( Actor.STATE_DYING, this, frames, 0, -1 ) );
                
                // state - dead
                this.addState( new ActorState( Actor.STATE_DEAD, this, null, 10, -1 ) );
                
                // init actor
                try{
                    this.initActor( 10, 1, 2, 1, posX, posY, facingLeft );
                } catch( Exception e ){}
                
                // init baddie characteristics
                if( currentLevel > 5 ){
                    _travellingTime  = 2000;
                    _attackRange = 100;
                    _vision = 300;
                    _freqToFire = 10;
                    _freqToMove = 60;
                    _freqToMoveToTarget = 20;
                    _fireTimeStamp = 0;
                    _fireDelay = 4000;
                } else if( currentLevel < 2 ){
                    _travellingTime  = 2000;
                    _attackRange = 100;
                    _vision = 300;
                    _freqToFire = 10;
                    _freqToMove = 60;
                    _freqToMoveToTarget = 20;
                    _fireTimeStamp = 0;
                    _fireDelay = 4000;
                } else{
                    _travellingTime  = 2000;
                    _attackRange = 100;
                    _vision = 300;
                    _freqToFire = 10;
                    _freqToMove = 60;
                    _freqToMoveToTarget = 20;
                    _fireTimeStamp = 0;
                    _fireDelay = 4000;
                }
                _onGround = false;
                _canFly = true;
                
                //</editor-fold>
                break;
            case BADDIE_FLYING_ALEIN_2:
                //<editor-fold defaultstate="collapsed" desc="create baddie states">
                
                // state - standing (hovering, floating)
                frames = new int[]{1};
                this.addState( new ActorState( Actor.STATE_STANDING, this, frames, 0, -1 ) );
                
                // state - walking (moving, flying)
                frames = new int[]{1};
                this.addState( new ActorState( Actor.STATE_WALKING, this, frames, 600, -1 ) );
                
                // state - attacking
                frames = new int[]{0};
                this.addState( new ActorState( Actor.STATE_ATTACKING, this, frames, 400, 0 ) );
                
                // state - dying (drop out of the sky and explode on the ground)
                frames = new int[]{0};
                this.addState( new ActorState( Actor.STATE_DYING, this, frames, 0, -1 ) );
                
                // state - dead
                this.addState( new ActorState( Actor.STATE_DEAD, this, null, 10, -1 ) );
                
                // init actor
                try{
                    this.initActor( 10, 1, 1, 1, posX, posY, facingLeft );
                } catch( Exception e ){}
                
                // init baddie characteristics
                if( currentLevel > 5 ){
                    _travellingTime  = 2000;
                    _attackRange = 100;
                    _vision = 300;
                    _freqToFire = 10;
                    _freqToMove = 60;
                    _freqToMoveToTarget = 20;
                    _fireTimeStamp = 0;
                    _fireDelay = 4000;
                } else if( currentLevel > 2 ){
                    _travellingTime  = 2000;
                    _attackRange = 100;
                    _vision = 300;
                    _freqToFire = 10;
                    _freqToMove = 60;
                    _freqToMoveToTarget = 20;
                    _fireTimeStamp = 0;
                    _fireDelay = 4000;
                } else{
                    _travellingTime  = 2000;
                    _attackRange = 100;
                    _vision = 300;
                    _freqToFire = 10;
                    _freqToMove = 60;
                    _freqToMoveToTarget = 20;
                    _fireTimeStamp = 0;
                    _fireDelay = 4000;
                }
                _onGround = false;
                _canFly = true;
                
                //</editor-fold>
                break;
            case BADDIE_THUNDER_CLOUD:
                //<editor-fold defaultstate="collapsed" desc="create baddie states">
                
                // state - standing (hovering, floating)
                frames = new int[]{0};
                this.addState( new ActorState( Actor.STATE_STANDING, this, frames, 0, -1 ) );
                
                // state - walking (moving, flying)
                frames = new int[]{0};
                this.addState( new ActorState( Actor.STATE_WALKING, this, frames, 600, -1 ) );
                
                // state - attacking
                frames = new int[]{1,2,2,3,2};
                this.addState( new ActorState( Actor.STATE_ATTACKING, this, frames, 1000, 0 ) );
                
                // state - dying (drop out of the sky and explode on the ground)
                this.addState( new ActorState( Actor.STATE_DYING, this, null, 0, -1 ) );
                
                // state - dead
                this.addState( new ActorState( Actor.STATE_DEAD, this, null, 10, -1 ) );
                
                // init actor
                try{
                    this.initActor( 300, 1, 1, 1, posX, posY, facingLeft );
                } catch( Exception e ){}
                
                // init baddie characteristics
                _travellingTime  = 6000;
                _attackRange = 30;
                _vision = 300;
                _freqToFire = 80;
                _freqToMove = 30;
                _freqToMoveToTarget = 0;
                _allowTransform = false;
                _onGround = false;
                _canFly = true;
                
                //</editor-fold>
                break;
        }
    }
    
    /** Do nothing **/
    public void handleInput(int keyState) {
    }    
    
    public void stateComplete(ActorState state) {
        
        LittleRed.getInstance().printDebug( this, "stateComplete", state.toString() );
        
        if( state.getStateID() == STATE_DYING ) {
            explodeEnemy();
        } else if (state.getStateID() == STATE_ATTACKING ){
            setState( STATE_WALKING );
        }
    }
    
    public void displayDyingAnim() {
        
    }
    
    /** AI **/
    public void cycle( long elapsedTime ){
        if( !isAlive() )
            return;
        
        boolean goingLeft = _facingLeft;
        _velocityX = 0;
        
        if( _currentState.getStateID() == Actor.STATE_DYING ){
            if( _baddieType == Baddie.BADDIE_FLYING_ALEIN_1 ||
                    _baddieType == Baddie.BADDIE_FLYING_ALEIN_2 ){
                
                // continue moving
                if( goingLeft )
                    _velocityX = -1*_speedX;
                else
                    _velocityX = _speedX;
                
                // has the baddie crashed into the ground?
                if( _onGround )
                    explodeEnemy();
            }
            
        }
        
        // AI
        int heroX = Stage.getInstance().getHero().getX();
        int heroY = Stage.getInstance().getHero().getY();
        int x = getX();
        int y = getY();
        
        int diffX = Math.max( heroX, x ) - Math.min( heroX, x );
        int diffY = Math.max( heroY, y ) - Math.min( heroY, y );
        
        if( diffX <= _vision && diffY <= _vision ){
            
            int perc = Math.abs(_rand.nextInt())%100;
            
            if( perc < _freqToMove ){
                // check to see if we need to change direction
                if( (System.currentTimeMillis() - _travelledTime) > _travellingTime ){
                    // change direction
                    if( goingLeft )
                        goingLeft = false;
                    else
                        goingLeft = true;
                    
                    _travelledTime = System.currentTimeMillis();
                }
                
                // see if we should move towards the target
                perc = Math.abs(_rand.nextInt())%100;
                if( perc < _freqToMoveToTarget && diffX > 40 ){
                    // move towards little red
                    if( heroX > x ){
                        // on right
                        goingLeft = false;
                    } else {
                        // on left
                        goingLeft = true;
                    }
                    
                }
                // set up the velocity
                if( goingLeft )
                    _velocityX = -1*_speedX;
                else
                    _velocityX = _speedX;
            }
            
            // in range to fire
            if( diffX <= _attackRange && diffY <= _attackRange && ((System.currentTimeMillis() - _fireTimeStamp) > _fireDelay) ){
                perc = Math.abs(_rand.nextInt())%100;
                if( perc < _freqToFire ){
                    // attack
                    if( _baddieType == BADDIE_FLYING_ALEIN_1 ){
                        if( goingLeft ){
                            Stage.getInstance().addProjectile( x, y+getHeight(), Projectile.PROJECTILE_CANNON_BALL, Projectile.PROJECTILE_DIRECTION_LEFT, this );
                        } else{
                            Stage.getInstance().addProjectile( x, y+getHeight(), Projectile.PROJECTILE_CANNON_BALL, Projectile.PROJECTILE_DIRECTION_RIGHT, this );
                        }
                    } else if( _baddieType == BADDIE_FLYING_ALEIN_2 ){
                        if( goingLeft ){
                            Stage.getInstance().addProjectile( x, y+getHeight(), Projectile.PROJECTILE_SMALL_BOMB, Projectile.PROJECTILE_DIRECTION_LEFT, this );
                        } else{
                            Stage.getInstance().addProjectile( x, y+getHeight(), Projectile.PROJECTILE_SMALL_BOMB, Projectile.PROJECTILE_DIRECTION_RIGHT, this );
                        }
                    }
                    
                    _fireTimeStamp = System.currentTimeMillis();
                    setState( STATE_ATTACKING );
                }
            }
        }
        
        super.cycle( elapsedTime );
    }
    
    public void setState( int stateID ){
        additionalStateLogic( stateID );
        super.setState( stateID );
    }
    
    public void setState( ActorState state ){
        additionalStateLogic( state.getStateID() );
        super.setState( state );
    }
    
    public void advanceLevel(){
        switch( _baddieType ){
            case BADDIE_FLYING_ALEIN_1:
            {
                
                break;
            }
            case BADDIE_FLYING_ALEIN_2:
            {
                
                break;
            }
            case BADDIE_THUNDER_CLOUD:
            {
                
                break;
            }
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="Helper methods">
    
    private void additionalStateLogic( int stateID ){
        if( stateID == Actor.STATE_DYING ){
            if( _baddieType == Baddie.BADDIE_FLYING_ALEIN_1 )
                _canFly = false;
            
            if( _baddieType == Baddie.BADDIE_FLYING_ALEIN_2 )
                _canFly = false;
        }
    }
    
    private void explodeEnemy(){
        setState( Actor.STATE_DEAD );
        Stage.getInstance().addGameEffect( getX(), getY(), GameEffect.GAME_EFFECT_EXPLOSION );
        setVisible( false );
        
        if( --_lives > 0 ){
            reset();
        }
    }
    
    //</editor-fold>
    
}
