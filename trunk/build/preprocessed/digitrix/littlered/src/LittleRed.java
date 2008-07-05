/*
 * LittleRed.java
 *
 * Created on 8 May 2007, 21:09
 */

package digitrix.littlered.src;

import digitrix.littlered.src.datahandler.GameDAO;
import digitrix.littlered.src.datahandler.ScoreDAO;
import java.io.IOException;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 *
 * @author  Josh
 * @version
 */
public class LittleRed extends MIDlet implements CommandListener {
    
    private static final String IMGPATH_FONT_STRIP             = "/digitrix/littlered/res/font.png";
    private static final String IMGPATH_SMALL_FONT_STRIP       = "/digitrix/littlered/res/font_range_small2.png"; 
    private static final String IMGPATH_DEFAULT_ARROWS         = "/digitrix/littlered/res/arrows.png";
    
    private static LittleRed _instance = null; 
        
    private Command _cmdExit; 
    private Command _cmdMenu;     
    
    private boolean _hasRunningGame = false; 
    private GameManager _gameManager = null;    
    private MenuManager _menuManager = null; 
    private Displayable _currentDisplay = null;
    
    private Image _imgFontStrip = null; 
    private Image _imgSmallFontStrip = null; 
    private Image _imgArrows = null; 
    private CustomFont _gameFont = null; 
    
    public LittleRed(){        
        _instance = this; 
        
        _cmdExit = new Command( "Exit", Command.EXIT, 1 );
        _cmdMenu = new Command( "Menu", Command.OK, 2 ); 
        
        // instantiate ScoreDAO just to give it a chance to preload the scores 
        new ScoreDAO(); 
        
        // instantiate GameDAO just to give it a chance to preload the scores 
        new GameDAO(); 
        
    }
    
    public static LittleRed getInstance(){
        return _instance;
    }
    
    public void startApp() {
        if( _gameFont == null )
            initGameFonts();
        
        // load arrows if have not already been loaded (as this method is called everytime the 
        // application is entered ie the user might get a call while playing the game, when the player 
        // returns this method will run. 
        if( _imgArrows == null ){
            try{
                _imgArrows = Image.createImage( IMGPATH_DEFAULT_ARROWS ); 
            }
            catch( IOException ioe ){
                printError( this, "startApp", ioe.toString() );
                destroyApp( true ); 
            }
        }
        
        //activateGameManager( true ); 
        activateMenuManager();
    }
    
    public void pauseApp() {
        if( _hasRunningGame && _gameManager != null ){
            _gameManager.setState( GameManager.GS_PAUSED ); 
        }
    }
    
    public void destroyApp(boolean unconditional) {
        try{
            // stop threads
            if( _gameManager != null )
                _gameManager.stop(); 
            
            if( _menuManager != null )
                _menuManager.stop(); 
            
            notifyDestroyed(); 
        }
        catch( Exception ex ){}
    }  
    
    public void exit(){
        try{
            destroyApp( true );
        }
        catch( Exception ex ){}
    }
    
    public Displayable getActivatedDisplayable(){
        return Display.getDisplay( this ).getCurrent();  
    }
    
    public void activateDisplayable( IManager s ){
        try{           
            Display.getDisplay( this ).setCurrent( (Displayable)s ); 
            _currentDisplay = (Displayable)s; 
        }
        catch( Exception e ){
            printError( this, "activateDisplayable", e.getMessage() ); 
        }
        
        s.wakeup(); 
    }   
    
    public boolean hasPausedGame(){
        if( _gameManager == null )
            return false;
        
        return _gameManager.getState() == GameManager.GS_PAUSED; 
    }  
    
    public boolean showResumeGame(){
        if( _gameManager == null )
            return false;
        
        return _gameManager.getState() == GameManager.GS_PAUSED && _gameManager.getPreviousState() != GameManager.GS_CLOCKED
                && _gameManager.getPreviousState() != GameManager.GS_GAMEOVER; 
    }
    
    public void activateMenuManager(){
        // make the game thread sleep 
        if( _gameManager != null )
            _gameManager.sleep(); 
        
        if( _menuManager == null ){
            try{
                _menuManager = new MenuManager();                               
                _menuManager.setState( MenuManager.MENU_SPLASH );                 
            }
            catch( Exception e ){
                printError( this, "activateMenuManager", e.toString() ); 
                e.printStackTrace();
            }
        }                                                    
        
        _currentDisplay = _menuManager;
        activateDisplayable( _menuManager );
    }
    
    /** called when the player would like to resume a saved game **/ 
    public void activateGameManager( int level, int lives, int score ) throws Exception {
        if( _gameManager == null ){
            try{
                _gameManager = new GameManager();
                _gameManager.addCommand( _cmdExit ); 
                _gameManager.addCommand( _cmdMenu ); 
                _gameManager.setCommandListener( this ); 
            }
            catch( Exception e ){
                printError( this, "activateGameManager", e.getMessage() );
            }
        }
                
        int attempts = 0; 
        while( !_gameManager.isGameManagerInitilised() && attempts < 1000){
            /*try{
                //wait( 1000 ); 
            }
            catch( InterruptedException ie ){}*/            
            attempts++; 
        }
        if( !_gameManager.isGameManagerInitilised() ){
            throw new Exception( "Game Manager took too long to initilise" ); 
        }                        
        
        activateDisplayable( _gameManager );
        _gameManager.wakeup(); 
        
        _gameManager.loadSavedGame( level, lives, score );                 
    }
    
    public void activateGameManager( boolean newGame ){
        // make the game thread sleep         
        
        if( _gameManager == null ){
            try{
                _gameManager = new GameManager();
                _gameManager.addCommand( _cmdExit ); 
                _gameManager.addCommand( _cmdMenu ); 
                _gameManager.setCommandListener( this ); 
            }
            catch( Exception e ){
                printError( this, "activateGameManager", e.getMessage() );
            }
        }
        else{
        
            if( !hasPausedGame() || newGame ){
                _gameManager.newGame(); 
            }
            else{
                _gameManager.setState( GameManager.GS_RESUME );
            }
        }
                    
        activateDisplayable( _gameManager );
        _gameManager.wakeup(); 
    }
    
    public Image getArrows(){
        return _imgArrows; 
    }
    
    public CustomFont getFontManager(){
        return _gameFont;                 
    }
    
    private void initGameFonts(){
        try{
            _imgFontStrip = Image.createImage( IMGPATH_FONT_STRIP ); 
            _imgSmallFontStrip = Image.createImage( IMGPATH_SMALL_FONT_STRIP ); 
             
            _gameFont = new CustomFont( );
            _gameFont.initLargeFont( "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", _imgFontStrip, 13, 12, true ); 
            _gameFont.initSmallFont( "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+,-./!\"<=>? ", _imgSmallFontStrip, 8, 8, true );                         
        }
        catch( IOException e ){
            System.out.println( "[ERR] FightClub.initGameFonts(1); " + e.toString() );  
        }
    }
        
    //<editor-fold defaultstate="collapsed" desc="Outputting methods">
    
    public static final boolean DEBUG = false; 
    
    public void printError( Object sender, String message ){
        if( DEBUG )
            System.out.println( "[ERROR] From " + sender.getClass().getName() + " :: Exception: - " + message );
    }
    
    public void printError( Object sender, String method, String message ){
        if( DEBUG )
            System.out.println( "[ERROR] From " + sender.getClass().getName() + "." + method + " :: Exception: - " + message );
    }
    
    public void printDebug( Object sender, String method, String message ){
        if( DEBUG )
            System.out.println( "[DEBUG] From " + sender.getClass().getName() + "." + method + " :: Message: - " + message);        
    }
    
    //</editor-fold>

    public void commandAction(Command command, Displayable displayable) {
        if( command == _cmdExit )
            exit(); 
        else if( command == _cmdMenu ){
            _gameManager.setState( GameManager.GS_PAUSED ); 
            activateMenuManager(); 
        }
        /*else if( displayable == _tbUsername ){
            if( command == _cmdLoginOK )
                _menuManager.setTempUsername( _tbUsername.getString() );
            activateMenuManager();
        }
        else if( displayable == _tbPassword ){
            if( command == _cmdLoginOK )
                _menuManager.setTempPassword( _tbPassword.getString() );
            activateMenuManager();
        }*/
    }        
}
