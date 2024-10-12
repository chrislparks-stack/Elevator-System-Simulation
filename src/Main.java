import elevator.Elevator;

import javax.swing.*;
import java.awt.*;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * The {@code Main} class serves as the entry point for the Elevator System application.
 * <p>
 * It sets up the graphical user interface (GUI) with areas for logging and displaying
 * the elevator's request queue. It prompts the user to input the top floor of the
 * building and allows the user to add outside and inside requests through a command-line
 * interface. The elevator system runs in a separate thread to process requests automatically.
 * </p>
 */
public class Main {

    // Public Method

    /**
     * The main entry point for the Elevator System application.
     * <p>
     * This method initializes the GUI with a log area and queue area, prompts the user
     * for input to set the top floor of the building, and creates an {@code Elevator} object
     * to manage floor requests. It runs the elevator queue processing on a separate thread
     * and provides a command-line interface for adding outside and inside requests.
     * </p>
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        // Set up the main GUI frame
        JFrame mainFrame = new JFrame("Elevator System");
        mainFrame.setLayout(new BorderLayout()); // Use BorderLayout for layout

        // Set up the log area at the top
        JTextArea logArea = new JTextArea(20, 40); // 20 rows, 40 columns
        logArea.setEditable(false); // No user edits
        JScrollPane logScrollPane = new JScrollPane(logArea); // Scroll if content exceeds area

        // Set up the queue area below the log area
        JTextArea queueArea = new JTextArea(10, 40); // 10 rows, 40 columns
        queueArea.setEditable(false); // No user edits
        JScrollPane queueScrollPane = new JScrollPane(queueArea); // Scroll if content exceeds area

        // Create a vertical panel to hold the log and queue areas
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS)); // Vertical layout
        textPanel.add(logScrollPane); // Add log area to the panel
        textPanel.add(queueScrollPane); // Add queue area to the panel

        // Add the panel to the main frame
        mainFrame.add(textPanel, BorderLayout.CENTER);

        // Final frame setup
        mainFrame.pack();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);

        // Set up the scanner for user input
        Scanner scanner = new Scanner(System.in);

        // Prompt the user for the top floor
        int topFloor = 0;
        while (topFloor < 1) {
            try {
                System.out.print("Enter the top floor of the building: ");
                topFloor = scanner.nextInt();
                if (topFloor < 1) {
                    System.out.println("Invalid input. The top floor must be at least 1.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
                scanner.next(); // Clear the invalid input
            }
        }

        // Create an Elevator object with the given top floor, logArea, and queueArea
        Elevator elevator = new Elevator(topFloor, logArea, queueArea);

        // Thread to automatically process the elevator queue
        Thread elevatorThread = new Thread(elevator::processQueueAutomatically);
        elevatorThread.start();

        // Listen for adding requests
        while (true) {
            try {
                System.out.println("\nChoose an action:");
                System.out.println("1. Add an outside request");
                System.out.println("2. Add an inside button request");
                System.out.println("3. Exit");
                System.out.print("> ");
                int choice = scanner.nextInt();

                // Add an outside button request
                if (choice == 1) {
                    System.out.print("Enter the floor number: ");
                    int floor = scanner.nextInt();
                    System.out.print("Enter direction (up/down): ");
                    String direction = scanner.next().toLowerCase();

                    if (!direction.equals("up") && !direction.equals("down")) {
                        System.out.println("Invalid direction. Please enter 'up' or 'down'.");
                    } else {
                        elevator.addRequest(floor, direction);
                    }

                // Add an inside button request
                } else if (choice == 2) {
                    System.out.print("Enter the inside floor button: ");
                    int floor = scanner.nextInt();
                    elevator.addInsideRequest(floor);

                // Exit the program
                } else if (choice == 3) {
                    System.out.println("Stopping the elevator...");
                    elevator.stop();
                    break;

                } else {
                    System.out.println("Invalid choice. Please try again.");
                }

            // Catch invalid entries
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.next();
            }
        }

        // Close the scanner to avoid resource leaks
        scanner.close();
        System.exit(0);
    }
}
