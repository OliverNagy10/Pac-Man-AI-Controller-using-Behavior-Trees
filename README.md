# Ms. Pac-Man Behavior Tree Controller

This repository contains a Java-based Ms. Pac-Man AI controller that utilizes a Behavior Tree (BT) for decision-making. The BT controls Ms. Pac-Man's behavior in the game, allowing her to exhibit various behaviors depending on the game state.

## Behavior Tree Logic

The Behavior Tree (BT) is structured with the following behaviors:

1. **Chase Edible Ghosts**:
   - **GetNearestEdibleGhost**: Finds the nearest edible ghost.
   - **FoundGhost**: Checks if an edible ghost is found.
   - **MoveTowardsGhost**: Moves towards the nearest edible ghost.

2. **Eat Pills**:
   - **GetAvailablePills**: Finds available pills on the game board.
   - **ArePillsAvailable**: Checks if pills are available.
   - **MoveTowardsNearestPill**: Moves towards the nearest available pill.

3. **Run Away from Non-Edible Ghosts**:
   - **GetGhostNear**: Checks if a non-edible ghost is near.
   - **IsGhostTooClose**: Checks if a non-edible ghost is too close.
   - **MoveAwayFromGhost**: Moves away from the non-edible ghost.

4. **Turn Around**:
   - **MoveInOppositeDirection**: Makes Ms. Pac-Man turn around.

## Customization

You can customize the behavior tree logic by modifying the `BH.java` file:
- Adjust the conditions for each behavior.
- Add new behaviors or actions as needed.
