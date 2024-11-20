

import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.util.Map;

public class Block {

    private int tileX = 0;
    private int tileY = 0;
    public int hitpoints;
    private Direction direction = Direction.NORTH;
    public BufferedImage northImage;
    public BufferedImage southImage;
    public BufferedImage eastImage;
    public BufferedImage westImage;
    public Block pickedBlock;

    public final String type;
    public ArrayList<Block> chunk;
    public BufferedImage image;
    public String blockTypeOnDeath;
    public boolean pickable;
    public String builtIntoType;
    public int damage;
    public String hitSound;
    public BehaviorType behaviorType;
    public StructureType structureType;
    public Map<String, String> upgrades;

    public int moveCooldown = 0;
    public int activityValue;
    public int attackCooldown = 0;

    public Block(int x, int y, String type) {
        this.type = type;
        this.tileX = x;
        this.tileY = y;

        this.hitpoints = DataDictionary.getObjectData(type, "hitpoints", 1);
        this.blockTypeOnDeath = DataDictionary.getObjectData(type, "blockTypeOnDeath", "None");
        this.pickable = DataDictionary.getObjectData(type, "pickable", false);
        this.builtIntoType = DataDictionary.getObjectData(type, "builtIntoType", "None");
        this.damage = DataDictionary.getObjectData(type, "damage", 0);
        this.hitSound = DataDictionary.getObjectData(type, "hitSound", "Hit");

        this.upgrades = (Map<String, String>) DataDictionary.getObjectData(type, "upgrades");

        this.behaviorType = BehaviorType.valueOf(DataDictionary.getObjectData(type, "behavior", "None"));
        if (behaviorType != BehaviorType.None) activityValue = Integer.MIN_VALUE;

        this.structureType = StructureType.valueOf(DataDictionary.getObjectData(type, "structure", "None"));

        this.southImage = DataDictionary.getImage(type + "_S");
        if (this.southImage == null) {
            this.southImage = DataDictionary.getImage(type);
            if (this.southImage == null) {
                this.southImage = DataDictionary.getImage("Error");
            }
        }
        this.northImage = DataDictionary.getImage(type + "_N");
        if (this.northImage == null) {
            this.northImage = southImage;
        }
        this.eastImage = DataDictionary.getImage(type + "_E");
        if (this.eastImage == null) {
            this.eastImage = southImage;
        }
        this.westImage = DataDictionary.getImage(type + "_W");
        if (this.westImage == null) {
            this.westImage = southImage;
        }
        image = southImage;
    }

    public void update() {
        if (this.behaviorType != BehaviorType.None) {
            switch (this.behaviorType) {
                case Wild:
                    if (moveCooldown == 0) {
                        if (Util.randInt(0, 3) == 0) {
                            if (Util.randInt(0, 1) == 0)
                                setDirection(Direction.values()[Util.randInt(0, Direction.values().length)]);
                            walkInDirection(getDirection(), 1);
                        }
                        moveCooldown = Util.MOVE_COOLDOWN;
                    }
                    break;
                case Monster:
                    if (moveCooldown == 0) {
                        if (Util.randInt(0, 3) == 0) {
                            if (Util.randInt(0, 1) == 0)
                                setDirection(Direction.values()[Util.randInt(0, Direction.values().length)]);
                            walkInDirection(getDirection(), 1);
                        }
                        moveCooldown = Util.MOVE_COOLDOWN;
                    }
                    if (attackCooldown == 0) {
                        Block neighborBlock = getNeighborBlock(getDirection());
                        if (neighborBlock != null && neighborBlock.type.equals("Player")) {
                            attack(neighborBlock);
                        }
                        attackCooldown = Util.ATTACK_COOLDOWN;
                    }
                    break;
            }

            if (moveCooldown > 0) moveCooldown--;
            if (attackCooldown > 0) attackCooldown--;
        }
    }

    public void updateChunk() {
        // TODO: IMPROVE LOGISTICS?
        ArrayList<Block> actualChunk = Game.getChunk(getChunkPosition());
        if (actualChunk != chunk) {
            if (this.chunk != null) {
                this.chunk.remove(this);
            }
            this.chunk = actualChunk;
            actualChunk.add(this);
        }
    }

    public void updatePickedBlockPosition() {
        pickedBlock.setTileX(getTileX());
        pickedBlock.setTileY(getTileY());
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public void setTileX(int newX) {
        updateChunk();
        tileX = newX;
    }

    public void setTileY(int newY) {
        updateChunk();
        tileY = newY;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
        switch (direction) {
            case NORTH:
                this.image = this.northImage;
                break;
            case SOUTH:
                this.image = this.southImage;
                break;
            case EAST:
                this.image = this.eastImage;
                break;
            case WEST:
                this.image = this.westImage;
                break;
        }
    }

    public MultipleKey getChunkPosition() {
        MultipleKey pos = new MultipleKey(Math.floorDiv(getTileX(), 16), Math.floorDiv(getTileY(), 16));
        return pos;
    }

    // @NOTE: uses blockPool instead of blocks
    public Block getNeighborBlock(Direction direction) {
        for (Block block : Game.blockPool) {
            switch (direction) {
                case NORTH:
                    if (tileX == block.getTileX() && tileY - 1 == block.getTileY()) return block;
                    break;
                case SOUTH:
                    if (tileX == block.getTileX() && tileY + 1 == block.getTileY()) return block;
                    break;
                case EAST:
                    if (tileX + 1 == block.getTileX() && tileY == block.getTileY()) return block;
                    break;
                case WEST:
                    if (tileX - 1 == block.getTileX() && tileY == block.getTileY()) return block;
                    break;
            }
        }
        return null;
    }

    public void moveTo(Block block, Direction direction, int distance) {
        switch (direction) {
            case NORTH:
                setTileX(block.getTileX());
                setTileY(block.getTileY() - distance);
                break;
            case SOUTH:
                setTileX(block.getTileX());
                setTileY(block.getTileY() + distance);
                break;
            case EAST:
                setTileX(block.getTileX() + distance);
                setTileY(block.getTileY());
                break;
            case WEST:
                setTileX(block.getTileX() - distance);
                setTileY(block.getTileY());
                break;
        }
    }

    public void moveX(int distance) {
        // Determine if and which block collides
        boolean obstructed = false;
        Block collidesWith = null;
        int thisX = getTileX();
        int thisY = getTileY();

        for (Block block : Game.blockPool) {
            if (block == this) continue;
            int blockX = block.getTileX();
            int blockY = block.getTileY();

            // Verify that 'this' block isn't obsctructed by structure of block at own position
            if (!block.pickable && blockX == thisX && blockY == thisY) {
                if (!canGoThroughBlock(block, distance, 0)) {
                    obstructed = true;
                    break;
                }
            }

            // Else verify that 'this' does collide onto block
            if (thisX + distance != blockX || thisY != blockY) {
                continue;
            }

            // Verify that colliding block's structure causes obstruction
            if (canGoThroughBlock(block, distance, 0)) {
                continue;
            }

            // Either pick up object or be obstructed
            if (block.pickable) {
                collidesWith = block;
            } else {
                obstructed = true;
            }
        }

        // Move if not blocked
        if (!obstructed) {
            setTileX(thisX + distance);
            if (collidesWith != null)
                this.pickedBlock = collidesWith;

            // Update picked block position
            if (this.pickedBlock != null)
                updatePickedBlockPosition();
        }
    }

    public void moveY(int distance) {
        // Determine if and which block collides
        boolean obstructed = false;
        Block collidesWith = null;
        int thisX = getTileX();
        int thisY = getTileY();
        for (Block block : Game.blockPool) {
            if (block == this) continue;
            int blockX = block.getTileX();
            int blockY = block.getTileY();

            // Verify that 'this' block isn't obsctructed by structure of block at own position
            if (!block.pickable && blockX == thisX && blockY == thisY) {
                if (!canGoThroughBlock(block, 0, distance)) {
                    obstructed = true;
                    break;
                }
            }

            // Else verify that 'this' does collide onto block
            if (thisX != blockX || thisY + distance != blockY) {
                continue;
            }

            // Verify that colliding block's structure causes obstruction
            if (canGoThroughBlock(block, 0, distance)) {
                continue;
            }

            // Either pick up object or be obstructed
            if (block.pickable) {
                collidesWith = block;
            } else {
                obstructed = true;
            }
        }

        // Move if not blocked
        if (!obstructed) {
            setTileY(thisY + distance);
            if (collidesWith != null)
                this.pickedBlock = collidesWith;

            // Update picked block position
            if (this.pickedBlock != null)
                updatePickedBlockPosition();
        }
    }

    public void walkWest(int distance) {
        if (getDirection() == Direction.WEST) {
            moveX(-distance);
        }
        setDirection(Direction.WEST);
    }

    public void walkEast(int distance) {
        if (getDirection() == Direction.EAST) {
            moveX(distance);
        }
        setDirection(Direction.EAST);
    }

    public void walkNorth(int distance) {
        if (getDirection() == Direction.NORTH) {
            moveY(-distance);
        }
        setDirection(Direction.NORTH);
    }

    public void walkSouth(int distance) {
        if (getDirection() == Direction.SOUTH) {
            moveY(distance);
        }
        setDirection(Direction.SOUTH);
    }

    public void walkInDirection(Direction direction, int distance) {
        switch (direction) {
            case NORTH:
                walkNorth(distance);
                break;
            case SOUTH:
                walkSouth(distance);
                break;
            case EAST:
                walkEast(distance);
                break;
            case WEST:
                walkWest(distance);
                break;
        }
    }

    private boolean canGoThroughBlock(Block block, int moveX, int moveY) {
        switch (block.structureType) {
            case Door:
                return ((block.direction == Direction.EAST || block.direction == Direction.WEST) && moveY == 0) || (block.direction == Direction.NORTH || block.direction == Direction.SOUTH) && (moveX == 0);

        }
        return false;
    }

    public void attack(Block block) {
        // Attack
        if (block.pickable) return;

        block.hitpoints -= damage;

        // Play sound
        Game.playSound(block.hitSound);

    }

    public void dropPickedBlock() {
        if (pickedBlock == null) return;

        pickedBlock.moveTo(this, this.getDirection(), 1);
        Game.selectedBlock = pickedBlock;
        pickedBlock = null;

        // Play sound
        Game.playSound("WoodDrop");
    }
}