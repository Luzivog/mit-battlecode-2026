package opp_powerpuff.Micro.Attack;

import opp_powerpuff.Micro.MicroBabyManager;
import battlecode.common.GameActionException;

public class Decision {
    public static void calculate() throws GameActionException {
        // Calculate costs once before the unrolled block
        boolean throwSuccess = Throw.cost();
        boolean biteSuccess = Bite.cost();
        boolean trapSuccess = Trap.cost();
        boolean carrySuccess = Carry.cost();

        // Unrolling loop 0 to 8
        // Index 0
        if (Bite.attack[0] != null) {
            MicroBabyManager.microInfos[0].atkId = 4;
            MicroBabyManager.microInfos[0].atkDir = Bite.attack[0];
        }

        if (trapSuccess) {
            if (Trap.attack[0] != null) {
                MicroBabyManager.microInfos[0].atkId = 3;
                MicroBabyManager.microInfos[0].atkDir = Trap.attack[0];                   
            }
        }

        if (carrySuccess) {
            if (Carry.attack[0] != null) {
                MicroBabyManager.microInfos[0].atkId = 2;
                MicroBabyManager.microInfos[0].atkDir = Carry.attack[0];
            }
        }

        if (biteSuccess) {
            if (Bite.kill[0] != null) {
                MicroBabyManager.microInfos[0].atkId = 1;
                MicroBabyManager.microInfos[0].atkDir = Bite.kill[0];
            }
        }

        if (throwSuccess) {
            if (Throw.attack[0] != null) {
                MicroBabyManager.microInfos[0].atkId = 0;
                MicroBabyManager.microInfos[0].atkDir = Throw.attack[0];
            }
            if (Throw.kill[0] != null) {
                MicroBabyManager.microInfos[0].atkId = 0;
                MicroBabyManager.microInfos[0].atkDir = Throw.kill[0];
            }
        }
        // Index 1
        if (Bite.attack[1] != null) {
            MicroBabyManager.microInfos[1].atkId = 4;
            MicroBabyManager.microInfos[1].atkDir = Bite.attack[1];
        }

        if (trapSuccess) {
            if (Trap.attack[1] != null) {
                MicroBabyManager.microInfos[1].atkId = 3;
                MicroBabyManager.microInfos[1].atkDir = Trap.attack[1];                   
            }
        }

        if (carrySuccess) {
            if (Carry.attack[1] != null) {
                MicroBabyManager.microInfos[1].atkId = 2;
                MicroBabyManager.microInfos[1].atkDir = Carry.attack[1];
            }
        }

        if (biteSuccess) {
            if (Bite.kill[1] != null) {
                MicroBabyManager.microInfos[1].atkId = 1;
                MicroBabyManager.microInfos[1].atkDir = Bite.kill[1];
            }
        }

        if (throwSuccess) {
            if (Throw.attack[1] != null) {
                MicroBabyManager.microInfos[1].atkId = 0;
                MicroBabyManager.microInfos[1].atkDir = Throw.attack[1];
            }
            if (Throw.kill[1] != null) {
                MicroBabyManager.microInfos[1].atkId = 0;
                MicroBabyManager.microInfos[1].atkDir = Throw.kill[1];
            }
        }
        // Index 2
        if (Bite.attack[2] != null) {
            MicroBabyManager.microInfos[2].atkId = 4;
            MicroBabyManager.microInfos[2].atkDir = Bite.attack[2];
        }

        if (trapSuccess) {
            if (Trap.attack[2] != null) {
                MicroBabyManager.microInfos[2].atkId = 3;
                MicroBabyManager.microInfos[2].atkDir = Trap.attack[2];                   
            }
        }

        if (carrySuccess) {
            if (Carry.attack[2] != null) {
                MicroBabyManager.microInfos[2].atkId = 2;
                MicroBabyManager.microInfos[2].atkDir = Carry.attack[2];
            }
        }

        if (biteSuccess) {
            if (Bite.kill[2] != null) {
                MicroBabyManager.microInfos[2].atkId = 1;
                MicroBabyManager.microInfos[2].atkDir = Bite.kill[2];
            }
        }

        if (throwSuccess) {
            if (Throw.attack[2] != null) {
                MicroBabyManager.microInfos[2].atkId = 0;
                MicroBabyManager.microInfos[2].atkDir = Throw.attack[2];
            }
            if (Throw.kill[2] != null) {
                MicroBabyManager.microInfos[2].atkId = 0;
                MicroBabyManager.microInfos[2].atkDir = Throw.kill[2];
            }
        }
        // Index 3
        if (Bite.attack[3] != null) {
            MicroBabyManager.microInfos[3].atkId = 4;
            MicroBabyManager.microInfos[3].atkDir = Bite.attack[3];
        }

        if (trapSuccess) {
            if (Trap.attack[3] != null) {
                MicroBabyManager.microInfos[3].atkId = 3;
                MicroBabyManager.microInfos[3].atkDir = Trap.attack[3];                   
            }
        }

        if (carrySuccess) {
            if (Carry.attack[3] != null) {
                MicroBabyManager.microInfos[3].atkId = 2;
                MicroBabyManager.microInfos[3].atkDir = Carry.attack[3];
            }
        }

        if (biteSuccess) {
            if (Bite.kill[3] != null) {
                MicroBabyManager.microInfos[3].atkId = 1;
                MicroBabyManager.microInfos[3].atkDir = Bite.kill[3];
            }
        }

        if (throwSuccess) {
            if (Throw.attack[3] != null) {
                MicroBabyManager.microInfos[3].atkId = 0;
                MicroBabyManager.microInfos[3].atkDir = Throw.attack[3];
            }
            if (Throw.kill[3] != null) {
                MicroBabyManager.microInfos[3].atkId = 0;
                MicroBabyManager.microInfos[3].atkDir = Throw.kill[3];
            }
        }
        // Index 4
        if (Bite.attack[4] != null) {
            MicroBabyManager.microInfos[4].atkId = 4;
            MicroBabyManager.microInfos[4].atkDir = Bite.attack[4];
        }

        if (trapSuccess) {
            if (Trap.attack[4] != null) {
                MicroBabyManager.microInfos[4].atkId = 3;
                MicroBabyManager.microInfos[4].atkDir = Trap.attack[4];                   
            }
        }

        if (carrySuccess) {
            if (Carry.attack[4] != null) {
                MicroBabyManager.microInfos[4].atkId = 2;
                MicroBabyManager.microInfos[4].atkDir = Carry.attack[4];
            }
        }

        if (biteSuccess) {
            if (Bite.kill[4] != null) {
                MicroBabyManager.microInfos[4].atkId = 1;
                MicroBabyManager.microInfos[4].atkDir = Bite.kill[4];
            }
        }

        if (throwSuccess) {
            if (Throw.attack[4] != null) {
                MicroBabyManager.microInfos[4].atkId = 0;
                MicroBabyManager.microInfos[4].atkDir = Throw.attack[4];
            }
            if (Throw.kill[4] != null) {
                MicroBabyManager.microInfos[4].atkId = 0;
                MicroBabyManager.microInfos[4].atkDir = Throw.kill[4];
            }
        }
        // Index 5
        if (Bite.attack[5] != null) {
            MicroBabyManager.microInfos[5].atkId = 4;
            MicroBabyManager.microInfos[5].atkDir = Bite.attack[5];
        }

        if (trapSuccess) {
            if (Trap.attack[5] != null) {
                MicroBabyManager.microInfos[5].atkId = 3;
                MicroBabyManager.microInfos[5].atkDir = Trap.attack[5];                   
            }
        }

        if (carrySuccess) {
            if (Carry.attack[5] != null) {
                MicroBabyManager.microInfos[5].atkId = 2;
                MicroBabyManager.microInfos[5].atkDir = Carry.attack[5];
            }
        }

        if (biteSuccess) {
            if (Bite.kill[5] != null) {
                MicroBabyManager.microInfos[5].atkId = 1;
                MicroBabyManager.microInfos[5].atkDir = Bite.kill[5];
            }
        }

        if (throwSuccess) {
            if (Throw.attack[5] != null) {
                MicroBabyManager.microInfos[5].atkId = 0;
                MicroBabyManager.microInfos[5].atkDir = Throw.attack[5];
            }
            if (Throw.kill[5] != null) {
                MicroBabyManager.microInfos[5].atkId = 0;
                MicroBabyManager.microInfos[5].atkDir = Throw.kill[5];
            }
        }
        // Index 6
        if (Bite.attack[6] != null) {
            MicroBabyManager.microInfos[6].atkId = 4;
            MicroBabyManager.microInfos[6].atkDir = Bite.attack[6];
        }

        if (trapSuccess) {
            if (Trap.attack[6] != null) {
                MicroBabyManager.microInfos[6].atkId = 3;
                MicroBabyManager.microInfos[6].atkDir = Trap.attack[6];                   
            }
        }

        if (carrySuccess) {
            if (Carry.attack[6] != null) {
                MicroBabyManager.microInfos[6].atkId = 2;
                MicroBabyManager.microInfos[6].atkDir = Carry.attack[6];
            }
        }

        if (biteSuccess) {
            if (Bite.kill[6] != null) {
                MicroBabyManager.microInfos[6].atkId = 1;
                MicroBabyManager.microInfos[6].atkDir = Bite.kill[6];
            }
        }

        if (throwSuccess) {
            if (Throw.attack[6] != null) {
                MicroBabyManager.microInfos[6].atkId = 0;
                MicroBabyManager.microInfos[6].atkDir = Throw.attack[6];
            }
            if (Throw.kill[6] != null) {
                MicroBabyManager.microInfos[6].atkId = 0;
                MicroBabyManager.microInfos[6].atkDir = Throw.kill[6];
            }
        }
        // Index 7
        if (Bite.attack[7] != null) {
            MicroBabyManager.microInfos[7].atkId = 4;
            MicroBabyManager.microInfos[7].atkDir = Bite.attack[7];
        }

        if (trapSuccess) {
            if (Trap.attack[7] != null) {
                MicroBabyManager.microInfos[7].atkId = 3;
                MicroBabyManager.microInfos[7].atkDir = Trap.attack[7];                   
            }
        }

        if (carrySuccess) {
            if (Carry.attack[7] != null) {
                MicroBabyManager.microInfos[7].atkId = 2;
                MicroBabyManager.microInfos[7].atkDir = Carry.attack[7];
            }
        }

        if (biteSuccess) {
            if (Bite.kill[7] != null) {
                MicroBabyManager.microInfos[7].atkId = 1;
                MicroBabyManager.microInfos[7].atkDir = Bite.kill[7];
            }
        }

        if (throwSuccess) {
            if (Throw.attack[7] != null) {
                MicroBabyManager.microInfos[7].atkId = 0;
                MicroBabyManager.microInfos[7].atkDir = Throw.attack[7];
            }
            if (Throw.kill[7] != null) {
                MicroBabyManager.microInfos[7].atkId = 0;
                MicroBabyManager.microInfos[7].atkDir = Throw.kill[7];
            }
        }
        // Index 8
        if (Bite.attack[8] != null) {
            MicroBabyManager.microInfos[8].atkId = 4;
            MicroBabyManager.microInfos[8].atkDir = Bite.attack[8];
        }

        if (trapSuccess) {
            if (Trap.attack[8] != null) {
                MicroBabyManager.microInfos[8].atkId = 3;
                MicroBabyManager.microInfos[8].atkDir = Trap.attack[8];                   
            }
        }

        if (carrySuccess) {
            if (Carry.attack[8] != null) {
                MicroBabyManager.microInfos[8].atkId = 2;
                MicroBabyManager.microInfos[8].atkDir = Carry.attack[8];
            }
        }

        if (biteSuccess) {
            if (Bite.kill[8] != null) {
                MicroBabyManager.microInfos[8].atkId = 1;
                MicroBabyManager.microInfos[8].atkDir = Bite.kill[8];
            }
        }

        if (throwSuccess) {
            if (Throw.attack[8] != null) {
                MicroBabyManager.microInfos[8].atkId = 0;
                MicroBabyManager.microInfos[8].atkDir = Throw.attack[8];
            }
            if (Throw.kill[8] != null) {
                MicroBabyManager.microInfos[8].atkId = 0;
                MicroBabyManager.microInfos[8].atkDir = Throw.kill[8];
            }
        }
    }
}