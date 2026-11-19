# Code arms of past packages in the lab copy (mkcopy.py --patch), each behind a lab constant whose 0 is main's code:
# - CB_MAXDMG (CB, t3/minimal-cheese-bites-arms-damage 24508a1): bite damage per target, kill now, flip the HP order
#   for a grab, win a 1v1 race; at most CB_MAXDMG (13 the arm, 16 its "cap 16"); also the grab skip ("just kill").
# - CC_STEP2 (CC arm A3, t3/cc-a3-step2): TryFight loses CC_STEP2 (42) on a visible enemy carrier's step-2 tiles.
# - CX_PAIR (CX arm A1, t3/cx-a-graded-only): a grab threat costs -THREAT_PENALTY - CX_PAIR per further (enemy,
#   neighbour) pair (50 the arm).
# Globals given by mkcopy: root (the copy's Path), pkg, re.
P = pkg


def sub(rel, pairs):
    f = root / rel
    s = f.read_text()
    for old, new, n in pairs:
        c = s.count(old)
        assert c == n, (rel, old[:60], c)
        s = s.replace(old, new)
    f.write_text(s)


# lab constants (mkcopy turns them into fields)
sub("Tune.java", [("\n}\n", "\n    // lab-only code arms (tools/fightlab/patches/codearms.py); 0 is main's code\n"
                   "    public static final int CB_MAXDMG = 0; // tune: 3 [0, 16] CB's bite damage cap (0: main's bite)\n"
                   "    public static final int CC_STEP2 = 0; // tune: 42 [0, 200] CC A3's loss on a carrier's step-2 tile\n"
                   "    public static final int CX_PAIR = 0; // tune: 50 [0, 200] CX A1's loss per further grab pair\n}\n", 1)])

C = "robot/rat/combat/"
# ---- CB
sub(C + "CombatStrategy.java.jinja2", [(
    "    public static int getBiteDamage() {",
    f"""    public static int biteDamage(RobotInfo target) {{
        int max = {P}.Tune.CB_MAXDMG;
        if (max == 0) return getBiteDamage();
        if (target.getType() != UnitType.BABY_RAT || rc.getAllCheese() < 300) return 10;
        int h = target.getHealth();
        if (h <= 12) return h > 10 ? h : 10;
        if (h <= max) return h;
        int myHealth = rc.getHealth();
        int d = h - myHealth + 13;
        if (d > max && RobotTracker.numEnemies == 1) {{
            int bites = (myHealth + 11) / 12;
            d = (h + bites - 1) / bites;
        }}
        return d > 12 && d <= max ? d : 12;
    }}

    public static int getBiteDamage() {{""", 1)])
sub(C + "moves/TryAttack.java.jinja2", [
    ("    static int bestDamage;\n", "    static int bestDamage;\n    static int damage;\n", 1),
    (f"        weight = {P}.Tune.ATTACK_W;\n        if (info.getHealth() <= CombatStrategy.getBiteDamage()) {{",
     f"        weight = {P}.Tune.ATTACK_W;\n        damage = CombatStrategy.biteDamage(info);\n        if (info.getHealth() <= damage) {{", 1),
    ("CombatStrategy.getCellScoreFor(moveDir, {{ face_dir }}, info, CombatStrategy.getBiteDamage());",
     "CombatStrategy.getCellScoreFor(moveDir, {{ face_dir }}, info, damage);", 1),
    ("CombatStrategy.getCellScoreFor(moveDir, facing, info, CombatStrategy.getBiteDamage());",
     "CombatStrategy.getCellScoreFor(moveDir, facing, info, damage);", 1),
    ("            bestDamage = CombatStrategy.getBiteDamage();\n", "            bestDamage = damage;\n", 2),
])
sub(C + "moves/TryRatNap.java.jinja2", [
    ("if (info.getHealth() <= GameConstants.RAT_BITE_DAMAGE) continue; // just kill",
     f"if (info.getHealth() <= ({P}.Tune.CB_MAXDMG == 0 ? GameConstants.RAT_BITE_DAMAGE : CombatStrategy.biteDamage(info))) continue; // just kill", 1),
])

# ---- CC A3
sub(C + "moves/TryFight.java.jinja2", [
    ("    static int curCatDist;\n", """    static int curCatDist;
    static int carrierDanger;

    private static final int[] CARRIER_DANGER = {
        {% for cx in range(-2, 3) %}
        {% for cy in range(-2, 3) %}
        {% set ns = namespace(mask=0) %}
        {% for d in allDirections %}
        {% if (dir_dx[d] - cx)|abs <= 1 and (dir_dy[d] - cy)|abs <= 1 %}
        {% set ns.mask = ns.mask + 2 ** loop.index0 %}
        {% endif %}
        {% endfor %}
        {{ ns.mask }},
        {% endfor %}
        {% endfor %}
    };

    private static void markCarrierDanger() {
        carrierDanger = 0;
        for (int i = RobotTracker.numEnemies; --i >= 0;) {
            RobotInfo e = RobotTracker.sortedEnemies[i];
            if (e.carryingRobot == null) continue;
            Direction f = e.direction;
            markThrowLine(e.location, f);
            markThrowLine(e.location, f.rotateLeft());
            markThrowLine(e.location, f.rotateRight());
        }
    }

    private static void markThrowLine(MapLocation from, Direction d) {
        int cx = from.x + 2 * d.dx - myLocX;
        int cy = from.y + 2 * d.dy - myLocY;
        if (cx < -2 || cx > 2 || cy < -2 || cy > 2) return;
        carrierDanger |= CARRIER_DANGER[(cx + 2) * 5 + cy + 2];
    }
""", 1),
    ("        bytecodeBudget = Clock.getBytecodesLeft() - 1500;\n        selectMoves();",
     f"        bytecodeBudget = Clock.getBytecodesLeft() - 1500;\n        carrierDanger = 0;\n        if ({P}.Tune.CC_STEP2 != 0) markCarrierDanger();\n        selectMoves();", 1),
    (f"            score += {P}.Tune.FIGHT_TURN;\n        }}\n",
     f"            score += {P}.Tune.FIGHT_TURN;\n        }}\n        if ((carrierDanger & (1 << moveDir.ordinal())) != 0) score -= {P}.Tune.CC_STEP2;\n", 1),
])

# ---- CX A1: threatMask copies of hasThreat, and the graded cell score
f = root / (C + "CombatTracker.java.jinja2")
s = f.read_text()
add = []
for sig in ("public static boolean hasThreat(Direction moveDir, Direction facing) {",
            "public static boolean hasThreat(Direction moveDir, Direction facing, RobotInfo target) {",
            "public static boolean hasThreat(Direction moveDir, Direction facing, RobotInfo target, int damage) {"):
    i = s.index(sig)
    j = s.index("\n    }\n", i) + len("\n    }\n")
    body = s[i:j]
    body = body.replace("public static boolean hasThreat(", "public static long threatMask(")
    body = body.replace(" != 0L; }", "; }").replace("return base != 0L;", "return base;").replace("return false;", "return 0L;")
    add.append("    " + body)
anchor = "    public static boolean hasThreat(Direction moveDir, Direction facing) {"
s = s.replace(anchor, "\n".join(add) + "\n" + anchor, 1)
f.write_text(s)
for args, call in (("Direction moveDir, Direction facing, RobotInfo target, int damage", "moveDir, facing, target, damage"),
                   ("Direction moveDir, Direction facing, RobotInfo target", "moveDir, facing, target"),
                   ("Direction moveDir, Direction facing", "moveDir, facing")):
    sub(C + "CombatStrategy.java.jinja2", [(
        f"    public static double getCellScoreFor({args}) throws GameActionException {{\n"
        f"        if (CombatTracker.hasThreat({call})) return -{P}.Tune.THREAT_PENALTY;\n",
        f"    public static double getCellScoreFor({args}) throws GameActionException {{\n"
        f"        if ({P}.Tune.CX_PAIR != 0) {{\n"
        f"            long t = CombatTracker.threatMask({call});\n"
        f"            return t == 0L ? 0 : -{P}.Tune.THREAT_PENALTY - {P}.Tune.CX_PAIR * (((t * 0x0101010101010101L) >>> 56) - 1);\n"
        f"        }}\n"
        f"        if (CombatTracker.hasThreat({call})) return -{P}.Tune.THREAT_PENALTY;\n", 1)])
