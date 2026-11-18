# Archived postmortem: Louis-max (2026)

| Field | Value |
|---|---|
| Title | Battlecode 2026 (blog series, English version) |
| Team | Louis-max (team name not stated) |
| Year | 2026 |
| Game | Uneasy Alliances (2026) |
| Placement | > 150th in 2026 (13th in 2025), per the author |
| Authors | Louis-Max Harter |
| Source | https://louis-max.gitlab.io/blog/en/battlecode/ |
| Retrieved | 2026-11-18 |
| Format | Several pages/files, concatenated |
| Original file | none (web source) |
| Listed on battlecode.org/past.html | no (found by search) |
| Note | Nine-part blog series concatenated in order (index, 01-08, 98 annex). Bot source: https://github.com/Louis-max-H/bc26 |

> **Archived copy of the authors' work.** Reproduced here unchanged in substance so this repo's agents can grep it; all rights remain with the authors (see Source). The text was machine-converted and lightly cleaned: page headers/footers, page numbers, broken hyphenation and tables of contents removed; figures replaced by `[figure omitted: <caption>]`. It is not a summary. Index and takeaways: `references/postmortems/INDEX.md`; cross-year lessons: `docs/lessons.md`.

---

# Battlecode 2026
Small disclaimer, I've never been good at ranking. (13th 2025, > 150 2026)
It's more the algorithm side, bytecode constraints and code organization that I really enjoyed 😃 !

You can find below some attempts that, I hope, will be useful to you.
Note that the best teams are those that evolve quickly, take the time to test against other players, make several strategies rather than large ones that are difficult to adjust.

I therefore recommend not taking all these tips if this is your first participation or if Battlecode has already started.
It's also part of the game to discover your limits and organize yourself ;)

However, if you're a team of 4 motivated people and you start to appropriate this architecture a few weeks before battlecode, I'd be curious to know the result obtained!
Don't hesitate to ping me if it works, @louis-max on the Battlecode discord :D

Link to our Git repo [here](<https://github.com/Louis-max-H/bc26>)

  * [01 Finite state machine](<https://louis-max.gitlab.io/blog/en/battlecode/01finitstatemachine/>) [FR] [EN] How to organise your code
  * [02 Moving](<https://louis-max.gitlab.io/blog/en/battlecode/02moving/>) [FR] [EN] How to move like a pro
  * [03 BugNav3](<https://louis-max.gitlab.io/blog/en/battlecode/03pathfinding/>) [FR] [EN] Can we unroll bugnav?
  * [04 Communication](<https://louis-max.gitlab.io/blog/en/battlecode/04communication/>) [FR] [EN] How to send mixed signals
  * [05 Micro and Jinja](<https://louis-max.gitlab.io/blog/en/battlecode/05micro/>) [FR] [EN] And how to generate bunch of pattern
  * [06 Scoring](<https://louis-max.gitlab.io/blog/en/battlecode/06scores/>) [FR] [EN] Scores, scores, scores everywhere
  * [07 Bytecode](<https://louis-max.gitlab.io/blog/en/battlecode/07bytecode/>) [FR] [EN] Some benchmarks
  * [08 Any more advices ?](<https://louis-max.gitlab.io/blog/en/battlecode/08divers/>) [FR] [EN] All the stuff that can't fit elsewhere
  * [98 Annex](<https://louis-max.gitlab.io/blog/en/battlecode/98annex/>) [FR] [EN] Robot.java (the state machine under the hood)
  * [99 For schools](<https://louis-max.gitlab.io/blog/en/battlecode/99schools/>) [FR] [EN] Why get your students involved in Battlecode

---

# 01 Finite state machine
**Created on 2026-01-28**

# Finite State Machine
## Introduction
You've probably already heard about automata or finite state machines. Here are some reasons to use them for your Battlecode!

  * Because it allows decoupling actions and their interactions with each other
  * Because you can have priorities in actions that evolve according to the situation
  * Because an action may need to unfold over several turns without being interrupted
  * From experience, this makes coordination easier, with everyone working in a `States/MyAction.java` file rather than a large `Main.java`

## Alright, tell me more!
In the `src/` folder:

  * `src/`
    * `States/` : Your actions (the nodes of your state machine)
    * `Robots/` : How to order your actions (describes the transitions of your state machine)
    * `Utils/` : Some functions that can help you (`PathFinding`, `DataStructures`, `Tools`)
    * `Templates/` : Jinja files to generate other files
    * `RobotPlayer.java` : The file that will call the right class in `Robots/`

## How do I create my state?
Now that we understand the project structure, let's create a new state :D
Start by copying the `HelloWorld` state in `States/`

    package current.States;
    import static current.States.Code.*;

    public class HelloWorld extends State {
        public HelloWorld(){

            // Initialization
            // Only once, when the state is instantiated

            this.name = "HelloWorld";
        }

        @Override
        public Result run(){

            // My action

            print("Hello World!");
            return new Result(OK, "My result message ?");
        };
    }

Wow... Is it really that simple?
But... What is `Result(OK, "msg")`?
To facilitate debugging, each state can return a string providing more information about what's happening, but also a result code. This way, you can have a nice and well-formatted log!

    [A: #10963@104] 	Init	80
    [A: #10963@104] 	AvoidCat	5478
    [A: #10963@104] 		<= OK No cat to avoid
    [A: #10963@104]
    [A: #10963@104] 	AttackEnemy	5577
    [A: #10963@104] 		<= ERR Can't move to best dir ! Maybe another unit ?
    [A: #10963@104]
    [A: #10963@104] 	ThrowToWalls	7811
    [A: #10963@104] 	BecomeKing	7882
    [A: #10963@104] 		<= OK No call for king nearby
    [A: #10963@104]
    [A: #10963@104] 	AttackCat	7993
    [A: #10963@104] 		<= OK No cat
    [A: #10963@104]
    [A: #10963@104] 	CheeseToKing	8092
    [A: #10963@104] 	PlaceTrap	8164
    [A: #10963@104] 		<= OK No trap placement needed
    [A: #10963@104]
    [A: #10963@104] 	CollectCheese	8281
    [A: #10963@104] 		<= END_OF_TURN Moving to cheese at [25, 36]
    [A: #10963@104]
    [A: #10963@104] 		Skipping to end of turn
    [A: #10963@104] 	EndTurn	10563
    [A: #12719@104] 		<= OK Ending turn gracefully at cell [5, 53]

Nice and well-formatted, right? But wait for the fun part! The return code has an impact on the next states.

Non-blocking return codes: They only differ during display

  * `Ok` : Everything is fine!
  * `Cant` : the task cannot be played (cooldown for example)
  * `Warn` / `Err` : Identical to OK, just for the log

Blocking codes:

  * `Lock` : Go to End the turn, go to init, then restart from the current state directly
  * `End of turn` : Go to End the turn, the next turn will restart at init
  * We could also imagine `GOTO` and use the returned string as the choice of the next state ;)

## Why LOCK and END_OF_TURN?
Let's use them in two examples, the first one when attacking an enemy. We want to stay in combat mode until there are no more enemies, so as not to be distracted by the appearance of cheese.

    public class Attack extends State {
        MapLocation target;
        public Attack(){
            this.name = "Attack";
        }

        @Override
        public Result run(){
            // Should I play this action?
            if(health == 1){
                return new Result(Warn, "I'm very weak guys, stop attacking");
            }

            // Update the action if it already exists
            if(target not null and not visible anymore){
                target == null;
            }

            // Update the action for new targets
            if(target == null){
                target = nearestEnemies something like that;
            }

            // If no target, return
            if(target == null){
                return new Result(CANT, "No enemies");
            }

            // Play the action
            attack(target);
            return new Result(LOCK, "Attacking " + target);
        };
    }

Did you notice that:

  * We use `LOCK` to make sure we stay in the same state
  * We stay on the same target between different calls of the state (useful for example, painting an SRP (BC2025), collecting a resource (AIC2025), or responding to a help call BC2026)
  * We program by negation, which avoids having lots of nested if-else, see the excellent video [ Why You Shouldn't Nest Your Code](<https://www.youtube.com/watch?v=CFRhGnuXG-4>)
  * We can easily know what's happening in the Logs 😊

Now, second example with `END_OF_TURN` for collecting resources

    public class CollectCheese extends State {
        MapLocation nearestCheeseMine;
        public CollectCheese(){
            this.name = "CollectCheese";
        }

        @Override
        public Result run(){
            // Should I play this action?
            if(already have cheese){
                return new Result(OK, "Already have cheese");
            }

            // If no target, return
            if(nearestCheeseMine == null){
                return new Result(CANT, "No cheese mine");
            }

            // Already too many rats
            if(nearestCheeseMine depleeted){
                Mark(nearestCheeseMine, depleted);
                return new Result(WARN, "Cheese mine depleted")
            }

            // Play the action
            if(fromage nearby){
                goCollectCheese();
                return new Result(END_OF_TURN, "Going for cheese ! ");
            }

            stayNearby(nearestCheeseMine);
            return new Result(END_OF_TURN, "Wait for cheese at " + nearestCheeseMine);
        };
    }

Here we have `END_OF_TURN`, because we don't want to do anything else but wait for cheese. With this state machine, we would otherwise explore and might lose sight of the cheese mine :'(

    Init -> Attack -> CollectCheese -> Explore -> End of turn
      ^                                           |
       \_________________________________________/

Since `END_OF_TURN` is less restrictive than `LOCK`, the finite state machine will resume at `Init` and can switch to `Attack` mode if you find an enemy on your way to cheese.

## Alright, perfect, how do I define such transitions between states?
Hop there, go to `Robots/King.java`:

    package current.Robots;
    import current.RobotPlayer;
    import current.States.*;

    public class King extends Robot {
        State attack;
        State CollectCheese;
        State explore;

        @Override
        public void init(){
            this.init = new Init();
            this.attack = new Attack();
            this.collectCheese = new CollectCheese();
            this.endTurn = new EndTurn();
        }

        @Override
        public void updateState(Result resultBefore){
            currentState = switch (currentState.name) {
                case "Init" -> attack;
                case "Attack" -> collectCheese;
                case "CollectCheese" -> explore;
                case "Explore" -> endTurn;
                case "EndTurn" -> init;
                default -> {
                    Robot.err(currentState.name + " does not match any state. Returning to init");
                    yield init;
                }
            };
        }
    }

The `updateState` function takes the result of the last `currentState.run()`, but you might not need it.
If you have blocking codes (`LOCK`, `END_OF_TURN`), they will be directly handled in `Robots/Robot.java`, the code is in the annex if you want to check it ;) And there you go!

But be careful, it's very tempting to unnecessarily complicate `updateState`!

Don't forget that each state starts by ensuring itself that it can be played. So don't add `if(enemy) currentState = attack;`, that would be redundant with the checks done by the state itself.
In general, you want to start with the most important states so they can return `END_OF_TURN` and thus, not play the other states.

Some branching examples:

  * I place the `Attack` state first before the resource deposit one, because it's more important
  * If I have more than X resources, I go directly to resource deposit without going through attack
  * From a certain round, I switch to a branch that only contains the `Rush` state, rather than adding a condition in each state `if(round<=RoundRush)`
  * If I specialize (become king or switch to explorer mode), I stay in a branch that only contains certain states.

Here's what our automaton looked like for BattleCode 2026: [figure omitted: State machine proposal]

## One last word?
So, does the `AvoidCat` state return `END_OF_TURN`, `LOCK`, `WARN` or `OK`?
Answer in the next part on movement!

---

# 02 Moving
**Created on 2026-01-28**

# Moving
## Scores for some finesse
This year, movement was all the more complex as we had to take into account unit orientation in addition to their more classic movements.
Let's start with movements, we chose for the `AvoidCat.java` state to return `OK` (or `WARN`), rather than blocking the following states using `END_OF_TURN`.

To do this, we had in our `Pathfinding.java` utility, an array that contained a score (`long`) for each direction.
Thus, the `AvoidCat.java` state didn't block our automaton, but simply added a (very) dissuasive score to go in certain directions in case of danger.

It's notably possible to add scores:

  * Manually: `PathFinding.addScoresWithoutNormalization(int[9] scores)`
  * Normalized: `PathFinding.addScoresWithNormalization(int[9] scores, long coef)`

Normalization for a score `x` corresponds to `x * coef * 1000_000_000 / (max(scores) - min(scores))`, this allows "smoothing the results".
For example, if we have a cat, we can add a penalty of `20 - distance(me, cat)`, 20 being the vision distance of rats. Let's say coefficient `30`.

But what if we have `700` cheeses guarded by a cat? Our rat will want to try to go there, (700 > 20*30). However, if it has that much cheese, it's because previous rats probably didn't manage to retrieve them.
You can therefore normalize this 700, to have a value that will be independent of the number of cheeses but rather of their distribution.

Vous obtenez ainsi :

    int[] scores = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
    MapLocation myLoc = rc.getLocation();
    for(MapInfo info : rc.senseNearbyMapInfos()){
        scores[myLoc.directionTo(info.getLocation())] += info.getCheeseAmount();
    }

    // We add the score with normalization.
    PathFinding.addScoresWithNormalization(scores, 1);

    // Take the best direction
    Direction bestDir = PathFinding.bestDir();
    if(PathFinding.move(bestDir).notOk()){
        print("Can't move to best direction.");
    }

Pretty nice, right? Thus, if you have 2 or 3 heuristics present in 2 or 3 different states, they will be able to accumulate to refine your movement choices.
Be careful however not to put scores for everything and anything, and to keep them simple to understand, otherwise it becomes difficult to debug.

You can add `printScore("Adding XXX")` to debug each time you add heuristics:

    [A: #13547@6] 			Adding XXX:
    [A: #13547@6] 			              1600000000
    [A: #13547@6] 			1350000000  ↖️⬆️↗️  1350000000
    [A: #13547@6] 			 850000000  ⬅️⏹️➡️   850000000
    [A: #13547@6] 			 600000000  ↙️⬇️↘️   600000000
    [A: #13547@6] 			              102500000     CENTER : 0

I strongly recommend having a `Pathfinding.move(direction)` function that notably allows:

  * Checking that everything is fine before actually performing the movement.
  * Returning a clear message `new Result(OK, "Moved to " + dir)` or `new Result(CANT, "Because of XXX")`
  * Updating certain variables (`myLoc`, `previousLoc`, etc...)
  * And why not, moving randomly every 10 turns to test the robustness of your algorithm?

## What if I want to move to a specific place?
### Smart movement
If you want to move in a direction in a smart way, you can do:

    PathFinding.modificatorOrientation(
        PathFinding.directionTo(target), 1
    );
    Pathfinding.moveBestDir();

    // Or directly
    Pathfinding.smartMoveTo(target);

The orientation modifier function takes care of drastically favoring the scores of cells that are in the desired direction, but still allows taking into account all directions.

    public static void modificatorOrientationSoft(Direction dir, long coef){
        // Boost score toward direction
        scores[dir.rotateLeft().ordinal()] += 250_000_000 * coef;
        scores[dir.ordinal()] += 300_000_000 * coef;
        scores[dir.rotateRight().ordinal()] += 250_000_000 * coef;

        Direction opposite = dir.opposite();
        scores[opposite.rotateLeft().rotateLeft().ordinal()] += 150_000_000 * coef;
        scores[opposite.rotateLeft().ordinal()] += 100_000_000 * coef;
        scores[opposite.ordinal()] += 500_000 * coef;
        scores[opposite.rotateRight().ordinal()] += 100_000_000 * coef;
        scores[opposite.rotateRight().rotateRight().ordinal()] += 150_000_000 * coef;
    }

We also had another variant that divided scores that weren't in the right direction, but I don't remember why we went back to adding scores.

## What about vision?
This year, it was particularly interesting to save the terrain given that our rat could only see in front of it and needed obstacles to throw enemies against them.

[figure omitted: Champ de vision d'un roi][figure omitted: Champ de vision d'un rat][figure omitted: Champ de vision d'un chat]

We therefore had an array that contained the cells (wall, dirt or empty) as well as another array concerning a score gained if the unit looks at this cell.

  * The value for a completely unknown cell was `2000_000`
  * If the cell was seen at turn `t`, we gave it a score of `score(t) = 2000 - t * 100`
  * Thus, if the cell was seen at turn `t=0` its score will be much higher than a cell that was seen at turn `t=2000`.
  * We use an array of `(8 + 60 + 8) * (8 + 60 + 8)` to be able to use Jinja and unroll loops without worrying about side effects.

This allowed for example:

  * Adding a large score next to enemies to look at them regularly
  * Exploring rather efficiently by heading towards cells that haven't been seen (works well individually, less well collectively)
  * If we need to look at a cell to perform an action, choosing between the 3 directions that allow covering this cell while maximizing other heuristics for the direction to look in

## Which pathfinding to use?
I would say without much hesitation, the simplest one from teams that participated in the finals last year?
Don't hesitate to copy, modify and thank them <3

Since I didn't follow this advice (and therefore our pathfinding was a bit buggy), I'll detail all this in Bugnav3!

---

# 03 BugNav3
**Created on 2026-01-28**

# PathFinding
Link to the repo : <https://github.com/Louis-max-H/Bugnav3> (Will move it to gitlab soon)

## Introduction
Here are some resources on the bugnav algorithm:

  * Presentation from [**BattleCode 2026**](<https://www.youtube.com/watch?v=Hkk33bEfrzg>)
  * Comparison of bug1 and bug2 [**in video**](<https://www.youtube.com/watch?v=T2PVaKyxMmY>)
  * Small [**slide on Bugnav**](<https://www.cs.cmu.edu/~motionplanning/lecture/Chap2-Bug-Alg_howie.pdf>)

Quick recap, each time, we head towards the destination, and in case of obstacle:

  * **Bugnav0 (tangential)** : We turn in the direction that brings us closer to the destination (Simple, no guarantee)
  * **Bugnav1** : We go around the obstacle and resume from the point closest to the destination (Slow)
  * **Bugnav2** : We go around the obstacle until we find ourselves on the line connecting start point and destination (More complicated, with guarantee)
  * **Bugnav3** : We split in two and test both sides (Difficult, oscillates, but new!)

[figure omitted: Camparaison de bugnav0123, en pixel art simple et efficace]

In a slightly more detailed way, the algorithm I propose includes 3 phases:

  * **Normal**

    * We move in the direction of the destination
    * When we encounter an obstacle, we switch to "Split" mode
  * **Split** : When we encounter a wall

    * We initialize a path that will follow the obstacle keeping it to its left and another keeping it to its right
    * We advance one then the other
    * When one of the two can head directly towards the destination, we return to normal mode and note the coordinate as `lastWallLeave`
  * **Reverse** : Once the destination is reached

    * We start from `lastWallLeave` to return towards the start (allows smoothing the trajectory).
    * We apply the same procedure, this time noting the movements performed.

[figure omitted: Le chemin retour apparait en violet]

This algorithm having somewhat unstable behavior in some cases, I recommend copying other algorithms.
However, if you want to learn more about certain calculation techniques or the difficulties I encountered, you can take a look :D

Ps: Here's an overview of the performance obtained:

    [A: #12097@399] ===Pathfinding report : REVERSE ===
    [A: #12097@399] Iterations normal : 11
    [A: #12097@399] Iterations split  : 18
    [A: #12097@399] Bytecode used     : 1534
    [B: #10724@399] ===Pathfinding report : Normal===
    [B: #10724@399] Iterations normal : 11
    [B: #10724@399] Iterations split  : 5
    [B: #10724@399] Iterations return : 2
    [B: #10724@399] Bytecode used     : 737

That's ~2200 bytecodes for a prediction at ~20 distance!

## Chars, my favorite friends
### Saving the terrain
To keep the already explored terrain in memory, we use a `char[] mapCost = "\u0000\u0000...\u0000".toCharArray();` Which has the advantage of costing $2$ bytecodes, regardless of the string size! But also the disadvantage of only being able to store chars (65536 values)

For jinja, I invite you to have an `encodeXY(x, y)` function that allows encoding your coordinates. For example, `x + 60*y`.
We also made it possible to pass strings to this Jinja function, which is necessary when we don't know the values of x or y in advance.

For example: `encodeXY("loc.x", "loc.y")` will give `loc.x + 60*loc.y` You may also need the inverse function, and a function to give you the size of your array to initialize (here 60*60=3600).

### Doing without MapLocation
Often, using standard libraries is much faster than recoding them, (for example `min` only takes 4 bytecode, while comparison + assignment takes 5 with a ternary)
But here, we are forced to have the terrain in memory, and therefore to have an `xy` index.
So, since we have an xy index anyway, maybe we can do without MapLocation?

The first problem (rather simple) consists of recoding `rc.isOnMap(MapLocation)`, which is done rather well by taking a 62x62 terrain and a `BORDER` value on the edges.
This causes a small performance loss, because our encode function gains an addition and becomes `(x+1) + (y+1)*62 = x + y*62 + 63`.\

Note that once our position is encoded, the offsets to go to the cell to the right and left are still done with `+1/-1`, but up and down, we now have `+62/-62`, don't hesitate to make a small Jinja function to get the offsets ;)

### Recoding DirectionTo
The second challenge is a bit more interesting!
First, we will now encode our position using `x + (y << 7)`. What happens if we try to calculate a vector between two cells, for example `encode(8, 4) - encode(2, 1)`? Or `encode(2, 1) - encode(8, 4)`?

                     0000YYYYYYY XXXXXXX
    encode(8, 4) = 0b00000001000_0000100 (a)
    encode(2, 1) = 0b00000000010_0000001 (b)
    (a) - (b)    = 0b00000000110_0000011  = encode(6, 3)
    (b) - (a)    = Ob11111111011_1111101  = encode(123, 125)

Hmm, in our second case, the negative delta in x gives a carry that impacts the result in y.
We could with the 7bit, know that there was an overflow from the X part to the Y part, but now, the result uses 16 bits. However... The limit for a static string is around $2^{14}$ characters.

We could add a mask to keep only the first 14 bits after calculations, but we will prefer a second solution which consists of setting to 1 the most significant bit of each of the X and Y parts.
Thus, in case of negative result, it will be this bit that will be set to zero and this will stop the carry propagation.

                        YYYYYYY XXXXXXX
    encode(8, 4)    = 0b0001000_0000100 (a)
    encode(2, 1)    = 0b0000010_0000001 (b)
    masque          = 0b1000000_1000000 (m)
    (m) + (a) - (b) = 0b1000110_1000011  = encode(64 + 6, 64 + 3)
    (m) + (b) - (a) = Ob0111010_0111101  = encode(64 - 6, 64 - 3)

Much better, right?!
Since the difference of two cells contained in $[0,59]^2$ is contained in $[-59, 59]^2$, we won't have other overflow problems.
We then only need to generate a large array with all directions for all types of obtainable vectors.

Since this operation is rather long (because we also check for collisions), I added a line at the top of the template to avoid Jinja recalculating it each time. We then just need to do `directionToArray[mask + locEnd - locStart]`. Since often, the destination doesn't change, we can group `mask + locEnd` into `locEndMask` and get `directionToArray[locEndMask - locStart]`

### Doing without rc.canSenseLocation
Rather than checking at each movement if the cell can be read, we will prefer updating the scores of all cells in the unit's vision field, and then calling our pathfinding function.

You can choose to represent passable cells by 0, walls by 1 and borders by 2. Checking if a cell can be traversed therefore comes down to comparing with 0 (`> 0`), thus gaining an operation.

Otherwise, in the case of walls or enemies, you can give a score to certain cells, and decide not to pass if it exceeds a certain value `MAX_VALUE`
We thus had a function in Jinja:

    def generateEmptyMapCosts7B(passableValue, mod2Value, borderValue, invalidValue):
        costs = [invalidValue for _ in range(encodeCellPathfinding(61, 61))]
        for x in range(-1, 62):
            for y in range(-1, 62):
                # Valid cells are between 1 and 60
                if x == -1 or x == 61 or y == -1 or y == 61:
                    costs[encodeCellPathfinding(x, y)] = borderValue
                elif x % 2 == 0 and y % 2 == 0:
                    costs[encodeCellPathfinding(x, y)] = mod2Value
                else:
                    costs[encodeCellPathfinding(x, y)] = passableValue

        return "New StringBuilder(" "\"" + "".join([intToChar(cost) for cost in costs]) + ")"

Setting this `MAX_VALUE` to:

  * `passableValue`: The robot will not move outside known zones
  * `mod2Value`: The path will perform zigzags
  * `passableValue`: The path will go straight

You can also make a custom score near enemies or other ;)

When an obstacle is encountered (`>= MAX_VALUE`), we simulate a route keeping the wall to its left and another keeping it to its right.
At each iteration, we choose to advance the one with the lowest score, which allows having a kind of small A*.

## Some edge cases (and possibly, their solutions)
### Smoothing the return path
Making the return path from the closest point obtained allows smoothing the result and obtaining a more direct path. But this also has the disadvantage of doubling the amount of necessary operations.

[figure omitted: Le chemin retour apparait en violet]

### Oscillation
The weak point (but also its strong point) is making a prediction over a long distance, but often, this also has the effect of making our robot oscillate between two very close choices.

For example, if we encounter a wall wider than our exploration capacity, bugnav will advise us to go up or down, before returning to the previous position which is finally, the closest to the destination.

[figure omitted: Example d'oscillation]

I haven't found a solution to this problem. Perhaps when an oscillation is detected, we can return to a classic bugnav?

### Small asperities
In the most basic version of the algorithm, as soon as a branch of split mode goes around the obstacle (so can head freely towards its destination), we return to normal mode.
This has the disadvantage of sometimes leaving split mode a bit too quickly when one or two additional iterations would have allowed us to realize that we hadn't completely gone around the obstacle.

For example, here `DEFAULT_SMOOTH = 0` or `DEFAULT_SMOOTH = 3`

[figure omitted: smooth=0][figure omitted: smooth=3]

And here `DEFAULT_SMOOTH = 3`, we will therefore need to be able to move freely 3 times in the direction of the destination to leave split mode.

### Going back on our steps?
Sometimes, especially after following the wall for a good while, the algorithm was tempted to go back on its steps in split mode.
I therefore added a penalty `MALUS_ALREADY_PASSED = SCORE_CELL_PASSABLE` that increases the score of one side faster if it goes back over an already explored cell.

At each new iteration, we take the branch that has the lowest score, a score that goes back on its steps will therefore be advanced half as often. Similarly, it would also be possible to add a small heuristic to advance one side faster if it is closer to the goal.

[figure omitted: Don't fall back in the rabbit hole]

### When to stop following an obstacle?
A first step consists of stopping following an obstacle when we encounter the edge of the terrain. A second consists of stopping following an obstacle when we are sure we have turned enough not to go back on our steps. I'll let you formalize it yourself, this small illustration will summarize the situation! (We must not describe 360-degree turns and fall back indefinitely into the pot)

[figure omitted: That's will be a long way :?]

### Quand s'arrêter de calculer un itinéraire ?
Tout d'abord, la limite de bytecode !
Quand nous calculons le chemin du retour, nous partons du point de décrochage du dernier obstacle rencontré.
Si jamais il n'y a pas eu d'obstacle, le retour est donc immédiat.

Mais dans certains cas, il peut aussi être beaucoup plus long...

Nous accordons donc 1/3 des bytecodes autorisé pour l'aller et 2/3 pour le retour. Peux être que la limite est à affiner ;)

Et que faire si la destination n'est jamais atteignable ?
Hop, encore un paramètre, `MAX_TIME_SAME_PATH=10`, le nombre de fois que l'on peut être sur un chemin déjà exploré en mode split. C'est notamment utile quand la branche gauche et droite tourne autour de l'obstacle.

### Testing method
To debug the program, I made a python script that takes the logs to generate a map and displays the results obtained.
This allows having a clear output and even a small illustration to help us ;)

Don't hesitate to:

  * Add clear prints, which you can disable in non-debug mode with Jinja
  * Make a micro `{{impossible("Error during X in part X of the algo")}}` that you can slip into the `default` of your switch cases, it will raise an exception if encountered.
  * Use indices that will raise an outOfBound if encountered, for example, a non-existent index 9 rather than an index 0 which corresponds to center which can also have its meaning.
  * Use different return codes. Rather than having a single return value `-1` for all your errors, you can make one per case and check if your algo returns a positive or negative error code.

## Can we do even better?
You can surely make a "flood fill" method with binary operators. For example:

    // Moving on x abscisse
    LINE_X_2_M2 = (LINE_X_2_M1 | (LINE_X_2_M1 << 1) | (LINE_X_2_M1 >> 1) ) & MASK_OBSTACLE;

    // Moving on y abscisse
    LINE_X_2_M3 = (LINE_X_1_M2 | LINE_X_2_M2 | LINE_X_3_M1) & MASK_OBSTACLE;

Which we can simplify, but I'm not sure, into:

    // Force moving on x abscissa
    LINE_X_2_M2 = (LINE_X_2_M1 * 5 / 2) & MASK_OBSTACLE; //

    // Force moving on y abscissa
    LINE_X_2_M3 = (LINE_X_1_M2 | LINE_X_3_M1) & MASK_OBSTACLE;

That is, 8 bytecodes to move in x on a line, 6 to move in y.
The number of lines to update after $n$ movements is $2(n+1)+1$. For example:

    x=10        *
    x=9        **
    x=8       ***
    x=7      ****
    x=6     *****
    x=5 ->  *****
    x=4     *****
    x=3      ****
    x=2       ***
    x=1        **
    x=0         *
    Iteration:

Calculating $n$ movements on the entire map would therefore take $(8+6)*\sum\limits{n}_{i=0}2(n+1) + 1 = (18+6)(2n + n) + 2n(n+1)/2$ So for 20 movements, we would get a lower bound of the cost of... 1860 bytecodes.

Let's keep in touch if you implement it? ;)

---

# 04 Communication
**Created on 2026-01-28**

# Communication
## Message structure
The important thing here is above all to be clear with yourself.

    //                                            30v  24v      16v       8v      0v
    // Messages types (long)                      0b000000_111111112222_222233333333; // 30 bits = 1 per squeak, 3 array size
    public static final int MASK_TYPE         =   0b011111_000000000000_000000000000;
    public static final int TYPE_CAT          =   0b000001_000000000000_000000000000;
    public static final int TYPE_KING         =   0b000010_000000000000_000000000000;
    public static final int TYPE_ENEMY_KING   =   0b000011_000000000000_000000000000;
    public static final int TYPE_MINE         =   0b000100_000000000000_000000000000;
    public static final int TYPE_ENEMY_RAT    =   0b000101_000000000000_000000000000;
    public static final int TYPE_ALLY_RAT     =   0b000110_000000000000_000000000000;
    public static final int TYPE_MICRO        =   0b000111_000000000000_000000000000;
    public static final int TYPE_MAKE_KING    =   0b001000_000000000000_000000000000;
    public static final int TYPE_DEFEND       =   0b001001_000000000000_000000000000; // x, y, id
    public static final int MASK_POSITION     =   0b000000_000000000000_111111111111;
    //                                                    _            |yyyyyyxxxxxx; // Bits 0-11 : 12 bits = log2(64*64)
    public static final int MASK_UNIT_ID      =   0b000000_111111111111_000000000000; // 12 bits 4096 values
    public static final int MASK_14BITS       =   0b000000_000000000011_111111111111; // Used to hash a message content

    // We can also encode x/y with relative position
    // With distance max of 8 (16 = -7 / +8) we need 4 bytes
    //
    //                                          32v     24v      16v      8v       0v
    // Messages types (long)                    0b000_00000_1111111122222222_33333333; // 30 bits = 1 per squeak, 3 array size
    //                                           |dir| type| target id  |dir|yyyyxxxx; // Bits 0-7 : 8 bits = log2(16*16)
    public static final int RELATIVE_POSITION = 0b000_00000_000000000000_000_11111111;
    public static final int MASK_DIR_TARGET =   0b000_00000_000000000000_111_00000000;
    public static final int MASK_TARGET_ID  =   0b000_00000_111111111111_000_00000000;
    //     static final int MASK_TYPE         = 0b000_11111_000000000000_000_00000000;
    public static final int MASK_DIR_SENDER =   0b111_00000_000000000000_000_00000000;

    public static final int SHIFT_DIR_TARGET  =  8;
    public static final int SHIFT_TARGET_ID   = 11;
    public static final int SHIFT_DIR_SENDER  = 29;

## Communication during combat
Oops, I spoiled it in the previous part, but during combat, we encoded:

  * The message type (4 bits)
  * Our position and that of the enemy
  * Our orientation and that of the enemy
  * The ID modulo 4096 (2**12) of the enemy and ours

Whoa, but that's a lot, right?
If you've noticed, the position and identifier of the one sending the squeak is already available in the `Message` data structure.
We can therefore send the enemy's position relative to ours!

If we're in combat mode, the enemy must be relatively close, less than 5 cells in a straight line. If we allocate 8 bits, we can encode its relative position in the range $[-8, 7]$, which is largely sufficient.

We could have used the two bits to ask for help or ask to target a particular enemy.
Transmitting the enemy's identifier allowed us to have a data structure with the positions of all rats updated as they moved, very practical!

## But how do we send our messages?
We had a small structure to store messages to send:

    public class MessageLIFO {
        public static IntLIFO buffer2 = new IntLIFO(); // Emergency
        public static IntLIFO buffer1 = new IntLIFO(); // Important
        public static IntLIFO buffer0 = new IntLIFO(); // Common

        public static void add(int msg, int priority){
            switch (priority){
                case 2: buffer2.add(msg); return;
                case 1: buffer1.add(msg); return;
                case 0: buffer0.add(msg); return;
                default:
                    System.out.println("ERR: Message priority should be in [0, 3] 0 is LOW, 3 is CRIT, got " + priority);
            }
        }

        public static int pop(){
            if(buffer2.size > 0){return buffer2.pop();}
            if(buffer1.size > 0){return buffer1.pop();}
            if(buffer0.size > 0){return buffer0.pop();}
            return 0;
        }
    }

We therefore had the possibility to add messages according to 3 priority levels:

  * 3, Critical: Protect the king, create a king, communicate mine position to the king
  * 2, Important: Position of a cat, enemy, mine
  * 1, Common: Not used much

We added level 3 messages as needed. As soon as the message stack was empty, new messages were generated.
To avoid asking too many questions, we used a 14-bit hash for messages, and the message was not sent if it had already been heard recently.

## Message size
We had 32-bit squeaks and 64 cells of 10 bits to communicate.
My first approach used 15-bit messages (2 per squeak) and others of 30 (1 for 3 cells), but this caused too many collisions in the shared array which used a circular list.

Again, don't hesitate to start with simple things ;)

You can also send 2 positions on a 32-bit message rather than making 2 16-bit messages.
For certain messages, you can also use 1 bit that is set to one only for this message, rather than the 4 standard ones to encode the message type.

Maybe a bit overkill, but I unrolled the encoding of positions relative to another position. Same for decoding 😊

---

# 05 Micro and Jinja
**Created on 2026-01-28**

# Micro
## Heuristics by direction
Now that we had all the information about nearby allies and enemies, all we had left was to generate the code for micros. For this, we reuse our 8 directions:

    public static void reset(){
        scoresAttack     = new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        scoresDanger     = new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        attackDirection  = new char[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        isThrowAction = new boolean[]{false, false, false, false, false, false, false, false, false};
    }

That is:

  * With an attack score (how much damage we can do, the max)
  * A danger score (how much we can take by going to the concerned cell, the cumulative)
  * The direction for the best attack from the cell located in this direction,
  * And finally, a boolean to know if the action in question corresponds to throwing a rat.

We then had an aggressiveness coefficient, which gave us a unified score: `score = attack * aggressiveness - danger`, to know when it was relevant to attack.

If the danger was too great, it meant there were many enemies, and therefore it was relevant to place a trap there.
I'll let you experiment with the few subtleties to make Movement + Attack or Attack + movement combos.

## Generation with Jinja
Here is an excerpt from our Jinja code to generate our macro. We use the relative position of the enemy relative to ours to assign scores to each array:

    {% for targetDir in dirsWithoutCenter -%}
    public static void addMicroScore{{targetDir}}(MapLocation myLoc, MapLocation targetLoc, long attackScore){
        long scoreAttack;

        MapLocation enemyRelativeLoc = targetLoc.translate(-myLoc.x, -myLoc.y);
        switch(enemyRelativeLoc.hashCode()){

            {% for cellEnemy in cellsInRadius(34) -%}
            case {{ battleCodehash(cellEnemy) }}: // Cell {{cellEnemy}}

                {# iterate on my neighbors cells -#}
                {% for dir in dirs -%}
                {% set cell = dirsDelta[dir] -%}

                {# ########### Add danger score ########### -#}
                {% set scoreDanger = 0 -%}

                {% if cellInVisionFrom(cellEnemy, targetDir, cell)-%}
                    {% set scoreDanger = scoreDanger + PARAMS_DANGER_IN_ENEMY_VIEW -%}
                {% else -%}
                    {% set scoreDanger = scoreDanger + PARAMS_ATTACK_RATNAT -%}
                {% endif -%}

                {% if chebyshevDistance(cellEnemy, cell) == 1-%}
                    {% set scoreDanger = scoreDanger + PARAMS_DANGER_IN_REACH -%}
                {% endif -%}

                {# ########### Add attack score ########### -#}
                {% set scoreAttack = 0 -%}
                {% set canAttack = false -%}
                {% set canRatnap = false -%}
                {% if chebyshevDistance(cellEnemy, cell) == 1-%}
                    {% set canAttack = true -%}
                {% endif -%}

                {% if not cellInVisionFrom(cellEnemy, targetDir, cell)-%}
                    {% set scoreAttack = scoreAttack + PARAMS_ATTACK_BONUS_CANT_SEE -%}
                    {% set canRatnap = true -%}
                {% endif -%}

                {# ########### Write result ########### -#}
                {% if canAttack -%}
                    if(scoresAttack[{{dirsOrds[dir]}}] < {{scoreAttack}}){ // Max because we can attack only once
                        isThrowAction[{{dirsOrds[dir]}}] = false;
                        scoresAttack[{{dirsOrds[dir]}}] = {{scoreAttack}};
                        attackDirection[{{dirsOrds[dir]}}] = {{dirsOrds[dirsToNearbyCell(cell, cellEnemy)]}};
                    }
                {% endif -%}
                {% if scoreDanger > 0-%}
                    scoresDanger[{{dirsOrds[dir]}}] += {{scoreDanger}}; // {{dir}}
                {% endif -%}

                {% endfor -%}{# Nearby cell -#}
                return;

            {% endfor -%} {# enemy cell -#}
            default:
                System.out.println("Warn: enemy location out of range " + enemyRelativeLoc);
                return;

        }// switch(enemyRelativeLoc.hashCode())
    }
    {% endfor -%} {# targetDir methods generation -#}

You can add a few variations: Enemy health, penalty if it's them who strikes first, but the idea is there!
To throw the enemy, we were able to reuse the obstacle map used by PathFinding.

Note that the `addMicroScore` function was too long to be used in one block, so this script will generate one per possible direction, and then call the right function depending on the case.\

You can get more information about the art of Micro in [this very good presentation](<https://www.youtube.com/watch?v=ORcI2Q6cDg4/>)

---

# 06 Scoring
**Created on 2026-01-28**

# GRASP
GRASP: greedy randomized adaptive search procedure
This term is little known, but often used by our second-year engineering school professor Philippe Lacomme.

## Do you have a good PC?
Or rather 15 PCs from your school's computer lab that you boot on a live iso?
Then Outercloudstudio made a super tool that should interest you! [Nudge](<https://github.com/outercloudstudio/nudge>)

You can for example make a `Params.java` file that allows loading your magic values

    public class Params {
        // Max cheese carried by rats
        public static int AGGRESSIVITY_MAP_SMALL = 45; // [15, 40]
        public static int AGGRESSIVITY_MAP_MEDIUM = 40; // [15, 60]
        public static int AGGRESSIVITY_MAP_LARGE = 30; // [15, 60]
        public static int agressivity;

        public static void init(){
            agressivity = switch (Robot.mapType){
                case 0 -> AGGRESSIVITY_MAP_SMALL;
                case 1 -> AGGRESSIVITY_MAP_MEDIUM;
                case 2 -> AGGRESSIVITY_MAP_LARGE;
                default -> throw new IllegalStateException("Unexpected value: MAP_type " + Robot.mapType);
            };
        }
    }

And then, use a python script to parse the intervals, automatically test the values and send the results with the best configurations found to your discord webhook.

Frankly, it's fire when you connect in the morning with 15 messages sent by the bot with the latest adjusted parameters. Did it allow us to get a good ranking? Not really, but the journey was good!

[figure omitted: Let him cook !]

## Map Small, medium or large?
We have a `testMapSize` bot that is defined as follows:

    public static void run(RobotController rc) throws GameActionException {
        if(rc.getMapWidth() * rc.getMapHeight() > 45*45){
            System.out.println("MAPTYPE: MAP_LARGE");
        }else if(rc.getMapWidth() * rc.getMapHeight() > 32*32){
            System.out.println("MAPTYPE: MAP_MEDIUM");
        }else{
            System.out.println("MAPTYPE: MAP_SMALL");
        }

        rc.resign();
    }

You then just need to run `test_map_size.py` and the bot will be launched on all maps in the `maps/` folder and will return a list of maps sorted by size (small, medium, large, all). This will also allow you to see the maps by loading the replays on the viewer.

## Small critique of automatic optimization
We vibecoded (lack of time required), a python script (`params.py`) to extract these values or load them from a json file, with their intervals. We could then compare json configurations (`compare_configs.py`), or launch tests to optimize parameters (`simple_optimizer.py`)

I would rather recommend using Nudge with well-chosen parameters rather than testing them somewhat randomly.

Also be careful to maintain a good understanding of what's happening. Too many coefficients could give you undesirable behaviors in some cases, or even, no longer understand what's happening. Machine learning against oneself also has a major flaw: Perhaps the parameter makes your bot better against itself on a specific strategy, but this modification will make you lose more matches against opponents with other approaches.

Working on your coefficients will also require time that you cannot devote to other things. For example, a simple strategy for 2026, was to suicide if we no longer had much health and were caught by the enemy. This allows respawning other rats more quickly, and avoids the enemy throwing you against other of your units. But this strategy cannot be found by doing gradient descent on your coefficients ;)

---

# 07 Bytecode
**Created on 2026-01-28**

# Bytecode
Here is a small benchmark of certain functions and their bytecode costs. We notably created a data structure to store a set of locations, and another to make the association between identifier (modulo 4096) and location. This allows updating a unit's position case by case. Code is available [here](<https://github.com/Louis-max-H/bc26/blob/main/src/current/Utils/MapLocationsWithId.java>)

`MapLocations` has similar performance to `FastIterableLocSet` with notably:

  * No need to regenerate the location list each time, but a slightly longer insertion time (+10 bytecodes per operation)
  * Duplicates are handled faster (-25 bytecodes per operation)
  * Longer deletion time (+26 bytecodes per operation)

Here is the comparison! Java benchmarking code is [available here](<https://github.com/Louis-max-H/bc26/blob/main/src/bytecode/RobotPlayer.java>)

    [B: #6@1] Empty benchmark                     :     0
    [B: #6@1] Empty benchmark, saving result      :     0
    [B: #6@1] Empty benchmark, with compare       :     0 (===)

    Reading values:
    [B: #6@1] Affecting local variable            :     2
    [B: #6@1] Affecting static                    :     2 (===)
    [B: #6@1] Affecting private static            :     2 (===)
    [B: #6@1] Affecting int                       :     2 (===)

    Array operations
    [B: #6@1] 20 elements using new               :    22
    [B: #6@1] 20 elements using toCharArray       :     2 (-20)
    [B: #6@1] 20 elements using StringBuilder     :     4 (-18)
    [B: #6@1] Reading item []                     :     4
    [B: #6@1] Reading charAt                      :     3 (- 1)
    [B: #6@1] Writing item []                     :     4
    [B: #6@1] Writing charAt                      :     3 (- 1)

    Loops
    [B: #6@1] Fori 10                             :    75
    [B: #6@1] Fori reversed 10                    :    64 (-11)
    [B: #6@1] Switch case                         :    23 (-52)

    Iterate on array elements
    [B: #6@1] Fori reversed                       :    84
    [B: #6@1] Fori reversed + local array         :    84 (===)
    [B: #6@1] Foreach + local array               :   120 (+36)

    FastIterableLocSet
    [B: #6@1] new FastIterableLocSet(100)         :   117
    [B: #6@1] Add MapLocation                     :    40
    [B: #6@1] Add      x, y                       :    26
    [B: #6@1] Add      x, y already existing      :    15
    [B: #6@1] Contains x, y                       :    15
    [B: #6@1] Remove   x, y                       :    34
    [B: #6@1] Update iterable on 10 elements      :   277 (~27 per elements !)
    [B: #6@1] Iterate 10 items, Fori reversed     :    97
    [B: #6@1] Clear                               :    12

    MapLocations
    [B: #6@1] new MapLocations(100)               :   123 (+ 6)
    [B: #6@1] Add MapLocation                     :    34 (- 6)
    [B: #6@1]
    [B: #6@1] Add      x, y                       :    36 (+10 to FastLocSet, but -17 if you take into account updating the iterable)
    [B: #6@1] Add      x, y already existing      :    15 (-25)
    [B: #6@1] Contains x, y                       :    16 (+ 1)
    [B: #6@1] Remove   x, y                       :    57 (+23)
    [B: #6@1]
    [B: #6@1] Add      xy                         :    33 (+ 7)
    [B: #6@1] Add      xy already exist           :     8 (-18)
    [B: #6@1] Contains xy                         :    11 (- 4)
    [B: #6@1] Remove   xy                         :    50 (+16)

    MapLocationsWithID
    [B: #6@1] new MapLocationsWithID(100)         :   229
    [B: #6@1] Add MapLocation                     :    42
    [B: #6@1] Update location                     :    26
    [B: #6@1] Contains id                         :    13
    [B: #6@1] Get location of id                  :    12
    [B: #6@1] Remove using id                     :     8
    [B: #6@1] Remove using index                  :    43
    [B: #6@1] Clear                               :    10

    Operations:
    [B: #6@1] x << 6                              :     4
    [B: #6@1] x * 65536                           :     4 (===)
    [B: #6@1] x++                                 :     1
    [B: #6@1] ++x                                 :     1 (===)
    [B: #6@1] array[x] = 1; x++                   :     5
    [B: #6@1] array[x++] = 1                      :     5 (===)
    [B: #6@1] int variable = 2;                   :     2
    [B: #6@1] variable = 2;                       :     2 (===)
    [B: #6@1] f(a[x]); f(a[x]);                   :     8
    [B: #6@1] i=a[x]; f(i) f(i)                   :     8 (===)

---

# 08 Any more advices ?
**Created on 2026-01-28**

# Some advice
## Teamwork
Less technical than the others, just think about:

  * Starting on the same bases: If you want to tryhard, how much time does that represent? 2h per day? 12h per day?
  * Think about communicating what you're doing to better coordinate, and say what others can reuse from your code
  * We used a `current/` folder in `src/`, and then branches for each. PRs were done on the same folder, this helped us compare changes.
  * Don't hesitate to copy the `current/` folder into one like `v001_adding_coms` to be able to compare your modifications
  * You can configure a Discord webhook to be informed in case of push on git
  * Think about doing code review <3

## Exploration
A first technique consists of taking a random destination on the terrain and trying to get there, it works well for the first week but we quickly reach its limits.
Here are some suggestions:

  * It seems simple to do, but actually not so trivial, don't hesitate to look at how others do it
  * You can generate an 8 by 8 grid and try to explore all sectors
  * You may need to coordinate exploration between units, for example, if you make it appear at the top right of your king, your unit will take the direction of the top right corner of the terrain. (In our case, we used the initial orientation of the unit to determine which corner to explore)

You may also want to explore parts far from the spawn point more often than those that are close.
As specified in moving, our units had an interest score for looking in a direction, and if one already covered that cell, its score was divided by 2, which allowed uniform distribution on the terrain, but slowed units from all going to mines.

## Playing with constraints
Do you know that moving in an invalid direction because occupied by a unit raises an error message "Already occupied by another unit"?
Which allowed us to have eyes in the back of our head... Provided we sacrifice a few bytecodes ;)

We had chosen to have a fixed king, and another that moved towards mines. But how to get the two to agree? We just had to take the one with the smallest ID :D

Need an expensive hashmap? You can do `"\u0000\u0000 ... \u0000".toCharArray()` for 2 bytecodes regardless of the number of elements ;)
(Up to 2**14, limit of static string size)

## Analyze crashes!
A rather classic error is wanting to catch `GameException` but:

  * If you have an error, it's better to fix its cause than to hide it under the rug
  * You then have a penalty of 500 bytecodes + context restoration
  * You're not going to make me believe that this limit behavior is checked every time?

I therefore recommend having a single try/catch and only using it if competitive mode is enabled.
By the way! We rather appreciated having an `isCompetitive` flag that allows blocking certain passages reserved for test matches (excessive display, resign at turn 400 to debug faster, limit to 4 rats to avoid having too loaded a display, or other) Colors This contributes to having clean and robust code.

## Need some colors?
Here are 64!

    // Credits to https://lospec.com/palette-list/old-z64
    public static char[] palette_r = {0, 77, 125, 192, 242, 179, 204, 217, 227, 230, 105, 161, 217, 240, 242, 64, 97, 133, 156, 176, 128, 173, 212, 230, 237, 153, 171, 199, 224, 222, 75, 103, 135, 179, 227, 24, 36, 71, 93, 167, 13, 40, 73, 114, 159, 32, 40, 54, 89, 105, 34, 56, 82, 119, 158, 82, 108, 149, 194, 225, 23, 51, 204, 247};
    public static char[] palette_g = {0, 48, 55, 88, 116, 36, 61, 108, 145, 172, 10, 16, 28, 94, 166, 43, 64, 88, 109, 144, 57, 81, 108, 135, 163, 96, 116, 146, 175, 202, 94, 125, 156, 194, 227, 102, 133, 163, 196, 230, 113, 161, 184, 207, 227, 61, 78, 115, 158, 187, 42, 56, 77, 107, 143, 95, 125, 166, 204, 242, 24, 56, 167, 193};
    public static char[] palette_b = {0, 191, 179, 204, 213, 179, 168, 164, 168, 172, 65, 64, 44, 65, 111, 39, 53, 65, 76, 113, 33, 35, 34, 41, 59, 40, 39, 40, 27, 111, 38, 31, 31, 21, 84, 68, 57, 60, 24, 56, 133, 149, 147, 151, 169, 128, 128, 153, 179, 191, 115, 140, 153, 179, 204, 102, 128, 162, 202, 233, 38, 64, 143, 186};

[figure omitted: The colors][figure omitted: example rendering]

## Do you want to visualize ranges?
I made a small tool available [here](<https://louis-max.gitlab.io/blog/outils/battlecode-rangeviewer/>).

[figure omitted: Range viewer]

---

# 98 Annex
**Created on 2026-01-28**

# Annex
Version with a few fewer attributes, because they had... Many 😊

    package current.Robots;

    import battlecode.common.*;
    import current.States.*;
    import current.Utils.ArrayUtils;
    import current.Utils.Tools;

    public class Robot {
        /////////////////////////////////////// Configuration ///////////////////////////////////////

        // All variables need to be static to be accessible from different part of the code
        public static RobotController rc;
        public static MapLocation spawnLoc;
        public static MapLocation lastLocation;

        // Meta variables
        public static boolean competitiveMode = false;

        // Nearest locations (automaticaly updated by Init.java and Communication.java)
        public static MapLocation nearestCat;
        public static MapLocation nearestKing;
        public static MapLocation nearestAllyRat;
        public static MapLocation nearestEnemyRat;
        public static MapLocation nearestEnemyKing;
        public static MapLocation nearestMine;
        public static MapLocation nearestDirt;
        public static MapLocation nearestCheese;

        // Nearest ID, ID are mod 4096
        public static int nearestAllyRatID;
        public static int nearestEnemyRatID;
        public static int nearestCatID;
        public static int nearestKingID;
        public static int nearestEnemyKingID;

        // Memory
        public static MapLocations cheeseMines = new MapLocations((char) 150);
        public static MapLocations cheeseMinesFromArray = new  MapLocations((char) 150);
        public static MapLocationsWithId alliesRats = new MapLocationsWithId((char) 100, true); // true: flush when full
        public static MapLocationsWithId enemiesRats = new MapLocationsWithId((char) 30, true);
        public static MapLocationsWithId cats = new MapLocationsWithId((char) 100, true);
        public static MapLocationsWithId kings = new MapLocationsWithId((char) 100, true);
        public static MapLocationsWithId enemiesKings = new MapLocationsWithId((char) 100, true);

        // Phases
        public static int gamePhase; // 0 Start, 1 Early, 2 Midle, 3 Final
        public static int mapType;   // 0 Small, 1 Medium, 2 Large

        // State Machine
        public State init;
        public State avoidCat;
        public State endTurn;
        public State currentState;
        public void onNewTurn(){};
        public void updateState(Result previousResult){}
        public void init() throws GameActionException {}

        /////////////////////////////////////// Run ///////////////////////////////////////

        public void run(RobotController rc) throws GameActionException {
            Robot.rc = rc;
            if(!competitiveMode && rc.getRoundNum() <= 2) {
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println("   This bot is running with competitiveMode set to false    ");
                System.out.println("            Edit the flag before submitting it !            ");
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }

            // Init states
            header("Starting at round " + Clock.getBytecodeNum());
            init = new Init();
            avoidCat = new AvoidCat(); // State shared by everybots
            endTurn = new EndTurn();
            currentState = init;
            header("Done init at bytecode " + Clock.getBytecodeNum());

            // Playing
            while (true) {
                // Resign if not competitive and round > 400
                if(!competitiveMode && round > 400) {
                    rc.resign();
                }

                // Play state
                header("\t" + currentState.name + "\t" + Clock.getBytecodeNum());
                Result result = currentState.run();
                print("<= " + result.code.name() + " " + result.msg);

                // Continue according to return state
                switch(result.code){
                    case LOCK:
                        print("Lock: End turn and resume to state.");
                        rc.setIndicatorString("LOCK " + currentState.name);

                        endTurn.run();
                        init.run();
                        avoidCat.run(); // Maybe init state should call avoidCat ?
                        break;

                    case END_OF_TURN:
                        print("Skipping to end of turn");
                        rc.setIndicatorString("END_OF_TURN " + currentState.name);

                        currentState = endTurn;
                        break;

                    default: // Ok, Err, Warn
                        updateState(result);
                        break;
                }
            }
        }

        /////////////////////////////////////// Debug ///////////////////////////////////////
        public static void _debug(String msg){
            // Print only for first 200 turns
            if(round < 200 && rc.getTeam() == Team.A) {
                System.out.println(msg);
            }
        }
        public static void header(String msg)    {_debug(msg);} // Important informations (state name, round, energy used)
        public static void print (String msg)    {_debug("\t\t" + msg);}     // Print inside state (target, action, etc.)
        public static void debug (String msg)    {_debug("\t\t\t" + msg);}   // Debug stuff
        public static void ddebug(String msg)    {_debug("\t\t\t\t" + msg);} // Debug stuff

        public static void warn  (String msg)    {_debug(" WW: " + msg);}
        public static void err(String msg)       {_debug(" EE: " + msg);}
    }
