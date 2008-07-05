/*
 * ActorState.java
 *
 * Created on 15 May 2007, 19:27
 *
 */

package digitrix.littlered.src.worldelements;

/**
 *
 * @author Josh
 *
 * Class is used to encapsulate the animation for a specific state 
 * 
 */
public class ActorState {
    
    private Actor _actor = null;        // actor that this state belongs to (a one to one relationship)
    private int _stateID = -1;          // state identifier    
    private int _timeForAnim;           // the total running time for the state/animation     
    private int[] _frameSequence;       // the animation sequence associated for the state
    private int _repeats = -1;          // how many time the state is repeated (-1 = repeat indefinitely)
    private int _currentFrame;          // the current frame of the animation associated to the state/time
    private boolean _initilised = false; 
    
    // actual private variables (not influenced by the outside world) 
    private long _currentStateTime;     // how long this state has been running for 
    private long _frameTime;            // how long the current frame has been shown
    private int _timePerFrame = -1;     // holds the calculated time that each frame should be allocated
    private int _repeated = 0;          // holds how many times the frames sequence has been repeated          
    
    /** Creates a new instance of ActorState */
    public ActorState(int stateID, Actor actor ) {
        _stateID = stateID;
        _actor = actor; 
    }
    
    public ActorState(int stateID, Actor actor,  int[] frameSeq, int timeForAnim, int repeats ) {
        _stateID = stateID;
        _actor = actor; 
        
        initilise( frameSeq, timeForAnim, repeats ); 
    }
    
    public void initilise( int[] frameSeq, int timeForAnim, int repeats ){
        _frameSequence = frameSeq;
        _timeForAnim = timeForAnim; 
        _repeats = repeats; 
        
        _initilised = true; 
    }
    
    public void activateState() throws Exception {
        // has been initilised        
        if( !_initilised )
            throw new Exception( "ActorState has not been properly initilised" );
            
        _currentStateTime = 0;
        _frameTime = 0; 
        _currentFrame = 0; 
        _repeated = 0;   
        
        if( _stateID == Actor.STATE_DYING ){
            System.out.print("ff");
        }
        
        // update actor properties
        _actor.setCurrentState( this );
        
        if( _frameSequence != null )
            _actor.setFrameSequence( _frameSequence ); 
        else
            _actor.setVisible( false );
        
        if( _timePerFrame < 0 )
            _timePerFrame = getTimePerFrame(); 
    }
    
    /**
     * Called from the associated actors cycle method 
     **/
    public void cycle( long elapsedTime ){        
        _currentStateTime += elapsedTime;
        _frameTime += elapsedTime;                 
        
        boolean stateCompleted = false;
        
        if( _stateID == Actor.STATE_DYING ){
            System.out.print("ff");
        }
        
        if( _frameSequence != null && _frameSequence.length == 1 && _repeats == -1 ){
            // return if the frame length is 0 and it is a infident cycle (repeats) 
            return; 
        }
        else if( _frameTime > _timePerFrame ){
            // state over or frame change?
            
            if( _frameSequence != null ){
                // move to next frame 
                if( ++_currentFrame >= getFrameSequence().length ){                

                    if( getRepeats() != -1 && (getRepeats() == 0 || ++_repeated > getRepeats()) ){
                        stateCompleted = true; 
                        _actor.stateComplete( this ); 
                    }
                    else{
                        setCurrentFrame(0); 
                    }
                }

                if( !stateCompleted ){
                    // update frame
                    _actor.setFrame( getCurrentFrame() );                                                                 
                }                                    

                _frameTime = 0;
            }
            else{
                _actor.stateComplete( this ); 
            }
        }                   
        
    }
    
    public String toString(){
        String details = "Unknown"; 
        
        switch( _stateID ){
            case Actor.STATE_ATTACKED:
                details = "Stated Attacked"; 
                break;
            case Actor.STATE_ATTACKING:
                details = "State Attacking"; 
                break; 
            case Actor.STATE_DEAD:
                details = "State Dead"; 
                break;
            case Actor.STATE_DYING:
                details = "State Dying";
                break;
            case Actor.STATE_FALLING:
                details = "State Falling";
                break;
            case Actor.STATE_JUMPING:
                details = "State Jumping";
                break; 
            case Actor.STATE_RUNNING:
                details = "State Running";
                break;
            case Actor.STATE_STANDING:
                details = "State Standing"; 
                break;
            case Actor.STATE_WALKING:
                details = "State Walking";
                break;
            default:
                details = "State Unknown";
        }
        
        return details; 
    }
    
    //<editor-fold defaultstate="collapsed" desc="Mutator">
    private int getTimePerFrame(){
        if( _frameSequence == null )
            return getTimeForAnim();
        else
            return getTimeForAnim() / getFrameSequence().length;
    }
    
    public int getStateID(){
        return _stateID; 
    }
    
    public Actor getActor() {
        return _actor;
    }

    public int getTimeForAnim() {
        return _timeForAnim;
    }

    public void setTimeForAnim(int timeForAnim) {
        this._timeForAnim = timeForAnim;
    }

    public int[] getFrameSequence() {
        return _frameSequence;
    }

    public void setFrameSequence(int[] frameSequence) {
        this._frameSequence = frameSequence;
    }

    public int getRepeats() {
        return _repeats;
    }

    public void setRepeats(int repeats) {
        this._repeats = repeats;
    }

    public int getCurrentFrame() {
        return _currentFrame;
    }

    public void setCurrentFrame(int currentFrame) {
        this._currentFrame = currentFrame;
    }    
    //</editor-fold>
    
}
