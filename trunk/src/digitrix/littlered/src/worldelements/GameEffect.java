/*
 * GameEffect.java
 *
 * Created on 17 May 2007, 08:08
 *
 */

package digitrix.littlered.src.worldelements;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 *
 * @author Josh
 * The GameEffect class is responsible for displaying a simple image or animation to
 * recognise a user action/provide feedback ie if the user picks up a coin then display a
 * floating point value for a specific time or when the player/baddie dies then display
 * a explosion or something
 */
public class GameEffect extends Sprite {
    
    //<editor-fold defaultstate="collapsed" desc="Game effect types">
    
    public static final int GAME_EFFECT_EXPLOSION       = 0;
    public static final int GAME_EFFECT_SMOKE           = 1;
    public static final int GAME_EFFECT_LASER_EXPLOSION = 2;
    public static final int GAME_EFFECT_SMALL_SMOKE     = 3;
    public static final int GAME_EFFECT_HEART           = 4;
    public static final int GAME_EFFECT_DISSAPPEAR      = 5;
    public static final int GAME_EFFECT_5_POINTS        = 6;
    public static final int GAME_EFFECT_10_POINTS       = 7;
    
    //</editor-fold>
    
    private static final int DEFAULT_FRAME_TIME     = 110; 
    private static final int DEFAULT_VISIBLE_TIME   = 850;
    private static final int DEFAULT_Y_ACCELERATION = 2;
    private static final int DEFAULT_FRAME_WIDTH    = 16;
    private static final int DEFAULT_FRAME_HEIGHT   = 16;
    
    // object/instance variables
    private int _frameTime = DEFAULT_FRAME_TIME;
    private int _visibleTime = DEFAULT_VISIBLE_TIME;
    private int _accY = DEFAULT_Y_ACCELERATION; // determines how fast the game effect floats 
    private boolean _floating = false; // if set to true (determined by its type) the game effect will slow float when animating 
    
    private int _effectType = -1;
    private long _elapsedFrameTime = 0; 
    private long _elapsedVisibleTime = 0;
    private static int[][] _frameSequences = null;
    
    
    /**
     * Creates a new instance of GameEffect
     * The GameEffect object can be any type of game effect ie in one instance a GameEffect object can
     * an explosion and in the next instance (of when it is used) it can be a smoke effect
     */
    public GameEffect( Image img ){
        super( img, DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT );
        
        defineReferencePixel( this.getWidth()/2, this.getHeight()/2 );
        setVisible(false);
        
        initFrameSequences();
    }
    
    public GameEffect( Image img, int width, int height ) {
        super( img, width, height );
        
        defineReferencePixel( this.getWidth()/2, this.getHeight()/2 );
        setVisible(false);                
        
        initFrameSequences();
    }
    
    /**
     * Create the animation frame instances
     **/
    private void initFrameSequences(){
        // check if already initilized
        if( _frameSequences != null )
            return;
        
        _frameSequences = new int[8][];
        
        int baddieBoom[] = {0,1,2};
        int smoke[] = {3,4,5};
        int heroBoom[] = {6,7,8};
        int sprintSmoke[] = {9,10,11,12};
        int heart[] = {13,14,15};
        int dizzy[] = {16,17,18,19};
        int points100[] = {20};
        int points25[] = {21};
        
        _frameSequences[GAME_EFFECT_EXPLOSION] = baddieBoom;
        _frameSequences[GAME_EFFECT_SMOKE] = smoke;
        _frameSequences[GAME_EFFECT_LASER_EXPLOSION] = heroBoom;
        _frameSequences[GAME_EFFECT_SMALL_SMOKE] = sprintSmoke;
        _frameSequences[GAME_EFFECT_HEART] = heart;
        _frameSequences[GAME_EFFECT_DISSAPPEAR] = dizzy;
        _frameSequences[GAME_EFFECT_5_POINTS] = points100;
        _frameSequences[GAME_EFFECT_10_POINTS] = points25;
    }
    
    
    /**
     * Called when a game effect is added to the world; the purpose of this method is
     * to initilize the specified effects characteristics and thus allow the game effect to be
     * rendered
     **/
    public void initilize( int effectType, int posX, int posY ){
        // make sure a valid type has been passed through
        if( effectType < 0 || effectType > 7 )
            return;
        
        setPosition( posX, posY );
        _elapsedFrameTime = 0;
        _elapsedVisibleTime = 0;
        _effectType = effectType;
        
        // assign the right frame sequence to the type of effect requested      
        setFrameSequence( _frameSequences[_effectType] );
        setFrame( 0 );
        
        if( _effectType == GAME_EFFECT_10_POINTS || _effectType == GAME_EFFECT_5_POINTS )
            _floating = true; 
        else
            _floating = false; 
        
        setVisible( true );
    }
    
    /**
     * Animates the game effect and checks to see if it has passed it's used by ie
     * the game effect has been on the stage for longer than its pre-determined 
     * time; therefore remove it from the stage/world
     **/
    public void cycle( long elapsedTime ){
        if( !this.isVisible() )
            return;
        
        _elapsedFrameTime += elapsedTime; 
        _elapsedVisibleTime += elapsedTime;                 
        
        // frame animation? - the game effect is finished if it has reached the end of it's animation strip 
        // or (if it only has one frame) then if it has been visible for longer or equal to it's predefined
        // visible time value 
        if( getFrameSequenceLength() > 1 ){
            
            if( _frameTime <= _elapsedFrameTime ){
                if( getFrame() == getFrameSequenceLength() - 1 ){
                    // if have reached the end of the animation then the game effect is 
                    // complete; therefore hide it and return
                    
                    _effectType = -1;
                    setVisible( false ); 
                }
                else{
                    // floating 
                    if( _floating )
                        setPosition( getX(), (getY()-_accY) );
                    
                    nextFrame();
                }
                
                _elapsedFrameTime = 0; 
            }
        }
        else{            
            // floating effect 
            if( _floating && _elapsedFrameTime >= _frameTime ){                
                setPosition( getX(), (getY()-_accY) );
                _elapsedFrameTime = 0; 
            }
            
            if( _elapsedVisibleTime >= _visibleTime ){
                _effectType = -1;
                setVisible( false ); 
            }
            
        }                
        
    }
    
    //<editor-fold defaultstate="collapsed" desc="Mutators">
    
    public int getType(){
        return _effectType;
    }
    
    //</editor-fold>
    
}
