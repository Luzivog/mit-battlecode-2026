package opp_powerpuff.Micro.Attack;

import opp_powerpuff.Rat;
import opp_powerpuff.Helpers.Comms;
import opp_powerpuff.Managers.VisionManager;
import opp_powerpuff.Utils.Calc;
import battlecode.common.*;

public class Fight {
    public static int lastCarried = 0;

    public static void attack(Direction moveDir, int id, Direction attackDir) throws GameActionException {
        Rat.rc.setIndicatorString("MOVE DIR: " + moveDir + ", ATTACK ID: " + id + ", ATTACK DIR: " + attackDir);
        RobotController rc = Rat.rc;
        switch (id) {
            case 0 -> {               
                if (attackDir == moveDir) wrappedTurn(moveDir);
                if (rc.canMove(moveDir)) rc.move(moveDir); // Fix lazy
                wrappedTurn(attackDir);
                if (rc.canThrowRat()) {
                    rc.throwRat();
                } else {
                    System.out.println("ISSUE: ROBOT CANNOT THROW");
                }
            }
            case 1 -> {
                moveForAttack(moveDir, attackDir);
                inVisionTurn(attackDir);
                MapLocation loc = Rat.rc.getLocation().add(attackDir);
                RobotInfo enemy = Rat.rc.senseRobotAtLocation(loc);
                if (enemy == null) return;
                int cheese = Calc.cheeseForDamage(enemy.health);
                if (rc.canAttack(loc, cheese)) {
                    rc.attack(loc, cheese);
                } else {
                    System.out.println("ISSUE: ROBOT CANNOT BITE KILL");
                }
            }
            case 2 -> {
                moveForAttack(moveDir, attackDir);
                inVisionTurn(attackDir);
                MapLocation loc = Rat.rc.getLocation().add(attackDir);
                if (rc.canCarryRat(loc)) {
                    rc.carryRat(loc);
                    lastCarried = rc.getCarrying().ID;
                } else {
                    System.out.println("ISSUE: ROBOT CANNOT CARRY");
                }
            }
            case 3 -> {
                moveForAttack(moveDir, attackDir);
                inVisionTurn(attackDir);
                MapLocation loc = Rat.rc.getLocation().add(attackDir);
                if (rc.canPlaceRatTrap(loc)) {
                    rc.placeRatTrap(loc);
                } else {
                    attack(Direction.CENTER, 4, attackDir);
                }
            }
            case 4 -> {
                moveForAttack(moveDir, attackDir);
                inVisionTurn(attackDir);
                MapLocation loc = Rat.rc.getLocation().add(attackDir);
                if (rc.canAttack(loc)) {
                    rc.attack(loc);
                } else {
                    System.out.println("ISSUE: CANNOT ATTACK NORMALLY");
                }
            }
            default -> {
                if (Rat.rc.canMove(moveDir))
                    Rat.rc.move(moveDir);
                if (Rat.rc.canTurn())
                    Rat.rc.turn(Rat.rc.getLocation().directionTo(VisionManager.closestEnemy.getLocation()));
            }
        }
    }

    public static void moveForAttack(Direction moveDir, Direction attackDir) throws GameActionException {
        if (moveDir == Direction.CENTER) return;
        if (attackDir == null) {
            wrappedTurn(moveDir);
            if (Rat.rc.canMove(moveDir)) {
                Rat.rc.move(moveDir);
            }
            return;
        }
        if (
            moveDir == attackDir.rotateLeft() ||
            moveDir == attackDir ||
            moveDir == attackDir.rotateRight()
        ) {
            wrappedTurn(moveDir);
            if (Rat.rc.canMove(moveDir)) {
                Rat.rc.move(moveDir);
            }
            else System.out.println("YOUR SHIT BROKE I CAN'T MOVE FORWARD");
        } else {
            if (Rat.rc.canMove(moveDir))
                Rat.rc.move(moveDir);
            else System.out.println("YOUR SHIT BROKE I CAN'T MOVE NOT FORWARD");
        }
    }

    public static void inVisionTurn(Direction d) throws GameActionException {
        if (
            d != Rat.rc.getDirection().rotateLeft() &&
            d != Rat.rc.getDirection() && 
            d != Rat.rc.getDirection().rotateRight()
        ) {
            wrappedTurn(d);
        }
    }

    public static void wrappedTurn(Direction d) throws GameActionException {
        if (Rat.rc.getDirection() != d) {
            if (Rat.rc.canTurn()) {
                Rat.rc.turn(d);
            }
        }
    }
}
