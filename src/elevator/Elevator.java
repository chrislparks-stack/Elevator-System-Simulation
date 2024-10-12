package elevator;

import javax.swing.*;
import java.util.*;

/**
 * The {@code Elevator} class models an elevator system capable of handling
 * multiple floor requests with priority management. It supports both inside
 * and outside button requests, processes them based on optimal paths, and
 * provides real-time logging and queue updates through the provided UI components.
 */
public class Elevator {
    private int currentFloor;
    private int topFloor;
    private Deque<Request> requestQueue; // Use Deque for queue manipulations
    private boolean running;
    private JTextArea logArea; // Log area for real-time updates
    private JTextArea queueArea; // Queue display area for real-time updates
    private Request currentRequest; // To track the current request being processed
    private boolean movingUp; // Tracks the direction of the elevator (true if moving up)
    private Thread currentThread;

    //Constructor

    /**
     * Constructs an {@code Elevator} object with the specified top floor
     * and UI components for logging and queue display.
     * <p>
     * The elevator is initialized at floor 1 with an empty request queue
     * and is set to run immediately. It uses the provided JTextAreas to
     * display real-time logs and the current state of the request queue.
     * </p>
     * @param topFloor  The highest floor the elevator can reach.
     * @param logArea   The JTextArea used to display log messages.
     * @param queueArea The JTextArea used to display the current request queue.
     */
    public Elevator(int topFloor, JTextArea logArea, JTextArea queueArea) {
        this.currentFloor = 1;
        this.topFloor = topFloor;
        this.requestQueue = new ArrayDeque<>();
        this.running = true;
        this.logArea = logArea;
        this.queueArea = queueArea;
        this.currentRequest = null;
        this.movingUp = false;
        this.currentThread = Thread.currentThread();
        log("Elevator initialized at floor 1.");
        updateQueueDisplay();
    }

    // Public Methods

    /**
     * Continuously processes the request queue. If no requests are available,
     * the elevator returns to floor 1.
     */
    public void processQueueAutomatically() {
        while (running) {
            if (!requestQueue.isEmpty() || currentRequest != null) {
                processNewRequest();
            } else if (currentFloor != 1) {
                // If there are no requests, return to floor 1 without opening doors
                log("Returning to floor 1 as no more requests are in the queue.");
                Request floorOneRequest = new Request(1, "up", false);
                moveToFloor(floorOneRequest, false, true);
            }

            updateQueueDisplay();

            try {
                Thread.sleep(1000); // Wait 1 second if no requests are in the queue
            } catch (InterruptedException e) {
                log("Elevator processing interrupted.");
            }
        }
    }

    /**
     * Adds a new outside request to the queue.
     *
     * @param floor     The target floor for the request.
     * @param direction The direction of travel ("up" or "down").
     */
    public void addRequest(int floor, String direction) {
        if ((floor == 1 && !direction.equals("up")) || (floor == topFloor && !direction.equals("down"))) {
            System.out.println("Invalid request. Floor 1 can only go up, and the top floor can only go down.");
        } else if (floor < 1 || floor > topFloor) {
            System.out.println("Invalid floor. Please select a floor between 1 and " + topFloor + ".");
        } else {
            Request newRequest = new Request(floor, direction, false); // Create a new outside request

            // Check if there’s a current request in progress
            if (currentRequest != null) {
                // Prioritize if the new request is on the way and closer in the same direction
                if (movingUp && floor > currentFloor && floor < currentRequest.getFloor()) {
                    log("Prioritizing request to stop at floor " + floor + " on the way up.");
                    requestQueue.addFirst(currentRequest); // Push the current request back into the queue
                    currentRequest = newRequest; // Set the new request as the priority
                } else if (!movingUp && floor < currentFloor && floor > currentRequest.getFloor()) {
                    log("Prioritizing request to stop at floor " + floor + " on the way down.");
                    requestQueue.addFirst(currentRequest); // Push the current request back into the queue
                    currentRequest = newRequest;
                } else {
                    // If the new request is in the same direction but further away
                    Optional<Request> nextRequestOpt = Optional.ofNullable(requestQueue.peekFirst());
                    if (nextRequestOpt.isPresent()) {
                        Request nextRequest = nextRequestOpt.get();
                        if (movingUp && direction.equals("up") && floor > currentFloor && floor < nextRequest.getFloor()) {
                            log("Inserting outside request at floor " + floor + " on the way up.");
                            requestQueue.addFirst(newRequest); // Insert in the queue before the next request
                        } else if (!movingUp && direction.equals("down") && floor < currentFloor && floor > nextRequest.getFloor()) {
                            log("Inserting outside request at floor " + floor + " on the way down.");
                            requestQueue.addFirst(newRequest); // Insert in the queue before the next request
                        } else {
                            // Otherwise, add it to the end of the queue
                            log("Adding request for floor " + floor + " to the end of the queue.");
                            requestQueue.addLast(newRequest);
                        }
                    } else {
                        // If no next request exists, simply add the new request to the end
                        requestQueue.addLast(newRequest);
                    }
                }
            } else {
                // If the elevator is idle, add the new request to the queue
                log("Elevator is idle, adding request to the queue.");
                requestQueue.addLast(newRequest);
            }

            // Sort the requests based on most optimal path
            sortQueue();
        }

        updateQueueDisplay();
    }

    /**
     * Adds an inside button request to the queue.
     *
     * @param floor The target floor requested from inside the elevator.
     */
    public void addInsideRequest(int floor) {
        if (floor < 1 || floor > topFloor) {
            log("Invalid floor. Please select a floor between 1 and " + topFloor + ".");
        } else if (floor == currentFloor) {
            log("You are already on floor " + floor + ".");
        } else {
            Request newRequest = new Request(floor, null, true);
            requestQueue.addFirst(newRequest);

            // Sort the requests based on most optimal path
            sortQueue();
        }

        updateQueueDisplay();
    }

    /**
     * Stops the elevator system immediately.
     */
    public void stop() {
        running = false;
        log("Forcing elevator system to stop.");
        if (currentThread != null) {
            currentThread.interrupt();
        }
    }

    // Private Methods

    /**
     * Sorts the queue to optimize the elevator's path.
     */
    private void sortQueue() {
        List<Request> allRequests = new ArrayList<>(new HashSet<>(requestQueue));  // Remove duplicates

        // Remove the current request from the list to avoid duplication
        if (currentRequest != null) {
            allRequests.removeIf(req -> req.getFloor() == currentRequest.getFloor());
        }

        // Build the optimal path from the current floor
        List<Request> sortedRequests = buildOptimalPath(currentFloor, allRequests);

        // Create a new queue with the sorted requests
        Deque<Request> newQueue = new ArrayDeque<>(sortedRequests);

        // Replace the original queue with the sorted one
        requestQueue = newQueue;
    }

    /**
     * Builds the optimal path based on the current floor and request list.
     *
     * @param startFloor The floor where the elevator starts.
     * @param requests   The list of pending requests.
     * @return A sorted list of requests representing the optimal path.
     */
    private List<Request> buildOptimalPath(int startFloor, List<Request> requests) {
        // Separate inside and outside requests
        List<Request> insideRequests = new ArrayList<>();
        List<Request> outsideRequests = new ArrayList<>();

        for (Request request : requests) {
            if (request.isInside()) {
                insideRequests.add(request);
            } else {
                outsideRequests.add(request);
            }
        }

        // Sort inside requests by direction
        List<Request> sortedInsideRequests = sortInsideRequests(startFloor, insideRequests);

        // Build the path by merging inside and outside requests logically
        return mergeRequests(sortedInsideRequests, outsideRequests);
    }

    /**
     * Sorts inside requests based on their direction relative to the starting floor.
     *
     * @param startFloor     The floor where sorting begins.
     * @param insideRequests The list of inside requests to be sorted.
     * @return A list of requests sorted by their direction and position.
     */
    private List<Request> sortInsideRequests(int startFloor, List<Request> insideRequests) {
        List<Request> goingUp = new ArrayList<>();
        List<Request> goingDown = new ArrayList<>();

        for (Request request : insideRequests) {
            if (request.getFloor() > startFloor) {
                goingUp.add(request);
            } else {
                goingDown.add(request);
            }
        }

        // Sort going up requests in ascending order
        goingUp.sort(Comparator.comparingInt(Request::getFloor));

        // Sort going down requests in descending order
        goingDown.sort((a, b) -> Integer.compare(b.getFloor(), a.getFloor()));

        // Determine initial direction
        boolean movingUp = goingUp.size() > goingDown.size();

        // Combine the lists based on the initial direction
        List<Request> sortedInsideRequests = new ArrayList<>();
        if (movingUp) {
            sortedInsideRequests.addAll(goingUp);
            sortedInsideRequests.addAll(goingDown);
        } else {
            sortedInsideRequests.addAll(goingDown);
            sortedInsideRequests.addAll(goingUp);
        }

        return sortedInsideRequests;
    }

    /**
     * Determines if the specified request matches the current direction of travel.
     *
     * @param movingUp {@code true} if the elevator is moving up, {@code false} if it is moving down.
     * @param request  The request to evaluate.
     * @return {@code true} if the request matches the elevator's direction, {@code false} otherwise.
     */
    private boolean matchesDirection(boolean movingUp, Request request) {
        return (movingUp && "up".equals(request.getDirection())) ||
                (!movingUp && "down".equals(request.getDirection()));
    }

    /**
     * Merges inside and outside requests into a single path logically.
     *
     * @param insideRequests  The list of inside requests.
     * @param outsideRequests The list of outside requests.
     * @return A merged list of requests representing the combined path.
     */
    private List<Request> mergeRequests(List<Request> insideRequests, List<Request> outsideRequests) {
        List<Request> mergedPath = new ArrayList<>(insideRequests);

        for (Request outsideRequest : outsideRequests) {
            boolean inserted = false;

            // Check if the outside request fits naturally between two inside requests
            for (int i = 0; i < mergedPath.size() - 1; i++) {
                Request current = mergedPath.get(i);
                Request next = mergedPath.get(i + 1);

                boolean movingUp = next.getFloor() > current.getFloor();
                boolean requestInRange = current.getFloor() <= outsideRequest.getFloor() &&
                        outsideRequest.getFloor() <= next.getFloor();

                if (requestInRange && matchesDirection(movingUp, outsideRequest)) {
                    mergedPath.add(i + 1, outsideRequest);
                    inserted = true;
                    break;
                }
            }

            // If the request doesn’t fit naturally, add it to the end
            if (!inserted) {
                mergedPath.add(outsideRequest);
            }
        }

        return mergedPath;
    }

    /**
     * Opens and closes the elevator doors with appropriate wait times.
     */
    private void openAndCloseDoors() {
        log("Opening doors...");

        try {
            Thread.sleep(1000); // Simulate doors opening
        } catch (InterruptedException e) {
            log("Elevator waiting interrupted.");
        }

        log("Waiting for passengers to enter/exit...");

        try {
            Thread.sleep(10000); // Wait for 10 seconds for people to exit
        } catch (InterruptedException e) {
            log("Elevator waiting interrupted.");
        }

        log("Closing doors...");

        try {
            Thread.sleep(1000); // Simulate doors closing
        } catch (InterruptedException e) {
            log("Elevator waiting interrupted.");
        }
    }

    /**
     * Checks if any inside request exists in the queue.
     *
     * @return {@code true} if an inside request is found, otherwise {@code false}.
     */
    private boolean checkForInsideRequest() {
        for (Request request : requestQueue) {
            if (request.isInside()) {
                return true;
            }
        }
        return false;
    }


    /**
     * Processes the next request in the queue.
     */
    private void processNewRequest() {
        if (currentRequest == null && !requestQueue.isEmpty()) {
            currentRequest = requestQueue.pollFirst();
        }

        updateQueueDisplay();

        if (currentRequest != null) {
            moveToFloor(currentRequest, true, false);
            // Clear current request after it's serviced
            currentRequest = null;
        }
    }

    /**
     * Moves the elevator to the specified target floor, handling door operations
     * and potential interruptions along the way.
     *
     * @param request           The floor request to process.
     * @param openDoors         {@code true} if the doors should open upon arrival,
     *                          {@code false} otherwise.
     * @param returningToFloor1 {@code true} if the elevator is returning to floor 1,
     *                          {@code false} otherwise.
     */
    private void moveToFloor(Request request, boolean openDoors, boolean returningToFloor1) {
        if (request.getFloor() < 1 || request.getFloor() > topFloor) {
            log("Invalid floor. Please select a floor between 1 and " + topFloor + ".");
            return;
        }

        log("Starting movement to floor " + request.getFloor());

        movingUp = request.getFloor() > currentFloor;

        while (currentFloor != request.getFloor()) {
            if (currentFloor < request.getFloor()) {
                currentFloor++;
            } else if (currentFloor > request.getFloor()) {
                currentFloor--;
            }

            // Only log passing floors, not the arrival floor
            if (currentFloor != request.getFloor()) {
                log("Passing floor " + currentFloor);
            }

            // Check if there is a request that should interrupt this movement
            if (currentRequest != null) {
                // Check if the current request is on the way and closer than the target floor
                if ((movingUp && currentRequest.getFloor() > currentFloor && currentRequest.getFloor() < request.getFloor()) ||
                        (!movingUp && currentRequest.getFloor() < currentFloor && currentRequest.getFloor() > request.getFloor())) {
                    processNewRequest();
                    return;
                }
            }

            try {
                Thread.sleep(3000); // 3-second delay between floors
            } catch (InterruptedException e) {
                log("Elevator movement interrupted.");
            }
        }

        // Log the final destination as "Arrived"
        log("Arrived at floor " + currentFloor);

        if (!requestQueue.isEmpty()) {
            Request firstRequest = requestQueue.peekFirst();
            if (firstRequest.getFloor() == currentFloor) {
                requestQueue.pollFirst(); // Remove this request from the queue
            }
        }

        // Update the elevator direction based on the requested direction if one exists
        if (request.getDirection() != null && request.getDirection().equals("up")) {
            movingUp = true;
        } else if (request.getDirection() != null && request.getDirection().equals("down")) {
            movingUp = false;
        }

        // Perform door operations only if openDoors is true
        if (openDoors) {
            openAndCloseDoors();

            // Check if there are any inside requests
            if (!checkForInsideRequest()) {
                log("Waiting for inside button calls...");

                // Determine how long to wait based on the queue state
                int waitTime = requestQueue.isEmpty() ? 30 : 10; // 30 seconds if no other requests, 10 if there are

                try {
                    for (int i = 0; i < waitTime; i++) {
                        // Check every second if an inside button is pressed
                        if (checkForInsideRequest()) {
                            log("Inside button pressed. Processing inside request...");
                            return;
                        }
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    log("Elevator waiting interrupted.");
                }

                // After waiting, decide if it should move to the next queue item
                if (requestQueue.isEmpty() && !returningToFloor1) {
                    log("No more requests. Returning to floor 1.");
                    Request floorOneRequest = new Request(1, "up", false);
                    moveToFloor(floorOneRequest, false, true); // Return to floor 1
                    updateQueueDisplay();
                }
            } else {
                log("Inside button pressed. Processing inside request...");
            }
        }

        updateQueueDisplay();
    }

    /**
     * Updates the request queue display in real-time.
     */
    private void updateQueueDisplay() {
        if (currentRequest != null) {
            queueArea.setText("Current queue item: " + currentRequest + "\n");
            if (!requestQueue.isEmpty()) {
                queueArea.append("--------------------------------" + "\n");
                queueArea.append("Awaiting queue items:" + "\n");
                queueArea.append("--------------------------------" + "\n");
                for (Request request : requestQueue) {
                    queueArea.append("Floor: " + request.getFloor() + ", Direction: " + request.getDirection() + "\n");
                }
            }
        } else {
            queueArea.setText("");
        }

        queueArea.revalidate();
        queueArea.repaint();
    }

    /**
     * Logs a message to the provided JTextArea.
     *
     * @param message The message to log.
     */
    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
