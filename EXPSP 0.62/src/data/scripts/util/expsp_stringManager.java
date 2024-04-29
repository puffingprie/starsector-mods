/*
By Tartiflette
 */
package data.scripts.util;

import com.fs.starfarer.api.Global;

public class expsp_stringManager {
    private static final String ML="expsp";    
    
    public static String txt(String id){
        return Global.getSettings().getString(ML, id);
    }
}