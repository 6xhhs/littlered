/*
 * Stage.java
 *
 * Created on 11 May 2007, 22:45
 *
 */

package digitrix.littlered.src.worldelements;

import digitrix.littlered.src.*;
import digitrix.littlered.src.worldelements.GameEffect;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author Josh
 */
public class Stage extends LayerManager implements Runnable {
    
    private static Stage _instance = null;
    
    //<editor-fold defaultstate="collapsed" desc="Tile types">
    
    // becuase we include our sprites and interactive tiles in the
    // map file itself we must manually identify these in order to add them
    // appropreitely to the world. Be a good idea to place these into the level data
    // file, therefore multiple tile maps could be used with the same Stage class
    public static final int TILE_SPIKE                      = 31; 
    public static final int TILE_BLOCK_GREY                 = 49;
    public static final int TILE_BLOCK_YELLOW               = 50;
    public static final int TILE_BLOCK_BROWN                = 51;
    public static final int TILE_BLOCK_BLUE                 = 52;
    public static final int TILE_WATER_1                    = 57;
    public static final int TILE_WATER_2                    = 58;
    public static final int TILE_SAVEPOINT                  = 59; // in-level checkpoint - the first one indicates the starting position of little red
    public static final int TILE_CLOUD                      = 60;
    public static final int TILE_FLOWER_1                   = 65;
    public static final int TILE_SPRING                     = 66;
    public static final int TILE_COIN                       = 67;
    public static final int TILE_CHECKPOINT                 = 68;
    public static final int TILE_GRASS_1                    = 69;
    public static final int TILE_GRASS_2                    = 70;
    public static final int TILE_FLOWER_2                   = 71;
    public static final int TILE_MUSHROOM                   = 72;
    public static final int TILE_GUN                        = 73;
    public static final int TILE_SHOE                       = 74;
    public static final int TILE_FLYING_BADDIE_1            = 75;
    public static final int TILE_FLYING_BADDIE_2            = 76;
    public static final int TILE_FRIENDLY_1                 = 77;  
    public static final int TILE_BOTTOM_LEFT_BANK           = 61;
    public static final int TILE_BOTTOM_LEFT_BANK_WATER     = 78;
    public static final int TILE_BOTTOM_RIGHT_BANK          = 64;
    public static final int TILE_BOTTOM_RIGHT_BANK_WATER    = 79;
    public static final int TILE_MOVABLE_BOX                = 80; 
    
    // Data about a tile
    public static final int TILE_SPIKE_DAMAGE   = 3;
    public static final int TILE_WATER_DAMAGE   = 3; 
    
    public static final int[] TILE_CAUSES_DAMAGE = {TILE_WATER_1, TILE_WATER_2, TILE_SPIKE}; 
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Resources Path">
    
    private static final String IMGPATH_BG_DEFAULT          = "/digitrix/littlered/res/bg0.png";
    private static final int BG_COL_DEFAULT                 = 0x314273;
    private static final String IMGPATH_BG_0                = "/digitrix/littlered/res/bg0.png";
    private static final int BG_COL_0                       = 0x314273;
    private static final String IMGPATH_BG_1                = "/digitrix/littlered/res/bg1.png";
    private static final int BG_COL_1                       = 0x927430;
    private static final String IMGPATH_BG_2                = "/digitrix/littlered/res/bg2.png";
    private static final int BG_COL_2                       = 0xF69A2F;
    private static final int BG_COUNT                       = 3;
    
    private static final String IMG_PATH_TILES              = "/digitrix/littlered/res/tiles.png";
    private static final String IMG_PATH_BLOCKS             = "/digitrix/littlered/res/floatingBlocks.png";         
    private static final String IMG_PATH_FLOWERS            = "/digitrix/littlered/res/flowers.png";
    private static final String IMG_PATH_GAME_EFFECTS       = "/digitrix/littlered/res/gameeffects.png";
    private static final String IMG_PATH_BULLET             = "/digitrix/littlered/res/bullet.png";
    private static final String IMG_PATH_CANNON_BALL        = "/digitrix/littlered/res/cannonball.png";
    private static final String IMG_PATH_GUN                = "/digitrix/littlered/res/gun.png";
    private static final String IMG_PATH_SHOE               = "/digitrix/littlered/res/shoe.png";      
    private static final String IMG_PATH_SPRING             = "/digitrix/littlered/res/spring.png";
    private static final String IMG_PATH_SMALL_BOMB         = "/digitrix/littlered/res/SmallBomb.png";
    private static final String IMG_PATH_CHECK_POINT        = "/digitrix/littlered/res/checkpoint.png";
    private static final String IMG_PATH_SAVE_POINT         = "/digitrix/littlered/res/savepoint.png";
    private static final String IMG_PATH_SPINNING_COIN      = "/digitrix/littlered/res/coin.png";
    private static final String IMG_PATH_SINGLE_COIN        = "/digitrix/littlered/res/singlecoin.png";
    private static final String IMG_PATH_CLOUDS             = "/digitrix/littlered/res/thunderCloud.png"; 
    private static final String IMG_PATH_LITTLERED          = "/digitrix/littlered/res/littlered.png"; 
    private static final String IMG_PATH_FAST_LITTLERED     = "/digitrix/littlered/res/littleredfast.png";
    private static final String IMG_PATH_SHOOTER_LITTLERED  = "/digitrix/littlered/res/littleredwithgun.png";
    private static final String IMG_PATH_FLYING_ALIEN_1     = "/digitrix/littlered/res/flyingAlien01.png";
    private static final String IMG_PATH_FLYING_ALIEN_2     = "/digitrix/littlered/res/flyingAlien02.png";
    private static final String IMG_PATH_JUNIOR             = "/digitrix/littlered/res/junior.png";
    private static final String IMG_PATH_PROJECTILES        = "/digitrix/littlered/res/projectiles.png";
    private static final String IMG_PATH_ICON               = "/digitrix/littlered/res/icon.png";
    private static final String IMG_PATH_BOX                = "/digitrix/littlered/res/box.png";
    
    private static final String RES_PATH_LEVELS             = "/digitrix/littlered/res/levels.dat";     
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Stage variables">
    public static final int STAGE_STATUS_ERROR             = 0;
    public static final int STAGE_STATUS_LEVEL_LOADED      = 1; 
    public static final int STAGE_STATUS_LEVEL_NOT_FOUND   = 2; // assume the player has clocked the game
    
    // method called when loading (this is to determine whether or not to reinitilise little red ie give him
    // all of this lives etc back 
    private static final int LOADING_NEW_GAME = 0;
    private static final int LOADING_NEXT_LEVEL = 1;
    private static final int LOADING_SAVED_GAME = 2; 
    private int _loadingMethod = LOADING_NEW_GAME; 
    
    private final int PADDINGX = 5;
    private final int PADDINGY = 5;
        
    public static int TILE_WIDTH = 32;
    public static int TILE_HEIGHT = 32;
    
    // world attributes
    public static int GRAVITY = 1;
    public static int SPIKE_DAMAGE = 3;   
    public static int COIN_POINTS = 5;
    
    private static Random _rand = null; 
    private int _stageBGColour = BG_COL_DEFAULT; // colour used to fill the screen with (dependant on what background image is loaded)
    private int _currentBackground = -1; // used so if the same background is selected (when randomly selecting one) we don't reload it'
    
    private TiledLayer _tiledWorld = null;
    
    private int _screenWidth = 0;
    private int _screenHeight = 0;
    
    private int _currentViewX = 0;
    private int _currentViewY = 0; 
    private float _pixelPerMS = 0.18f; 
    private float _panPixelsToMove; 
    private int _panPaddingX = 0;
    private int _panPaddingY = 0; 
    private int _viewX = 0;
    private int _viewY = 0;
    private int _backgroundX = 0;
            
    private int _currentLevel = 0;
    private int _requiredCoins = 0;  // number of coins the player must collect (relative to level)
    private int _coinsCollected = 0; // number of coins the player has collected
    
    // remember the current score and current number of lives left, this is so that when update is called (for either
    // score or lives) and the value hasn't changed we don't have to re-generate the image thus saving some
    // processing.
    private int _totalCoinsCollected = 0;
    private int _currentLives = -1;   
    
    // game stat images
    public Image _imgLife = null;
    public Image[] _imgNums = null;
    public Image _imgLives = null;
    public Image _imgSingleCoin = null;
    public Image _imgGun = null; 
    public Image _imgShoe = null; 
    public Image _imgIcon = null; 
    private Image _imgBackGround = null;     
    
    // game actors and props 
    private Vector _sprites = null;
    private Vector _effects = null;
    private Vector _projectiles = null; 
    private Hashtable _liveActors = null; // keep track of the live actors on the stage as these elements will be used when checking for collsion on a projectile
    private boolean _gameEffectsLoaded = false; 
    private boolean _projectilesLoaded = false;    
    
    private Hero _littleRed = null;
    
    private boolean go = false;
    private boolean _levelLoaded = false; 
    
    
    //</editor-fold>
    
    public static Stage getInstance(){
        return _instance;         
    }
    
    /**
     * Creates a new instance of Stage
     */
    public Stage( int screenWidth, int screenHeight) throws Exception {
        
        _instance = this; 
        _screenWidth = screenWidth;
        _screenHeight = screenHeight; 
        
        _currentLives = Hero.ATT_LIVES;
        
        // set up panning padding
        _panPaddingX = 10; 
        _panPaddingY = 10; 
        
        // initilise our collections 
        _sprites = new Vector(); 
        _effects = new Vector(); 
        _projectiles = new Vector(); 
        
        _levelLoaded = false;
        
        if( !initiliseImages() )
            throw new Exception( "Stage.Create: Error while trying to load images" );         
    }
    
    public void newGame( ){
        _currentLevel = 0;
        _currentLives = Hero.ATT_LIVES;
        _coinsCollected = 0;
        _loadingMethod = LOADING_NEW_GAME; 
        
        loadWorld( ++_currentLevel );
    }
    
    public void nextLevel( ){
        _coinsCollected = 0;
        _loadingMethod = LOADING_NEXT_LEVEL; 
        
        loadWorld( ++_currentLevel );
    }   
    
    public void savedGame( int level, int lives ){
        _coinsCollected = 0;
        _currentLevel = level; 
        _currentLives = lives; 
        _loadingMethod = LOADING_SAVED_GAME; 
        
        loadWorld( _currentLevel ); 
        
        // block thread until level is loaded 
        while( !_levelLoaded ){
            // do nothing 
        }
        
        if( _littleRed != null )
            _littleRed.setLives( lives ); 
    }
    
    /** Load only those images that we need to hold a reference to **/ 
    private boolean initiliseImages(){
        
        try{
            _imgShoe = Image.createImage( IMG_PATH_SHOE );
            _imgGun = Image.createImage( IMG_PATH_GUN );             
            _imgSingleCoin = Image.createImage( IMG_PATH_SINGLE_COIN ); 
            _imgIcon = Image.createImage( IMG_PATH_ICON ); 
        }
        catch( IOException e ){ 
            LittleRed.getInstance().printError( this, "initiliseImages", e.toString() );
            return false; 
        }
        
        return true; 
    }
    
    /**
     * Called by the Game Manager at every cycle to update the viewport in that the user is always in the center of the
     * screen (unless the end of the level has been hit) as well as implement our parallax scrolling for the
     * background image :)
     **/
    public void cycle( long elapsedTime ){
        
        if( !_levelLoaded )
            return; 
        
        // update the viewport window
        if ( _littleRed == null ){
            _viewX = 0;
            _viewY = 0;
        } else{
            // if the player is invisible then don't update the viewpoint as we dont want to follow a blank screen
            if( _littleRed.getState().getStateID() == Actor.STATE_DYING )
                return;
            
            _panPixelsToMove += _pixelPerMS * elapsedTime; 
            int wholePixels = (int)_panPixelsToMove; 
            
            // find x viewpoint
            // TODO; Add panning here... 
            _viewX = _littleRed.getX() + _littleRed.getWidth()/2 - _screenWidth/2; // optimal view
            // now make sure the view does not run off the map
            if ( _viewX < 0 )
                _viewX = 0;
            else if ( (_viewX + _screenWidth) > _tiledWorld.getWidth() )
                _viewX = _tiledWorld.getWidth() - _screenWidth;
            
            // adjust the current move slightly towards the ideal view point+            
            if( _currentViewX < _viewX ){
                if( (_viewX - _currentViewX) < _panPaddingX )
                    _currentViewX = _viewX; 
                else
                    _currentViewX += wholePixels;
            }
            else if( _currentViewX > _viewX ){
                if( (_currentViewX - _viewX) < _panPaddingY )
                    _currentViewX = _viewX; 
                else
                    _currentViewX -= wholePixels;                 
            }
            
            // and finally lets update viewY
            // TODO: Add panning here
            _viewY = _littleRed.getY() + _littleRed.getHeight()/2 - (_screenHeight/3)*2;
            // now lets make sure the view does not run off the map
            if ( _viewY < 0 )
                _viewY = 0;
            else if ( ( _viewY + _screenHeight ) > _tiledWorld.getHeight() )
                _viewY = _tiledWorld.getHeight() - _screenHeight;
            
            if( _currentViewY < _viewY ){
                 if( (_viewY - _currentViewY) < _screenHeight/8 )
                    _currentViewY = _viewY; 
                else
                    _currentViewY += wholePixels;                                 
            }
            else if( _currentViewY > _viewY ){
                if( (_currentViewY - _viewY) < _screenHeight/8 )
                    _currentViewY = _viewY; 
                else
                    _currentViewY -= wholePixels;
            }
            
            // take away the pixels that were moved
            _panPixelsToMove = _panPixelsToMove - wholePixels; 
        }
        
        //setViewWindow( _viewX, _viewY, _screenWidth, _screenHeight );
        setViewWindow( _currentViewX, _currentViewY, _screenWidth, _screenHeight );                
        
        // now work out the position for our background image to demonstrate parallax scrolling
        int _backgroundX = 0;
        if( _viewX == 0 )
            _backgroundX = 0;
        else if( _viewX == ( _screenWidth - _tiledWorld.getWidth() ) )
            _backgroundX = _screenWidth - _imgBackGround.getWidth();
        else
            _backgroundX = _viewX * ( _screenWidth - _imgBackGround.getWidth() ) / ( _screenWidth - _tiledWorld.getWidth() );        
    }
    
    //<editor-fold defaultstate="collapsed" desc="Drawing methods">     
    
    public void paint( Graphics g, int x, int y, int screenWidth, int screenHeight ){
        g.drawImage( _imgBackGround, _backgroundX, screenHeight - _imgBackGround.getHeight(), Graphics.LEFT | Graphics.TOP );
        
        // *** draw game stats *** 
        // heart and lives
        
        // coins left
        
        // paint the other layers
        super.paint( g, x, y );                        
    }        
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Loading and unloading of the stage">
    
    /** 
     * unloadWorld is called when a new level is to be loaded; it is responsible to remove all sprites 
     * from the tilelayer and clear out the sprites to prepare for the loading of the next level.
     **/
    private void unloadWorld(){
        _currentViewX = 0;
        _currentViewY = 0;        
        _panPixelsToMove = 0;
        
        // remove all sprites of the layer
        if( _sprites != null ){
            Enumeration gameSprites = _sprites.elements();
            try{
                for( int i=0; ; i++ ){
                    remove( (Sprite)gameSprites.nextElement() );
                }
            } catch( Exception e ){}
            
            _sprites.removeAllElements();
        }
        
        // remove all effects and projectiles
        if( _effects == null ){
            Enumeration effectsSprites = _effects.elements();
            try{
                for( int i=0; ; i++ ){
                    remove( (Sprite)effectsSprites.nextElement() );
                }
            }catch( Exception e ){}
        } 
        
        // remove all projectiles
        if( _projectiles == null ){
            Enumeration projectileSprites = _projectiles.elements();
            try{
                for( int i=0; ; i++ ){
                    remove( (Sprite)projectileSprites.nextElement() );
                }
            }catch( Exception e ){}
        }
        
        // remove the hero from the layer manager
        remove( _littleRed );
        
        // remove the tiled world from this (the layer manager)
        remove( _tiledWorld );
        
        // reset number of coins required to complete the level
        _requiredCoins = 0; 
    }
    
    private void loadWorld( int level ) {
        _currentLevel = level; 
        
        Thread thread = new Thread( this );
        thread.start(); 
    }
    
    public void run(){ 
        System.out.println( "Stage.run(); new thread started to load the level " + _currentLevel );
        
        _levelLoaded = false;
        
        int status = 9;
        
        int tilesHigh = 0;
        int tilesWide = 0; 
        
        int playerStartingPosX = -1;
        int playerStartingPosY = -1;
        
        Image imgCheckPoint = null;
        Image imgSpinningCoin = null;
        Image imgBlocks = null;
        Image imgFlowers = null;
        Image imgAlien1 = null;
        Image imgAlien2 = null;
        Image imgCloud = null; 
        Image imgSpring = null; 
        Image imgJunior = null;        
        Image imgTiles = null; 
        Image imgSavePoint = null;
        Image imgMovableBox = null; 
        
        // load images
        
        try{
            imgCheckPoint = Image.createImage( IMG_PATH_CHECK_POINT );
            imgSpinningCoin = Image.createImage( IMG_PATH_SPINNING_COIN );
            imgBlocks = Image.createImage( IMG_PATH_BLOCKS );
            imgFlowers = Image.createImage( IMG_PATH_FLOWERS );
            imgAlien1 = Image.createImage( IMG_PATH_FLYING_ALIEN_1 );
            imgAlien2 = Image.createImage( IMG_PATH_FLYING_ALIEN_2 );
            imgCloud = Image.createImage( IMG_PATH_CLOUDS );
            imgSpring = Image.createImage( IMG_PATH_SPRING );
            imgJunior = Image.createImage( IMG_PATH_JUNIOR );
            imgTiles = Image.createImage( IMG_PATH_TILES );
            imgSavePoint = Image.createImage( IMG_PATH_SAVE_POINT ); 
            imgMovableBox = Image.createImage( IMG_PATH_BOX );
            
        }
        catch( IOException e ){
            LittleRed.getInstance().printError( this, "loadWorld", e.toString() ); 
            GameManager.getInstance().levelLoaded( STAGE_STATUS_ERROR );
            return; 
        }
        
        _liveActors = new Hashtable(); 
        
        // load level data
        try{
            InputStream is = null;
            is = this.getClass().getResourceAsStream( RES_PATH_LEVELS );
            boolean foundLevel = false;
            
            // loop through until we get the correct level
            int b = is.read();
            while ( b != -1 && !foundLevel ){
                // level names starts with a ! and terminates with a ~ character
                // The readString method wraps p reading the string from the stream
                if ( b == '!' ){
                    // got a start of a level name char, read the name string
                    String thisLevel = readString( is, (byte)'~').toLowerCase();
                    
                    if( Integer.parseInt( thisLevel ) == _currentLevel )
                        foundLevel = true;
                }
                
                // if the level hasn't been found yet then continue reading
                if ( !foundLevel ){
                    b = is.read();
                }                
            }
            
            if( !foundLevel ){
                GameManager.getInstance().levelLoaded( STAGE_STATUS_LEVEL_NOT_FOUND );
                return;
            }
            
            try{
                unloadWorld();   
            }
            catch( Exception e ){
                LittleRed.getInstance().printDebug( this, "run", e.toString() );
            }
            
            // load the level
            byte[] buffer = new byte[2];
            is.read(buffer);
            String ths = new String(buffer, 0, 2);
            is.read(buffer);
            String tws = new String(buffer, 0, 2);
            tilesHigh = Integer.parseInt(tws);
            tilesWide = Integer.parseInt(ths);
            
            // tiles must be of size 32, 32
            _tiledWorld = new TiledLayer( tilesWide,tilesHigh, imgTiles, TILE_WIDTH, TILE_HEIGHT );
            
            // Next you read all the tiles into the tilemap.
            int bytesRead=0;
            
            // set a place holder for the baddie and powerups
            Baddie baddie;
            Prop prop;
            
            //<editor-fold defaultstate="collapsed" desc="Add sprites to the stage">
            
            byte prevTile = -1; 
            byte curTile = -1;
            
            for (int ty=0; ty < tilesHigh; ty++) {                
                
                boolean onWaterLevel = false; // will be set to true when a water tile is found; this will mean that 
                                              // any cliff edges found will be changed to their water level substitues
                
                for (int tx = 0; tx < tilesWide; tx++) {
                    bytesRead = is.read(buffer);
                    if (bytesRead > 0) {
                        tws = new String(buffer, 0, 2).trim();
                        
                        prevTile = curTile; 
                        curTile = Byte.parseByte(tws);
                                                
                        // *** LITTLE BLUE ***
                        if ( curTile == TILE_SAVEPOINT ) {
                            // set if is the first we have come across or its x position is less than the current one
                            if( playerStartingPosX == -1 ||  playerStartingPosX > tx ){
                                playerStartingPosX = tx*TILE_WIDTH;
                                playerStartingPosY = ty*TILE_HEIGHT; 
                            }     
                            
                            prop = new Prop( imgSavePoint );
                            prop.initProp( Prop.PROP_TYPE_SAVE_POINT, (tx*TILE_WIDTH), (ty*TILE_HEIGHT) );
                            _sprites.addElement( prop );
                            _tiledWorld.setCell( tx, ty,  0 ); 
                        }
                        
                        // *** BAD GUYS ***
                        else if ( curTile == TILE_FLYING_BADDIE_1 ) {
                            baddie = new Baddie( Baddie.BADDIE_FLYING_ALEIN_1, imgAlien1, 48, 34 );
                            baddie.initActor( (tx*TILE_WIDTH), (ty*TILE_HEIGHT), true );
                            _sprites.addElement( baddie );
                            
                            _liveActors.put( baddie, baddie ); //???
                            
                            _tiledWorld.setCell( tx, ty,  0 ); // add a blank curTile to our tiled world                           
                        } else if ( curTile == TILE_FLYING_BADDIE_2 ) {
                            baddie = new Baddie( Baddie.BADDIE_FLYING_ALEIN_2, imgAlien2, 47, 33 );
                            baddie.initActor( (tx*TILE_WIDTH), (ty*TILE_HEIGHT), true );
                            _sprites.addElement( baddie );
                            
                            _liveActors.put( baddie, baddie ); //???
                            
                            _tiledWorld.setCell( tx, ty,  0 ); // add a blank curTile to our tiled world                           
                            
                        } else if ( curTile == TILE_CLOUD ) {
                            baddie = new Baddie( Baddie.BADDIE_THUNDER_CLOUD, imgCloud, 42, 32 );
                            baddie.initActor( (tx*TILE_WIDTH), (ty*TILE_HEIGHT), true );
                            _sprites.addElement( baddie );
                            
                            _liveActors.put( baddie, baddie ); //???
                            
                            _tiledWorld.setCell( tx, ty,  0 ); // add a blank curTile to our tiled world                           
                        }
                        
                        // *** PROPS ***
                        else if ( curTile == TILE_SPRING ) {
                            prop = new Prop( imgSpring, 28, 19 );
                            prop.initProp( Prop.PROP_TYPE_SPRING, (tx*TILE_WIDTH), (ty*TILE_HEIGHT) );
                            _sprites.addElement( prop );
                            _tiledWorld.setCell( tx, ty,  0 );
                        } else if ( curTile == TILE_COIN ) {
                            _requiredCoins++; // incremement how many coins the player needs to collect
                            prop = new Prop( imgSpinningCoin, 8, 8 );                            
                            prop.initProp( Prop.PROP_TYPE_COIN,(tx*TILE_WIDTH), ((ty*TILE_HEIGHT)+(TILE_HEIGHT/2)) );
                            _sprites.addElement( prop );
                            _tiledWorld.setCell( tx, ty,  0 );
                        } else if ( curTile == TILE_CHECKPOINT ) {                            
                            prop = new Prop( imgCheckPoint );
                            prop.initProp( Prop.PROP_TYPE_CHECKPOINT,(tx*TILE_WIDTH), (ty*TILE_HEIGHT) );
                            _sprites.addElement( prop );
                            _tiledWorld.setCell( tx, ty,  0 );
                        } else if ( curTile == TILE_FLOWER_1 ) {
                            prop = new Prop( imgFlowers, 32, 28 );
                            prop.initProp( Prop.PROP_TYPE_FLOWER_1, (tx*TILE_WIDTH), (ty*TILE_HEIGHT) );
                            _sprites.addElement( prop );
                            _tiledWorld.setCell( tx, ty,  0 );
                        } else if ( curTile == TILE_FLOWER_2 ) {
                            prop = new Prop( imgFlowers, 32, 28 );
                            prop.initProp( Prop.PROP_TYPE_FLOWER_2, (tx*TILE_WIDTH), (ty*TILE_HEIGHT) );
                            _sprites.addElement( prop );
                            _tiledWorld.setCell( tx, ty,  0 );
                        } else if ( curTile == TILE_GRASS_1 ) {
                            prop = new Prop( imgFlowers, 32, 28 );
                            prop.initProp( Prop.PROP_TYPE_GRASS_1, (tx*TILE_WIDTH), (ty*TILE_HEIGHT) );
                            _sprites.addElement( prop );
                            _tiledWorld.setCell( tx, ty,  0 );
                        } else if ( curTile == TILE_GRASS_2 ) {
                            prop = new Prop( imgFlowers, 32, 28 );
                            prop.initProp( Prop.PROP_TYPE_GRASS_2, (tx*TILE_WIDTH), (ty*TILE_HEIGHT) );
                            _sprites.addElement( prop );
                            _tiledWorld.setCell( tx, ty,  0 );
                        } else if ( curTile == TILE_MUSHROOM ) {
                            prop = new Prop( imgFlowers, 32, 28 );
                            prop.initProp( Prop.PROP_TYPE_MUSHROOM, (tx*TILE_WIDTH), (ty*TILE_HEIGHT) );
                            _sprites.addElement( prop );
                            _tiledWorld.setCell( tx, ty,  0 );
                        } else if( curTile == TILE_BLOCK_GREY ){
                            prop = new Prop( imgBlocks, 32, 32 );
                            prop.initProp( Prop.PROP_TYPE_BLOCK_GREY, (tx*TILE_WIDTH), (ty*TILE_HEIGHT) );
                            _sprites.addElement( prop );
                            _tiledWorld.setCell( tx, ty,  0 );
                        } else if( curTile == TILE_BLOCK_YELLOW ){
                            prop = new Prop( imgBlocks, 32, 32 );
                            prop.initProp( Prop.PROP_TYPE_BLOCK_YELLOW, (tx*TILE_WIDTH), (ty*TILE_HEIGHT) );
                            _sprites.addElement( prop );
                            _tiledWorld.setCell( tx, ty,  0 );
                        } else if( curTile == TILE_BLOCK_BLUE ){
                            prop = new Prop( imgBlocks, 32, 32 );
                            prop.initProp( Prop.PROP_TYPE_BLOCK_BLUE, (tx*TILE_WIDTH), (ty*TILE_HEIGHT) );
                            _sprites.addElement( prop );
                            _tiledWorld.setCell( tx, ty,  0 );
                        } else if( curTile == TILE_FRIENDLY_1 ){
                            prop = new Prop( imgJunior, 14, 15 );
                            prop.initProp( Prop.PROP_TYPE_JUNIOR, (tx*TILE_WIDTH), (ty*TILE_HEIGHT) );
                            _sprites.addElement( prop );
                            _tiledWorld.setCell( tx, ty,  0 );                          
                        } else if( curTile == TILE_GUN ){
                            prop = new Prop( _imgGun );
                            prop.initProp( Prop.PROP_TYPE_GUN, (tx*TILE_WIDTH), (ty*TILE_HEIGHT) );
                            _sprites.addElement( prop );
                            _tiledWorld.setCell( tx, ty,  0 );                          
                        } else if( curTile == TILE_SHOE ){
                            prop = new Prop( _imgShoe );
                            prop.initProp( Prop.PROP_TYPE_SHOE, (tx*TILE_WIDTH), (ty*TILE_HEIGHT) );
                            _sprites.addElement( prop );
                            _tiledWorld.setCell( tx, ty,  0 );
                        } else if( curTile == TILE_MOVABLE_BOX ){
                            prop = new Prop( imgMovableBox );
                            prop.initProp( Prop.PROP_TYPE_MOVEABLE_BOX, (tx*TILE_WIDTH), (ty*TILE_HEIGHT) );
                            _sprites.addElement( prop );
                            _tiledWorld.setCell( tx, ty,  0 );
                        } else{
                            // all else fails, try and add it as a standard curTile
                            if( curTile == TILE_WATER_1 || curTile == TILE_WATER_2 )
                                onWaterLevel = true; 
                            
                            if( onWaterLevel && curTile == TILE_BOTTOM_LEFT_BANK )                                
                                curTile = TILE_BOTTOM_LEFT_BANK_WATER;
                            else if( onWaterLevel && curTile == TILE_BOTTOM_RIGHT_BANK )                                
                                curTile = TILE_BOTTOM_RIGHT_BANK_WATER;
                                                                
                                _tiledWorld.setCell( tx, ty,  curTile );
                        }
                    }
                }
            }
            //</editor-fold>
            
        }
        
        catch (Exception e) {
            LittleRed.getInstance().printError( this, "run", e.toString() ); 
            GameManager.getInstance().levelLoaded( STAGE_STATUS_ERROR );
        }
        
        LittleRed.getInstance().printDebug( this, "run", "finished loading the tiles" ); 
        
        // insert the player (just to make sure he's on top of all the baddies)
        try{
            initLittleRed( playerStartingPosX, playerStartingPosY, false ); 
            _liveActors.put( _littleRed, _littleRed );
        }
        catch( Exception e ){ LittleRed.getInstance().printError( this, "loadWorld", e.toString() ); }
        
        // add some empty game effects, done last so they're on top of all of the other sprites
        _gameEffectsLoaded = initGameEffects();
        
        // add some projectiles
        _projectilesLoaded = initProjectiles(); 
        
        // add our sprites/baddies
        Enumeration sprites = _sprites.elements();

        while( sprites.hasMoreElements() ) {
            Sprite nextSprite = (Sprite)sprites.nextElement();
            if( nextSprite instanceof Prop && ((Prop)nextSprite).isInFront() )
                insert( nextSprite, 0 );
            else
                append( nextSprite );
        }
        
        // add our tiles to the world
        append( _tiledWorld );        
        
        // now randomly select a background image for this level (to keep things interesting)        
        initBackground();
        
        // return true to indicate that a level has been loaded and everything has loaded 
        // successfully 
        _levelLoaded = true;        
        
        GameManager.getInstance().levelLoaded( STAGE_STATUS_LEVEL_LOADED );
        
        LittleRed.getInstance().printDebug( this, "run", "level finished loading" ); 
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Public ation methods">
      
    /**
     * Used when updading the logic of the game; if the game is panning to the hero when he/she is 
     * not in view then this method is used to determine in the hero is displayed on screen. If not then 
     * give littleblue a chance and don't update the baddies
     **/
    public boolean isHeroInView(){
        return actorInView( _littleRed );         
    }
    
    /**
     * Used to determine in a actor is in view; if not then don't update 
     **/
    public boolean actorInView( Actor actor ){
        if( actor == null )
            return false; 
        
        int difX = actor.getX() - _currentViewX; 
        int difY = actor.getY() - _currentViewY;         
        
        // check x
        if( difX < 0 )
            return false;
        else if( difX > _screenWidth )
            return false;
        else if( difY < 0 )
            return false;
        else if( difY > _screenHeight )
            return false; 
        
        return true; 
    }
    
    public Hero getHero(){
        return _littleRed;
    }
    
    /**
     * Adds a game effect object onto the stage; first we will search for a 
     * free game effect and initilize it with the type and coordinates if no 
     * free game effects exist then create a new game effect and add it to the 
     * queue
     **/
    public void addGameEffect( int posX, int posY, int gameEffect ){
        GameEffect effect = null; 
        
        Enumeration existingEffects = _effects.elements();
        while( existingEffects.hasMoreElements() ){
            GameEffect curEffect = (GameEffect)existingEffects.nextElement(); 
            if( !curEffect.isVisible() ){
                effect = curEffect; 
                break; 
            }
        }
        
        // check to see if we have found a free game effect on our current queue; if not then create a new 
        // one
        if( effect == null ){
            // load image - nb: I wonder if it is more efficent to hold a reference to the 
            // game effects image?
            Image img  = null; 
            
            try{
                img = Image.createImage( IMG_PATH_GAME_EFFECTS ); 
            }
            catch( IOException e ){
                LittleRed.getInstance().printError( this, "addGameEffect", e.getMessage() );
                return; 
            }
            
            effect = new GameEffect( img ); 
            _effects.addElement( effect );
            insert( effect, 0 ); 
        }
        
        // initilise the game effect
        effect.initilize( gameEffect, posX, posY ); 
    }

    /**
     * 
     **/
    public void addProjectile( int posX, int posY, int projectileType, int dir, Actor owner ){
        Projectile projectile = null;
        
        Enumeration existingProjectiles = _projectiles.elements(); 
        while( existingProjectiles.hasMoreElements() ){
            Projectile curProj = (Projectile)existingProjectiles.nextElement(); 
            if( curProj.isFree() ){
                projectile = curProj; 
                break; 
            }
        }
        
        // check to see if we have found a free game effect on our current queue; if not then create a new 
        // one
        if( projectile == null ){
            // load image - nb: I wonder if it is more efficent to hold a reference to the 
            // game effects image?
            Image img  = null; 
            
            try{
                img = Image.createImage( IMG_PATH_PROJECTILES ); 
            }
            catch( IOException e ){
                LittleRed.getInstance().printError( this, "addProjectile", e.getMessage() );
                return; 
            }
            
            projectile = new Projectile( img, 17, 17 ); 
            _projectiles.addElement( projectile );
            insert( projectile, 0 ); 
        }
        
        // initilise the game effect
        projectile.initilise( projectileType, posX, posY, dir, owner );
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Accessors">
    
    public boolean getLevelLoaded(){
        return _levelLoaded; 
    }
    
    public int getBackgroundColour(){
        return _stageBGColour; 
    }
    
    public int getCurrentLevel(){
        return _currentLevel; 
    }
    
    public int getActualLevel(){
        return _currentLevel - 1; 
    }
    
    public int getViewX(){
        return _viewX;
    }
    
    public int getViewY(){
        return _viewY;
    }
    
    public int getScreenWidth(){
        return this._screenWidth;
    }
    public int getScreenHeight(){
        return this._screenHeight;
    }
    
    public int pixelsXToTiles( int pixels ){
        return pixels/_tiledWorld.getCellWidth();
    }
    
    public int pixelsYToTiles( int pixels ){
        return pixels/_tiledWorld.getCellHeight();
    }
    
    public int tilesXToPixels( int tiles ){
        return tiles * _tiledWorld.getCellWidth();
    }
    
    public int tilesYToPixels( int tiles ){
        return tiles * _tiledWorld.getCellHeight();
    }
    
    public int getWidth(){
        return _tiledWorld.getWidth();
    }
    
    public int getHeight(){
        return _tiledWorld.getHeight();
    }
    
    public int getTile( int x, int y ){
        try{
            return _tiledWorld.getCell(x, y);
        } catch( Exception e ){
            // out of bounds therefore return something greater than 0 to indicate that
            // a collision was detected
            return 1;
        }
    }
    
    /**
     * Returns the number of coins the user has to collect before the user can advance to the next
     * level
     **/
    public int coinsLeft(){
        return ( _requiredCoins - _coinsCollected );
    }
    
    /**
     * Called every time the user collects a coin; if coins left is equal to zero then scroll through the
     * props (looking for the checkpoint) and sets its visibility to true
     **/
    public void coinCollected(){
        _coinsCollected++;
        _totalCoinsCollected++;        
    }
    
    public void coinCollected( int value ){
        _coinsCollected += value;
        _totalCoinsCollected += value;
    }
    
    public Enumeration getWorldSprites(){
        return _sprites.elements();
    }
    
    public Enumeration getWorldEffects(){
        return _effects.elements();
    } 
    
    public Enumeration getProjectiles(){
        return _projectiles.elements();
    }
    
    public Enumeration getLiveActors(){
        return _liveActors.elements(); 
    }
    
    public void removeLiveActor( Actor actor ){
        _liveActors.remove( actor ); 
    }
    
    /**
     * Gets the tile that a Sprites collides with. Only the
     * Sprite's X or Y should be changed, not both. Returns null
     * if no collision is detected.
     */
    public int[] getTileCollision(Sprite sprite,
            int newX, int newY) {
        
        int[] point = null;
        
        int fromX = Math.min(sprite.getX(), newX);
        int fromY = Math.min(sprite.getY(), newY);
        int toX = Math.max(sprite.getX(), newX);
        int toY = Math.max(sprite.getY(), newY);
        
        // get the tile locations
        int fromTileX = pixelsXToTiles(fromX);
        int fromTileY = pixelsYToTiles(fromY);
        int toTileX = pixelsXToTiles(
                toX + sprite.getWidth() - 1 );
        int toTileY = pixelsYToTiles(
                toY + sprite.getHeight() - 1 );
        
        // check each tile for a collision
        for (int x=fromTileX; x<=toTileX; x++) {
            for (int y=fromTileY; y<=toTileY; y++) {
                if (x < 0 || x >= pixelsXToTiles(getWidth()) || y < 0 || y >= pixelsYToTiles(getHeight()) ||
                        (getTile(x, y) != 0) ) {
                    // collision found, return the tile
                    point = new int[2];
                    point[0] = x;
                    point[1] = y;
                    
                    if( newX > sprite.getX() ){ // moving right
                        //return point;
                    }
                }
            }
        }
        
        return point;
    }
    
    /**
     * Helper method used by the AI controlled actors so they're smart enough to 
     * know when to turn and not fall off the edge
     **/
    public boolean nextHorizontalTileEmpty( Actor actor ){
        int nextX = actor.getX() + (int)actor.getVelocityX();
        int nextY = actor.getY() + (int)actor.getVelocityY();
        
        // ensure that the next position is in bounds
        if( nextX <= 0 || nextX >= getWidth() || nextY <= 0 || nextY >= getHeight() )
            return true;
        
        return getTile( pixelsXToTiles(nextX), pixelsXToTiles(nextY)+1 ) == 0; // pixelsXToTiles(nextY)+1 (+1 to look beneath the actor)
    }
    
    /**
     * Search the array TILE_CAUSES_DAMAGE for the tile, if exists then this indicates that 
     * the tile causes damage so return true. 
     **/
    public boolean isTileDangerous( int x, int y ){
        int tile = getTile( x, y );
        
        return isTileDangerous( tile ); 
    }
    
    /**
     * Search the array TILE_CAUSES_DAMAGE for the tile, if exists then this indicates that 
     * the tile causes damage so return true. 
     **/
    public boolean isTileDangerous( int tileIndex ){
        if( tileIndex < 0 )
            return false;
        
        // search in the array of tiles that cause damage, if found return 
        // true else return false
        for( int i=0; i<TILE_CAUSES_DAMAGE.length; i++ ){
            if( TILE_CAUSES_DAMAGE[i] == tileIndex )
                return true; 
        }
        
        return false; 
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Helper methods">
    
    private void initLittleRed( int posX, int posY, boolean facingLeft ) throws Exception{
        if( _littleRed != null ){
            // if loading from a saved game or new game then we want to reset littlered's attributes
            if( _loadingMethod == LOADING_SAVED_GAME || _loadingMethod == LOADING_NEW_GAME ){
                _littleRed.setLives( _currentLives ); 
                _littleRed.setHealth( Hero.ATT_MAX_HEALTH ); 
            }
            
            _littleRed.initActor( posX, posY, facingLeft ); 
            insert( _littleRed, 0 ); 
            return; 
        }
        
        Image imgLittleRed = null;
        Image imgLittleRedWithGun = null; 
        Image imgFastLittleRed = null; 
        
        try{
            imgLittleRed = Image.createImage( IMG_PATH_LITTLERED );
            imgLittleRedWithGun = Image.createImage( IMG_PATH_SHOOTER_LITTLERED ); 
            imgFastLittleRed = Image.createImage( IMG_PATH_FAST_LITTLERED );
        }
        catch( IOException e ){
            LittleRed.getInstance().printError( this, "initLittleRed", e.getMessage() ); 
            throw e; 
        }
        
        // create a instance of little red
        _littleRed = new Hero( imgLittleRed, 26, 26 ); 
        
        // create modes 
        _littleRed.addMode( Hero.MODE_FAST, imgFastLittleRed, 26, 26 );
        _littleRed.addMode( Hero.MODE_GUN, imgLittleRedWithGun, 26, 26 ); 
                
        // create states 
        // state - standing
        int[] frames = {0};  
        _littleRed.addState( new ActorState( Actor.STATE_STANDING, _littleRed, frames, 0, -1 ) );                
        
        // state - walking 
        frames = new int[]{1,2,3,2}; 
        _littleRed.addState( new ActorState( Actor.STATE_WALKING, _littleRed, frames, 600, -1 ) );
        
        // state - running 
        frames = new int[]{4,5,6,5}; 
        _littleRed.addState( new ActorState( Actor.STATE_RUNNING, _littleRed, frames, 90, -1 ) );
        
        // state - jumping 
        frames = new int[]{7}; 
        _littleRed.addState( new ActorState( Actor.STATE_JUMPING, _littleRed, frames, 0, -1 ) );
        
        // state - falling
        frames = new int[]{8}; 
        _littleRed.addState( new ActorState( Actor.STATE_FALLING, _littleRed, frames, 0, -1 ) );
        
        // state - dying  
        _littleRed.addState( new ActorState( Actor.STATE_DYING, _littleRed, null, 2000, -1 ) ); 
        
        _littleRed.initActor( Hero.ATT_MAX_HEALTH, _currentLives, Hero.ATT_SPEED_X, Hero.ATT_SPEED_Y, posX, posY, facingLeft );         
        
        insert( _littleRed, 0 ); 
        
        LittleRed.getInstance().printDebug( this, "initLittleRed", "method complete!" );                
        
    }
    
    /**
     * Called by the loadLevel method to add some empty effects into our effects
     * vector; the reason for this is that object creation is an expensive process and
     * hopefully we get away with just doing it once
     **/
    private boolean initGameEffects(){
        
        Image imgGameEffects = null; 
        GameEffect effect = null;
        
        if( _gameEffectsLoaded ){
            Enumeration effects = _effects.elements();
            while( effects.hasMoreElements() ){
                insert( (GameEffect)effects.nextElement(), 0 ); 
            }
        }
        else{
            try{
                imgGameEffects = Image.createImage( IMG_PATH_GAME_EFFECTS ); 
            }
            catch( IOException e ){
                LittleRed.getInstance().printError( this, "initGameEffects", e.getMessage() );
                return false; 
            }                

            effect = new GameEffect( imgGameEffects );
            _effects.addElement( effect );
            insert( effect, 0 );

            effect = new GameEffect( imgGameEffects );
            _effects.addElement( effect );
            insert( effect, 0 );

            effect = new GameEffect( imgGameEffects );
            _effects.addElement( effect );
            insert( effect, 0 );

            effect = new GameEffect( imgGameEffects );
            _effects.addElement( effect );
            insert( effect, 0 );

            imgGameEffects = null;
        }
        
        return true; 
    }
    
     /**
     * Called by the loadLevel method to add some empty projectiles into our projectiles
     * vector; the reason for this is that object creation is an expensive process and
     * hopefully we get away with just doing it once
     **/
    private boolean initProjectiles(){
        
        Image imgProjectiles = null; 
        Projectile proj = null;
        
        if( _projectilesLoaded ){
            Enumeration projectiles = _projectiles.elements();
            while( projectiles.hasMoreElements() ){
                insert( (Projectile)projectiles.nextElement(), 0 ); 
            }
        }
        else{
            try{
                imgProjectiles = Image.createImage( IMG_PATH_PROJECTILES ); 
            }
            catch( IOException e ){
                LittleRed.getInstance().printError( this, "initProjectiles", e.toString() );
                return false; 
            }                
            
            for( int i=0; i< 6; i++ ){
                proj = new Projectile( imgProjectiles, 17, 17 );
                _projectiles.addElement( proj );
                insert( proj, 0 );
            }
            
            imgProjectiles = null;
        }
        
        return true; 
    }
    
    /**
     * Called each time loadLevel is called; to keep things interesting I decided to
     * randomly selected a background image for every new level; keeps the player
     * interested?
     **/
    private void initBackground(){
        if( _rand == null )
            _rand = new Random();
        
        int i = _rand.nextInt( BG_COUNT ); // select a random number between 0 and 2
        
        // if random background is the same as the one currently displaying then return
        // and use the current settings
        if( i == _currentBackground )
            return;
        
        _currentBackground = i;
        
        String bgPath = IMGPATH_BG_DEFAULT;
        
        switch( _currentBackground ){
            case 0:
                bgPath = IMGPATH_BG_0;
                _stageBGColour = 0x314273;
                break;
            case 1:
                bgPath = IMGPATH_BG_1;
                _stageBGColour = 0x927430;
                break;
            case 2:
                bgPath = IMGPATH_BG_2;
                _stageBGColour = 0xF69A2F;
                break;
        }
        try{
            _imgBackGround = Image.createImage( bgPath );
        } catch( Exception e ){
            LittleRed.getInstance().printError( this, "initBackground", e.getMessage() );
        }
        
        LittleRed.getInstance().printDebug( this, "initBackground", "finished!" ); 
    }
    
    private String readString(InputStream is, byte terminator) {
        try {
            StringBuffer sb = new StringBuffer();
            int b = is.read();
            while (b != -1) {
                if (b == terminator) {
                    return sb.toString();
                } else
                    sb.append((char)b);
                
                b = is.read();
            }
            
            return null;
        }
        
        catch(IOException e) {
            LittleRed.getInstance().printError( this, "readString", e.getMessage() );
            return null;
        }
        
    }
    
    private String reverseString( String value ){
        char[] split = value.toCharArray();
        String result = "";
        
        for( int i = split.length-1; i >= 0; i-- )
            result += split[i];
        
        return result;
    }
    
    //</editor-fold>
    
}
