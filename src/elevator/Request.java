package elevator;

/**
 * The {@code Request} class represents a floor request made to the elevator system.
 * <p>
 * A request can either be an inside request (from within the elevator) or an outside
 * request (from a specific floor). Inside requests do not specify a direction, while
 * outside requests include a direction of travel ("up" or "down").
 * </p>
 */
public class Request {
    private int floor;
    private String direction; // "up" or "down", null for inside requests
    private boolean isInside; // Distinguishes inside vs. outside requests

    // Constructor

    /**
     * Constructs a new {@code Request} with the specified floor, direction, and type.
     *
     * @param floor     The target floor for the request.
     * @param direction The direction of travel ("up" or "down"), or {@code null} for inside requests.
     * @param isInside  {@code true} if the request is an inside request, {@code false} if it is an outside request.
     */
    public Request(int floor, String direction, boolean isInside) {
        this.floor = floor;
        this.direction = direction;
        this.isInside = isInside;
    }

    // Public Method

    /**
     * Returns a string representation of this request.
     *
     * @return A string representation of this request.
     */
    @Override
    public String toString() {
        return "Request [Floor: " + floor + (isInside ? " (inside)" : ", Direction: " + direction) + "]";
    }

    // Getters

    /**
     * Returns the floor associated with this request.
     *
     * @return The requested floor.
     */
    public int getFloor() {
        return floor;
    }

    /**
     * Returns the direction of the request if it is an outside request.
     *
     * @return The direction of travel ("up" or "down"), or {@code null} if this is an inside request.
     */
    public String getDirection() {
        return direction;
    }

    /**
     * Indicates whether this request is an inside request.
     *
     * @return {@code true} if this is an inside request, {@code false} otherwise.
     */
    public boolean isInside() {
        return isInside;
    }
}
