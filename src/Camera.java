public class Camera {

	static public float translationX = 0;
	static public float translationY = 0;
	static public float scale = 2f;

	public static void setPosition(float x, float y) {
		translationX = x;
		translationY = y;
	}
	
	public static float getMousePositionX() {
		return Input.getMousePositionX() / scale + translationX;
	}
	
	public static float getMousePositionY() {
		return Input.getMousePositionY() / scale + translationY;
	}
}
