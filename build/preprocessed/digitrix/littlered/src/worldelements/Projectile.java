/*
 * Projectile.java
 *
 * Created on 10 June 2007, 15:13
 *
 */

package digitrix.littlered.src.worldelements;

import digitrix.littlered.src.GameManager;
import digitrix.littlered.src.worldelements.GameEffect;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 *
 * @author Josh
 */
public class Projectile extends Sprite {
    
    //<editor-fold defaultstate="collapsed" desc="Projectile types">
    public static final int PROJECTILE_TYPE_BULLET          = 0;    
    public static final int PROJECTILE_CANNON_BALL          = 1;
    public static final int PROJECTILE_SMALL_BOMB           = 2;    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Direction types">
    public static final int PROJECTILE_DIRECTION_LEFT       = 0;    
    public static final int PROJECTILE_DIRECTION_RIGHT      = 1;
    //</editor-fold>
    
    private static final int BULLET_TRAVELLING_TIME = 2000;
    private static final int BOMB_TIME              = 2000; 
    private static final int CANNON_BALL_TRAVELLING_TIME   = 3000; // determines when to decrement the rolling cannon ball
    private static final int CANNON_BALL_ANIMATION_TICK_DELAY = 3; // three ticks must pass before animating the cannon ball
    private static final int SMALL_BOMB_FRAME_TIME  = 30; 
    
    // animation 
    private static final int[] CANNON_BALL_ANIM = {0,1,2,3,4,5,6,7};
    private static final int[] BOMB_ANIM        = {8,9};     
    private static final int[] BULLET_ANIM      = {10}; 
    
    private int _projectileType = -1;
    private int _travellingTime = -1; 
    private int _frameTime = -1;
    private int _currentDirection = -1; 
    private int _dx = -1;
    private boolean _onGround = false;   
    private Actor _owner = null; 
    private boolean _hitTarget = false;
    private int _animationTick = 0;
    
    /** Creates a new instance of Projectile */
    public Projectile( Image img, int width, int height ) {
        super( img, width, height );
        setVisible( false ); 
    }
    
    public void initilise( int type, int posX, int posY, int dir, Actor owner ){
        _projectileType = type; 
        _travellingTime = 0; 
        _frameTime = 0;
        _currentDirection = dir;
        _onGround = false; 
        _owner = owner; 
        _hitTarget = false; 
        _animationTick = 0;
        
        setPosition( posX, posY );
        
        // set the animation 
        switch( _projectileType ){
            case PROJECTILE_TYPE_BULLET:
                if( dir == PROJECTILE_DIRECTION_LEFT )
                    _dx = -8;
                else
                    _dx = 8;
                
                setFrameSequence( BULLET_ANIM );
                setFrame( 0 );
                break;
            case PROJECTILE_CANNON_BALL:
                if( dir == PROJECTILE_DIRECTION_LEFT )
                    _dx = -1;
                else
                    _dx = 1;
                
                setFrameSequence( CANNON_BALL_ANIM );
                setFrame( 0 );
                break; 
                
            case PROJECTILE_SMALL_BOMB:
                setFrameSequence( BOMB_ANIM );
                setFrame( 0 );
                break;         
        }
        
        setVisible( true ); 
    }
    
    public void cycle( long elapsedTime ){
        if( _projectileType < 0 )
            return;                
        
        if( _projectileType == PROJECTILE_CANNON_BALL || _projectileType == PROJECTILE_SMALL_BOMB  ){
            if( _onGround )
                _travellingTime += elapsedTime; 
        }
        else 
            _travellingTime += elapsedTime;         
        
        // apply gravity - excludes the bullet 
        if( _projectileType != PROJECTILE_TYPE_BULLET ){
            int[] tile = Stage.getInstance().getTileCollision( this, getX(), getY() + Stage.GRAVITY );
            
            if( tile == null ){
                setPosition( getX(), getY() + Stage.GRAVITY );
                                
            } else{
                // if already landed on the ground then dispose projectile as it has obviously fallen off
                // a edge or something
                if( _onGround ){
                    //disposeProjectile(); 
                }
                else{
                    // line up with tile boundary                                
                    setPosition( getX(), Stage.getInstance().tilesYToPixels(tile[1]) - getHeight() );                
                    _onGround = true;                                
                }
            }
        }
        
        switch( _projectileType ){
            case PROJECTILE_TYPE_BULLET:
                // move horizontially
                moveHorizontally(); 
                
                if( _travellingTime >= BULLET_TRAVELLING_TIME )
                    disposeProjectile(); 
                break;
            case PROJECTILE_CANNON_BALL:
                if( _onGround )
                    moveHorizontally();
                
                if( _travellingTime >= CANNON_BALL_TRAVELLING_TIME ){
                        disposeProjectile(); 
                }
                else{
                    if( _onGround ){
                        if( _animationTick >= CANNON_BALL_ANIMATION_TICK_DELAY ){
                            if( _currentDirection == PROJECTILE_DIRECTION_LEFT ){                            
                                nextFrame(); 
                            }
                            else{
                                prevFrame();
                            }
                            _animationTick = 0;
                        }
                        else{
                            _animationTick++; 
                        }
                    }
                }
                break; 
                
            case PROJECTILE_SMALL_BOMB:
                if( _travellingTime >= BOMB_TIME )
                    disposeProjectile(); 
                
                if( _onGround ){
                    _frameTime += elapsedTime; 
                    if( _frameTime >= elapsedTime ){
                        _frameTime = 0; 
                        nextFrame();                     
                    } 
                }
                break;         
        }
    }
    
    private void moveHorizontally(){        
        
        if( (_projectileType == PROJECTILE_CANNON_BALL && _dx != 0) || _projectileType == PROJECTILE_TYPE_BULLET ){
            int[] tile = Stage.getInstance().getTileCollision( this, getX() + _dx, getY() );
            
            if( tile == null ){
                setPosition( getX() + _dx, getY());
                                
            } else{                
                disposeProjectile();                 
            }        
        }
    }
    
    public void consume( Actor actor ){
        
        if( actor == _owner )
            return; 
        
        int damage = 0; 
        if( _projectileType == PROJECTILE_TYPE_BULLET )
            damage = 4; 
        else if( _projectileType == PROJECTILE_CANNON_BALL )
            damage = 10;
        else if ( _projectileType == PROJECTILE_SMALL_BOMB )
            damage = 12; 
        
        actor.takeDamage( damage );        
        
        _hitTarget = true; 
        disposeProjectile(); 
    }
    
    private void disposeProjectile(){       
        
        if( _projectileType == PROJECTILE_TYPE_BULLET ){
            if( _hitTarget )
                Stage.getInstance().addGameEffect( getX(), getY(), GameEffect.GAME_EFFECT_SMALL_SMOKE ); 
            
            setVisible( false );             
        }
        else if( _projectileType == PROJECTILE_CANNON_BALL ){
            // add an explostion
            setVisible( false );
            Stage.getInstance().addGameEffect( getX(), getY(), GameEffect.GAME_EFFECT_EXPLOSION ); 
        }
        else if (_projectileType == PROJECTILE_SMALL_BOMB ){
            // add an explostion
            setVisible( false );
            Stage.getInstance().addGameEffect( getX(), getY(), GameEffect.GAME_EFFECT_EXPLOSION ); 
        }
        
         _projectileType = -1; 
    }
    
    public boolean isFree(){
        return _projectileType == -1; 
    }
    
}
