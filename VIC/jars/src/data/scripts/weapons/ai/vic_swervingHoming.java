package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;

public class vic_swervingHoming extends VIC_BaseMissile {


    private final MissileAPI missile;
    IntervalUtil timer;
    swervingState state;

    enum swervingState {
        left,
        right
    }

    float initialAngle;

    public vic_swervingHoming(MissileAPI missile, ShipAPI launchingShip)
    {
        super(missile, launchingShip);
        this.missile = missile;
        this.initialAngle = missile.getFacing();
        state = (Math.random() > 0.5 ? swervingState.left : swervingState.right);
        timer = new IntervalUtil(0.05f,0.2f);
        missile.getVelocity().scale(MathUtils.getRandomNumberInRange(0.5f,1.5f));
    }

    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused() || missile.isFading() || missile.isFizzling()) {
            return;
        }
        missile.giveCommand(ShipCommand.ACCELERATE);
        timer.advance(amount);
        boolean changeDirection = false;

        float desiredAngle = missile.getFacing();
        if (target != null) desiredAngle = Misc.getAngleInDegrees(missile.getLocation(), target.getLocation());

        float rotationNeeded = MathUtils.getShortestRotation(missile.getFacing(), desiredAngle);
        if ((rotationNeeded > 3f && state.equals(swervingState.right)) ||
                (rotationNeeded < -3f && state.equals(swervingState.left))){
            timer.forceIntervalElapsed();
            //Global.getCombatEngine().addFloatingText(missile.getLocation(),Misc.getAngleDiff(initialAngle, missile.getFacing()) + "",10, Color.WHITE,missile,0,0 );
        }

        if (timer.intervalElapsed()) changeDirection = true;

        if (changeDirection){
            if (state.equals(swervingState.left)){
                state = swervingState.right;
            } else {
                state = swervingState.left;
            }
        }

        boolean damp = false;
        switch (state){
            case left:
                missile.giveCommand(ShipCommand.TURN_LEFT);
                break;
            case right:
                missile.giveCommand(ShipCommand.TURN_RIGHT);
                break;
        }

    }


}
