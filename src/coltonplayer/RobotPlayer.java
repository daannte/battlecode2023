package coltonplayer;

import battlecode.common.*;

import java.awt.*;
import java.util.*;

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
    static int carriersThisHqHasBuilt = 0;
    static int attackersThisHqHasBuilt = 0;
    static ArrayList<MapLocation> coordsOfHqs = new ArrayList<MapLocation>();
    static ArrayList<MapLocation> possibleCoordsOfEnemyHqsAlwaysThree = new ArrayList<MapLocation>();
    static ArrayList<MapLocation> possibleCoordsOfEnemyHqs = new ArrayList<MapLocation>();
    static ArrayList<MapLocation> theActualEnemyHqCoords = new ArrayList<MapLocation>();
    static int amountOfLaunchersWhoChoseAHq = 0;
    static boolean enemyHqCoordsLocated = false;
    static MapLocation possibleEnemyHqFromVSym;
    static MapLocation possibleEnemyHqFromHSym;
    static MapLocation possibleEnemyHqFromRSym;
    static String theSymmetryIs = null;
    static int amountOfHqsThisHqKnows;
    static int amountOfHqsInThisGame;
    static MapLocation middlePos = null;
    //static boolean launcherBeenToMiddle = false;
    static boolean isScout = false;
    static int attackerIncrementer = 0;
    static MapLocation attackerIsAttackingThisThing;

    /**
     * KEEPING TRACK OF WHAT'S IN THE SHARED ARRAY
     * [       0-8       ,   9   ,           10-17             ,    18    ,        19           ,          20-63                              ]
     *      hq coords     #ofhqs       enemy hq coords      #ofenemyhqsInArray  AtckHQEvenlyCounter
     */
    static int numOfEnemyHqsInArray = 0;


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
    static void runHeadquarters(RobotController rc) throws GameActionException {
        // takes up 3-9 spots in the shared array (depending on how many hq's)
        MapLocation me = rc.getLocation();
        if (turnCount == 1) {
            rc.setIndicatorString("Its turn 1 baby");
            // turn 1 shenanigans
            // put this hq into the array with our hq's
            int indicator = rc.readSharedArray(0);
            if (indicator == 0) {
                rc.writeSharedArray(9, rc.getRobotCount());
            }
            rc.writeSharedArray(indicator + 1, me.x);
            rc.writeSharedArray(indicator + 2, me.y);
            rc.writeSharedArray(0, indicator + 2);

            // get x,y coords of the middle of the map
            middlePos = new MapLocation((int) Math.round((double) rc.getMapWidth() / 2), (int) Math.round((double) rc.getMapWidth() / 2));

            int width = rc.getMapWidth();
            int halfWidth = (width / 2);
            int height = rc.getMapHeight();
            int halfHeight = (height / 2);

            // this block puts the three possible locations the enemy hq can be based on its position into a list
            //horizontal possible loc
            possibleEnemyHqFromVSym = new MapLocation((width - me.x) - 1 , me.y);
            //vertical possible loc
            possibleEnemyHqFromHSym = new MapLocation(me.x , (height - me.y) - 1);
            //rotational possible loc
            possibleEnemyHqFromRSym = new MapLocation((width - me.x) - 1 , (height - me.y) - 1);
            possibleCoordsOfEnemyHqs.add(possibleEnemyHqFromVSym);
            possibleCoordsOfEnemyHqsAlwaysThree.add(possibleEnemyHqFromVSym);
            possibleCoordsOfEnemyHqs.add(possibleEnemyHqFromHSym);
            possibleCoordsOfEnemyHqsAlwaysThree.add(possibleEnemyHqFromHSym);
            possibleCoordsOfEnemyHqs.add(possibleEnemyHqFromRSym);
            possibleCoordsOfEnemyHqsAlwaysThree.add(possibleEnemyHqFromRSym);

            amountOfHqsThisHqKnows = rc.readSharedArray(0) / 2;
            amountOfHqsInThisGame = rc.readSharedArray(9);
            //System.out.println("rc.getRobotCount() returns: " + rc.getRobotCount() + "\n");


            // if equal, that means this hq was the last to add its coords to the shared array. Meaning, it has all the
            // necessary information to GUESS which symmetry the map may be in.


            rc.setIndicatorString(amountOfHqsThisHqKnows + " | " + amountOfHqsInThisGame);
            if (amountOfHqsThisHqKnows == amountOfHqsInThisGame) {
                rc.setIndicatorString("This hq is the last to add its coords to the array");
                //this block uses all of our hqs to guess the symmetry
                for (int i = 0; i < amountOfHqsThisHqKnows; i++) {
                    int ind = i * 2;
                    MapLocation Hq = new MapLocation(rc.readSharedArray(ind + 1), rc.readSharedArray(ind + 2));
                    coordsOfHqs.add(Hq);
                }

                //plan: calculate symmetry for some maps, then use that map to make bots split evenly to enemy hqs

                //symmetry only has a chance to be calculated if # of hqs > 1
                if (amountOfHqsThisHqKnows > 1) {
                    // calculate the symmetry of the map
                    // use first hq in list and compare against remaining in list
                    MapLocation originalCheckerHqPos = coordsOfHqs.get(0);

                    boolean notVerticalSymmetry = false;
                    boolean notHorizontalSymmetry = false;
                    boolean notRotationalSymmetry = false;
                    for (int i = 1; i < coordsOfHqs.size(); i++) {
                        MapLocation currentCheckerHqPos = coordsOfHqs.get(i);
                        //see if it fails being a vertical symmetry

                        if (((originalCheckerHqPos.x < middlePos.x) && (currentCheckerHqPos.x > middlePos.x)) || (originalCheckerHqPos.x > middlePos.x) && (currentCheckerHqPos.x < middlePos.x)) {
                            //these two hqs are on opposite sides of the vertical symmetry line, so it CANNOT BE VERTICAL SYMMETRY
                            notVerticalSymmetry = true;
                            //remove the enemy hq calculated with vertical symmetry from the possible enemy hq spots
                            possibleCoordsOfEnemyHqs.remove(possibleEnemyHqFromVSym);
                        }
                        if (((originalCheckerHqPos.y < middlePos.y) && (currentCheckerHqPos.y > middlePos.y)) || ((originalCheckerHqPos.y > middlePos.y) && (currentCheckerHqPos.y < middlePos.y))) {
                            //these two hqs are on opposite sides of the vertical symmetry line, so it CANNOT BE VERTICAL SYMMETRY
                            notHorizontalSymmetry = true;
                            //remove the enemy hq calculated with vertical symmetry from the possible enemy hq spots
                            possibleCoordsOfEnemyHqs.remove(possibleEnemyHqFromHSym);
                        }
                        if (false) {
                            // uhhhhh so how do eliminate rotational symmetry as an option
                            notRotationalSymmetry = true;
                            possibleCoordsOfEnemyHqs.remove(possibleEnemyHqFromRSym);
                        }
                    }

                    // if the length of possibleCoordsOfEnemyHqs is 1, then we know the mf symmetry baby
                    rc.setIndicatorString("SIZE: " + possibleCoordsOfEnemyHqs.size() + " Thing: " + possibleCoordsOfEnemyHqs.get(0));
                    if (possibleCoordsOfEnemyHqs.size() == 1) {
                        MapLocation theFabledLocation = possibleCoordsOfEnemyHqs.get(0);
                        rc.setIndicatorDot(theFabledLocation, 0, 255, 0);
                        if (notHorizontalSymmetry && notVerticalSymmetry) theSymmetryIs = "R";
                        if (notVerticalSymmetry && notRotationalSymmetry) theSymmetryIs = "H";
                        if (notRotationalSymmetry && notHorizontalSymmetry) theSymmetryIs = "V";
                    } else {
                        // these indicators are what the hq's have narrowed the possible enemy hq locations to
                        for (MapLocation possibleCoordsOfEnemyHq : possibleCoordsOfEnemyHqs) {
                            rc.setIndicatorDot(possibleCoordsOfEnemyHq, 0, 0, 255);
                            //System.out.println("Indicator dot placed");
                        }
                        //rc.setIndicatorString("I wrote my dots");
                    }
                }
            }
        }
        if (turnCount == 2) {
            //this will now just always be how many hqs there actually are
            amountOfHqsThisHqKnows = rc.readSharedArray(0) / 2;
            //every hq other than the last one now needs to use symmetry to try to narrow options
            //this block uses all of our hqs to guess the symmetry
            for (int i = 0; i < amountOfHqsThisHqKnows; i++) {
                int ind = i * 2;
                MapLocation Hq = new MapLocation(rc.readSharedArray(ind + 1), rc.readSharedArray(ind + 2));
                coordsOfHqs.add(Hq);
            }

            //symmetry only has a chance to be calculated if # of hqs > 1
            if (amountOfHqsThisHqKnows > 1) {
                // calculate the symmetry of the map
                // use first hq in list and compare against remaining in list
                MapLocation originalCheckerHqPos = coordsOfHqs.get(0);

                boolean notVerticalSymmetry = false;
                boolean notHorizontalSymmetry = false;
                boolean notRotationalSymmetry = false;
                for (int i = 1; i < coordsOfHqs.size(); i++) {
                    MapLocation currentCheckerHqPos = coordsOfHqs.get(i);
                    //see if it fails being a vertical symmetry

                    if (((originalCheckerHqPos.x < middlePos.x) && (currentCheckerHqPos.x > middlePos.x)) || (originalCheckerHqPos.x > middlePos.x) && (currentCheckerHqPos.x < middlePos.x)) {
                        //these two hqs are on opposite sides of the vertical symmetry line, so it CANNOT BE VERTICAL SYMMETRY
                        notVerticalSymmetry = true;
                        //remove the enemy hq calculated with vertical symmetry from the possible enemy hq spots
                        possibleCoordsOfEnemyHqs.remove(possibleEnemyHqFromVSym);
                    }
                    if (((originalCheckerHqPos.y < middlePos.y) && (currentCheckerHqPos.y > middlePos.y)) || ((originalCheckerHqPos.y > middlePos.y) && (currentCheckerHqPos.y < middlePos.y))) {
                        //these two hqs are on opposite sides of the horizontal symmetry line, so it CANNOT BE HORIZONTAL SYMMETRY
                        notHorizontalSymmetry = true;
                        //remove the enemy hq calculated with horizontal symmetry from the possible enemy hq spots
                        possibleCoordsOfEnemyHqs.remove(possibleEnemyHqFromHSym);
                    }

                    if (true == false) {
                        // uhhhhh so how do eliminate rotational symmetry as an option
                        notRotationalSymmetry = true;
                        possibleCoordsOfEnemyHqs.remove(possibleEnemyHqFromRSym);
                    }
                }

                // if the length of possibleCoordsOfEnemyHqs is 1, then we know the mf symmetry baby
                rc.setIndicatorString("SIZE: " + possibleCoordsOfEnemyHqs.size() + " Thing: " + possibleCoordsOfEnemyHqs.get(0));
                if (possibleCoordsOfEnemyHqs.size() == 1) {
                    MapLocation theFabledLocation = possibleCoordsOfEnemyHqs.get(0);
                    rc.setIndicatorDot(theFabledLocation, 0, 255, 0);
                    if (notHorizontalSymmetry && notVerticalSymmetry) theSymmetryIs = "R";
                    if (notVerticalSymmetry && notRotationalSymmetry) theSymmetryIs = "H";
                    if (notRotationalSymmetry && notHorizontalSymmetry) theSymmetryIs = "V";
                } else {
                    // these indicators are what the hq's have narrowed the possible enemy hq locations to
                    for (MapLocation possibleCoordsOfEnemyHq : possibleCoordsOfEnemyHqs) {
                        rc.setIndicatorDot(possibleCoordsOfEnemyHq, 0, 0, 255);
                        //System.out.println("Indicator dot placed");
                    }
                    //rc.setIndicatorString("I wrote my dots");
                }
            }
        }
        if (turnCount == 3) {
            //theSymmetryIs, is either "V", "R", "H" if it was found, null if not
            if (theSymmetryIs != null) {
                // oh my god we found the map symmetry just off of hq math lmaooooo
                // here, each hq will add its enemy hq counterpart, we just gotta use the known map symmetry to figure
                // out which of the three it is, and then we add it
                if (theSymmetryIs.equals("H")) {
                    theActualEnemyHqCoords.add(possibleCoordsOfEnemyHqsAlwaysThree.get(0));
                } else if (theSymmetryIs.equals("V")) {
                    theActualEnemyHqCoords.add(possibleCoordsOfEnemyHqsAlwaysThree.get(1));
                } else if (theSymmetryIs.equals("R")) {
                    theActualEnemyHqCoords.add(possibleCoordsOfEnemyHqsAlwaysThree.get(2));
                }
                numOfEnemyHqsInArray = rc.readSharedArray(18);
                for (MapLocation anActualEnemyHqCoords : theActualEnemyHqCoords) {
                    rc.writeSharedArray(((numOfEnemyHqsInArray*2)+10), anActualEnemyHqCoords.x);
                    rc.writeSharedArray(((numOfEnemyHqsInArray*2)+10)+1, anActualEnemyHqCoords.y);
                    rc.writeSharedArray(18, numOfEnemyHqsInArray+1);
                }

                //this if is always true if code reaches here but added for context
                if (theActualEnemyHqCoords.size() > 0) {
                    enemyHqCoordsLocated = true;
                }
            }
        }


        middlePos = new MapLocation((int) Math.round( (double) rc.getMapWidth() / 2), (int) Math.round( (double) rc.getMapWidth() / 2));
        // Pick a direction to build in.
        MapLocation[] attackerSpawnLocs = {
                me.add(me.directionTo(middlePos)),
                me.add(me.directionTo(middlePos).rotateRight()),
                me.add(me.directionTo(middlePos).rotateLeft()),
                me.add(me.directionTo(middlePos).rotateRight().rotateRight()),
                me.add(me.directionTo(middlePos).rotateLeft().rotateLeft()),
        };

        MapLocation[] carrierSpawnLocs = new MapLocation[directions.length];
        for (int i = 0; i < directions.length; i++) {
            Direction direction = directions[i];
            carrierSpawnLocs[i] = me.add(direction);
        }

        if (attackersThisHqHasBuilt < 3) {
            //build an attacker
            spawnADude(rc, attackerSpawnLocs, RobotType.LAUNCHER);

        } else if (carriersThisHqHasBuilt < 4) {
            //build a carrier
            spawnADude(rc, carrierSpawnLocs, RobotType.CARRIER);
        } else {
            //build one at random
            if (rng.nextBoolean()) {
                spawnADude(rc, attackerSpawnLocs, RobotType.LAUNCHER);

            } else {
                spawnADude(rc, carrierSpawnLocs, RobotType.CARRIER);
            }
        }






//        //MapLocation attackerSpawnLocation = me.add(me.directionTo(middlePos)).add(me.directionTo(middlePos));
//        MapLocation attackerSpawnLocation = me.add(me.directionTo(middlePos));
//
//        rc.setIndicatorString(attackerSpawnLocation.toString());
//        MapLocation carrierSpawnLocationClose = attackerSpawnLocation;
//        MapLocation carrierSpawnLocationFar = attackerSpawnLocation;
//
//        WellInfo[] wells = rc.senseNearbyWells(RobotType.HEADQUARTERS.visionRadiusSquared);
//        if (wells.length > 0) {
////            for (WellInfo well : wells) {
////                Direction closestToWell = me.directionTo(well.getMapLocation());
////                carrierSpawnLocationClose = me.add(closestToWell);
////                carrierSpawnLocationFar = carrierSpawnLocationClose.add(closestToWell);
////            }
//            carrierSpawnLocationClose = me.add(me.directionTo(wells[0].getMapLocation()));
//            carrierSpawnLocationFar = carrierSpawnLocationClose.add(me.directionTo(wells[0].getMapLocation()));
//        }
//        if (turnCount <= 3) {
//            Direction dir = me.directionTo(middlePos);
//            Direction[] moveDirs = new Direction[5];
//            moveDirs[0] = dir;
//            moveDirs[1] = dir.rotateRight();
//            moveDirs[2] = dir.rotateLeft();
//            if (rc.canBuildRobot(RobotType.LAUNCHER, attackerSpawnLocation)){
//                rc.buildRobot(RobotType.LAUNCHER, attackerSpawnLocation);
//                rc.setIndicatorString("Spawned attacker at " + attackerSpawnLocation);
//                // increment the AttackHqEvenlyCounter
//                rc.writeSharedArray(18, rc.readSharedArray(18) + 1);
//            }
//        } else if (turnCount <= 7) {
//            if (rc.canBuildRobot(RobotType.CARRIER, carrierSpawnLocationFar)) {
//                rc.buildRobot(RobotType.CARRIER, carrierSpawnLocationFar);
//            } else if (rc.canBuildRobot(RobotType.CARRIER, carrierSpawnLocationClose)) {
//                rc.buildRobot(RobotType.CARRIER, carrierSpawnLocationClose);
//            }
//        } else {
//            if (rc.canBuildRobot(RobotType.LAUNCHER, attackerSpawnLocation)) {
//                rc.buildRobot(RobotType.LAUNCHER, attackerSpawnLocation);
//                rc.setIndicatorString("Spawned attacker at " + attackerSpawnLocation);
//                // increment the AttackHqEvenlyCounter
//                rc.writeSharedArray(18, rc.readSharedArray(18) + 1);
//            }
//            else if (rc.canBuildRobot(RobotType.CARRIER, carrierSpawnLocationFar)) {
//                rc.buildRobot(RobotType.CARRIER, carrierSpawnLocationFar);
//            } else if (rc.canBuildRobot(RobotType.CARRIER, carrierSpawnLocationClose)) {
//                rc.buildRobot(RobotType.CARRIER, carrierSpawnLocationClose);
//            }
//        }

        System.out.println("here");
        ArrayList<String> arrayContents= new ArrayList<String>();
        int counter = 0;
        while (counter < GameConstants.SHARED_ARRAY_LENGTH) {
            arrayContents.add(String.valueOf(rc.readSharedArray(counter)));
            counter++;
        }

        System.out.println("also here");
        System.out.println(arrayContents);

    }

    /**
     * Run a single turn for a Carrier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runCarrier(RobotController rc) throws GameActionException {
        middlePos = new MapLocation((int) Math.round( (double) rc.getMapWidth() / 2), (int) Math.round( (double) rc.getMapWidth() / 2));
        // System.out.println(middlePos);
        MapLocation me = rc.getLocation();
        if (turnCount == 1) {
            int amountOfHqs = rc.readSharedArray(0) / 2;
            for (int i = 0; i < amountOfHqs; i++) {
                int ind = i*2;
                MapLocation Hq = new MapLocation(rc.readSharedArray(ind+1), rc.readSharedArray(ind+2));
                coordsOfHqs.add(Hq);
            }
        }
        if (rc.getAnchor() != null) {
            // If I have an anchor singularly focus on getting it to the first island I see
            int[] islands = rc.senseNearbyIslands();
            Set<MapLocation> islandLocs = new HashSet<>();
            for (int id : islands) {
                MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
                islandLocs.addAll(Arrays.asList(thisIslandLocs));
            }
            if (islandLocs.size() > 0) {
                MapLocation islandLocation = islandLocs.iterator().next();
                rc.setIndicatorString("Moving my anchor towards " + islandLocation);
                while (!rc.getLocation().equals(islandLocation)) {
                    Direction dir = rc.getLocation().directionTo(islandLocation);
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                }
                if (rc.canPlaceAnchor()) {
                    rc.setIndicatorString("Huzzah, placed anchor!");
                    rc.placeAnchor();
                }
            }
        }

        // Occasionally try out the carriers attack
//        if (rng.nextInt(20) == 1) {
//            RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
//            if (enemyRobots.length > 0) {
//                if (rc.canAttack(enemyRobots[0].location)) {
//                    rc.attack(enemyRobots[0].location);
//                }
//            }
//        }
        
        // If we can see a well, move towards it
        int amountOfAdamantium = rc.getResourceAmount(ResourceType.ADAMANTIUM);
        int amountOfMana = rc.getResourceAmount(ResourceType.MANA);

        boolean dontMove = false;
        if ((amountOfAdamantium + amountOfMana) < 40) {
            // needa find a well
            // rc.setIndicatorString("Tried to sense a well near me and move to it");
            WellInfo[] wells = rc.senseNearbyWells();
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
                    // Try to gather from squares around us.
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            MapLocation wellLocation = new MapLocation(me.x + dx, me.y + dy);
                            if (rc.canCollectResource(wellLocation, closestWell.getRate())) {
                                rc.collectResource(wellLocation, closestWell.getRate());
                                rc.setIndicatorString("Collecting, now have, AD:" +
                                        rc.getResourceAmount(ResourceType.ADAMANTIUM) +
                                        " MN: " + rc.getResourceAmount(ResourceType.MANA) +
                                        " EX: " + rc.getResourceAmount(ResourceType.ELIXIR));
                            }
                        }
                    }
                } else {
                    // if we aren't adjacent to the well, we can't collect, so move to it
                    Direction dir = me.directionTo(closestWellLoc);
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                        rc.setIndicatorString("Closest well at " + closestWellLoc + " , im omw by moving " + dir);
                    }
                    if (amountOfAdamantium + amountOfMana == 0) {
                        //empty dudes can move a second time hehe
                        Direction dir2 = me.directionTo(closestWellLoc);
                        if (rc.canMove(dir2)) {
                            rc.move(dir2);
                            rc.setIndicatorString("Closest well at " + closestWellLoc + " , im omw by moving " + dir2);
                        }
                    }
                }
            }
        }
        else {
            // we have max we can carry, go back to the (closest) hq and deposit!
            //System.out.println(coordsOfHqs);
            MapLocation hqPos = coordsOfHqs.get(0);
            for (MapLocation hqCoords : coordsOfHqs) {
                if (me.distanceSquaredTo(hqCoords) < me.distanceSquaredTo(hqPos)) {
                    hqPos = hqCoords;
                }
            }

            Direction dirToHq = me.directionTo(hqPos);
            if (rc.getLocation().isAdjacentTo(hqPos)) {
                for (ResourceType resource: ResourceType.values()) {
                    // System.out.println("" + resource + "");
                    if (rc.canTransferResource(hqPos, resource, rc.getResourceAmount(resource))) {
                        rc.transferResource(hqPos, resource, rc.getResourceAmount(resource));
                        rc.setIndicatorString("Transferred " + resource + " to " + hqPos);
                    }
                }
            } else if (rc.canMove(dirToHq)) {
                rc.move(dirToHq);
                rc.setIndicatorString("Moving " + dirToHq + " to get to " + hqPos);
            } else {

            }
        }

        // move randomly if we want to move but couldn't find a valid spot
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir) && !dontMove) {
            rc.move(dir);
            //rc.setIndicatorString("Moving " + dir);
        }
    }

    /**
     * Run a single turn for a Launcher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runLauncher(RobotController rc) throws GameActionException {
        middlePos = new MapLocation((int) Math.round( (double) rc.getMapWidth() / 2), (int) Math.round( (double) rc.getMapWidth() / 2));
        amountOfHqsInThisGame = rc.readSharedArray(9);

        if (rc.readSharedArray(18) != 0) {
            enemyHqCoordsLocated = true;
        }
        if (turnCount == 1) {
            //middlePos = new MapLocation((int) Math.round( (double) rc.getMapWidth() / 2), (int) Math.round( (double) rc.getMapWidth() / 2));
            rc.writeSharedArray(19, rc.readSharedArray(19) + 1);
        }
        int modNumber = amountOfHqsInThisGame;
        // get the AttackHqEvenlyCounter, and mod it by how many enemy hqs there are
        int attackHqEvenlyCounter = rc.readSharedArray(19) % modNumber;
        MapLocation me = rc.getLocation();

        // on turn 3, attackers can go to a hq if we know symmetry
        //rc.setIndicatorString(String.valueOf(rc.readSharedArray(10)));

        int ohBoy = rng.nextInt(69420) % modNumber;
        if ((rc.getRoundNum() == 3) || (turnCount == 1)) {
            if (enemyHqCoordsLocated) {
                int enemyHqX = (ohBoy * 2) + 10;
                int enemyHqY = ((ohBoy * 2) + 1) + 10;
                // enemy hq locked and loaded to be targeted
                attackerIsAttackingThisThing = new MapLocation(rc.readSharedArray(enemyHqX), rc.readSharedArray(enemyHqY));
            }
        }


        // can you attack? then attack!
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                //rc.setIndicatorString("Merked this poor disabled kid");
                rc.attack(toAttack);
            }
        }

        // handle movement
        rc.setIndicatorString(String.valueOf(enemyHqCoordsLocated));
        if (attackerIsAttackingThisThing != null) {
            rc.setIndicatorString("attacking hq " + attackerIsAttackingThisThing + " because " + ohBoy);
            if (me.isAdjacentTo(attackerIsAttackingThisThing)) {
                //we are right beside the thing we wanna attack, just vibeeeeee
            } else {
                // we aren't by the thing we wanna attack, move to it

                // if we have enemy hq, move to it
                // every attacker from this hq will just beeline its symmetrical enemy hq partner
                Direction dir = me.directionTo(attackerIsAttackingThisThing);
                Direction[] moveDirs = new Direction[5];
                moveDirs[0] = dir;
                moveDirs[1] = dir.rotateRight();
                moveDirs[2] = dir.rotateLeft();
                moveDirs[3] = dir.rotateRight().rotateRight();
                moveDirs[4] = dir.rotateLeft().rotateLeft();

                for (int i = 0; i < moveDirs.length; i++) {
                    Direction moveDir = moveDirs[i];
                    if (rc.canMove(moveDir)) {
                        rc.move(moveDir);
                        break;
                    }
                    if (i == moveDirs.length - 1) {
                        Direction randomDir = directions[rng.nextInt(directions.length)];
                        if (rc.canMove(randomDir)) {
                            rc.move(randomDir);
                            //rc.setIndicatorString("Moving " + dir);
                        }
                    }
                }

            }
        } else {
            // if we don't have anything we wanna attack rn, move randomly
            Direction randomDir = directions[rng.nextInt(directions.length)];
            if (rc.canMove(randomDir)) {
                rc.move(randomDir);
                //rc.setIndicatorString("Moving " + dir);
            }
        }
    }
//        // if we are not at the middle
//        // if (!(me.isAdjacentTo(middlePos)) && !launcherBeenToMiddle) {
//        if (!(me.isAdjacentTo(middlePos))) {
//            // try to move to middle
//            if (rc.canMove(rc.getLocation().directionTo(middlePos))) {
//                rc.move(rc.getLocation().directionTo(middlePos));
//                rc.setIndicatorString("Moving to middle");
//            } else {
//                // try to move randomly.
//                Direction dir = directions[rng.nextInt(directions.length)];
//                if (rc.canMove(dir)) {
//                    rc.setIndicatorString("Tried moving to middle, moving randomly instead");
//                    rc.move(dir);
//                }
//            }
//        } else {
//            // we are near the middle
//            launcherBeenToMiddle = true;
//            // try to move randomly.
//            Direction dir = directions[rng.nextInt(directions.length)];
//            if (rc.canMove(dir)) {
//                rc.setIndicatorString("Moving very randomly");
//                rc.move(dir);
//            }
//        }

    static void spawnADude(RobotController rc, MapLocation[] spawnLocations, RobotType robotType) throws GameActionException {
        for (int i = 0; i < spawnLocations.length; i++) {
            MapLocation spawnLoc = spawnLocations[i];
            rc.setIndicatorString(spawnLoc.toString());
            if (rc.canBuildRobot(robotType, spawnLoc)) {
                rc.buildRobot(robotType, spawnLoc);
                rc.setIndicatorString("Built " + robotType + " at " + spawnLoc);
                if (robotType == RobotType.LAUNCHER) {
                    attackersThisHqHasBuilt++;
                } else {
                    carriersThisHqHasBuilt++;
                }
                break;
            }
            if (i+1 == spawnLocations.length) {
                //no attacker was built with those supplied MapLocations
                rc.setIndicatorString("Tried building " + robotType + " but couldn't");
            }
        }
    }

}

