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

    public static Path decode(String string) {
        int partCount = 0;
        int[] parts = new int[string.length()];
        for(int i = 0; i < string.length(); i++) {
            int value = decode64(string.charAt(i));
            if(value <= 36) {
                parts[partCount++] = value;
            } else {
                int count = value - 36;
                value = 0;
                while(count-- > 0) {
                    value <<= 6;
                    value += decode64(string.charAt(++i));
                }
                parts[partCount++] = value;
            }
        }
        return new Path(Arrays.copyOf(parts, partCount));
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < parts.length; i++) {
            int value = parts[i];
            if(value >= 0 && value <= 36) {
                sb.append(encode64(value));
            } else {
                int count = 0;
                int bits = value;
                while(bits != 0) {
                    count++;
                    bits >>>= 6;
                }
                sb.append(encode64(36 + count));
                int shift = (count-1) * 6;
                while(shift >= 0) {
                    sb.append(encode64((value >>> shift) & 63));
                    shift -= 6;
                };
            }
        }
        return sb.toString();
    }

    static char encode64(int n) {
        if(n < 0 || n >= 64) {
            throw new IllegalArgumentException();
        } else if(n < 10) {
            return (char) ('0' + n);
        } else if(n < 36) {
            return (char) ('A' + n - 10);
        } else if(n < 37) {
            return '_';
        } else if(n < 63) {
            return (char) ('a' - 37 + n);
        } else {
            return '~';
        }
    }

    static int decode64(char c) {
        if(c >= '0' && c <= '9') {
            return c - '0';
        } else if(c >= 'A' && c <= 'Z') {
            return c - 'A' + 10;
        } else if(c == '_') {
            return 36;
        } else if(c >= 'a' && c <= 'z') {
            return c - 'a' + 37;
        } else if(c == '~') {
            return 63;
        } else {
            throw new IllegalArgumentException();
        }
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
