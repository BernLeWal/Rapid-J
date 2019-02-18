package rapid.net.port;

public class PortFactory {

    // static factory methods
    public static OneHotPort createOneHot(String name, int max, int cycle) {
        return new OneHotPort(name, max, cycle);
    }

    public static MapToOneHotPort createMapToOneHot(String name) {
        return new MapToOneHotPort(name);
    }

    public static FuzzyPort createFuzzy(String name, int max, int cycle) {
        return new FuzzyPort(name, max, cycle);
    }

    public static BinaryPort createBinary(String name, int bits, int cycle) {
        return new BinaryPort("b" + name, bits, cycle);
    }

    public static PortGroup createGroup(String name, Portable[] childPorts) {
        return new PortGroup(name, childPorts);
    }

    public static PortArray createArray(String name, Portable[] prototype) {
        return new PortArray(name, prototype);
    }

    // create new instance by a prototype depending on the prototype-class
    public static Portable createByPrototype(String name, Portable prototype, int cycle) {
        if (prototype instanceof OneHotPort) {
            return new OneHotPort(name, (OneHotPort) prototype, cycle);
        } else if (prototype instanceof MapToOneHotPort) {
            return new MapToOneHotPort(name, (MapToOneHotPort) prototype);
        } else if (prototype instanceof FuzzyPort) {
            return new FuzzyPort(name, (FuzzyPort) prototype, cycle);
        } else if (prototype instanceof BinaryPort) {
            return new BinaryPort(name, (BinaryPort) prototype, cycle);
        }
        return null;
    }
}
