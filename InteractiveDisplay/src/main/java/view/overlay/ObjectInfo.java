package view.overlay;

/**
 * Created by moon on 14/04/14.
 */
public class ObjectInfo{
    public int X, Y;
    public String Label;

    public ObjectInfo(int x, int y, String label)
    {
        this.X = x;
        this.Y = y;
        this.Label = label;
    }

    @Override
    public boolean equals(Object e)
    {
        return this.hashCode() == e.hashCode();
    }

    // http://stackoverflow.com/questions/9135759/java-hashcode-for-a-point-class
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.X;
        hash = 71 * hash + this.Y;
        return hash;
    }
}
