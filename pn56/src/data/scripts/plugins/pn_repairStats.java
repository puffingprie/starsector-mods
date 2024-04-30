package pn.data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;

import java.awt.*;

/*
to be put on a weapon that will execute script on a ship
multiple weapons will increase the efficiency
 */
public class pn_repairStats implements EveryFrameWeaponEffectPlugin {
    //engine
    private CombatEngineAPI engine = Global.getCombatEngine();

    //////////////////////////////////////////////////////////////////////////////////////////////

    //timers
    private IntervalUtil fasttimer = new IntervalUtil(.1f, .11f);
    private IntervalUtil slowtimer = new IntervalUtil(.15f, .16f);

    //////////////////////////////////////////////////////////////////////////////////////////////


    //main armor effect
    float lastcycle = 0;

    @Override
    public void advance(float v, CombatEngineAPI engineAPI, WeaponAPI weaponAPI) {
        //ship
        ShipAPI ship;
        ship = weaponAPI.getShip();

        //////////////////////////////////////////////////////////////////////////////////////////////

        //stats of system
        float FluPerAPoint = 0;    //how much 1 armor point is worth in terms of flux
        //float ReBaArmRate = .005f;  //how fast armor rebalanced  max 1 for instant
        float MaxFlux = .8f;        //cap for this system being active
        float RegenRate = .002f;    //rate armor regenerates as a decimal
        float activeRate = .005f;    //how fast active armor balancer works
        float MinHealthActive = .4f;//minimum health for active armor sharing
        float weaponsizemult = 1f;
        //weapon size multiplier
        WeaponAPI.WeaponSize weapsize = weaponAPI.getSize();
        if (weapsize.equals(WeaponAPI.WeaponSize.SMALL)) {
            weaponsizemult = .5f;
        }
        if (weapsize.equals(WeaponAPI.WeaponSize.MEDIUM)) {
            weaponsizemult = 1f;
        }
        if (weapsize.equals(WeaponAPI.WeaponSize.LARGE)) {
            weaponsizemult = 2f;
        }

        //////////////////////////////////////////////////////////////////////////////////////////////

        //game is paused dont do anything  //weapon is disabled ""
        if (engine.isPaused() || weaponAPI.isDisabled()) {
            return;
        }

        //////////////////////////////////////////////////////////////////////////////////////////////

        //if(ship.getSystem().isActive()) optional link to ship system
        {
            //advance timers
            slowtimer.advance(v);
            fasttimer.advance(v);

            //////////////////////////////////////////////////////////////////////////////////////////////

            //main code
            if (fasttimer.intervalElapsed()) {
                //stuff that is used alot
                ArmorGridAPI armorgrid = ship.getArmorGrid();
                float armorrating = armorgrid.getArmorRating();
                float MaxCell = armorgrid.getMaxArmorInCell();

                //////////////////////////////////////////////////////////////////////////////////////////

                //armor grid stats
                int maxX = armorgrid.getLeftOf() + armorgrid.getRightOf();
                int maxY = armorgrid.getAbove() + armorgrid.getBelow();

                //////////////////////////////////////////////////////////////////////////////////////////

                //avarage armor of ship hull
                float armorcells = 0;               //number of cells ship has
                for (int X = 0; X < maxX; X++) {
                    for (int Y = 0; Y < maxY; Y++) {
                        armorcells++;
                    }
                }
                //float ReBalArmor = curarmor/armorcells;

                //////////////////////////////////////////////////////////////////////////////////////////

                //adjusted stats
                float adjust = weaponsizemult * Math.min(125 / armorcells, 4); //max increase of rate (prevents 100x rate on small ship)
                FluPerAPoint = 0;    //how much 1 armor point is worth in terms of flux
                //float ReBaArmRate = .005f;  //how fast armor rebalanced  max 1 for instant
                MaxFlux = .8f;        //cap for this system being active
                RegenRate = .002f * adjust;    //rate armor regenerates as a decimal
                activeRate = .005f * adjust;    //how fast active armor balancer works
                MinHealthActive = .4f * adjust;//minimum health for active armor sharing

                //////////////////////////////////////////////////////////////////////////////////////////

                //basic armor state of ship
                float curarmor = getTotalArmor(ship);
                //float ArmLost = armorgrid.getArmorRating()-curarmor;  //how much armor was damaged

                //////////////////////////////////////////////////////////////////////////////////////////

                //calculate regen rate based on flux (prevents cells from filling up sequentially at low flux)
                float FluxRemaining = (ship.getFluxTracker().getMaxFlux() * MaxFlux) - ship.getFluxTracker().getCurrFlux();
                //float FluxToRepairMax = ArmLost * FluPerAPoint;
                float NormRepPerFrame = (MaxCell * RegenRate)
                        * ((MaxFlux - ship.getFluxTracker().getFluxLevel()) / MaxFlux);//aditional level of repair decrease
                //float FluxToRepairNorm = NormRepPerFrame * FluPerAPoint * armorcells;
                //float FluxForRep = (FluxToRepairMax < FluxToRepairNorm ? FluxToRepairMax : FluxToRepairNorm);

                //easier, more accurate  (compares the cost to repair in last cycle to amount of flux left)
                if (lastcycle == 0) {
                    lastcycle = NormRepPerFrame * armorcells * FluPerAPoint;
                }
                float FluxForRep = lastcycle;
                float FluxToRepairNorm = lastcycle;
                float RepRate = (FluxForRep < FluxRemaining ? NormRepPerFrame : NormRepPerFrame * (FluxRemaining / FluxToRepairNorm));

                //////////////////////////////////////////////////////////////////////////////////////////


                //armor manager
                float next = 0;
                lastcycle = 0; //clears lastcycle
                //active cycle (needs to be separate)
                //lets damaged cells take armor from nearby healthy cells
                for (int X = 0; X < maxX; X++)       //
                {                                //cycle through all armor cells on ship
                    for (int Y = 0; Y < maxY; Y++)   //
                    {
                        float cur = armorgrid.getArmorValue(X, Y); //health of current cell
                        //Active ReBalArmor
                        //mover armor from nearby cells to damaged ones
                        //can be tied to an if statement
                        {
                            //take armor from nearby healthy cells
                            float Forwardsum = 0;
                            for (int Xa = (X == 0 ? X : X - 1); Xa < maxX && Xa >= 0 && Xa <= X + 1; Xa++) {
                                for (int Ya = (Y == 0 ? Y : Y - 1); Ya < maxY && Ya >= 0 && Ya <= Y + 1; Ya++) {
                                    float cell = armorgrid.getArmorValue(Xa, Ya);
                                    if (cell > cur && armorgrid.getArmorFraction(Xa, Ya) > MinHealthActive) {
                                        float diff = (cell - cur) * activeRate;
                                        next = (cell - diff);
                                        armorgrid.setArmorValue(Xa, Ya, next > 0 ? next : 0);
                                        cur += diff;
                                        //ship.getFluxTracker().increaseFlux(diff*FluPerAPoint*.1f, true);
                                        //uses 1/10th of the normal flux to move armor around (too costly in flux)
                                    }
                                }
                            }
                            next = cur;
                            armorgrid.setArmorValue(X, Y, next < MaxCell ? next : MaxCell); //add it to cell
                        }
                    }
                }

                /////////////////////////////////////////////////////////////////////////////////////


                //passive cycle
                for (int X = 0; X < maxX; X++)       //
                {                                //cycle through all armor cells on ship
                    for (int Y = 0; Y < maxY; Y++)   //
                    {
                        float cur = armorgrid.getArmorValue(X, Y); //health of current cell

                        //only do repair if cell health is more then 0 prevents immortal ship syndrome
                        if (cur > 0) {
                            //regen armor
                            if (cur < MaxCell) {
                                next = cur + (cur / MaxCell) * RepRate;          //how much armor should be regenerated


                                armorgrid.setArmorValue(X, Y, next < MaxCell ? next : MaxCell);
                                float fluxuse = (next - cur) * FluPerAPoint;
                                ship.getFluxTracker().increaseFlux(fluxuse, true);
                                lastcycle += fluxuse;
                            }
                        }

                    }
                }


                /////////////////////////////////////////////////////////////////////////////////////


            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    //gets total armor of a ship
    public static float getTotalArmor(ShipAPI ship) {
        ArmorGridAPI armorgrid = ship.getArmorGrid();
        float sum = 0;
        int maxX = armorgrid.getLeftOf() + armorgrid.getRightOf();
        int maxY = armorgrid.getAbove() + armorgrid.getBelow();

        for (int X = 0; X < maxX; X++) {
            for (int Y = 0; Y < maxY; Y++) {
                sum += armorgrid.getArmorValue(X, Y);
            }
        }
        return sum;
    }

}