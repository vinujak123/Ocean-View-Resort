package src;

public enum RoomType {
    STANDARD("Standard", 15000.0),
    DELUXE("Deluxe", 25000.0),
    SUITE("Suite", 45000.0);

    private final String name;
    private final double rate;

    RoomType(String name, double rate) {
        this.name = name;
        this.rate = rate;
    }

    public String getName() {
        return name;
    }

    public double getRate() {
        return rate;
    }

    public static RoomType fromString(String text) {
        for (RoomType b : RoomType.values()) {
            if (b.name.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
