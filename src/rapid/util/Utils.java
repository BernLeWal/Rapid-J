package rapid.util;

/**
 * Utils - general purpose simple utility functions to be used everywhere in the
 * project
 *
 * @author Bernhard
 */
public class Utils {

    /**
     * intArrayToString - returns an given int-array as comma-seperated string
     *
     * @param values a jav-array of int values
     * @return comma-separated string with values
     */
    public static String intArrayToString(int[] values) {
        boolean addSeparator = false;
        StringBuilder sb = new StringBuilder();
        for (int value : values) {
            if (addSeparator) {
                sb.append(',');
            } else {
                addSeparator = true;
            }
            sb.append(value);
        }
        return sb.toString();
    }

    /**
     *longArrayToString - returns an given int-array as comma-seperated string
     *
     * @param values a jav-array of int values
     * @return comma-separated string with values
     */
    public static String longArrayToString(long[] values) {
        boolean addSeparator = false;
        StringBuilder sb = new StringBuilder();
        for (long value : values) {
            if (addSeparator) {
                sb.append(',');
            } else {
                addSeparator = true;
            }
            sb.append(value);
        }
        return sb.toString();
    }
}
