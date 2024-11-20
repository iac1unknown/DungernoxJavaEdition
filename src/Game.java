import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Game {
    public static ArrayList<Block> blocks = new ArrayList<Block>();
    public static ArrayList<Block> blockPool = new ArrayList<Block>();
    public static Map<MultipleKey, ArrayList<Block>> chunks = new HashMap<MultipleKey, ArrayList<Block>>();
    public static Map<MultipleKey, ArrayList<Block>> chunkPool = new HashMap<MultipleKey, ArrayList<Block>>();
    public static Block player;
    public static Block selectedBlock;
    public static Random tileRNG = new Random();
    public static AffineTransform displayTransform = new AffineTransform();
    public static Clip backgroundMusicClip;

    public static Scene scene = Scene.Menu;


    static void init() {
        DataDictionary.init();
        try {
            backgroundMusicClip = AudioSystem.getClip();
        } catch (Exception e) {

        }
        initMenuScene();
    }

    static void update(Graphics2D g, float deltaTime) {
        if (scene == Scene.Menu)
            updateMenuScene(g, deltaTime);
        else if (scene == Scene.Game)
            updateGameScene(g, deltaTime);
    }

    private static void updateMenuScene(Graphics2D g, float deltaTime) {
        // Display menu
        BufferedImage img = DataDictionary.getImage("Menu");
        g.drawImage(img, 0, 0, Main.gameWidth, Main.gameHeight, null);

        // Enter 'Game' scene if key pressed
        if (Input.isKeyPressed(KeyEvent.VK_SPACE)) {
            initMap();
            startBackgroundMusic("BackgroundMusic");
            scene = Scene.Game;
        }
    }

    static void updateGameScene(Graphics2D g, float deltaTime) {
        processPlayerControl();
        updateChunkPool();

        ArrayList<Block> blockPoolCopy = (ArrayList<Block>) blockPool.clone();
        for (Block block : blockPoolCopy) {
            block.update();
            if (block.hitpoints <= 0) {
                String newType = block.blockTypeOnDeath;
                if (!newType.equals("None")) {
                    Block newBlock = new Block(block.getTileX(), block.getTileY(), newType);
                    spawnBlock(newBlock);
                }
                despawnBlock(block);
            }
        }

        // Update camera
        Camera.setPosition(player.getTileX() * 16 - (int) (Main.gameWidth / 2 / Camera.scale),
                player.getTileY() * 16 - (int) (Main.gameHeight / 2 / Camera.scale));


        // Draw background
        g.setColor(new Color(23, 194, 8));
        g.fillRect(0, 0, Main.canvasWidth, Main.canvasHeight);

        // Begin transform
        displayTransform.scale(Camera.scale, Camera.scale);
        displayTransform.translate(-Camera.translationX, -Camera.translationY);
        g.setTransform(displayTransform);

        // Draw blocks
        for (Block block : blockPool) {
            g.drawImage(block.image, (int) block.getTileX() * 16, (int) block.getTileY() * 16, 16, 16, null);

        }

        // Highlight selected block
        if (selectedBlock != null) {
            g.drawImage(DataDictionary.getImage("BlockHighlight"), (int) selectedBlock.getTileX() * 16, (int) selectedBlock.getTileY() * 16, 16, 16, null);

        }

        // Reset Transform
        displayTransform.translate(Camera.translationX, Camera.translationY);
        displayTransform.scale(1 / Camera.scale, 1 / Camera.scale);
        g.setTransform(displayTransform);
    }

    private static void processPlayerControl() {
        // Movement keys
        if (Input.isKeyPressed(KeyEvent.VK_W)) {
            if (player.moveCooldown == 0) {
                player.walkNorth(1);
                player.moveCooldown = Util.moveCooldown;
                selectedBlock = null;
            }
        } else if (Input.isKeyPressed(KeyEvent.VK_S)) {
            if (player.moveCooldown == 0) {
                player.walkSouth(1);
                player.moveCooldown = Util.moveCooldown;
                selectedBlock = null;
            }
        } else if (Input.isKeyPressed(KeyEvent.VK_A)) {
            if (player.moveCooldown == 0) {
                player.walkWest(1);
                player.moveCooldown = Util.moveCooldown;
                selectedBlock = null;
            }
        } else if (Input.isKeyPressed(KeyEvent.VK_D)) {
            if (player.moveCooldown == 0) {
                player.walkEast(1);
                player.moveCooldown = Util.moveCooldown;
                selectedBlock = null;
            }
        }

        // Drop key
        if (Input.isKeyPressed(KeyEvent.VK_X)) {
            if (player.pickedBlock != null) {
                Block block = player.getNeighborBlock(player.getDirection());
                if (block == null)
                    player.dropPickedBlock();

            }
        }

        // Building key
        if (Input.isKeyPressed(KeyEvent.VK_Z)) {
            if (player.attackCooldown == 0) {
                processPlayerBuilding();
            }
        }
    }

    private static void processPlayerBuilding() {
        // attack or build
        Block neighborBlock = player.getNeighborBlock(player.getDirection());
        if (player.pickedBlock == null) {
            if (neighborBlock == null) return;

            player.attack(neighborBlock);
            selectedBlock = neighborBlock;
            player.attackCooldown = Util.attackCooldown;
        } else {
            if (neighborBlock == null) {
                // build new block
                Block builtBlock = spawnBlock(new Block(0, 0, player.pickedBlock.builtIntoType));
                builtBlock.moveTo(player, player.getDirection(), 1);
                builtBlock.setDirection(Util.getOppositeDirection(player.getDirection()));
                despawnBlock(player.pickedBlock);
                player.attackCooldown = 25;

                // play sound
                playSound("Place");
            } else {
                // upgrade block
                Map<String, String> neighborBlockUpgrades = neighborBlock.upgrades;

                if (neighborBlockUpgrades == null) return;

                String newType = neighborBlockUpgrades.get(player.pickedBlock.type);
                Block builtBlock = spawnBlock(new Block(0, 0, newType));
                builtBlock.moveTo(player, player.getDirection(), 1);
                builtBlock.setDirection(Util.getOppositeDirection(player.getDirection()));
                despawnBlock(neighborBlock);
                despawnBlock(player.pickedBlock);
                player.attackCooldown = 25;

                // play sound
                playSound("Place");
            }
        }
    }

    public static void playSound(String name) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(DataDictionary.getSound(name)));
            clip.start();
        } catch (Exception e) {

        }
    }

    private static void initMenuScene() {
        startBackgroundMusic("MenuMusic");
    }

    private static void initMap() {
        ArrayList<Block> chunk = generateChunk(0, 0);
        Block block = chunk.get(0);
        Block playerBlock = new Block(block.getTileX(), block.getTileY(), "Player");
        despawnBlock(block);
        spawnBlock(playerBlock);
        player = playerBlock;
    }

    private static void startBackgroundMusic(String name) {
        try {
            // Load music
            backgroundMusicClip.close();
            backgroundMusicClip.open(AudioSystem.getAudioInputStream(DataDictionary.getSound(name)));

            // Lower clip volume
            FloatControl gainControl = (FloatControl) backgroundMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(20f * (float) Math.log10(0.5f));

            // Play clip
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            while (backgroundMusicClip.isRunning()) {
                Thread.sleep(100);
            }

        } catch (Exception e) {

        }
    }

    private static void loadChunkToPool(MultipleKey chunkPos) {
        if (chunkPool.containsKey(chunkPos)) {
            return;
        }
        ArrayList<Block> chunk = getChunk(chunkPos);
        chunkPool.put(chunkPos, chunk);
        blockPool.addAll(chunk);
    }

    private static void unloadChunkInPool(MultipleKey chunkPos) {
        ArrayList<Block> chunk = getChunk(chunkPos);
        chunkPool.remove(chunkPos);
        blockPool.removeAll(chunk);
    }

    private static void updateChunkPool() {
        MultipleKey playerChunkPos = player.getChunkPosition();
        ArrayList<MultipleKey> unloadableChunksPoses = new ArrayList<MultipleKey>();
        for (MultipleKey chunkPos : chunkPool.keySet()) {
            if (chunkPos.offset(-playerChunkPos.x, -playerChunkPos.y).norm() > 2) {
                unloadableChunksPoses.add(chunkPos);
            }
        }
        for (MultipleKey chunkPos : unloadableChunksPoses) {
            unloadChunkInPool(chunkPos);
        }

        // @NOTE: Could be optimized?
        loadChunkToPool(playerChunkPos);
        loadChunkToPool(playerChunkPos.offset(-1, 0));
        loadChunkToPool(playerChunkPos.offset(1, 0));
        if (player.getTileY() % 16 < 8) {
            loadChunkToPool(playerChunkPos.offset(0, -1));
            loadChunkToPool(playerChunkPos.offset(-1, -1));
            loadChunkToPool(playerChunkPos.offset(1, -1));
        } else {
            loadChunkToPool(playerChunkPos.offset(0, 1));
            loadChunkToPool(playerChunkPos.offset(-1, 1));
            loadChunkToPool(playerChunkPos.offset(1, 1));
        }
    }

    public static ArrayList<Block> getChunk(MultipleKey pos) {
        ArrayList<Block> chunk = chunks.get(pos);
        if (chunk == null) {
            chunk = generateChunk(pos.x, pos.y);
        }
        return chunk;
    }

    private static ArrayList<Block> generateChunk(int chunkX, int chunkY) {
        ArrayList<Block> chunk = new ArrayList<Block>();
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                String type;
                int num = tileRNG.nextInt(230);
                if (num < 50) {
                    type = "Tree";
                } else if (num < 70) {
                    type = "Rock";
                } else if (num == 71) {
                    type = "Monster";
                } else if (num == 72) {
                    type = "Slime";
                } else {
                    type = null;
                }
                if (type != null) {
                    chunk.add(new Block(chunkX * 16 + i, chunkY * 16 + j, type));
                }
            }
        }

        blocks.addAll(chunk);
        MultipleKey pos = new MultipleKey(chunkX, chunkY);
        chunks.put(pos, chunk);
        return chunk;
    }

    private static Block spawnBlock(Block block) {
        blocks.add(block);
        block.updateChunk();
        blockPool.add(block);
        return block;
    }


    private static void despawnBlock(Block block) {
        ArrayList<Block> chunk = chunks.get(block.getChunkPosition());
        chunk.remove(block);
        blocks.remove(block);
        blockPool.remove(block);
        if (Game.selectedBlock == block)
            Game.selectedBlock = null;
        if (player != null && player.pickedBlock == block)
            player.pickedBlock = null;
    }
}
