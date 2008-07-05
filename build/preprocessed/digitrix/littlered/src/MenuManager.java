/*
 * MenuManager.java
 *
 * Created on 27 July 2007, 22:48
 *
 */

package digitrix.littlered.src;

//<editor-fold defaultstate="collapsed" desc="imports">
import com.sun.midp.dev.GraphicalInstaller;
import digitrix.littlered.src.datahandler.GameDAO;
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
 */
public class MenuManager extends GameCanvas implements IManager, Runnable {
    
    private static final int BGCOL_SPLASH = 0xffffff;
    private static final int BGCOL_MENU = 0x000000;
    
    private static final String IMGPATH_LOGO = "/digitrix/littlered/res/digitrix.png";
    private static final String IMGPATH_TITLE = "/digitrix/littlered/res/littleredlogo.png";
    
    // game states
    public static final int MENU_SPLASH             = 20;
    public static final int MENU_ADVERT             = 21;
    public static final int MENU_MAIN               = 22;
    public static final int MENU_HELP               = 23;
    public static final int MENU_ABOUT              = 24;
    public static final int MENU_PLAYERSTATS        = 25;
    public static final int MENU_SUBMITSCORE        = 26;
    public static final int MENU_SUBMITSCORE_TEMP   = 27;
    public static final int MENU_LOGIN              = 28;
    public static final int MENU_NETSTART           = 29;
    public static final int MENU_NETEND             = 30;
    public static final int MENU_CHOICE             = 31;
    public static final int MENU_MESSAGE            = 32;   
    public static final int MENU_EXIT               = 33; 
    
    private static final int SPLASH_DELAY           = 2000; // time the splash screen is displayed
    private static final int ADVERT_DELAY           = 3000; // time the advert is displayed
    private static final int MESSAGE_DELAY          = 3000;
    private static final int NETWORK_RESULT_DELAY   = 4000;
    private static final int NETWORK_TIMEOUT        = 888000; 
    
    private static final int MAX_FPS = 10;
    
    public static final byte LOGIN_USERNAME_SEL = 0;
    public static final byte LOGIN_PASSWORD_SEL = 1;
    public static final byte LOGIN_SUBMIT_SEL   = 2; 
    public static final byte LOGIN_CANCEL_SEL   = 3;    
    
    public static final byte CHOICE_YES_SEL = 1;
    public static final byte CHOICE_NO_SEL = 0;
    
    public static final byte ACTION_SAVE_GAME = 0;
    
    public static final int MENUITM_NEW_GAME        = 0;
    public static final int MENUITM_RESUME_GAME     = 1;
    public static final int MENUITM_SAVE_GAME       = 2;
    public static final int MENUITM_PLAY_SAVED_GAME = 3;   
    public static final int MENUITM_HELP            = 4;
    public static final int MENUITM_SPLASH          = 5;
    public static final int MENUITM_PLAYER_STATES   = 6;
    public static final int MENUITM_SUBMIT_SCORE    = 7;
    public static final int MENUITM_ABOUT           = 8;
    public static final int MENUITM_EXIT            = 9;
    
    private static final String[] MENUITMS = { "NEW GAME", "RESUME GAME", "SAVE GAME","PLAY SAVED", "HELP", "SPLASH", "YOUR STATS", "SUBMIT SCORE", "ABOUT", "EXIT" };
    private static final String[] HELP_TEXT = {"LITTLERED","", "HELP LITTLERED ON", "HIS JOURNEY TO",
    "WELL...", "COLLECT ALL OF THE", "COINS...", "THATS ABOUT IT", "", "FIRE TO SHOT", "OR JUMP", "UP TO JUMP", "LEFT OR RIGHT", "TO RUN"};
    private static final String[] ABOUT_TEXT = {"DIGITRIX", "PRESENTS" ,"" , "ANOTHER GREAT", "-YEAH RIGHT-", "PRODUCT","" , "LITTLERED", "","CHECKOUT", "WEBSITE", "COMING SOON",
    "", "", "COPYRIGHT 2007"};
    
    private static MenuManager _instance;
    
    private Thread _menuThread = null;    
    private Image _imgLogo = null;
    private Image _imgTitle = null; 
    
    // grab reference to some objects we need to use
    private Image _imgArrows = null;
    private LittleRed _midlet = null; 
    private CustomFont _fontManager = null;     
    
    private boolean _running;
    
    // game variables
    private boolean _sleeping = false;
    private int _currState = MENU_SPLASH; // menu state
    private int _prevState = MENU_SPLASH;
    private long _stateTime = 0;
    private long _currTime = 0;
    
    private String _tmpUsername = null;
    private String _tmpPassword = null;
    private String _tmpNotification = null;
    //private ConnectionManager _cxnManager = null;
    
    private byte _selectedLoginItem = LOGIN_USERNAME_SEL;
    private byte _selectedChoiceItem = CHOICE_YES_SEL;   
    
    private int _curMenuItem = 0;
    
    private int posScrollingY = 0;
    
    /** Creates a new instance of GameManager */
    public MenuManager() {
        super( true );
        _instance = this;
        
        setCommandListener( _midlet );
        
        initMenu();
        
        // start games thread
        if ( _menuThread == null || !_menuThread.isAlive() ){
            _menuThread = new Thread( this );
            _running = true;
            _menuThread.start();
        }
    }
    
    public static MenuManager getInstance(){
        return _instance;
    }
    
    public void sleep(){
        _sleeping = true;
    }
    
    public void stop(){        
        
        setState( MENU_EXIT ); 
        
        try{
            this.notifyAll(); 
        }
        catch( Exception e ){ 
            // do nothing
        }
    }
    
    public void wakeup(){
        _midlet.printDebug( this, "wakeup()", "waking up!" ); 
        
        // if we are woken up and our current selected menu item is on resume BUT resmue is now not valid (ie the user has clocked the game or 
        // lost) then skip back one menu item 
        if( _curMenuItem == MENUITM_RESUME_GAME && !_midlet.showResumeGame() )
            _curMenuItem--; 
        
        _currTime = System.currentTimeMillis();
        _sleeping = false;
        synchronized( this ){
            try{
                this.notify();
            } catch( Exception e ){
                _midlet.printError( this, "wakeup", e.toString() );
            }
        }
    }
    
    private void initMenu(){
        _midlet = LittleRed.getInstance();
        _imgArrows = _midlet.getArrows();         
        _fontManager = _midlet.getFontManager(); 
        
        try{
            _imgLogo = Image.createImage( IMGPATH_LOGO );
            _imgTitle = Image.createImage( IMGPATH_TITLE ); 
        } catch( Exception e ){
            _midlet.printError( this, "initMenu", e.toString() ); 
        }
        
    }
    
    public void setState( int newState ){
        _midlet.printDebug( this, "setState", "Updating Menu State to " + newState );
        
        _stateTime = 0;
        _prevState = _currState;
        _currState = newState;
        
        if( _prevState == MENU_MESSAGE && newState != MENU_MESSAGE )
            _tmpNotification = null;
        
       
        switch( _currState ){
            case MENU_SPLASH:
                
                break;
            case MENU_ADVERT:
                
                break;
            case MENU_MAIN:
                
                break;
            case MENU_HELP:
                posScrollingY = getHeight() - 12;
                break;
            case MENU_ABOUT:
                posScrollingY = getHeight() - 12;
                break;
            case MENU_PLAYERSTATS:
                
                break;
            case MENU_LOGIN:
                _selectedLoginItem = LOGIN_USERNAME_SEL;
                break;
            case MENU_SUBMITSCORE:
                //_cxnManager = _midlet.openNewConnection();
                setState( MENU_NETSTART );
                //_cxnManager.submitHighScore();
                break;
            case MENU_SUBMITSCORE_TEMP:
                //_cxnManager = _midlet.openNewConnection();
                setState( MENU_NETSTART );
                //_cxnManager.submitHighScore(_tmpUsername, _tmpPassword );
                break;
            case MENU_NETSTART:
                
                break;
            case MENU_NETEND:
                
                break;
            case MENU_CHOICE:
                
                break;
            case MENU_MESSAGE:
                
                break;
            case MENU_EXIT: // exit the application 
                _running = true; 
                _sleeping = false; 
                break; 
        }
    }
    
    public void run() {
        _currTime = System.currentTimeMillis();
        
        long endTime;
        
        while( _running ){
            long elapsedTime = System.currentTimeMillis() - _currTime;
            _currTime += elapsedTime;
            
            // sleep game thread not focused or set to sleep
            if( _sleeping || _midlet.getActivatedDisplayable() != this ){
                _midlet.printDebug( this, "run", "MenuManager.run(1); Going to sleep" ); 
                _sleeping = true;
                synchronized( this ){
                    try{
                        this.wait();
                    } catch( Exception e ){
                    }
                }
                _midlet.printDebug( this, "run", "MenuManager.run(2); Back to life!" ); 
            }
            
            if( _currState == MENU_EXIT )
                continue; // jump the code to exit the loop
            
            // process state
            _stateTime += elapsedTime;
            if( _currState == MENU_SPLASH ){
                if( _stateTime > SPLASH_DELAY )
                    setState( MENU_ADVERT );
            } else if( _currState == MENU_ADVERT ){
                if( _stateTime > ADVERT_DELAY )
                    setState( MENU_MAIN );
            } else if( _currState == MENU_MESSAGE ){
                if( _stateTime > MESSAGE_DELAY ){
                    if( _prevState == MENU_SUBMITSCORE || _prevState == MENU_NETSTART )
                        setState( MENU_MAIN );
                    else
                        setState( _prevState );
                }
            } else if( _currState == MENU_NETEND ){
                if( _stateTime > NETWORK_RESULT_DELAY )
                    setState( MENU_MAIN );
            } else if( _currState == MENU_NETSTART ){
                if( _stateTime > NETWORK_TIMEOUT ){
                    //if( _cxnManager != null )
                    //    _cxnManager.Stop(); 
                    
                    // report timeout to user
                    _tmpNotification = "NETWORK TIMEOUT";
                    setState( MENU_MESSAGE ); 
                }
                    
            }           
            
            // process game
            checkUserInput( elapsedTime );
            
            render();
            
            syncGameLoop( _currTime );
            
        }
    }
    
    
    //<editor-fold defaultstate="collapsed" desc="helper methods">
    
    private void checkUserInput( long elapsedTime ){
        int keyState = getKeyStates();
        
        switch( _currState ){
            case MENU_MAIN:
                if( (keyState & FIRE_PRESSED) != 0 ){                    
                    handleMainMenuSelection();                    
                } else if( (keyState & LEFT_PRESSED) != 0 ){
                    _curMenuItem--;
                    
                    if( _curMenuItem == MENUITM_PLAY_SAVED_GAME && !GameDAO.getInstance().hasSavedGame() )
                        _curMenuItem--; 
                    
                    // skip resume game if we do not have a paused game
                    if( _curMenuItem == MENUITM_SAVE_GAME && !_midlet.showResumeGame() )
                        _curMenuItem-=2;                                        
                    
                    if( _curMenuItem < 0 )
                        _curMenuItem = MENUITMS.length-1;
                } else if( (keyState & RIGHT_PRESSED) != 0 ){
                    _curMenuItem++;
                    
                    // skip resume game if we do not have a paused game
                    if( _curMenuItem == MENUITM_RESUME_GAME && !_midlet.showResumeGame() )
                        _curMenuItem+=2;
                    
                    if( _curMenuItem == MENUITM_PLAY_SAVED_GAME && !GameDAO.getInstance().hasSavedGame() )
                        _curMenuItem++; 
                    
                    if( _curMenuItem >= MENUITMS.length )
                        _curMenuItem = 0;
                }
                break;
            case MENU_ABOUT:
            case MENU_HELP:
            case MENU_PLAYERSTATS:
            case MENU_NETEND:
                if( (keyState & FIRE_PRESSED) != 0 )
                    setState( MENU_MAIN );
                break;
            case MENU_LOGIN:
                if( (keyState & FIRE_PRESSED) != 0 ){
                    switch( _selectedLoginItem ){
                        case LOGIN_USERNAME_SEL:
                            //_midlet.activateUsernameTB();
                            break;
                        case LOGIN_PASSWORD_SEL:
                            //_midlet.activatePasswordTB();
                            break;
                        case LOGIN_SUBMIT_SEL:
                            if( _tmpUsername == null || _tmpUsername.toLowerCase().equals("enter username" ) ){
                                _tmpNotification = "USERNAME REQUIRED";
                            }
                            else if( _tmpPassword == null || _tmpPassword.toLowerCase().equals("enter password") ){
                                _tmpNotification = "PASSWORD REQUIRED";
                            }
                            else{
                                setState( MENU_SUBMITSCORE_TEMP );                                
                            }
                            break;
                        case LOGIN_CANCEL_SEL:
                            setState( MENU_MAIN ); 
                            break;
                    }
                }
                else if( (keyState & UP_PRESSED) != 0 ){
                    _selectedLoginItem--;
                    if( _selectedLoginItem < 0 )
                        _selectedLoginItem = 3; 
                }                                
                else if( (keyState & DOWN_PRESSED) != 0 ){
                    _selectedLoginItem++;
                    if( _selectedLoginItem > 3 )
                        _selectedLoginItem = 0; 
                }
                break;
        }
        
    }
    
    private void handleMainMenuSelection(){
        switch( _curMenuItem ){
            case MENUITM_NEW_GAME:{
                _midlet.activateGameManager( true );
                break;
            }
            case MENUITM_RESUME_GAME:{
                _midlet.activateGameManager( false );
                break;
            }
            case MENUITM_SAVE_GAME:{
                GameManager gm = GameManager.getInstance();                 
                try{
                    GameDAO.getInstance().saveGame( gm.getLevel(), gm.getLives(), gm.getScore() );                 
                    _tmpNotification = "GAME SAVED";
                }
                catch( Exception e ){
                    _midlet.printError( this, "setState().MENUITM_SAVE_GAME", e.toString() ); 
                    _tmpNotification = "ERROR WHILE TRYING TO SAVE GAME";
                }
                                
                setState( MENU_MESSAGE ); 
                break; 
            }
            case MENUITM_PLAY_SAVED_GAME:{                
                GameDAO gd = GameDAO.getInstance(); 
                try{
                    _midlet.activateGameManager( gd.getLevel(), gd.getLives(), gd.getScore() );                
                }
                catch( Exception e ){
                    _midlet.printError( this, "handleMainMenuSelection().MENUITM_PLAY_SAVED_GAME", e.toString() ); 
                    _tmpNotification = "UNABLE TO LOAD GAME";
                    setState( MENU_MESSAGE ); 
                }
                break; 
            }
            case MENUITM_SPLASH:{
                setState( MENU_SPLASH );
                break;
            }
            case MENUITM_ABOUT:{
                setState( MENU_ABOUT );
                break;
            }
            case MENUITM_HELP:{
                setState( MENU_HELP );
                break;
            }
            case MENUITM_PLAYER_STATES:{
                setState( MENU_PLAYERSTATS );
                break;
            }
            case MENUITM_SUBMIT_SCORE:{
                //if( DAO.getUserName() == null )
                //    setState( MENU_LOGIN );
                //else
                //    setState( MENU_SUBMITSCORE );
                break;
            }
            case MENUITM_EXIT:{
                _midlet.exit();
                break;
            }
        }
    }        
    
    private void render(){
        // if the current display is not equal to this then return (maybe even better to set this one to 
        // sleep 
        if( _midlet.getActivatedDisplayable() != this )
            return;
        
        Graphics g = getGraphics();
        
        // clear background
        g.setColor( 0xffffff );
        g.fillRect( 0, 0, getWidth(), getHeight() );
        
        // draw wallpaper
        switch( _currState ){
            case MENU_SPLASH:
                renderSplash( g );
                break;
            case MENU_ADVERT:
                renderAdvert( g );
                break;
            case MENU_MAIN:
                renderMainMenu( g );
                break;
            case MENU_HELP:
                renderHelp( g );
                break;
            case MENU_ABOUT:
                renderAbout( g );
                break;
            case MENU_PLAYERSTATS:
                renderPlayerStats( g );
                break;
            case MENU_LOGIN:
                renderLogin( g );
                break;
            case MENU_NETSTART:
                renderNetStatus( g ); 
                break;
            case MENU_NETEND:
                renderNetworkResult( g ); 
                break;
            case MENU_CHOICE:
                
                break;
            case MENU_MESSAGE:
                renderMessage( g );
                break;
        }
        
        flushGraphics();
        
    }
    
    /** put the thread to sleep for a small amount of time to allow the app to run 
     * consistantly over all phones **/ 
    private void syncGameLoop( long currTime ){
        // sleep if necessary to make a smooth framerate
        long endTime = System.currentTimeMillis() - currTime;
        //System.out.println( "Start Time = " + startTime + " End Time = " + endTime );
        if (endTime < (1000 / MAX_FPS)) {
            try {
                // frames per second
                _menuThread.sleep((1000 / MAX_FPS) - endTime);
            } catch (Exception e) { }
        } else {
            try {
                _menuThread.yield();
            } catch (Exception e) { }
        }
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Rendering methods">
    private void renderSplash( Graphics g ){
        g.setColor( BGCOL_SPLASH );
        g.fillRect( 0, 0, getWidth(), getHeight() );
        g.drawImage( _imgLogo, getWidth()/2, getHeight()/2, Graphics.HCENTER | Graphics.VCENTER );
    }
    
    private void renderAdvert( Graphics g ){
        //Image ad = DAO.getClientImage();
        Image ad = null;
        
        if( ad == null ){
            setState( MENU_MAIN );
            return;
        }
        
        // clear screen
        g.setColor( BGCOL_SPLASH );
        g.fillRect( 0, 0, getWidth(), getHeight() );
        
        // get x coordinate
        int x = (getWidth()-ad.getWidth())/2;
        if( x < 0 )
            x = 0;
        
        // get x coordinate
        int y = (getHeight()-ad.getHeight())/2;
        if( y < 0 )
            y = 0;
        
        // paint ad
        g.drawImage( ad, x, y, Graphics.TOP | Graphics.LEFT );
        
        // print whatever message
//        y = (getHeight()-ad.getHeight())/2;
//        if( y >= 20 ){
//            printToScreen( g, getWidth()/2, getHeight()-12, "PRESENT", false );
//        }
        
    }
    
    public void renderHelp( Graphics g ){
        renderScrollingText( HELP_TEXT, g );
    }
    
    public void renderAbout( Graphics g ){
        renderScrollingText( ABOUT_TEXT, g );
    }
    
    public void renderScrollingText( String[] text, Graphics g ){
        g.setColor( BGCOL_MENU );
        g.fillRect( 0, 0, getWidth(), getHeight() );
        
        // if the bottom line is above the top of the screen, then reset the variable/pointer
        // posScrollingY to the screensHeight - 10;
        if( posScrollingY + (text.length * 10) < 10 )
            posScrollingY = getHeight() -10;
        
        for( int i = 0; i < text.length; i ++ ){
            _fontManager.drawString( text[i], CustomFont.FONT_SMALL, (getWidth()-(text[i].length()*13))/2, posScrollingY + (i*15), g ); 
        }
        
        posScrollingY -= 1;
    }
    
    private void renderPlayerStats( Graphics g ){
        g.setColor( BGCOL_MENU );
        g.fillRect( 0, 0, getWidth(), getHeight() );
                
        // try and load player stats
        int curPosY = 20;
        int curPosX = getWidth()/2;
        
        // render title 
        int fntWidth = _fontManager.getFontsWidth( "HIGH SCORES", CustomFont.FONT_LARGE); 
        int fntHeight = _fontManager.getFontsHeight( CustomFont.FONT_LARGE );
        
        _fontManager.drawString( "HIGH SCORES", CustomFont.FONT_LARGE, (getWidth()-fntWidth)/2, curPosY, g );
        g.setClip( 0, 0, getWidth(), getHeight() );
        g.setColor( 0xffffff );
        g.drawLine( 25, curPosY+fntHeight+2, getWidth() -25, curPosY+fntHeight+2 );                
        
        curPosY += 40;
        fntHeight = _fontManager.getFontsHeight( CustomFont.FONT_SMALL );
        int[][] scores = ScoreDAO.getInstance().getHighScores(); 
        if( scores != null ){
            for( int i=0; i<scores.length; i++ ){
                // no more scores left so break out of loop
                if( scores[i][ScoreDAO.LEVEL] == 0 )
                    break; 
                
                _fontManager.drawString( "LEVEL " + scores[i][ScoreDAO.LEVEL] + " SCORE " + scores[i][ScoreDAO.SCORE]
                        , CustomFont.FONT_SMALL, 20, curPosY, g );
                
                curPosY += (fntHeight+3);
            }
        }
        
    }
    
    private void renderMainMenu( Graphics g ){
        // clear background
        g.setColor( BGCOL_MENU );
        g.fillRect( 0, 0, getWidth(), getHeight() );
        
        // paint logo
        g.drawImage( _imgTitle, (getWidth()-_imgTitle.getWidth())/2,  20, Graphics.TOP | Graphics.LEFT );
        
        // paint up arrow
        int pos1 = getWidth()/2 - (MENUITMS[_curMenuItem].length()*7 + 14);
        int pos2 = getWidth()/2 - MENUITMS[_curMenuItem].length()*6;
        int pos3 = getWidth()/2 + MENUITMS[_curMenuItem].length()*8;
        
        int pos4 = 40+_imgTitle.getHeight()+16;
        
        int vTest = getHeight()-5;
        while( pos4>vTest ){ pos4--; }
        
        g.setClip( pos1, pos4, 14, 12);
        g.drawImage( _imgArrows, pos1, pos4, Graphics.TOP | Graphics.LEFT );
        
        g.setClip( pos3, pos4, 14, 12);
        g.drawImage( _imgArrows, pos3-14, pos4, Graphics.TOP | Graphics.LEFT );
        
        g.setClip( 0, 0, getWidth(), getHeight() );
        _fontManager.drawString( MENUITMS[_curMenuItem], CustomFont.FONT_LARGE, pos2, pos4, g );
        
    }
    
    public void renderMessage( Graphics g ){
        g.setColor( BGCOL_MENU );
        g.fillRect( 0, 0, getWidth(), getHeight() );
        
        int posY = getHeight()/2-4;
                
        if( _tmpNotification.length()*_fontManager.getFontsWidth( CustomFont.FONT_SMALL ) > getWidth() ){
            // TODO:
            _fontManager.drawString( _tmpNotification, CustomFont.FONT_SMALL, (getWidth()-(_tmpNotification.length()*13))/2, posY, g );
        } else{
            _fontManager.drawString( _tmpNotification, CustomFont.FONT_SMALL, (getWidth()-(_tmpNotification.length()*13))/2, posY, g );
        }
    }
    
    public void renderLogin( Graphics g ){
        g.setColor( BGCOL_MENU );
        g.fillRect( 0, 0, getWidth(), getHeight() );
        
        int curPosY = 20;
        int curPosX = getWidth()/2;
        
        int fntWidth = 0; // tepm variable to hold string with
        int fntHeight = 0; 
                
        // draw title
        fntWidth = _fontManager.getFontsWidth( "LOGIN", CustomFont.FONT_LARGE); 
        fntHeight = _fontManager.getFontsHeight( CustomFont.FONT_LARGE );
        
        _fontManager.drawString( "LOGIN", CustomFont.FONT_LARGE, (getWidth()-fntWidth)/2, curPosY, g );
        g.setClip( 0, 0, getWidth(), getHeight() );
        g.setColor( 0xffffff );
        g.drawLine( 25, curPosY+fntHeight+2, getWidth() -25, curPosY+fntHeight+2 );
        
        // draw name details
        fntWidth = _fontManager.getFontsWidth( "USERNAME", CustomFont.FONT_SMALL); 
        fntHeight = _fontManager.getFontsHeight( CustomFont.FONT_SMALL );
        
        curPosY += 40;
        _fontManager.drawString( "USERNAME", CustomFont.FONT_SMALL, (getWidth()-fntWidth)/2, curPosY, g );        
        curPosY += 20;
        if( _tmpUsername != null )
            _fontManager.drawString( _tmpUsername, CustomFont.FONT_SMALL, (getWidth()-_fontManager.getFontsWidth( _tmpUsername, CustomFont.FONT_SMALL))/2, curPosY, g );                    
        else
            _fontManager.drawString( "ENTER USERNAME", CustomFont.FONT_SMALL, (getWidth()-_fontManager.getFontsWidth( "ENTER USERNAME", CustomFont.FONT_SMALL))/2, curPosY, g );        
        
        // draw rectangle around username field if currently selected
        if( _selectedLoginItem == LOGIN_USERNAME_SEL ){
            g.setClip( 0, 0, getWidth(), getHeight() );
            g.setColor( 0xffffff );
            g.drawRoundRect( 25, curPosY-6, getWidth()-50, fntHeight+7, 5, 5 );            
        }
        
        // draw password details
        fntWidth = _fontManager.getFontsWidth( "PASSWORD", CustomFont.FONT_SMALL); 
        fntHeight = _fontManager.getFontsHeight( CustomFont.FONT_SMALL );
        
        curPosY += 40;
        _fontManager.drawString( "PASSWORD", CustomFont.FONT_SMALL, (getWidth()-fntWidth)/2, curPosY, g );                
        curPosY += 20;
        if( _tmpPassword != null )
            _fontManager.drawString( _tmpPassword, CustomFont.FONT_SMALL, (getWidth()-_fontManager.getFontsWidth( _tmpPassword, CustomFont.FONT_SMALL))/2, curPosY, g );                
        else
            _fontManager.drawString( "ENTER PASSWORD", CustomFont.FONT_SMALL, (getWidth()-_fontManager.getFontsWidth( "ENTER PASSWORD", CustomFont.FONT_SMALL))/2, curPosY, g );                
        
        // draw rectangle around password field if currently selected
        if( _selectedLoginItem == LOGIN_PASSWORD_SEL ){
            g.setClip( 0, 0, getWidth(), getHeight() );
            g.setColor( 0xffffff );
            g.drawRoundRect( 25, curPosY-6, getWidth()-50, fntHeight+7, 5, 5 );
        }
        
        // draw login buttons
        // draw submit button
        curPosY += 25;
        
        g.setClip( 0, 0, getWidth(), getHeight() );
        g.setColor( 0xcccccc );
        g.fillRoundRect( 25, curPosY-6, getWidth()-50, fntHeight+7, 5, 5 );
        
        fntWidth = _fontManager.getFontsWidth( "LOGIN", CustomFont.FONT_SMALL );
        fntHeight = _fontManager.getFontsHeight( CustomFont.FONT_SMALL ); 
        _fontManager.drawString( "LOGIN", CustomFont.FONT_SMALL, (getWidth()-fntWidth)/2, curPosY, g );                
        
        // draw rectangle around login button if currently selected
        if( _selectedLoginItem == LOGIN_SUBMIT_SEL ){
            g.setClip( 0, 0, getWidth(), getHeight() );
            g.setColor( 0xffffff );
            g.drawRoundRect( 25, curPosY-6, getWidth()-50, fntHeight+7, 5, 5 );
        }
        
        // draw cancel button
        curPosY += 18;
        
        g.setClip( 0, 0, getWidth(), getHeight() );
        g.setColor( 0xcccccc );
        g.fillRoundRect( 25, curPosY-6, getWidth()-50, fntHeight+7, 5, 5 );
        
        fntWidth = _fontManager.getFontsWidth( "CANCEL", CustomFont.FONT_SMALL );
        _fontManager.drawString( "CANCEL", CustomFont.FONT_SMALL, (getWidth()-fntWidth)/2, curPosY, g );
        
        // draw rectangle around login button if currently selected
        if( _selectedLoginItem == LOGIN_CANCEL_SEL ){
            g.setClip( 0, 0, getWidth(), getHeight() );
            g.setColor( 0xffffff );
            g.drawRoundRect( 25, curPosY-6, getWidth()-50, fntHeight+7, 5, 5 );
        }
        
        
        // print a notification (maybe a red background)
        if( _tmpNotification != null ){
            fntWidth = _fontManager.getFontsWidth( _tmpNotification, CustomFont.FONT_SMALL ); 
            curPosY += 20;
            _fontManager.drawString( _tmpNotification, CustomFont.FONT_SMALL, (getWidth()-fntWidth)/2, curPosY, g );
        }
    }
    
    private void renderNetStatus( Graphics g ){        
        g.setColor( BGCOL_MENU );
        g.fillRect( 0, 0, getWidth(), getHeight() );
        
        int posY = getHeight()/2-4;        
        
        // draw status
        /*if( _cxnManager == null ){
            setState( MENU_NETEND );
        } else{
            switch( _cxnManager.getResponse() ){
                case ConnectionManager.RES_CONNECTING:
                    FightClub.getInstance().getGameFont().drawSmallString( "CONNECTING", (getWidth()-("CONNECTING".length()*13))/2, posY, g );
                    break;
                case ConnectionManager.RES_SUCCESS_CXN:
                    FightClub.getInstance().getGameFont().drawSmallString( "UPLOADING SCORE", (getWidth()-("UPLOADING SCORE".length()*13))/2, posY, g );
                    break;
                default:
                    FightClub.getInstance().getGameFont().drawSmallString( "TRYING", (getWidth()-("TRYING".length()*13))/2, posY, g );
            }
        }*/        
    }
    
    private void renderNetworkResult( Graphics g ){
        
        int posY = getHeight()/2;       
        int pos = 0; 
        
        g.setColor( BGCOL_MENU );
        g.fillRect( 0, 0, getWidth(), getHeight() );
        
        // draw network result
        /*if( _cxnManager != null ){
            switch( _cxnManager.getResponse() ){                
                case ConnectionManager.RES_SUCCESS:
                    pos = _cxnManager.getPosition();
                    if( pos > 0 ){
                        if( pos <= 10 )
                            FightClub.getInstance().getGameFont().drawSmallString( "TOP TEN", (getWidth()-("TOP TEN".length()*13))/2, posY, g );
                        else if( pos <= 15 )
                            FightClub.getInstance().getGameFont().drawSmallString( "GOOD STUFF", (getWidth()-("GOOD STUFF".length()*13))/2, posY, g );
                        else if( pos > 100 )
                            FightClub.getInstance().getGameFont().drawSmallString( "YOU SUCK", (getWidth()-("YOU SUCK".length()*13))/2, posY, g );
                        else
                            FightClub.getInstance().getGameFont().drawSmallString( "YOUR " + pos + "TH", (getWidth()-(("YOUR " + pos + "TH").length()*13))/2, posY, g );                          
                    } else{
                        FightClub.getInstance().getGameFont().drawSmallString( "SCORE UPLOADED", (getWidth()-(("SCORE UPLOADED").length()*13))/2, posY, g );                          
                    }                                        
                    break;
                    
                case ConnectionManager.RES_FAIL_CXN:
                    FightClub.getInstance().getGameFont().drawSmallString( "CONNECTION FAILED", (getWidth()-("CONNECTION FAILED".length()*13))/2, posY, g );
                    break;
                    
                case ConnectionManager.RES_FAIL_NOSCORE:
                    FightClub.getInstance().getGameFont().drawSmallString( "NO SCORE", (getWidth()-("NO SCORE".length()*13))/2, posY, g );     
                    break;
                    
                case ConnectionManager.RES_FAIL_USERREG:
                    posY -= 20;
                    FightClub.getInstance().getGameFont().drawSmallString( "NOT REGISTERED", (getWidth()-("NOT REGISTERED".length()*13))/2, posY, g );     
                    posY += 10;
                    FightClub.getInstance().getGameFont().drawSmallString( "REGISTER AT", (getWidth()-("REGISTER AT".length()*13))/2, posY, g );           
                    posY += 10;
                    FightClub.getInstance().getGameFont().drawSmallString( "WWW.DIGITRIX.CO.NZ", (getWidth()-("WWW.DIGITRIX.CO.NZ".length()*13))/2, posY, g );     
                    posY += 10;
                    FightClub.getInstance().getGameFont().drawSmallString( "AND BE IN TO WIN", (getWidth()-("AND BE IN TO WIN".length()*13))/2, posY, g );     
                    break;
            }
        } else{
            // no connection manager found
            FightClub.getInstance().getGameFont().drawSmallString( "ERROR", (getWidth()-("ERROR".length()*13))/2, posY, g );     
            
        }*/
        
        // near the buttom indicate that the user can press any key to continue
        // length of 'press fire to continue' =
        //printToScreen( g, getWidth()/2, (getHeight() - getHeight()/5), _pressFireToContinue, false );
        
    }
    
    //</editor-fold>
       
    public void setNotification( String notification ){
        _tmpNotification = notification;
    }
    
    public void setTempUsername( String un ){
        _tmpUsername = un;
    }
    
    public void setTempPassword( String pw ){
        _tmpPassword = pw; 
    }
    
}
