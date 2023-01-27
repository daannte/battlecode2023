package coltonplayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;
    static int funnyTurnCountHeHe = 0;
    static int carriersThisHqHasBuilt = 0;
    static int attackersThisHqHasBuilt = 0;
    static ArrayList<String> syms = new ArrayList<String>();
    static int whatHqAmI;
    static ArrayList<MapLocation> coordsOfOurHqs = new ArrayList<MapLocation>();
    static ArrayList<MapLocation> coordsOfEnemyHqs = new ArrayList<MapLocation>();
    static ArrayList<MapLocation> possibleCoordsOfEnemyHqsAlwaysThree = new ArrayList<MapLocation>();
    static ArrayList<MapLocation> possibleCoordsOfEnemyHqs = new ArrayList<MapLocation>();
    static MapLocation thisHqsEnemyHqMirror = null;
    static int amountOfLaunchersWhoChoseAHq = 0;
    static boolean enemyHqCoordsLocated = false;
    static MapLocation possibleEnemyHqFromVSym;
    static MapLocation possibleEnemyHqFromHSym;
    static MapLocation possibleEnemyHqFromRSym;
    static final int hqsStillProcessingAttackerInfo = 6969;
    static boolean symmetryFound;
    static int amountOfHqsThisHqKnows;
    static int amountOfHqsInThisGame;
    static MapLocation middlePos = null;
    //static boolean launcherBeenToMiddle = false;
    static boolean isScout = false;
    static int attackerIncrementer = 0;
    static MapLocation attackerIsAttackingThisLocation;
    static MapLocation thisAttackersMapLocationTheyScouted;
    static int numOfEnemyHqsInArray = 0;
    static int testCounter = 0;
    static boolean scoutResultFromPossibleHqLocation;
    static boolean scoutReturningHome = false;
    static boolean attackerAttacked;
    static boolean weShouldBuildAnAnchor = false;
    static boolean carryingAnAnchor = false;
    static MapLocation islandLocation = null;
    static boolean manaMiner = false;
    static boolean adamMiner = false;

    /**
     * KEEPING TRACK OF WHAT'S IN THE SHARED ARRAY
     * [0 ,    1-4     ,  5  ,       6-17       ,         18        ,         19          ,   20   ,     21         ,   22
     * ind  hq coords  #ofhqs  enemy hq coords   #ofenemyhqsInArray   AtckHQEvenlyCounter    sym   scoutedEnemyHqLoc  hqOrNot
     * ,     23     ,      24     ,    25-28   ,                  29-63              ]
     *  rewriteEhQ's    hqsActed   comeKillDude
     */

    static final int hqStoringIndicatorIndex = 0;
    static final int hqCoordsStartingIndex = 1;
    static final int numOfHqsIndex = 5;
    static final int enemyHqCoordsStartingIndex = 6;
    static final int enemyHqCoordsEndingIndex = 17;
    static final int numOfEnemyHqsInArrayIndex = 18;
    static final int AdamVsManaRatioIndex = 19;
    static final int symIndex = 20;
    static final int scoutedEnemyHqLocationIndex = 21;
    static final int hqOrNotIndex = 22;
    static final int rewriteEnemyHqsIndex = 23;
    static final int orderOfHqsActingThisRoundIndex = 24;
    static final int comeKillThisDudeAtHq1Index = 25;
    static final int comeKillThisDudeAtHq2Index = 26;
    static final int comeKillThisDudeAtHq3Index = 27;
    static final int comeKillThisDudeAtHq4Index = 28;
    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!

        System.out.println("I'm a " + rc.getType() + " and I just got created! I have health " + rc.getHealth());

        // You can also use indicators to save debug notes in replays.
        // rc.setIndicatorString("Hello world!");

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the RobotType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()) {
                    case HEADQUARTERS:      runHeadquarters(rc);    break;
                    case CARRIER:           runCarrier(rc);         break;
                    case LAUNCHER:          runLauncher(rc);        break;
                    case BOOSTER:
                    case DESTABILIZER:
                    case AMPLIFIER:                                 break;
                }

            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    /**
     * Run a single turn for a Headquarters.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runHeadquarters(RobotController rc) throws Exception {
        // write what place you are in the acting for hqs this round
        int x = rc.readSharedArray(orderOfHqsActingThisRoundIndex);
        rc.writeSharedArray(orderOfHqsActingThisRoundIndex, x+1);
        int iAmThisHqToActThisRound = rc.readSharedArray(orderOfHqsActingThisRoundIndex);

        // takes up 3-9 spots in the shared array (depending on how many hq's)
        MapLocation me = rc.getLocation();
        int width = rc.getMapWidth();
        int height = rc.getMapHeight();

        if (rc.getRoundNum() == 1) {
            // round one puts each hq into the shared array, and puts each hq's possible guesses of its doppelgänger
            // enemy hq into static lists (used on round 2)

            // put this hq into the array with our hq's
            storeOurHqToArray(rc, me);

            middlePos = new MapLocation((int) Math.round( (double) width / 2), (int) Math.round( (double) height / 2));

            // put the enemy hq coords to possibleCoordsOfEnemyHqs and possibleCoordsOfEnemyHqsAlwaysThree


            // amountOfHqsThisHqKnows = rc.readSharedArray(0) / 2;
            amountOfHqsInThisGame = rc.readSharedArray(numOfHqsIndex);
            // System.out.println("rc.getRobotCount() returns: " + rc.getRobotCount() + "\n");

        }
        if (rc.getRoundNum() == 2) {
            // try and guess the symmetry of the map on this turn. If you can't, then add a bunch of possible enemy hq

            giveCallingRobotAListOfOurHqs(rc);

            addEnemyHqCoordsToTheStaticListsAndSyms(width, height, me);
            //System.out.println("After: " + possibleCoordsOfEnemyHqs + " | " + syms);

            // guess the symmetry based on only our starting hq positions
            // syms = guessSymmetryBasedOnOurInitialHqLocations();
            // rc.setIndicatorString(syms.toString());

            if (possibleCoordsOfEnemyHqs.size() > 1) {
                //symmetry wasn't guessed just based on our initialHqPositions, so try another way
                System.out.println("\nBefore" + syms);
                syms = guessSymmetryBasedOnEnemyHqsWeCanSee(rc);
                System.out.println("\nAfter" + syms);
            }

            if (possibleCoordsOfEnemyHqs.size() == 1) {
                enemyHqCoordsLocated = true;
                writeTheSymToSharedArray(rc, syms.get(0));
                // we found the symmetry
                MapLocation theFabledLocation = possibleCoordsOfEnemyHqs.get(0);
                rc.setIndicatorDot(theFabledLocation, 0, 255, 0);
            } else {
                // didn't find the symmetry
                for (MapLocation possibleCoordsOfEnemyHq : possibleCoordsOfEnemyHqs) {
                    rc.setIndicatorDot(possibleCoordsOfEnemyHq, 0, 0, 255);
                }
            }
            //System.out.println(numOfEnemyHqsInArray);
            // write the current guesses to the array so that launchers can go to one of them, even if these change on round 3
            //System.out.println(numOfEnemyHqsInArray);
            //System.out.println(possibleCoordsOfEnemyHqs);
            writeEnemyHqsToArray(rc);
        }
        if (rc.getRoundNum() == 3) {
            rewriteTheEnemyHqPositions(rc, iAmThisHqToActThisRound);
        }
        // rc.setIndicatorString(String.valueOf(rc.readSharedArray(rewriteEnemyHqsIndex)));

        if (rc.readSharedArray(rewriteEnemyHqsIndex) == rc.getRoundNum()) {
            //System.out.println("rewriting the enemy hq positions");
            //rc.setIndicatorString("rewriting the enemy hq positions");
            rewriteTheEnemyHqPositions(rc, iAmThisHqToActThisRound);
            if (iAmThisHqToActThisRound == amountOfHqsInThisGame) {
                rc.writeSharedArray(rewriteEnemyHqsIndex, 0);
                rc.writeSharedArray(scoutedEnemyHqLocationIndex, 0);
            }

        }

        //rc.setIndicatorString(String.valueOf(rc.readSharedArray(scoutedEnemyHqLocationIndex)));
        // if an attacker came back with enemy hq information, we can make another symmetry guess!
        //System.out.println(rc.readSharedArray(scoutedEnemyHqLocationIndex));
        if ((rc.readSharedArray(scoutedEnemyHqLocationIndex) != 0) && (rc.readSharedArray(scoutedEnemyHqLocationIndex) != hqsStillProcessingAttackerInfo)) {
            //System.out.println("got info from attackers");
            rc.setIndicatorString("GOT INFORMATION FROM THE ATTACKERS");
            // an attacker deposited the information about one of our guesses for enemy hq locations! let's try to
            // guess the symmetry again
            MapLocation scoutedMapPos = intToLocation(rc, rc.readSharedArray(scoutedEnemyHqLocationIndex));
            boolean hqOrNot = intToBoolean(rc.readSharedArray(hqOrNotIndex));
            System.out.println("syms before: " + syms);
            syms = guessSymmetryBasedOnAttackerDepositedInformation(rc, scoutedMapPos, hqOrNot);
            System.out.println("syms after: " + syms);
            //System.out.println("the size of syms is " + syms.size());
            if (syms.size() == 1) {
                enemyHqCoordsLocated = true;
                writeTheSymToSharedArray(rc, syms.get(0));
                // we found the symmetry
                MapLocation theFabledLocation = possibleCoordsOfEnemyHqs.get(0);
                rc.setIndicatorDot(theFabledLocation, 0, 255, 0);
            } else {
                // didn't find the symmetry
                for (MapLocation possibleCoordsOfEnemyHq : possibleCoordsOfEnemyHqs) {
                    rc.setIndicatorDot(possibleCoordsOfEnemyHq, 0, 0, 255);
                }
            }
            if (iAmThisHqToActThisRound == amountOfHqsInThisGame) {
                System.out.println("I am writing 6969 to the scoutedEnemyHqIndex so our hqs dont get overloaded with info fr");
                rc.writeSharedArray(scoutedEnemyHqLocationIndex, 6969);
                rc.writeSharedArray(hqOrNotIndex, 0);
                //System.out.println("wrote to rewriteEnemyHqsIndex");
                rc.writeSharedArray(rewriteEnemyHqsIndex, rc.getRoundNum()+1);
            }
        }


        /*

        everything above of here deals with finding symmetry and calculating it again and blah blah blah, you probably won't
        have to change it. Under here is where the hq decides to spawn dudes

         */


        // Pick a direction to build in.

        //what map locations we want to try spawning an attacker in, starting with most wanted, ending with the least wanted
        MapLocation[] attackerSpawnLocs = {
                me.add(me.directionTo(middlePos)),
                me.add(me.directionTo(middlePos).rotateRight()),
                me.add(me.directionTo(middlePos).rotateLeft()),
                me.add(me.directionTo(middlePos).rotateRight().rotateRight()),
                me.add(me.directionTo(middlePos).rotateLeft().rotateLeft()),
        };

        //what map locations we want to try spawning a carrier in, starting with most wanted, ending with the least wanted
        MapLocation[] carrierSpawnLocs = new MapLocation[directions.length];
        for (int i = 0; i < directions.length; i++) {
            Direction direction = directions[i];
            carrierSpawnLocs[i] = me.add(direction);
        }


        //build attackers and carriers after this

        if (!(rc.getRoundNum() >= (int) ((width + height) * 4))) {
            funnyTurnCountHeHe--;
        }

        if ((funnyTurnCountHeHe % 30 == 0) && ((rc.getNumAnchors(Anchor.STANDARD) == 0) && (rc.getNumAnchors(Anchor.ACCELERATING) == 0))) {
            weShouldBuildAnAnchor = true;
        }

        funnyTurnCountHeHe++;
        if (weShouldBuildAnAnchor) funnyTurnCountHeHe--;

        //rc.setIndicatorString("" + weShouldBuildAnAnchor + " | " + funnyTurnCountHeHe);

        for (int i = 1; i <= 5; i++) {
            // can spawn up to 5 dudes a turn
            if (attackersThisHqHasBuilt < 3) {
                //build an attacker
                spawnADude(rc, attackerSpawnLocs, RobotType.LAUNCHER);
            } else if (carriersThisHqHasBuilt < 4) {
                //build a carrier
                spawnADude(rc, carrierSpawnLocs, RobotType.CARRIER);
            } else {
                if (weShouldBuildAnAnchor) {
                    if (rc.canBuildAnchor(Anchor.STANDARD)) {
                        rc.buildAnchor(Anchor.STANDARD);
                        weShouldBuildAnAnchor = false;
                        funnyTurnCountHeHe++;
                    }
                    if ((rc.getResourceAmount(ResourceType.MANA) - RobotType.LAUNCHER.getBuildCost(ResourceType.MANA)) > Anchor.STANDARD.getBuildCost(ResourceType.MANA)) {
                        spawnADude(rc, attackerSpawnLocs, RobotType.LAUNCHER);
                    }
                    else if ((rc.getResourceAmount(ResourceType.ADAMANTIUM) - RobotType.CARRIER.getBuildCost(ResourceType.ADAMANTIUM)) > Anchor.STANDARD.getBuildCost(ResourceType.ADAMANTIUM)) {
                        spawnADude(rc, carrierSpawnLocs, RobotType.CARRIER);
                    }
                } else {
                    // keep roughly 1/1.6 carrier/attacker ratio
                    //if (((carriersThisHqHasBuilt) >= (int) (attackersThisHqHasBuilt * 1.6)) && (rc.getResourceAmount(ResourceType.MANA) >= RobotType.LAUNCHER.getBuildCost(ResourceType.MANA))) {
                    if (rc.getResourceAmount(ResourceType.MANA) >= RobotType.LAUNCHER.getBuildCost(ResourceType.MANA)) {
                        spawnADude(rc, attackerSpawnLocs, RobotType.LAUNCHER);
                    } else {
                        spawnADude(rc, carrierSpawnLocs, RobotType.CARRIER);
                    }
                }
            }
        }
        // printSharedArray(rc);

        // if the hq sees an enemy robot (that's not a carrier), tell the attackers to come and merk it
        RobotInfo[] robotsAroundUs = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        boolean changedTargets = false;
        int thisHqsSpotToWrite = comeKillThisDudeAtHq1Index + iAmThisHqToActThisRound - 1;
        MapLocation enemyHqIsCurrentlyGettingAttackersToAttackThisLoc = intToLocation(rc, rc.readSharedArray(thisHqsSpotToWrite));
        for (RobotInfo enemyRobot : robotsAroundUs) {
            if (enemyRobot.getType() != RobotType.CARRIER) {
                MapLocation enemyLocation = enemyRobot.getLocation();
                if ((rc.readSharedArray(thisHqsSpotToWrite) == 0) || !(enemyLocation.equals(enemyHqIsCurrentlyGettingAttackersToAttackThisLoc))) {
                    // if spot is open, or its a different target, write the new enemy to attack
                    if (!(enemyLocation.equals(enemyHqIsCurrentlyGettingAttackersToAttackThisLoc))) {
                        changedTargets = true;
                    }
                    rc.writeSharedArray(thisHqsSpotToWrite, locationToInt(rc, enemyLocation));
                    break;
                }
                // if spot isnt open, u already put someone to get merked in there, so ur fine
            }
        }
        if ((robotsAroundUs.length == 0) && (rc.readSharedArray(thisHqsSpotToWrite) != 0)) {
            // if there was an enemy around us but now there's not, write 0 to the array so attackers don't come anymore
            rc.writeSharedArray(thisHqsSpotToWrite, 0);
        }


        if (iAmThisHqToActThisRound == amountOfHqsInThisGame) {
            rc.writeSharedArray(orderOfHqsActingThisRoundIndex, 0);
        }
    }

    /**
     * Run a single turn for a Carrier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runCarrier(RobotController rc) throws GameActionException {
        // System.out.println(middlePos);
        MapLocation me = rc.getLocation();

        if (turnCount == 1) {
            if (rc.getID() % 3 == 0) {
                adamMiner = true;
            } else {
                manaMiner = true;
            }
        }

        if (adamMiner) rc.setIndicatorString("Adam miner");
        if (manaMiner) rc.setIndicatorString("Mana miner");

        if (turnCount == 2) {
            // guaranteed to be able to read this on turn 1 btw
            middlePos = new MapLocation((int) Math.round( (double) rc.getMapWidth() / 2), (int) Math.round( (double) rc.getMapHeight() / 2));
            amountOfHqsInThisGame = rc.readSharedArray(numOfHqsIndex);
            giveCallingRobotAListOfOurHqs(rc);
        }
        for (MapLocation coordsOfOurHq : coordsOfOurHqs) {
            if (rc.canTakeAnchor(coordsOfOurHq, Anchor.STANDARD)) {
                rc.takeAnchor(coordsOfOurHq, Anchor.STANDARD);
                break;
            }
        }

        for (int yeaboiiiiiii = 1; yeaboiiiiiii <= 2; yeaboiiiiiii++) {

            boolean dontMove = false;

            if (rc.getAnchor() != null) {
                carryingAnAnchor = true;
            } else {
                carryingAnAnchor = false;
            }

            // If we can see a well, move towards it
            if (carryingAnAnchor) {
                for (int i = 0; i < 2; i++) {
                    // we will prob be double moving
                    scanIslands(rc);
                    if (islandLocation != null) {
                        moveToThisLocation(rc, islandLocation);
                    } else {
                        moveRandomly(rc);
                    }
                    islandLocation = null;
                    if (rc.canPlaceAnchor() && rc.senseTeamOccupyingIsland(rc.senseIsland(rc.getLocation())) == Team.NEUTRAL) {
                        rc.placeAnchor();
                        carryingAnAnchor = false;
                        break;
                    }
                }
            } else {

                int amountOfAdamantium = rc.getResourceAmount(ResourceType.ADAMANTIUM);
                int amountOfMana = rc.getResourceAmount(ResourceType.MANA);

                if ((amountOfAdamantium + amountOfMana) < 40) {
                    // try to just do everything twice cuz yeah
                    // needa find a well
                    // rc.setIndicatorString("Tried to sense a well near me and move to it");
                    WellInfo[] wells = new WellInfo[0];
                    WellInfo[] manaWells = rc.senseNearbyWells(ResourceType.MANA);
                    WellInfo[] adamWells = rc.senseNearbyWells(ResourceType.ADAMANTIUM);
                    if (manaMiner) wells = manaWells;
                    else if (adamMiner) wells = adamWells;

                    if (wells.length > 0) {
                        MapLocation closestWellLoc = wells[0].getMapLocation();
                        WellInfo closestWell = wells[0];
                        for (WellInfo well : wells) {
                            if (me.distanceSquaredTo(well.getMapLocation()) < me.distanceSquaredTo(closestWellLoc)) {
                                closestWellLoc = well.getMapLocation();
                                closestWell = well;
                            }
                        }

                        if (me.isAdjacentTo(closestWellLoc)) {
                            // if we are close enough to collect, we don't need to move closer, so don't move, and just collect
                            dontMove = true;
                            // Try to gather from the well we are beside
                            if (rc.isActionReady()) {
                                if (rc.canCollectResource(closestWellLoc, closestWell.getRate())) {
                                    rc.collectResource(closestWellLoc, closestWell.getRate());
                                }
                            }

                        } else {
                            // if we aren't adjacent to the well, we can't collect, so move to it
                            moveToThisLocation(rc, closestWellLoc);
//                        if (amountOfAdamantium + amountOfMana == 0) {
//                            //empty dudes can move a second time hehe
//                            Direction dir2 = me.directionTo(closestWellLoc);
//                            if (rc.canMove(dir2)) {
//                                rc.move(dir2);
//                                rc.setIndicatorString("Closest well at " + closestWellLoc + " , im omw by moving " + dir2);
//                            }
//                        }
                        }
                    }
                    if (rc.isActionReady()) {
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dy = -1; dy <= 1; dy++) {
                                MapLocation wellLocation = new MapLocation(me.x + dx, me.y + dy);
                                if (rc.canCollectResource(wellLocation, -1)) {
                                    rc.collectResource(wellLocation, -1);
                                    rc.setIndicatorString("Collecting, now have, AD:" +
                                            rc.getResourceAmount(ResourceType.ADAMANTIUM) +
                                            " MN: " + rc.getResourceAmount(ResourceType.MANA) +
                                            " EX: " + rc.getResourceAmount(ResourceType.ELIXIR));
                                }
                            }
                        }
                    }

                } else {
                    // we have max we can carry, go back to the (closest) hq and deposit!
                    // System.out.println(coordsOfOurHqs);
                    MapLocation hqPos = getTheClosestHq(rc);
                    if (rc.getLocation().isAdjacentTo(hqPos)) {
                        for (ResourceType resource : ResourceType.values()) {
                            // System.out.println("" + resource + "");
                            if (rc.canTransferResource(hqPos, resource, rc.getResourceAmount(resource))) {
                                rc.transferResource(hqPos, resource, rc.getResourceAmount(resource));
                                rc.setIndicatorString("Transferred " + resource + " to " + hqPos);
                            }
                        }
                    } else if (rc.isMovementReady()) {
                        moveToThisLocation(rc, hqPos);
                        rc.setIndicatorString("Moving to " + hqPos);
                    }
                }
            }
            // move randomly if we want to move but couldn't find a valid spot
            if (rc.isMovementReady() && !dontMove) {
                //System.out.println(rc.canMove(dir));
                moveRandomly(rc);
                //rc.setIndicatorString("Moving " + dir);
            }
        }
    }

    /**
     * Run a single turn for a Launcher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runLauncher(RobotController rc) throws GameActionException {

        amountOfHqsInThisGame = rc.readSharedArray(numOfHqsIndex);
        numOfEnemyHqsInArray = rc.readSharedArray(numOfEnemyHqsInArrayIndex);
        MapLocation me = rc.getLocation();

        // on every turn, the attackers get an array of all hq locations in the array
        giveCallingRobotAListOfEnemyHqs(rc);
        giveCallingRobotAListOfOurHqs(rc);

        // if # of our hqs == # of enemy hq locations in the array, those probable enemy locations are the real deal!
        if (amountOfHqsInThisGame == numOfEnemyHqsInArray) {
            enemyHqCoordsLocated = true;
        }
        if (turnCount == 1) {
            // SPAWNED :D
            middlePos = new MapLocation((int) Math.round( (double) rc.getMapWidth() / 2), (int) Math.round( (double) rc.getMapHeight() / 2));

        }

        MapLocation closestEnemyHq = coordsOfEnemyHqs.get(0);
        StringBuilder indString = new StringBuilder();
        if (!enemyHqCoordsLocated) {
            for (MapLocation coordsOfEnemyHq : coordsOfEnemyHqs) {
                if (me.distanceSquaredTo(coordsOfEnemyHq) < me.distanceSquaredTo(closestEnemyHq)) {
                    closestEnemyHq = coordsOfEnemyHq;
                }
            }
            attackerIsAttackingThisLocation = closestEnemyHq;
        } else {
            ArrayList<MapLocation> possibleEnemiesAttackingOurHq = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                int possibleEnemyLoc = rc.readSharedArray(comeKillThisDudeAtHq1Index + i);
                if (possibleEnemyLoc != 0) {
                    possibleEnemiesAttackingOurHq.add(intToLocation(rc, rc.readSharedArray(comeKillThisDudeAtHq1Index + i)));
                }
            }
            if (possibleEnemiesAttackingOurHq.size() > 0) {
//                if (rc.getID() % 2 == 0) {
//                    MapLocation closestEnemyLoc = possibleEnemiesAttackingOurHq.get(0);
//                    for (MapLocation enemyLoc : possibleEnemiesAttackingOurHq) {
//                        if (me.distanceSquaredTo(enemyLoc) < me.distanceSquaredTo(closestEnemyLoc)) {
//                            closestEnemyLoc = enemyLoc;
//                        }
//                    }
//                    attackerIsAttackingThisLocation = closestEnemyLoc;
//                } else {
//                    attackerIsAttackingThisLocation = coordsOfEnemyHqs.get(rc.getID() % numOfEnemyHqsInArray);
//                }
                MapLocation closestEnemyLoc = possibleEnemiesAttackingOurHq.get(0);
                for (MapLocation enemyLoc : possibleEnemiesAttackingOurHq) {
                    if (me.distanceSquaredTo(enemyLoc) < me.distanceSquaredTo(closestEnemyLoc)) {
                        closestEnemyLoc = enemyLoc;
                    }
                }
                attackerIsAttackingThisLocation = closestEnemyLoc;
            } else {
                attackerIsAttackingThisLocation = coordsOfEnemyHqs.get(rc.getID() % numOfEnemyHqsInArray);
            }
        }

        // try attacking before movement
        if (rc.isActionReady()) {
            ArrayList<Object> attackedInfo = attackerAttackAround(rc);
            if (attackerAttacked) {
                assert attackedInfo != null;
                if (attackedInfo.get(1) == RobotType.LAUNCHER) {
                    // if we attacked an attacker before moving, use the movement this turn to try to move away from them
                    moveOppositeDirection(rc, (MapLocation) attackedInfo.get(0));
                }
            }
        }

        if (rc.isMovementReady()) {
            if (attackerIsAttackingThisLocation != null) {
                if (enemyHqCoordsLocated) {
                    scoutReturningHome = false;
                } else {
                    checkIfWeSeeAHqOnOurTravels(rc);
                }
                rc.setIndicatorString(String.valueOf(scoutReturningHome));
                if (scoutReturningHome) {
                    MapLocation hqPos = getTheClosestHq(rc);
                    moveToThisLocation(rc, hqPos);
                    rc.setIndicatorString("Heading home to " + hqPos);

                    didScoutMakeItHome(rc, hqPos, me);
                } else {
                    // moves the attacker closer to this location target
                    attackerMoveToLocation(rc, me);
                }
            } else {
                moveRandomly(rc);
            }
        }

        //try attacking after moving
        if (rc.isActionReady()) {
            attackerAttackAroundAndClouds(rc);
        }
    }

/*
------------------------------------------------------------------------------------------------------------------------
                                        GENERAL FUNCTIONS
------------------------------------------------------------------------------------------------------------------------
*/

    /**
     * simply gives the calling robot the knowledge of where all of our hqs are, for ease of access later on shall we
     * need it. Sets the coordsOfOurHqs static list
     * @param rc RobotController
     * @throws GameActionException from reading array
     */
    static void giveCallingRobotAListOfOurHqs(RobotController rc) throws GameActionException {
        coordsOfOurHqs.clear();
        // give the calling robot the coords of our hqs
        for (int i = hqCoordsStartingIndex; i < hqCoordsStartingIndex + amountOfHqsInThisGame; i++) {
            MapLocation Hq = intToLocation(rc, rc.readSharedArray(i));
            coordsOfOurHqs.add(Hq);
        }
    }

    /**
     * simply gives calling robot the locations of the enemy hqs that are in the shared array (all the enemy hq
     * positions that we know so far at this point). Sets the coordsOfEnemyHqs static list
     * @param rc RobotController
     * @throws GameActionException from reading array
     */
    static void giveCallingRobotAListOfEnemyHqs(RobotController rc) throws GameActionException {
        coordsOfEnemyHqs.clear();
        //System.out.println(enemyHqCoordsStartingIndex);
        //System.out.println(numOfEnemyHqsInArray + enemyHqCoordsStartingIndex);
        ArrayList<MapLocation> newCoordsOfEnemyHqs = null;
        for (int i = enemyHqCoordsStartingIndex; i < enemyHqCoordsStartingIndex + numOfEnemyHqsInArray; i++) {
            //System.out.println("RAN THE FOR LOOP");
            MapLocation Hq = intToLocation(rc, rc.readSharedArray(i));
            //System.out.println(Hq);
            coordsOfEnemyHqs.add(Hq);
        }
        //System.out.println("after: " + coordsOfEnemyHqs);
    }

    /**
     * prints the shared array
     * @param rc RobotController
     * @throws GameActionException from readSharedArray
     */
    static void printSharedArray(RobotController rc) throws GameActionException {
        StringBuilder sharedArray = new StringBuilder();
        int counter = 0;
        while (counter < GameConstants.SHARED_ARRAY_LENGTH) {
            sharedArray.append((rc.readSharedArray(counter)));
            sharedArray.append(", ");
            counter++;
        }
        System.out.println(sharedArray);
    }

    /**
     * puts a map location to an integer
     * @param rc RobotController
     * @param location MapLocation to convert
     * @return location in integer form
     */
    static int locationToInt(RobotController rc, MapLocation location) {
        if (location == null) return 0;
        return 1 + location.x + location.y * rc.getMapWidth();
    }

    /**
     * puts an integer to a map location
     * @param rc RobotController
     * @param integerLocation integer to convert
     * @return integer in location form
     */
    static MapLocation intToLocation(RobotController rc, int integerLocation) {
        if (integerLocation == 0) return null;
        integerLocation--;
        return new MapLocation(integerLocation % rc.getMapWidth(), integerLocation / rc.getMapWidth());
    }

    /**
     *
     * @param i
     * @return
     * @throws Exception if i is not 0 or 1
     */
    static boolean intToBoolean(int i) throws Exception {
        if (i == 0) return false;
        if (i == 1) return true;
        throw new Exception();
    }

    static int booleanToInt(boolean bool) {
        if (bool) return 1;
        else return 0;
    }

    /**
     * moves a robot randomly
     * @param rc RobotController
     * @throws GameActionException from move()
     */
    static void moveRandomly(RobotController rc) throws GameActionException {
        //HashSet<Direction> random = new HashSet<Direction>(Arrays.asList(directions));
        ArrayList<Direction> random = new ArrayList<Direction>(Arrays.asList(directions));
        Collections.shuffle(random);
        for (Direction direction : random) {
            if (rc.canMove(direction)) {
                rc.move(direction);
                break;
            }
        }
    }

    /**
     * try moving to this location
     * @param rc RobotController
     * @param tryToMoveToThis location to move to
     * @throws GameActionException form moving
     */
    static void moveToThisLocation(RobotController rc, MapLocation tryToMoveToThis) throws GameActionException {
        MapLocation me = rc.getLocation();
        Direction dir = me.directionTo(tryToMoveToThis);
//        Direction[] moveDirs = new Direction[5];
//        moveDirs[0] = dir;
//        moveDirs[1] = dir.rotateRight();
//        moveDirs[2] = dir.rotateLeft();
//        moveDirs[3] = dir.rotateRight().rotateRight();
//        moveDirs[4] = dir.rotateLeft().rotateLeft();
        Direction[] moveDirs = new Direction[3];
        moveDirs[0] = dir;
        moveDirs[1] = dir.rotateRight();
        moveDirs[2] = dir.rotateLeft();

        for (int i = 0; i < moveDirs.length; i++) {
            Direction moveDir = moveDirs[i];
            if (rc.canMove(moveDir)) {
                rc.move(moveDir);
                break;
            }
            if (i == (moveDirs.length - 1)) {
                moveRandomly(rc);
            }
        }
        if (rc.isMovementReady()) {
            rc.setIndicatorString("Didn't move somehow");
        }
    }

    /**
     * moves robot to closest hq. PLEASE have coordsOfOurHqs set before calling this.
     * @param rc RobotController
     */
    static MapLocation getTheClosestHq(RobotController rc) {
        MapLocation me = rc.getLocation();
        MapLocation hqPos = coordsOfOurHqs.get(0);
        // System.out.println(coordsOfOurHqs);

        // find the closest hq to us
        //System.out.println(coordsOfOurHqs);
        for (MapLocation hqCoords : coordsOfOurHqs) {
            if ((rc.getID() == 11270) && (rc.getRoundNum() == 80)) System.out.println("uhhhh  " + Clock.getBytecodesLeft());
            if (me.distanceSquaredTo(hqCoords) < me.distanceSquaredTo(hqPos)) {
                hqPos = hqCoords;
            }
        }
        return hqPos;
    }

/*
------------------------------------------------------------------------------------------------------------------------
                                          HQ FUNCTIONS
------------------------------------------------------------------------------------------------------------------------
*/

    static void clearEnemyHqsFromTheSharedArray(RobotController rc) throws GameActionException {
        numOfEnemyHqsInArray = 0;
        rc.writeSharedArray(numOfEnemyHqsInArrayIndex, 0);
        for (int i = enemyHqCoordsStartingIndex; i <= enemyHqCoordsEndingIndex; i++) {
            rc.writeSharedArray(i, 0);
        }
    }

    static void writeTheSymToSharedArray(RobotController rc, String sym) throws GameActionException {
        if (sym.equals("V")) rc.writeSharedArray(symIndex, 1);
        if (sym.equals("H")) rc.writeSharedArray(symIndex, 2);
        if (sym.equals("R")) rc.writeSharedArray(symIndex, 3);
    }

    /**
     * gives the calling hq its doppelgänger enemy hq's map location, could be useful later
     * @param sym the symmetry of the map.
     */
    static void getThisHqsMirroredEnemyHqLocation(String sym) {
        enemyHqCoordsLocated = true;
        if (sym.equals("V")) {
            thisHqsEnemyHqMirror = possibleCoordsOfEnemyHqsAlwaysThree.get(0);
        } else if (sym.equals("H")) {
            thisHqsEnemyHqMirror = possibleCoordsOfEnemyHqsAlwaysThree.get(1);
        } else if (sym.equals("R")) {
            thisHqsEnemyHqMirror = possibleCoordsOfEnemyHqsAlwaysThree.get(2);
        }
    }

    /**
     * simply just adds the possible enemy hqs that we know because of map symmetry. Adds to static lists, where these
     * lists contain the coords that this current hq thinks its doppelgänger enemy hq is (3 different locations, because
     * 3 different possible symmetries)
     * @param width width of the map
     * @param height height of the map
     * @param me this robots location
     */
    static void addEnemyHqCoordsToTheStaticListsAndSyms(int width, int height, MapLocation me) {
        syms.add("V");
        syms.add("H");
        syms.add("R");
        // this block puts the three possible locations the enemy hq can be based on its position into a list
        //horizontal possible loc
        possibleEnemyHqFromVSym = new MapLocation((width - me.x) - 1, me.y);
        //vertical possible loc
        possibleEnemyHqFromHSym = new MapLocation(me.x, (height - me.y) - 1);
        //rotational possible loc
        possibleEnemyHqFromRSym = new MapLocation((width - me.x) - 1, (height - me.y) - 1);
        possibleCoordsOfEnemyHqs.add(possibleEnemyHqFromVSym);
        possibleCoordsOfEnemyHqsAlwaysThree.add(possibleEnemyHqFromVSym);
        possibleCoordsOfEnemyHqs.add(possibleEnemyHqFromHSym);
        possibleCoordsOfEnemyHqsAlwaysThree.add(possibleEnemyHqFromHSym);
        possibleCoordsOfEnemyHqs.add(possibleEnemyHqFromRSym);
        possibleCoordsOfEnemyHqsAlwaysThree.add(possibleEnemyHqFromRSym);
        //System.out.println(coordsOfOurHqs);
        //System.out.println(possibleCoordsOfEnemyHqs);

        //System.out.println("Before: " + possibleCoordsOfEnemyHqs + " | " + syms);

        ArrayList<MapLocation> loopLocs = new ArrayList<>();
        ArrayList<String> loopSyms = new ArrayList<>();

        for (MapLocation possibleCoordsOfEnemyHq : possibleCoordsOfEnemyHqs) {
            loopLocs.add(possibleCoordsOfEnemyHq);
        }
        for (String sym : syms) {
            loopSyms.add(sym);
        }

        boolean takeOne = false;

        for (MapLocation coordsOfOurHq : coordsOfOurHqs) {
            for (MapLocation possibleCoordsOfEnemyHq : loopLocs) {
                if (possibleCoordsOfEnemyHq.equals(coordsOfOurHq)) {
                    takeOne = true;
                    possibleCoordsOfEnemyHqs.remove(possibleCoordsOfEnemyHq);
                    syms.remove(loopSyms.get(0));
                }
            }
        }

        if (takeOne) {
            MapLocation temp = possibleCoordsOfEnemyHqs.get(0);
            possibleCoordsOfEnemyHqs.clear();
            possibleCoordsOfEnemyHqs.add(temp);
            String temp2 = syms.get(0);
            syms.clear();
            syms.add(temp2);
        }
    }

    /**
     * guess the symmetry of the map, only knowing our hq locations
     * @return the symmetries we have narrowed it down to after doing this process (["H", "V", "R"] if we didn't narrow
     * it at all, and only one of those would remain if we can narrow it down all the way
     */
    static ArrayList<String> guessSymmetryBasedOnOurInitialHqLocations() {

        System.out.println("Before: " + syms);
        MapLocation originalCheckerHqPos = coordsOfOurHqs.get(0);
        //can only guess symmetries with more than 1 hq, so all three will remain valid after this
        if (coordsOfOurHqs.size() > 1) {
            for (int i = 1; i < coordsOfOurHqs.size(); i++) {
                MapLocation currentCheckerHqPos = coordsOfOurHqs.get(i);

                if (((originalCheckerHqPos.x < middlePos.x) && (currentCheckerHqPos.x > middlePos.x)) || (originalCheckerHqPos.x > middlePos.x) && (currentCheckerHqPos.x < middlePos.x)) {
                    //these two hqs are on opposite sides of the vertical symmetry line, so it CANNOT BE VERTICAL SYMMETRY
                    syms.remove("V");
                    //remove the enemy hq calculated with vertical symmetry from the possible enemy hq spots
                    possibleCoordsOfEnemyHqs.remove(possibleEnemyHqFromVSym);
                }
                if (((originalCheckerHqPos.y < middlePos.y) && (currentCheckerHqPos.y > middlePos.y)) || ((originalCheckerHqPos.y > middlePos.y) && (currentCheckerHqPos.y < middlePos.y))) {
                    //these two hqs are on opposite sides of the horizontal symmetry line, so it CANNOT BE HORIZONTAL SYMMETRY
                    syms.remove("H");
                    //remove the enemy hq calculated with horizontal symmetry from the possible enemy hq spots
                    possibleCoordsOfEnemyHqs.remove(possibleEnemyHqFromHSym);
                }
                if (false) {
                    // uhhhhh so how do eliminate rotational symmetry as an option
                    syms.remove("R");
                    possibleCoordsOfEnemyHqs.remove(possibleEnemyHqFromRSym);
                }
            }
        }
        System.out.println("After: " + syms);
        return syms;
    }

    /**
     *
     * @param rc
     * @return
     * @throws GameActionException from senseNearbyRobots
     */
    static ArrayList<String> guessSymmetryBasedOnEnemyHqsWeCanSee(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();
        ArrayList<String> theActualSymmetry = new ArrayList<>();
        for (int i = 0; i < possibleCoordsOfEnemyHqs.size(); i++) {
            //System.out.println("looping through the possible coords of enemy hqs we have");
            MapLocation possibleHqPosition = possibleCoordsOfEnemyHqs.get(i);
            //if (me.distanceSquaredTo(possibleHqPosition) <= RobotType.HEADQUARTERS.visionRadiusSquared) {
            if (me.distanceSquaredTo(possibleHqPosition) <= 34) {

                // if the possible hq position is in range of this hq
                if (rc.canSenseRobotAtLocation(possibleHqPosition)) {
                    RobotInfo enemyRobot = rc.senseRobotAtLocation(possibleHqPosition);
                    //if ((enemyRobot.getType() == RobotType.HEADQUARTERS) && (enemyRobot.getType() != RobotType.CARRIER) && (enemyRobot.getType() != RobotType.LAUNCHER)) {
                    if (enemyRobot.getType() == RobotType.HEADQUARTERS) {
                        System.out.println("theres a hq at " + possibleHqPosition);
                        // if there's a headquarters at this location, great!

                        // this possible hq spot is actually a hq!
                        possibleCoordsOfEnemyHqs.clear();
                        possibleCoordsOfEnemyHqs.add(enemyRobot.getLocation());
                        theActualSymmetry.add(syms.get(i));
                        // should be the symmetry of the map
                        return theActualSymmetry;
                    } else {
                        System.out.println("no hq at " + possibleHqPosition);
                        // if there's no headquarters at this location, that's fine, we can narrow the possible spots down
                        possibleCoordsOfEnemyHqs.remove(i);
                        syms.remove(i);
                        return syms;
                    }
                } else {
                    System.out.println("no hq at " + possibleHqPosition);
                    // if there's no headquarters at this location, that's fine, we can narrow the possible spots down
                    possibleCoordsOfEnemyHqs.remove(i);
                    syms.remove(i);
                    return syms;
                }
            }
        }

        // return original symmetry if we couldn't guess it here
        return syms;
    }

    static ArrayList<String> guessSymmetryBasedOnAttackerDepositedInformation(RobotController rc, MapLocation scoutedMapPos, boolean ifThereWasAHqThereOrNot) {
        //System.out.println("Before (attacker info): " + possibleCoordsOfEnemyHqs + " | " + syms);
        ArrayList<String> theActualSymmetry = new ArrayList<>();
        for (int i = 0; i < possibleCoordsOfEnemyHqs.size(); i++) {
            MapLocation possibleHqPosition = possibleCoordsOfEnemyHqs.get(i);
            if (possibleHqPosition.equals(scoutedMapPos)) {
                System.out.println("THE LOCATIONS MATCH UP FOR " + scoutedMapPos);
                if (ifThereWasAHqThereOrNot) {
                    // this possible hq spot is actually a hq!
                    possibleCoordsOfEnemyHqs.clear();
                    possibleCoordsOfEnemyHqs.add(scoutedMapPos);
                    theActualSymmetry.add(syms.get(i));
                    // should be the symmetry of the map
                    System.out.println("After (attacker info): " + possibleCoordsOfEnemyHqs + " | " + syms);
                    return theActualSymmetry;
                } else {
                    // if there's no headquarters at this location, that's fine, we can narrow the possible spots down
                    possibleCoordsOfEnemyHqs.remove(i);
                    syms.remove(i);
                }
            }
        }
        //System.out.println("After (attacker info): " + possibleCoordsOfEnemyHqs + " | " + syms);
        return syms;
    }

    /**
     * write this hq location to the array
     * @param rc RobotController
     * @param me this robots location
     * @throws GameActionException from read/write to array
     */
    static void storeOurHqToArray(RobotController rc, MapLocation me) throws GameActionException {
        int indicator = rc.readSharedArray(hqStoringIndicatorIndex);

        indicator++;

        // System.out.println("writing " + locationToInt(rc, me) + " to index " + indicator);
        rc.writeSharedArray(indicator, locationToInt(rc, me));

        // if the first hq to write your coords to the array, also write how many hqs we have in the game
        if (indicator == 1) {
            rc.writeSharedArray(numOfHqsIndex, rc.getRobotCount());
        }
        rc.writeSharedArray(hqStoringIndicatorIndex, indicator);
    }

    static void writeEnemyHqsToArray(RobotController rc) throws GameActionException {
        for (MapLocation anEnemyHqCoords : possibleCoordsOfEnemyHqs) {
            numOfEnemyHqsInArray = rc.readSharedArray(numOfEnemyHqsInArrayIndex);
            rc.writeSharedArray((enemyHqCoordsStartingIndex + numOfEnemyHqsInArray), locationToInt(rc, anEnemyHqCoords));
            rc.writeSharedArray(numOfEnemyHqsInArrayIndex, numOfEnemyHqsInArray + 1);
        }
    }

    /**
     *
     * @param rc
     * @throws GameActionException read and write to array
     */
    static void rewriteTheEnemyHqPositions(RobotController rc, int iAmThisHqToActThisRound) throws GameActionException {
        if (iAmThisHqToActThisRound == 1) clearEnemyHqsFromTheSharedArray(rc);

        // if a hq managed to guess the sym, and we haven't updated our possible hq spots with that information, do it!!!!!
        if ((rc.readSharedArray(symIndex) != 0) && (possibleCoordsOfEnemyHqs.size() > 1)) {
            possibleCoordsOfEnemyHqs.clear();
            possibleCoordsOfEnemyHqs.add(possibleCoordsOfEnemyHqsAlwaysThree.get(rc.readSharedArray(symIndex) - 1));
        }

        // write every enemy hq that we have guessed a location for into the array. If we guessed the symmetry
        // earlier, then each hq is only writing one location (the actual location). Otherwise, we are writing 2 or
        // more guesses for the enemy hq location.
        //System.out.println(numOfEnemyHqsInArray);

        writeEnemyHqsToArray(rc);

        if (rc.readSharedArray(symIndex) != 0) {
            enemyHqCoordsLocated = true;
            // we found the symmetry
            MapLocation theFabledLocation = possibleCoordsOfEnemyHqs.get(0);
            rc.setIndicatorDot(theFabledLocation, 0, 255, 0);
        } else {
            // didn't find the symmetry
            for (MapLocation possibleCoordsOfEnemyHq : possibleCoordsOfEnemyHqs) {
                rc.setIndicatorDot(possibleCoordsOfEnemyHq, 0, 0, 255);
            }
        }
    }

    /**
     * Using an array of map locations, spawn a person as close to that map location as possible
     * @param rc robot controller
     * @param spawnLocations array of locations to be tested if we can spawn there. Sorted by index for what we want to
     * try first (index 0 is first choice, last index is last choice)
     * @param robotType which type of robot we want to spawn
     * @throws GameActionException if buildRobot fails drastically (it won't dw)
     */
    static void spawnADude(RobotController rc, MapLocation[] spawnLocations, RobotType robotType) throws GameActionException {
        //rc.setIndicatorString("spawn a dude");
        // will always spawn a dude when fully completed
        for (int i = 0; i < spawnLocations.length; i++) {
            MapLocation spawnLoc = spawnLocations[i];
            //rc.setIndicatorString(spawnLoc.toString());
            //rc.setIndicatorString("Tryna build a boy at " + spawnLoc);
            if (rc.canBuildRobot(robotType, spawnLoc)) {
                rc.buildRobot(robotType, spawnLoc);
                //rc.setIndicatorString("Built " + robotType + " at " + spawnLoc);
                if (robotType == RobotType.LAUNCHER) {
                    attackersThisHqHasBuilt++;
                } else {
                    carriersThisHqHasBuilt++;
                }
                break;
            }
//            if (i+1 == spawnLocations.length) {
//                //no attacker was built with those supplied MapLocations
//                rc.setIndicatorString("Tried building " + robotType + " but couldn't");
//            }
        }
        if (rc.isActionReady()) {
            //rc.setIndicatorString("Didn't build anything my fault g");
        }
    }

/*
------------------------------------------------------------------------------------------------------------------------
                                        CARRIER FUNCTIONS
------------------------------------------------------------------------------------------------------------------------
*/

    static void scanIslands(RobotController rc) throws GameActionException {
        int[] ids = rc.senseNearbyIslands();
        for (int id : ids) {
            if (rc.senseTeamOccupyingIsland(id) == Team.NEUTRAL) {
                MapLocation[] locs = rc.senseNearbyIslandLocations(id);
                if (locs.length > 0) {
                    islandLocation = locs[0];
                    break;
                }
            }
        }
    }

/*
------------------------------------------------------------------------------------------------------------------------
                                        ATTACKER FUNCTIONS
------------------------------------------------------------------------------------------------------------------------
*/

    /**
     * attacker tries attacking anything around them
     * @param rc RobotController
     * @throws GameActionException from senseNearbyRobots
     */
    static ArrayList<Object> attackerAttackAround(RobotController rc) throws GameActionException {
        ArrayList<Object> output = new ArrayList<Object>();
        attackerAttacked = false;
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        RobotInfo target = null;

        if (enemies.length > 0) {
            // the target is the random first in the array. Over each iteration, if another enemy in the array has
            // qualities that make it higher priority to attack than current target, then switch the target

            // get the first non-hq as our first possible target
            for (RobotInfo enemy : enemies) {
                if (enemy.getType() != RobotType.HEADQUARTERS) {
                    target = enemy;
                    break;
                }
            }

            RobotType targetEnemyRobotType = null;
            int targetEnemyHealth = 0;
            int targetEnemyDistance = 0;

            for (RobotInfo enemy: enemies) {
                // we cannot attack a headquarters!!! So make sure we don't even consider them!!!
                if (enemy.getType() != RobotType.HEADQUARTERS) {
                    // target qualities
                    targetEnemyRobotType = target.getType();
                    targetEnemyHealth = target.getHealth();
                    targetEnemyDistance = target.getLocation().distanceSquaredTo(rc.getLocation());

                    // current enemy to check qualities
                    RobotType checkerEnemyRobotType = enemy.getType();
                    int checkerEnemyHealth = enemy.getHealth();
                    int checkerEnemyDistance = enemy.getLocation().distanceSquaredTo(rc.getLocation());

                    // System.out.println("loc: " + enemy.getLocation() + " health: " + checkerEnemyHealth + " distance: " + checkerEnemyDistance);

                    // 1st priority: LAUNCHERS
                    // if this enemy is a launcher, and the target is not a launcher, switch the target to this enemy
                    if (checkerEnemyRobotType == RobotType.LAUNCHER && targetEnemyRobotType != RobotType.LAUNCHER) {
                        target = enemy;
                    }
                    // 2nd priority: THE LEAST HEALTH
                    // if this enemy has less health than the target, switch target to this enemy
                    else if (checkerEnemyHealth < targetEnemyHealth){
                        target = enemy;
                    }
                    // 3rd priority: THE CLOSEST
                    // if this enemy is closer than the target, switch target to this enemy
                    else if (checkerEnemyDistance < targetEnemyDistance) {
                        target = enemy;
                    }
                }
            }
            if (target != null) {
                rc.setIndicatorString("TARGET: " + targetEnemyRobotType + " AT " + target.getLocation() + " WITH HEALTH: " + targetEnemyHealth + " DISTANCE: " + targetEnemyDistance);
            }
        }
        if (target != null) {
            if (rc.canAttack(target.getLocation())) {
                rc.attack(target.getLocation());
                attackerAttacked = true;
                output.add(target.getLocation());
                output.add(target.getType());
                return output;
            }
        }
        return null;
    }

    static void attackerAttackAroundAndClouds(RobotController rc) throws GameActionException {
        attackerAttacked = false;
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        RobotInfo target = null;

        if (enemies.length > 0) {
            // the target is the random first in the array. Over each iteration, if another enemy in the array has
            // qualities that make it higher priority to attack than current target, then switch the target

            // get the first non-hq as our first possible target
            for (RobotInfo enemy : enemies) {
                if (enemy.getType() != RobotType.HEADQUARTERS) {
                    target = enemy;
                    break;
                }
            }

            RobotType targetEnemyRobotType = null;
            int targetEnemyHealth = 0;
            int targetEnemyDistance = 0;

            for (RobotInfo enemy: enemies) {
                // we cannot attack a headquarters!!! So make sure we don't even consider them!!!
                if (enemy.getType() != RobotType.HEADQUARTERS) {
                    // target qualities
                    targetEnemyRobotType = target.getType();
                    targetEnemyHealth = target.getHealth();
                    targetEnemyDistance = target.getLocation().distanceSquaredTo(rc.getLocation());

                    // current enemy to check qualities
                    RobotType checkerEnemyRobotType = enemy.getType();
                    int checkerEnemyHealth = enemy.getHealth();
                    int checkerEnemyDistance = enemy.getLocation().distanceSquaredTo(rc.getLocation());

                    // System.out.println("loc: " + enemy.getLocation() + " health: " + checkerEnemyHealth + " distance: " + checkerEnemyDistance);

                    // 1st priority: LAUNCHERS
                    // if this enemy is a launcher, and the target is not a launcher, switch the target to this enemy
                    if (checkerEnemyRobotType == RobotType.LAUNCHER && targetEnemyRobotType != RobotType.LAUNCHER) {
                        target = enemy;
                    }
                    // 2nd priority: THE LEAST HEALTH
                    // if this enemy has less health than the target, switch target to this enemy
                    else if (checkerEnemyHealth < targetEnemyHealth){
                        target = enemy;
                    }
                    // 3rd priority: THE CLOSEST
                    // if this enemy is closer than the target, switch target to this enemy
                    else if (checkerEnemyDistance < targetEnemyDistance) {
                        target = enemy;
                    }
                }
            }
            if (target != null) {
                rc.setIndicatorString("TARGET: " + targetEnemyRobotType + " AT " + target.getLocation() + " WITH HEALTH: " + targetEnemyHealth + " DISTANCE: " + targetEnemyDistance);
            }
        }
        rc.setIndicatorString(String.valueOf(target));
        if (target != null) {
            if (rc.canAttack(target.getLocation())) {
                rc.attack(target.getLocation());
                attackerAttacked = true;
            }
        } else {
            MapLocation[] cloudLocs = rc.senseNearbyCloudLocations(-1);
            ArrayList<MapLocation> randomCloudLocs = new ArrayList<>(Arrays.asList(cloudLocs));
            Collections.shuffle(randomCloudLocs);
            rc.setIndicatorString(String.valueOf(randomCloudLocs.size()));
            for (MapLocation cloudLoc : randomCloudLocs) {
                if (rc.canAttack(cloudLoc)) {
                    rc.attack(cloudLoc);
                    attackerAttacked = true;
                    rc.setIndicatorString("Attacked " + cloudLocs[0]);
                }


//                if (!(cloudLocs[0].equals(rc.getLocation()))) {
//                    if (rc.canAttack(cloudLocs[0])) {
//                        rc.attack(cloudLocs[0]);
//                        attackerAttacked = true;
//                        rc.setIndicatorString("Attacked " + cloudLocs[0]);
//                    }
//                }
            }
        }
    }

    static void moveOppositeDirection(RobotController rc, MapLocation moveAwayFrom) throws GameActionException {
        MapLocation me = rc.getLocation();
        Direction directionToAttacker = me.directionTo(moveAwayFrom);
        Direction straightOpposite = directionToAttacker.rotateRight().rotateRight().rotateRight().rotateRight();
        Direction almostOpposite1 = directionToAttacker.rotateRight().rotateRight().rotateRight();
        Direction almostOpposite2 = directionToAttacker.rotateLeft().rotateLeft().rotateLeft();

        if (rc.canMove(straightOpposite)) rc.move(straightOpposite);
        else if (rc.canMove(almostOpposite1)) rc.move(almostOpposite1);
        else if (rc.canMove(almostOpposite2)) rc.move(almostOpposite2);
    }


    /**
     * moves attacker towards a location
     * @param rc RobotController
     * @param me current location
     * @throws GameActionException from move()
     */
    static void attackerMoveToLocation(RobotController rc, MapLocation me) throws GameActionException {
        if (rc.canSenseLocation(attackerIsAttackingThisLocation)) {
            //rc.setIndicatorString("here1");
            RobotInfo enemyHq = rc.senseRobotAtLocation(attackerIsAttackingThisLocation);
            //System.out.println("enemyHq = " + enemyHq);
            //System.out.println("attacking this location: " + attackerIsAttackingThisLocation);
            if (enemyHq != null) {
                //rc.setIndicatorString("" + String.valueOf(rc.senseRobotAtLocation(attackerIsAttackingThisLocation).getType() == RobotType.HEADQUARTERS) + " | " + me.isWithinDistanceSquared(attackerIsAttackingThisLocation, enemyHq.getType().actionRadiusSquared));
                if ((enemyHq.getType() == RobotType.HEADQUARTERS) && (me.isWithinDistanceSquared(attackerIsAttackingThisLocation, (enemyHq.getType().actionRadiusSquared + 12)))) {
                    //rc.setIndicatorString("here2");
                    //if (me.isWithinDistanceSquared(attackerIsAttackingThisLocation, enemyHq.getType().actionRadiusSquared)) {
                    //if (attackerIsAttackingThisLocation.isWithinDistanceSquared(me, enemyHq.getType().actionRadiusSquared + 1)) {
                    // vibe right outside the range of the attacking hq
                    //rc.setIndicatorString("here3");
                    //System.out.println("here");
                    //circleLocationBeingAttacked(rc, me, attackerIsAttackingThisLocation);
                    //}
                    if ((me.isWithinDistanceSquared(attackerIsAttackingThisLocation, (enemyHq.getType().actionRadiusSquared)))) {
                        moveOppositeDirection(rc, attackerIsAttackingThisLocation);
                    }
                } else {
                    moveToThisLocation(rc, attackerIsAttackingThisLocation);
                }
            }
        } else {
            moveToThisLocation(rc, attackerIsAttackingThisLocation);
        }
    }

    static void circleLocationBeingAttacked(RobotController rc, MapLocation me, MapLocation locationToSwarm) throws GameActionException {
        Direction directionToAttacker = me.directionTo(locationToSwarm);
        Direction straightLeft = directionToAttacker.rotateLeft().rotateLeft();
        Direction backLeft = directionToAttacker.rotateLeft().rotateLeft().rotateLeft();
        rc.setIndicatorString("" + straightLeft + " | " + backLeft);
        if (rc.canMove(straightLeft)) rc.move(straightLeft);
        else if (rc.canMove(backLeft)) rc.move(backLeft);
        //if (rc.canMove(backLeft)) rc.move(backLeft);
    }

    /**
     * Sets scoutResultFromPossibleHqLocation true/false based on if we got to a possible hq location, if there actually
     * was a hq there or not. Sets scoutReturningHome to true if we found a possible
     * @param rc RobotController
     * @throws GameActionException from reading array and sensing location
     */
    static void checkIfWeSeeAHqOnOurTravels(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();
        if (rc.readSharedArray(symIndex) == 0) {
            // if symIndex is 0, then enemy hq positions are NOT located, so we need to do some scouting
            if (me.isWithinDistanceSquared(attackerIsAttackingThisLocation, RobotType.LAUNCHER.visionRadiusSquared)) {
                // if we are at a hq location, and it's only a possible hq location, we need to report our findings
                // back to a hq, so we can guess the symmetry and know for sure where the other hqs are
                if (rc.canSenseLocation(attackerIsAttackingThisLocation)) {
                    rc.senseRobotAtLocation(attackerIsAttackingThisLocation);
                    RobotInfo possibleHq = rc.senseRobotAtLocation(attackerIsAttackingThisLocation);
                    if (possibleHq == null) {
                        scoutResultFromPossibleHqLocation = false;
                    } else {
                        if ((possibleHq.getType() == RobotType.CARRIER) || (possibleHq.getType() == RobotType.LAUNCHER)) {
                            // no hq here
                            scoutResultFromPossibleHqLocation = false;
                        } else {
                            // hq here
                            scoutResultFromPossibleHqLocation = true;
                        }
                    }
                    // now we want to go back to the closest hq
                    thisAttackersMapLocationTheyScouted = attackerIsAttackingThisLocation;
                    scoutReturningHome = true;
                    rc.setIndicatorString("Returning home");
                }
            }
        }
    }

    static void didScoutMakeItHome(RobotController rc, MapLocation hqPos, MapLocation me) throws GameActionException {
        if (hqPos.isWithinDistanceSquared(me, GameConstants.DISTANCE_SQUARED_FROM_HEADQUARTER)) {
            //rc.setIndicatorString("in range of hq, but cant write.....");
            //System.out.println("Attacker sees " + rc.readSharedArray(scoutedEnemyHqLocationIndex));
            if ((rc.readSharedArray(scoutedEnemyHqLocationIndex) == 0) && (rc.readSharedArray(scoutedEnemyHqLocationIndex) != hqsStillProcessingAttackerInfo)) {
                // only write what u got if hqs aren't currently calculating another attackers findings
                if (rc.canWriteSharedArray(scoutedEnemyHqLocationIndex, locationToInt(rc, thisAttackersMapLocationTheyScouted))) {
                    //rc.setIndicatorString("MADE IT HOME, depositing information to the array");
                    rc.writeSharedArray(scoutedEnemyHqLocationIndex, locationToInt(rc, thisAttackersMapLocationTheyScouted));
                    rc.writeSharedArray(hqOrNotIndex, booleanToInt(scoutResultFromPossibleHqLocation));
                    scoutReturningHome = false;
                    System.out.println("\ni wrote " + thisAttackersMapLocationTheyScouted + " to the array and now not returning home\n");
                }
            }
        }
    }
}

