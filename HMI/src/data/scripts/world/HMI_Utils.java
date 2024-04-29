/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.world;

import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import java.util.Random;

public class HMI_Utils {
    
        Random characterSaveSeed = StarSystemGenerator.random;
        Random random2 = new Random(characterSaveSeed.nextLong());
        float selector = random2.nextFloat();
    
        //Mess Remnant Station generation
        public static final String MESS_REMNANT_STATION = "mess_remnant_station2_Standard";
        public static int level = 20;
    
}
