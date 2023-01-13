package carterplayer;

import battlecode.common.*;

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
    static ArrayList<MapLocation> coordsOfHqs = new ArrayList<MapLocation>();

    /**
     * KEEPING TRACK OF WHAT'S IN THE SHARED ARRAY
     * [       0-8       ,                               10-63                              ]
     *      hq coords
     */

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
        rc.setIndicatorString("Hello world!");

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
            int indicator = rc.readSharedArray(0);
            rc.writeSharedArray(indicator+1, me.x);
            rc.writeSharedArray(indicator+2, me.y);
            rc.writeSharedArray(0, indicator+2);
        }

        // Pick a direction to build in.
        for (Direction direction : directions) {
            MapLocation attackerSpawnLocation = rc.getLocation().add(direction).add(direction);
            MapLocation carrierSpawnLocationClose = attackerSpawnLocation;
            MapLocation carrierSpawnLocationFar = attackerSpawnLocation;

            WellInfo[] wells = rc.senseNearbyWells(-1);
            for (WellInfo well : wells) {
                Direction closestToWell = me.directionTo(well.getMapLocation());
                carrierSpawnLocationClose = me.add(closestToWell);
                carrierSpawnLocationFar = carrierSpawnLocationClose.add(closestToWell);
            }
            if (turnCount <= 3) {
                if (rc.canBuildRobot(RobotType.LAUNCHER, attackerSpawnLocation)){
                    rc.buildRobot(RobotType.LAUNCHER, attackerSpawnLocation);
                    break;
                }
            } else if (turnCount <= 7) {
                if (rc.canBuildRobot(RobotType.CARRIER, carrierSpawnLocationFar)) {
                    rc.buildRobot(RobotType.CARRIER, carrierSpawnLocationFar);
                    break;
                } else if (rc.canBuildRobot(RobotType.CARRIER, carrierSpawnLocationClose)) {
                    rc.buildRobot(RobotType.CARRIER, carrierSpawnLocationClose);
                    break;
                }
            } else {
                if (rc.canBuildRobot(RobotType.LAUNCHER, attackerSpawnLocation)) {
                    rc.buildRobot(RobotType.LAUNCHER, attackerSpawnLocation);
                    break;
                }
                if (rc.canBuildRobot(RobotType.CARRIER, carrierSpawnLocationFar)) {
                    rc.buildRobot(RobotType.CARRIER, carrierSpawnLocationFar);
                    break;
                } else if (rc.canBuildRobot(RobotType.CARRIER, carrierSpawnLocationClose)) {
                    rc.buildRobot(RobotType.CARRIER, carrierSpawnLocationClose);
                    break;
                }
            }
        }
    }

    /**
     * Run a single turn for a Carrier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runCarrier(RobotController rc) throws GameActionException {
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

        MapLocation me = rc.getLocation();
        boolean dontMove = false;
        if ((amountOfAdamantium + amountOfMana) < 40) {
            // needa find a well
            // rc.setIndicatorString("Tried to sense a well near me and move to it");
            WellInfo[] wells = rc.senseNearbyWells();
            if (wells.length > 0) {
                MapLocation closestWellLoc = wells[0].getMapLocation();
                for (WellInfo well : wells) {
                    if (me.distanceSquaredTo(well.getMapLocation()) < me.distanceSquaredTo(closestWellLoc)) {
                        closestWellLoc = well.getMapLocation();
                    }
                }
                if (me.isAdjacentTo(closestWellLoc)) {
                    // if we are close enough to collect, we don't need to move closer, so don't move, and just collect
                    dontMove = true;
                    // Try to gather from squares around us.
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
        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation myLoc = rc.getLocation();
        if (enemies.length > 0) {
            // MapLocation toAttack = enemies[0].location;
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.setIndicatorString("Attacking");        
                rc.attack(toAttack);
            }
            for (RobotInfo r : enemies){
                if (r.type.equals(RobotType.HEADQUARTERS)){
                    dir = myLoc.directionTo(r.location);
                    break;
                }
            }
        }
        // Also try to move randomly.

        if (rc.canMove(dir)) {
            rc.move(dir);//a
        }
        else {
            dir = directions[rng.nextInt(directions.length)];
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        }
    }
}
