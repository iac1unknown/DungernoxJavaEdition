public class MultipleKey {
    public final int x;
    public final int y;

    public MultipleKey(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Implement equals() and hashCode() methods
    // These methods ensure proper functioning of the map
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MultipleKey)) return false;
        MultipleKey key = (MultipleKey) o;
        return x == key.x && y == key.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return x + " " + y;
    }

    public MultipleKey offset(int offX, int offY) {
    	return new MultipleKey(x + offX, y + offY);
    }

    public double norm() {
        return Math.sqrt(x*x + y*y);
    }
}