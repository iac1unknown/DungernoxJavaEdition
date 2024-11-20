import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Input implements KeyListener, MouseListener, MouseMotionListener {

    private static boolean[] pressedKeys = new boolean[128];
    private static Point mousePosition = new Point();
    private static boolean mousePressed = false;
    private static boolean mouseClicked = false;

    public static boolean isKeyPressed(int keyCode) {
        return pressedKeys[keyCode];
    }

    public static Point getPhysicalMousePosition() {
        return mousePosition;
    }

    public static float getMousePositionX() {
        return (float)mousePosition.x / Main.canvasWidth * Main.gameWidth;
    }

    public static float getMousePositionY() {
        return (float)mousePosition.y / Main.canvasHeight * Main.gameHeight;
    }

    public static boolean isMousePressed() {
        return mousePressed;
    }

    public static boolean isMouseClicked() {
        return mouseClicked;
    }

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        pressedKeys[keyCode] = true;
    }

    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        pressedKeys[keyCode] = false;
    }

    public void keyTyped(KeyEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        mousePosition = e.getPoint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePosition = e.getPoint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePressed = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;
        mouseClicked = true;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

}
