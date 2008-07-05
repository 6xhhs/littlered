/*
 * GameDAO.java
 *
 * Created on 30 July 2007, 21:42
 *
 */

package digitrix.littlered.src.datahandler;

import digitrix.littlered.src.LittleRed;
import java.util.*;
import java.io.*;
import javax.microedition.rms.*;
import javax.microedition.lcdui.*;

/**
 * This class is responsible for handling saving and retrieving game data 
 *
 * @author Josh
 */
public class GameDAO {
    
    private static GameDAO _self = null; // singleton    
    
    // RecordStore Details for Game
    public static final String RS_GAME                = "SavedGame"; // store level and store
    public static final int REC_LEVEL                 = 3;
    public static final int REC_LIVES_LEFT            = 2;
    public static final int REC_CURRENT_SCORE         = 1;    
    
    private LittleRed _midlet = null; 
    private boolean _hasSavedGame = false;     
    
    // game data
    private int _level = -1;
    private int _lives = -1;
    private int _score = -1;
    
    /** Creates a new instance of GameDAO */
    public GameDAO() {
        _self = this; 
        _midlet = LittleRed.getInstance();         
        
        // load saved game 
        try{
            loadSavedGame(); 
        }        
        catch( RecordStoreException rse ){
            _midlet.printError( this, "GameDAO()", rse.toString() ); 
        }
    }
    
    /** Called when the object is instantiated to load the saved details, the flag _hasSavedGame is set to true if 
     ** a game exist in the RMS**/ 
    public void loadSavedGame() throws RecordStoreException{
        RecordStore rs = null; 
        RecordEnumeration re = null;;
        
        try{
            rs = RecordStore.openRecordStore( RS_GAME, false ); 
            
            if( rs == null )
                return; 
            
            re = rs.enumerateRecords( null, null, false );
            
            int recordID = 1; 
            
            while( re.hasNextElement() ){
                byte[] raw = re.nextRecord(); 
                String data = new String(raw); 
                int details = Integer.parseInt( data ); 
                
                switch( recordID ){
                    case REC_LEVEL:                        
                        _level = details;
                        _midlet.printDebug( this, "loadGame()", "Loading level " + details + " from record " + recordID + " in the GAME RMS" );
                        break;
                    case REC_LIVES_LEFT:
                        _lives = details; 
                        _midlet.printDebug( this, "loadGame()", "Loading lives " + details + " from record " + recordID + " in the GAME RMS" );
                        break; 
                    case REC_CURRENT_SCORE:
                        _score = details; 
                        _midlet.printDebug( this, "loadGame()", "Loading score " + details + " from record " + recordID + " in the GAME RMS" );
                        break;
                }
                
                recordID++; 
            }
            
            _hasSavedGame = true; 
            
        }
        catch( Exception e ){            
            _midlet.printError( this, "loadSavedGame()", e.toString() ); 
        }
        finally{
            if( re != null ) re.destroy(); 
            if( rs != null ) rs.closeRecordStore(); 
        }
    }
    
    /** save the details of the current level, should be called everytime the user passes the flag and resets his/her starting position **/ 
    public void saveGame( int currentLevel, int livesLeft, int currentScore ) throws RecordStoreException {
        RecordStore rs = null;
        RecordEnumeration re = null; 
        byte[] raw = null; 
        
        try{
            rs = RecordStore.openRecordStore( RS_GAME, true );
            
            // delete all existing records 
            re = rs.enumerateRecords( null, null, false );
            while( re.hasNextElement() ){
                int recordID =  re.nextRecordId();
                rs.deleteRecord( recordID );                 
            }
            
            // assign details to current instance 
            _level = currentLevel; 
            _lives = livesLeft; 
            _score = currentScore;             
            
            // save level details
            // REC_LEVEL:
            _midlet.printDebug( this, "saveGame()", "Adding level " + _level + " to record store");
            raw = String.valueOf( _level ).getBytes(); 
            rs.addRecord( raw, 0, raw.length ); 
            
            // REC_LIVES_LEFT:
            _midlet.printDebug( this, "saveGame()", "Adding lives " + _lives + " to record store" );
            raw = String.valueOf( _lives ).getBytes(); 
            rs.addRecord( raw, 0, raw.length ); 
            
            // REC_CURRENT_SCORE:
            _midlet.printDebug( this, "saveGame()", "Adding score " + _score + " to record store" );
            raw = String.valueOf( _score ).getBytes(); 
            rs.addRecord( raw, 0, raw.length );                         
            
            _hasSavedGame = true; 
            
        }
        catch( Exception e ){
            _midlet.printError( this, "saveGame", e.toString() );
        }
        finally{
            if( re != null ) re.destroy();
            if( rs != null ) rs.closeRecordStore(); 
        }
                    
    }
    
    /** Get the singleton of the object GameDAO **/ 
    public static GameDAO getInstance(){
        if( _self == null )
            _self = new GameDAO();
        
        return _self; 
    }

    //<editor-fold defaultstate="collapsed" desc="Properties">
    public boolean hasSavedGame() {
        return _hasSavedGame;
    }

    public int getLevel() {
        return _level;
    }

    public int getLives() {
        return _lives;
    }

    public int getScore() {
        return _score;
    }
    //</editor-fold>
    
}
