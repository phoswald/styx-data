package styx.data.db;

import java.util.Arrays;
import java.util.Objects;

public class Path implements Comparable<Path> {

    private final int[] parts;

    private Path(int[] parts) {
        this.parts = Objects.requireNonNull(parts);
    }

    public static Path of(int... parts) {
        return new Path(Arrays.copyOf(parts, parts.length));
    }

    @Override
    public String toString() {
        return Arrays.toString(parts);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Path &&
                Arrays.equals(parts, ((Path) other).parts);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(parts);
    }

    @Override
    public int compareTo(Path other) {
        for(int i = 0; i < parts.length && i < other.parts.length; i++) {
            if(parts[i] != other.parts[i]) {
                return Integer.compare(parts[i], other.parts[i]);
            }
        }
        return Integer.compare(parts.length, other.parts.length);
    }

    public int length() {
        return parts.length;
    }

    public boolean startsWith(Path other) {
       if(parts.length < other.parts.length) {
           return false;
       }
       for(int i = 0; i < other.parts.length; i++) {
           if(parts[i] != other.parts[i]) {
               return false;
           }
       }
       return true;
    }

    public int prefixLength(Path other) {
        int i = 0;
        while(i < parts.length && i < other.parts.length && parts[i] == other.parts[i]) {
            i++;
        }
        return i;
    }

    public Path add(int part) {
        int[] parts = Arrays.copyOf(this.parts, this.parts.length + 1);
        parts[this.parts.length] = part;
        return new Path(parts);
    }

    public Path sub() {
        return new Path(Arrays.copyOf(this.parts, this.parts.length - 1));
    }
}
