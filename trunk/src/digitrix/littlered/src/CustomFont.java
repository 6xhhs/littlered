/*
 * CustomFont.java
 *
 * Created on 27 November 2005, 19:41
 * http://www.j2meforums.com/forum/index.php?topic=11106.15
 */

package digitrix.littlered.src;

/**
 *
 * @author CJT
 */
import javax.microedition.lcdui.*;

public class CustomFont {
    
    public static final int FONT_LARGE = 0;
    public static final int FONT_SMALL = 1; 
    
    /** Creates a new instance of CustomFont */
    public CustomFont() {
    }
    
    // large font details
    Image lCustomFont;
    int lLettersX[] = new int[256];
    int lLettersY[] = new int[256];
    boolean lCapsOnly = true;
    int lCharWidth = 0;
    int lCharHeight = 0;
    int lSpacing=0;
    
    // small font details
    Image sCustomFont;
    int sLettersX[] = new int[256];
    int sLettersY[] = new int[256];
    boolean sCapsOnly = true;
    int sCharWidth = 0;
    int sCharHeight = 0;
    int sSpacing=0;
    
    public void initLargeFont(String fontChars, Image fontImage, int fontWidth, int fontHeight, boolean allCaps) {
        lCustomFont = fontImage;
        lCapsOnly=allCaps;
        lCharWidth = fontWidth;
        lSpacing=lCharWidth;
        lCharHeight = fontHeight;
        byte charBytes[] = fontChars.getBytes();
        int charsPerRow = (int)(fontImage.getWidth()/fontWidth);
        int columnCharCount = 0;
        int Xtrans=0;
        int Ytrans=0;
        int ASCIIval=0;
        for (int i=0; i<charBytes.length; i++) {
            ASCIIval=(0|charBytes[i])&0xFF;
            lLettersX[ASCIIval]=Xtrans;
            lLettersY[ASCIIval]=Ytrans;
            columnCharCount++;
            if (columnCharCount>=charsPerRow) {
                Xtrans=0;
                Ytrans+=fontHeight;
                columnCharCount=0;
            } else Xtrans+=fontWidth;
            
        }
    }
    
     public void initSmallFont(String fontChars, Image fontImage, int fontWidth, int fontHeight, boolean allCaps) {
        sCustomFont = fontImage;
        sCapsOnly=allCaps;
        sCharWidth = fontWidth;
        sSpacing=lCharWidth;
        sCharHeight = fontHeight;
        byte charBytes[] = fontChars.getBytes();
        int charsPerRow = (int)(fontImage.getWidth()/fontWidth);
        int columnCharCount = 0;
        int Xtrans=0;
        int Ytrans=0;
        int ASCIIval=0;
        for (int i=0; i<charBytes.length; i++) {
            ASCIIval=(0|charBytes[i])&0xFF;
            sLettersX[ASCIIval]=Xtrans;
            sLettersY[ASCIIval]=Ytrans;
            columnCharCount++;
            if (columnCharCount>=charsPerRow) {
                Xtrans=0;
                Ytrans+=fontHeight;
                columnCharCount=0;
            } else Xtrans+=fontWidth;
            
        }
    }
     
     public void drawString(String plotString, int fontType, int plotX, int plotY, Graphics gC) {
        if( fontType == FONT_SMALL )
            drawSmallString( plotString, plotX, plotY, gC );
        else if( fontType == FONT_LARGE )
            drawLargeString( plotString, plotX, plotY, gC );    
     }
    
    public int getFontsWidth( int fontType ){
        if( fontType == FONT_SMALL )
            return getSmallFontsWidth();
        else if( fontType == FONT_LARGE )
            return getLargeFontsWidth();
        else 
            return -1;        
    }
    
    public int getFontsWidth( String text, int fontType ){
        if( fontType == FONT_SMALL )
            return getSmallFontsWidth( text );
        else if( fontType == FONT_LARGE )
            return getLargeFontsWidth( text );
        else 
            return -1;        
    }
    
    public int getFontsHeight( int fontType ){
        if( fontType == FONT_SMALL )
            return getSmallFontsHeight();
        else if( fontType == FONT_LARGE )
            return getLargeFontsHeight(); 
        else 
            return -1; 
    }
    
    public void setSpacing( int newSpacing, int fontType ) {
        if( fontType == FONT_SMALL )
            setSSpacing( newSpacing );
        else if( fontType == FONT_LARGE )
            setLSpacing( newSpacing );
    }
    
    private void setLSpacing(int newSpacing) {
        lSpacing=newSpacing;
    }
    
    private void setSSpacing(int newSpacing) {
        sSpacing=newSpacing;
    }
    
    private void drawLargeString(String plotString, int plotX, int plotY, Graphics gC) {
        int clipX = gC.getClipX();
        int clipY = gC.getClipY();
        int clipW = gC.getClipWidth();
        int clipH = gC.getClipHeight();
        
        if (lCapsOnly)
            plotString = plotString.toUpperCase();
        byte charBytes[] = plotString.getBytes();
        int ASCIIval=0;
        for (int i=0; i<charBytes.length; i++) {
            ASCIIval=(0|charBytes[i])&0xFF;
            if( ASCIIval != 32 ){
                gC.setClip(plotX, plotY, lCharWidth, lCharHeight);
                gC.drawImage(lCustomFont, plotX-lLettersX[ASCIIval], plotY-lLettersY[ASCIIval], Graphics.LEFT|Graphics.TOP);
            }
            plotX+=lSpacing;
        }
        
        gC.setClip( clipX, clipY, clipW, clipH );
    }               
    
    private int getLargeFontsWidth(){
        return lCharWidth;
    }
    
    private int getLargeFontsWidth( String text ){
        return lCharWidth * text.length(); 
    }
    
    private  int getLargeFontsHeight(){
        return lCharHeight; 
    }    
    
    private void drawSmallString(String plotString, int plotX, int plotY, Graphics gC) {
        int clipX = gC.getClipX();
        int clipY = gC.getClipY();
        int clipW = gC.getClipWidth();
        int clipH = gC.getClipHeight();
        
        if (sCapsOnly)
            plotString = plotString.toUpperCase();
        byte charBytes[] = plotString.getBytes();
        int ASCIIval=0;
        for (int i=0; i<charBytes.length; i++) {            
            ASCIIval=(0|charBytes[i])&0xFF;
            if( ASCIIval != 32 ){
                gC.setClip(plotX, plotY, sCharWidth, sCharHeight);
                gC.drawImage(sCustomFont, plotX-sLettersX[ASCIIval], plotY-sLettersY[ASCIIval], Graphics.LEFT|Graphics.TOP);                
            }
            plotX+=sSpacing;
        }
        
        gC.setClip( clipX, clipY, clipW, clipH );
    }
    
    private int getSmallFontsWidth(){
        return sCharWidth;
    }
    
    private int getSmallFontsWidth( String text ){
        return sCharWidth * text.length();
    }
    
    private int getSmallFontsHeight(){
        return sCharHeight; 
    }
    
}
