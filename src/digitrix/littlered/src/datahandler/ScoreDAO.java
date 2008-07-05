/*
 * ScoreDAO.java
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
 * This class is responsble for saving and retrieving the players score data
 * (used to display user details in the my status menu). 
 * @author Josh
 */
public class ScoreDAO {
    
    private static ScoreDAO _self = null; 
    private LittleRed _midlet = null; 
    
    public static final String RS_HIGHSCORES    = "HiScores";
    public static final int NUMSCORES          = 5;
    public static final int LEVEL              = 0;
    public static final int SCORE              = 1; 
    
    // RecordStore Details for Game
    public static final String RS_GAME          ="Scores"; // store level and store
    public static int REC_LEVEL                 = 1;
    public static int REC_SCORE                 = 2;
    
    private static int[][] _highScores = null;
    
    /** Creates a new instance of ScoreDAO */
    public ScoreDAO() {                
        _self = this; 
        _midlet = LittleRed.getInstance(); 
        
        try{
            loadHighScores(); 
        }
        catch( Exception e ){
            _midlet.printError( this, "ScoreDAO()", e.toString() ); 
        }
    }    
    
    public void loadHighScores() throws RecordStoreException {
        RecordStore rs = null;
        RecordEnumeration re = null; 
        
        // initilise scores
        _highScores = new int[5][2]; 
        for( int i=0; i<_highScores.length; i++ ){
            _highScores[i][LEVEL] = 0;
            _highScores[i][SCORE] = 0;            
        }
        
        try{
            rs = RecordStore.openRecordStore( RS_HIGHSCORES, true );
            re = rs.enumerateRecords( null, null, false );
            
            int i=0;  // array indexer
            
            while( re.hasNextElement() ){
                byte[] raw = re.nextRecord(); 
                String details = new String(raw); 
                
                _midlet.printDebug( this, "loadHighScores", "Fetching data (" + details + ") from the RMS" );
                
                // parse out level and score 
                int index = details.indexOf( "|" );
                int level = Integer.parseInt( details.substring( 0, index ) ); 
                int score = Integer.parseInt( details.substring( index+1 ) ); 
                _highScores[i][LEVEL] = level; 
                _highScores[i][SCORE] = score;
                
                i++;                 
            }
        } 
        catch( Exception e ){
            _midlet.printError( this, "loadHighScores()", e.toString() ); 
        }
        finally{
            if( re != null ) re.destroy(); 
            if( rs != null ) rs.closeRecordStore(); 
        }                
        
    }
    
    /** add a new score to the array and save it to the recordstore, before we add we must sort out the 
     * existing array in order of level and score and then finally save to the recordstore **/ 
    public void addScore( int level, int score ){
        
        // add new scores to the array 
        if( !sortoutScores( level, score ) )
            return; 
        
        // persist changes to the array 
        try{
            saveHighScores(); 
        }
        catch( Exception e ){
            _midlet.printError( this, "addScore", e.toString() ); 
        }
        
        
    }
    
    /** purpose of this method is to persist the array of high scores to the phones RMS **/ 
    private void saveHighScores() throws RecordStoreException {
        RecordStore rs = null;
        RecordEnumeration re = null;
        
        try{
            rs = RecordStore.openRecordStore( RS_HIGHSCORES, true );
            re = rs.enumerateRecords( null, null, false );
            
            // first remove all records 
            while( re.hasNextElement() ){
                int id = re.nextRecordId();
                rs.deleteRecord( id );
            }
            
            // now save the scores 
            for( int i=0; i<_highScores.length; i++ ){
                String data = _highScores[i][LEVEL] + "|" + _highScores[i][SCORE];
                
                _midlet.printDebug( this, "saveHighScores", "Submitting data (" + data + ") into the RMS" );
                
                byte[] raw = data.getBytes(); 
                rs.addRecord( raw, 0, raw.length ); 
            }
        }
        catch( Exception e ){
            _midlet.printError( this, "saveHighScores()", e.toString() ); 
        }
        finally{
            if( re != null ) re.destroy();
            if( rs != null ) rs.closeRecordStore(); 
        }
    }
    
    /** sole purpose is to add existing level and score to the array if it is higher than one of the existing ones **/ 
    private boolean sortoutScores( int level, int score ){
        int iLevel = -1; 
        int iScore = -1; 
        
        for( int i=0; i<_highScores.length; i++ ){
            // if the new level and score match the existing one then exit 
            if( level == _highScores[i][LEVEL] && score == _highScores[i][SCORE] )
                return false; 
            
            if( level >= _highScores[i][LEVEL] )
                iLevel = i; 
            
            if( score >= _highScores[i][SCORE] )
                iScore = i;
            
            if( iLevel != -1 && iScore != -1 )
                break; 
        }
        
        // if all existing levels and scores are higher than the one passed 
        // via the parameters then exit out of the method 
        if( iLevel == -1 && iScore == -1 )
            return false; 
        
        // else move every record down from newIndex if newIndex is within range and 
        // the existing values are not zero (ie no scores have been saved yet) 
        int newIndex = Math.max( iLevel, iScore ); 
        
        if( newIndex >= _highScores.length)
            return false;                 
        
        if( _highScores[newIndex][SCORE] > 0 ){
            for( int i=NUMSCORES-2; i>newIndex; i-- ){
                _highScores[i][LEVEL] = _highScores[i-1][LEVEL];
                _highScores[i][SCORE] = _highScores[i-1][SCORE];
            }
        }
        
        _highScores[newIndex][LEVEL] = level; 
        _highScores[newIndex][SCORE] = score;
        
        return true; 
        
    }
    
    /** returns the array of highscores **/ 
    public int[][] getHighScores(){
        return _highScores; 
    }
    
    public static ScoreDAO getInstance(){
        if( _self == null )
            _self = new ScoreDAO(); 
        
        return _self; 
    }
    
}
