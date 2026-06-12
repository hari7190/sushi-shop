package foo.hari.livebarn.sushishop.domain;

public enum OrderStatus {
    CREATED("created"),
    IN_PROGRESS("in-progress"),
    PAUSED("paused"),
    FINISHED("finished", "completed"),
    CANCELLED("cancelled");

    private final String dbName;
    private final String apiBucket;

    OrderStatus(String dbName) {
        this(dbName, dbName);
    }

    OrderStatus(String dbName, String apiBucket) {
        this.dbName = dbName;
        this.apiBucket = apiBucket;
    }

    public String dbName() {
        return dbName;
    }

    public String apiBucket() {
        return apiBucket;
    }
}
