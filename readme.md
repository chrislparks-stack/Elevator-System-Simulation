# Elevator System Simulation - README

## Overview
The **Elevator System** program simulates the basic functionality of an elevator in a multi-story building. The elevator can handle both inside requests (when passengers inside the elevator press buttons for their desired floors) and outside requests (when people on specific floors press buttons to summon the elevator up or down). The system is represented using a combination of graphical user interface (GUI) elements and command-line prompts, providing real-time logging and displaying the elevator's request queue. The program is written in Java and utilizes the `Swing` library for the GUI components.

## Features
- **Graphical User Interface (GUI):** Displays real-time log updates and current request queue using a `JFrame` with `JTextArea` components.
- **Outside and Inside Requests:** Handles requests made from inside the elevator and calls made from specific floors.
- **Priority Management:** Processes requests using an optimal path strategy to minimize travel time and maximize efficiency.
- **Multi-threading:** The elevator runs in a separate thread that continuously checks the request queue for new commands and processes them automatically.
- **Simulation of Real Elevator Behavior:** The elevator "moves" between floors with a time delay and logs its actions (e.g., passing floors, opening and closing doors).

## How to Run the Program
1. **Setup:** Make sure you have Java installed on your system. Compile the program using the following command:
   ```sh
   javac Main.java elevator/Elevator.java elevator/Request.java
   ```

2. **Run the Program:** Execute the compiled `Main` class using the following command:
   ```sh
   java Main
   ```

3. **Usage:** After launching the program, follow the command-line prompts to set the top floor of the building and interact with the elevator.
    - Add outside and inside requests through the provided prompts.
    - The elevator will process requests automatically and return to floor 1 when idle.

## Class Descriptions
### 1. Main Class
The `Main` class serves as the entry point for the elevator system simulation. It sets up the GUI and allows user input for defining the building's top floor and adding floor requests. Key components include:
- **GUI Setup:** The main GUI frame (`JFrame`) contains two areas: a **log area** for real-time logging of elevator activities and a **queue area** for displaying current requests.
- **User Interaction:** Prompts the user to add requests (either inside or outside requests) or exit the application.

### 2. Request Class
The `Request` class represents a floor request made to the elevator. It distinguishes between **inside requests** (requests made from within the elevator) and **outside requests** (requests made from a floor).
- **Attributes:** Each request has a **floor number**, a **direction** (either "up" or "down" for outside requests), and a flag to indicate whether it is an **inside request**.
- **Methods:** The class provides getters for accessing the request's attributes and a `toString()` method for generating a string representation of the request.

### 3. Elevator Class
The `Elevator` class models the elevator system and its behavior.
- **Attributes:**
    - **Current Floor & Top Floor:** Tracks the current floor of the elevator and the highest floor available.
    - **Request Queue:** A `Deque` used to store and manage requests, allowing flexible addition and removal of requests from both ends.
- **Methods:**
    - **`processQueueAutomatically()`:** Continuously checks for and processes requests in the queue. If no requests are present, the elevator returns to floor 1.
    - **`addRequest()` and `addInsideRequest()`:** Add outside and inside requests to the queue, respectively.
    - **`sortQueue()` and `buildOptimalPath()`:** Sorts requests based on an optimal path to minimize travel time and unnecessary stops.
    - **`moveToFloor()`:** Handles the movement of the elevator to a specific floor, including logging, door operations, and checking for new requests while en route.

- **GUI Integration:**
    - **Log Area & Queue Area:** Updates these components in real time to display the elevator's activity and current request queue.

## Assumptions
### 1. Optimal Path & Request Handling
- The elevator **prioritizes inside button requests** before processing other queued requests. This approach ensures that passengers inside the elevator have their requests satisfied first, minimizing their wait time.
- The elevator **takes the shortest path** to complete the next queued request. If there are multiple requests in the queue, they are processed to minimize backtracking.
- In the case where the elevator is in motion and a new request comes in that is on the current path and in the same direction, this new request is prioritized to optimize efficiency.

### 2. Door Controls
- Most elevators have buttons to **open or close doors manually**, as well as safety mechanisms to **prevent doors from closing if blocked**. These features were not implemented because the simulation has no mechanism to detect door states or control external physical inputs.
- The doors in this program open and close automatically, with a wait time of **10 seconds** when servicing a floor. Additionally, if no inside button is pressed after 10 seconds, the elevator proceeds to the next request.

### 3. Floor Indicators and Button Lights
- Real elevators have **floor indicators** to show which floor the elevator is passing or currently on, as well as **light indicators** for pressed buttons. In this simulation, there is no graphical representation of these lights or indicators, as it focuses primarily on request handling and movement.
- The floor number is logged in the log area, and the request queue is displayed in a separate queue area. These logs serve as a substitute for visual lights or indicators in a physical elevator.

### 4. Safety Measures
- **Safety mechanisms** are a critical component of real elevator systems, which include measures to handle situations like **malfunction, power loss, or system errors**. In this simulation, no safety features are implemented due to the lack of external sensors or physical control mechanisms.
- This program does not simulate conditions such as **overloading**, **emergency stops**, or **fire safety** protocols, as there are no means to monitor or enforce these safety conditions within the code.

## Limitations
- **No Physical Simulation:** The simulation does not involve any physical hardware or actual sensors, so it lacks features like weight sensors, emergency alarms, or safety stop buttons.
- **Single Elevator:** This simulation handles only **one elevator**. In real-world scenarios, buildings often have multiple elevators managed by complex control algorithms to distribute requests efficiently.
- **Manual Input Only:** Requests must be **manually entered** through the command line. There is no automation for generating requests based on user behavior or time of day.

## Future Enhancements
- **Door Control Simulation:** Introduce a more complex door mechanism, such as manual open/close buttons and obstacle detection to prevent doors from closing on passengers.
- **Multiple Elevators:** Expand the simulation to handle multiple elevators working together, along with an algorithm to determine which elevator should respond to each request.
- **Safety Features:** Add simulated safety features, such as emergency stop functionality, weight capacity checks, and fail-safe shutdowns in case of system errors.

## License
This project is licensed under the MIT License. Feel free to use, modify, and distribute the code as you see fit.

## Author
This elevator system simulation was created by Chris Parks, a software developer based in Colorado.  The project was developed to explore the basics of elevator control algorithms, multi-threading, and GUI integration in Java.
