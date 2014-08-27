package view.overlay;

import java.awt.*;
import java.util.HashSet;

/**
 * Created by moon on 16/04/14.
 */
public class ObjectSetInfo {
    public HashSet<Point> Set;
    public String Label;
    public String Next;

    public ObjectSetInfo(HashSet<Point> set, String label, String next)
    {
        this.Set = set;
        this.Label = label;
        this.Next = next;
    }

    @Override
    public boolean equals(Object e)
    {
        return this.hashCode() == e.hashCode();
    }

    // http://stackoverflow.com/questions/9135759/java-hashcode-for-a-point-class
    @Override
    public int hashCode() {
        return Set.hashCode();
    }
}
