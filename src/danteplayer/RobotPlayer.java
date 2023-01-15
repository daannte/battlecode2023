package danteplayer;

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
    static MapLocation headquarter;
    static MapLocation[] headquarterLocations = new MapLocation[GameConstants.MAX_STARTING_HEADQUARTERS];
    static MapLocation islandLoc;
    static MapLocation closestWell;
    static MapLocation middleOfMap;
    static boolean hasAnchor = false;
    static boolean collectAd = true;
    static Direction currentDirection = null;
    static Set<MapLocation> adWells = new HashSet<>();
    static Set<MapLocation> mnWells = new HashSet<>();


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
                    case HEADQUARTERS:     runHeadquarters(rc);  break;
                    case CARRIER:      runCarrier(rc);   break;
                    case LAUNCHER: runLauncher(rc); break;
                    case BOOSTER: // Examplefuncsplayer doesn't use any of these robot types below.
                    case DESTABILIZER: // You might want to give them a try!
                    case AMPLIFIER:       break;
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
        MapLocation hqLocation = rc.getLocation();
        Direction direction = directions[rng.nextInt(directions.length)];
        MapLocation spawnLocation = hqLocation.add(direction);

        if (turnCount == 1) {
            addHqLocations(rc);
            lookForWellType(rc);
        }
        if (turnCount <= 3) spawnRobot(rc, RobotType.LAUNCHER, spawnLocation);
//        if (rc.canBuildAnchor(Anchor.STANDARD) && turnCount > 50) {
//            rc.buildAnchor(Anchor.STANDARD);
//        }
        if (collectAd && !adWells.isEmpty()) {
            spawnRobot(rc, RobotType.CARRIER, hqLocation.add(hqLocation.directionTo(adWells.iterator().next())));
            collectAd = false;
        } else if (!collectAd && !mnWells.isEmpty()) {
            spawnRobot(rc, RobotType.CARRIER, hqLocation.add(hqLocation.directionTo(mnWells.iterator().next())));
            collectAd = true;
        } else {
            spawnRobot(rc, RobotType.CARRIER, spawnLocation);
        }
        spawnRobot(rc, RobotType.LAUNCHER, spawnLocation);
    }

    /**
     * Run a single turn for a Carrier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runCarrier(RobotController rc) throws GameActionException {
        // Get the location of the HQ
        if (turnCount == 1) findHq(rc);
        if (turnCount == 2) updateHqLocations(rc);
        if (rc.canTakeAnchor(headquarter, Anchor.STANDARD)) {
            rc.takeAnchor(headquarter, Anchor.STANDARD);
            hasAnchor = true;
        }
        // If carrier has an anchor go try and place it, else go get resources
        lookForWellType(rc);
        if(closestWell == null) getClosestWell(rc);
        scanIslands(rc);

        if (closestWell != null && rc.canCollectResource(closestWell, -1))
            rc.collectResource(closestWell, -1);
        if (totalResources(rc) == GameConstants.CARRIER_CAPACITY) {
            if (rc.getLocation().isAdjacentTo(headquarter)) {
                for (ResourceType resourceType : ResourceType.values()) {
                    if (rc.canTransferResource(headquarter, resourceType, rc.getResourceAmount(resourceType))) {
                        rc.transferResource(headquarter, resourceType, rc.getResourceAmount(resourceType));
                    }
                }
            } else {
                moveTo(rc, headquarter);
            }
        }

        if (hasAnchor) {
            if (islandLoc == null) moveRandomDir(rc);
            else {
                moveTo(rc, islandLoc);
            }
            if(rc.canPlaceAnchor() && rc.senseTeamOccupyingIsland(rc.senseIsland(rc.getLocation())) == Team.NEUTRAL) {
                rc.placeAnchor();
                hasAnchor = false;
            }
        } else {
            if (totalResources(rc) == 0) {
                if (closestWell == null) moveRandomDir(rc);
                else if (!(rc.getLocation().isAdjacentTo(closestWell))) {
                    moveTo(rc, closestWell);
                }
            }
        }
    }

    /**
     * Run a single turn for a Launcher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runLauncher(RobotController rc) throws GameActionException {
        if (turnCount == 1) updateHqLocations(rc);

        middleOfMap = new MapLocation((int) Math.round( (double) rc.getMapWidth() / 2)
                , (int) Math.round( (double) rc.getMapWidth() / 2));

        Team opponent = rc.getTeam().opponent();
        int radius = rc.getType().actionRadiusSquared;
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        int smallestDistance = 100;
        RobotInfo target = null;
        if (enemies.length > 0) {
            for (RobotInfo enemy: enemies){
                if (enemy.getType() == RobotType.LAUNCHER) {
                    int enemyDistance = enemy.location.distanceSquaredTo(rc.getLocation());
                    if (enemyDistance < smallestDistance) {
                        target = enemy;
                        smallestDistance = enemyDistance;
                    }
                } else target = enemy;
            }
        }
        if (target != null){
            if (rc.canAttack(target.location)) rc.attack(target.location);
        } else {
            moveTo(rc, middleOfMap);
            if (rc.getLocation().equals(middleOfMap)) moveRandomDir(rc);
        }

        RobotInfo[] visibleEnemies = rc.senseNearbyRobots(-1, opponent);
        for (RobotInfo enemy : visibleEnemies) {
            if (enemy.getType() == RobotType.HEADQUARTERS) {
                moveTo(rc, enemy.getLocation());
            }
            else if (enemy.getType() != RobotType.HEADQUARTERS) {
                moveTo(rc, enemy.getLocation());
            }
        }

    }
    /**
     * Spawn a robot on the map
     * @param rc Robot Controller.
     * @param robot The type of robot to spawn.
     * @param location Where to spawn the robot.
     */
    static void spawnRobot(RobotController rc, RobotType robot, MapLocation location) throws GameActionException {
        if(rc.canBuildRobot(robot, location)) rc.buildRobot(robot, location);
        else {
            Direction dir = directions[rng.nextInt(directions.length)];
            MapLocation spawnLocation = rc.getLocation().add(dir);
            if (rc.canBuildRobot(robot, spawnLocation)) rc.buildRobot(robot, spawnLocation);
        }
    }
    /**
     * Make the robot move
     * @param rc Robot Controller
     * @param dir direction that the robot should move in
     */
    static void move(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            if(rc.isMovementReady()) {
                if(rc.canMove(dir)) rc.move(dir);
            }
        }
        else moveRandomDir(rc);
    }
    static void moveRandomDir(RobotController rc) throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        if(rc.canMove(dir)) {
            rc.move(dir);
            if (rc.isMovementReady()) {
                if (rc.canMove(dir)) rc.move(dir);

            }
        }
    }
    static void moveTo(RobotController rc, MapLocation targetDir) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(targetDir);
        if (rc.canMove(dir)) {
            rc.move(dir);
            if(rc.isMovementReady()) {
                if(rc.canMove(dir)) rc.move(dir);
            }
            currentDirection = null;
        } else {
            if (currentDirection == null) currentDirection = dir;
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(currentDirection)) {
                    rc.move(currentDirection);
                    if(rc.isMovementReady()) {
                        if(rc.canMove(currentDirection)) rc.move(currentDirection);
                    }
                    currentDirection = currentDirection.rotateRight();
                    break;
                } else currentDirection = currentDirection.rotateLeft();
            }
        }
    }

    static void scanIslands(RobotController rc) throws GameActionException {
        int[] ids = rc.senseNearbyIslands();
        for(int id : ids) {
            if(rc.senseTeamOccupyingIsland(id) == Team.NEUTRAL) {
                MapLocation[] locs = rc.senseNearbyIslandLocations(id);
                if(locs.length > 0) {
                    islandLoc = locs[0];
                    break;
                }
            }
        }
    }
    static void getClosestWell(RobotController rc) {
        MapLocation me = rc.getLocation();
        WellInfo[] wells = rc.senseNearbyWells();
        if (wells.length >= 1) {
            closestWell = wells[0].getMapLocation();
            for (WellInfo well : wells) {
                if (me.distanceSquaredTo(well.getMapLocation()) < me.distanceSquaredTo(closestWell)) {
                        closestWell = well.getMapLocation();
                }
            }
        }
    }
    static void lookForWellType(RobotController rc) {
        WellInfo[] wells = rc.senseNearbyWells();
        if (wells.length >= 1) {
            for (WellInfo well : wells) {
                if (well.getResourceType() == ResourceType.ADAMANTIUM) adWells.add(well.getMapLocation());
                else mnWells.add(well.getMapLocation());
            }
        }
    }
    static int totalResources(RobotController rc) {
        return rc.getResourceAmount(ResourceType.ADAMANTIUM) + rc.getResourceAmount(ResourceType.MANA)
                + rc.getResourceAmount(ResourceType.ELIXIR);
    }
    static void addHqLocations(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();
        for (int i = 0; i < GameConstants.MAX_STARTING_HEADQUARTERS; i++) {
            if (rc.readSharedArray(i) == 0) {
                if(rc.canWriteSharedArray(i, locationToInt(rc, me))) {
                    rc.writeSharedArray(i, locationToInt(rc, me));
                    break;
                }
            }
        }
    }
    static void updateHqLocations(RobotController rc) throws GameActionException {
        if (RobotPlayer.turnCount == 2) {
            for (int i = 0; i < GameConstants.MAX_STARTING_HEADQUARTERS; i++) {
                headquarterLocations[i] = intToLocation(rc, rc.readSharedArray(i));
                if (rc.readSharedArray(i) == 0) break;
            }
        }
    }
    static int locationToInt(RobotController rc, MapLocation location) {
        if (location == null) return 0;
        return 1 + location.x + location.y * rc.getMapWidth();
    }

    static MapLocation intToLocation(RobotController rc, int integerLocation) {
        if (integerLocation == 0) return null;
        integerLocation--;
        return new MapLocation(integerLocation % rc.getMapWidth(), integerLocation / rc.getMapWidth());
    }

    static void findHq(RobotController rc) {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.getType() == RobotType.HEADQUARTERS && robot.getTeam() == rc.getTeam()) {
                headquarter = robot.getLocation();
                break;
            }
        }
    }
}
