import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    private static Frame frame;
    public static Canvas canvas;

    public static int canvasWidth = 0;
    public static int canvasHeight = 0;

    public static int gameWidth = 0;
    public static int gameHeight = 0;

    private static GraphicsConfiguration gc;
    private static VolatileImage vImage;

    public static void main(String[] args) {
        gameHeight = 360;
        gameWidth = 480;
        canvasHeight = 600;
        canvasWidth = 800;

        frame = new Frame();
        canvas = new Canvas();

        canvas.setPreferredSize(new Dimension(canvasWidth, canvasHeight));

        frame.add(canvas);
        frame.pack();
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        frame.setTitle("Dungernox Java Edition");

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int newCanvasWidth = frame.getWidth();
                int newCanvasHeight = frame.getHeight();
                canvasWidth = newCanvasWidth;
                canvasHeight = newCanvasHeight;
                canvas.setPreferredSize(new Dimension(canvasWidth, canvasHeight));
            }
        });

        frame.setVisible(true);

        Game.init();

        canvas.addKeyListener(new Input());
        canvas.addMouseListener(new Input());
        canvas.addMouseMotionListener(new Input());

        // Start thread
        Thread thread = new Thread() {
            Timer timer = new Timer();
            public void run() {

                gc = canvas.getGraphicsConfiguration();
                vImage = gc.createCompatibleVolatileImage(gameWidth, gameHeight);
                timer.schedule(new TimerTask() {
                    public void run() {
                        update();
                    }
                }, 0, 10);
            }
        };
        thread.setName("Main Thread");
        thread.start();
    }

    private static void update() {
        // Create graphics
        if (vImage.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
            vImage = gc.createCompatibleVolatileImage(gameWidth, gameHeight);
        }

        Graphics2D g2d = vImage.createGraphics();
        Game.update(g2d, 0.1f);

        g2d.dispose();

        Graphics g = canvas.getGraphics();
        g.drawImage(vImage, 0, 0, canvasWidth, canvasHeight, null);
        g.dispose();
    }

    public static BufferedImage loadImage(File file) throws IOException {
        BufferedImage rawImage = ImageIO.read(file);
        BufferedImage finalImage = canvas.getGraphicsConfiguration().createCompatibleImage(rawImage.getWidth(),
                rawImage.getHeight(), rawImage.getTransparency());

        finalImage.getGraphics().drawImage(rawImage, 0, 0, rawImage.getWidth(), rawImage.getHeight(), null);

        return finalImage;
    }
}
