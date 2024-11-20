public class Util {
    static final int MOVE_COOLDOWN = 23;
    static final int ATTACK_COOLDOWN = 60;

    static int randInt(int min, int max) {
        return min + (int) (Math.random() * max);
    }

    static Direction getOppositeDirection(Direction direction) {
        switch (direction) {
            case NORTH:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.NORTH;
            case EAST:
                return Direction.WEST;
            case WEST:
                return Direction.EAST;
            default:
                return null;
        }
    }
}
