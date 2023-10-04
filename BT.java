package pacman.controllers.examples;

import java.util.ArrayList;
import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class BT extends Controller<MOVE> {
	//////////////////// Constructor and get
	//////////////////// move//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private MOVE myMove = MOVE.NEUTRAL;
	private BehaviorTree behaviorTree;

	// Constructs behavior tree
	public BT() {
		behaviorTree = new BehaviorTree();

	}

	// returns move set by BT
	public MOVE getMove(Game game, long timeDue) {

		behaviorTree.setGame(game);
		myMove = behaviorTree.step();

		// myMove = finiteStateMachine.step(game);
		return myMove;
	}

	/////////////////////////////////////// Behavior Tree Building Blocks includes :
	/////////////////////////////////////// Behavior Tree , Sequence , Selector and
	/////////////////////////////////////// Decorator (as shown in
	/////////////////////////////////////// slides)////////////////////////////////////////////////////////////////////////

	// Behavior Tree
	public class BehaviorTree {

		private Selector selector;

		public BehaviorTree() {
			selector = new Root();
		}

		public void setGame(Game game) {
			((Root) selector).setGame(game);
		}

		public Constants.MOVE step() {
			return selector.execute();
		}
	}

	// Decorator
	public interface Decorator {

		boolean execute(Game game);
	}

	// Action
	public interface Action {

		Constants.MOVE execute(Game game);
	}

	// Selector
	public interface Selector {

		Constants.MOVE execute();

	}

	// Sequence
	public interface Sequence {

		Constants.MOVE execute(Game game);
	}

	public class Root implements Selector {

		private Game game;
		private ArrayList<Sequence> listSequence;

		// Roots
		public Root() {
			listSequence = new ArrayList<>();
			Sequence RunAway = new RunAway(
					new GetGhostNear(),
					new IsGhostTooClose(),
					new MoveAwayFromGhost());
			Sequence chase = new ChaseGhosts(
					new GetNearestEdibleGhost(),
					new FoundGhost(),
					new MoveTowardsGhost());
			Sequence eatPills = new EatPills(
					new GetAvailablePills(),
					new ArePillsAvailable(),
					new MoveTowardsNearestPill());

			Sequence turnAround = new TurnAround(
					new MoveInOppositeDirection());

			// Adds behavior to Sequence
			listSequence.add(RunAway);
			listSequence.add(chase);
			listSequence.add(eatPills);
			listSequence.add(turnAround);
		}

		/// Game States
		public Game getGame() {
			return game;
		}

		public void setGame(Game game) {
			this.game = game;
		}

		public Constants.MOVE execute() {
			for (Sequence sequence : listSequence) {
				Constants.MOVE execute = sequence.execute(game);
				if (execute != null) {
					return execute;
				}
			}
			return null;
		}
	}

	//////////////////////////////////////////////////// Behaviors
	//////////////////////////////////////////////////// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	// Chases ghosts when blue they are edible
	public class ChaseGhosts implements Sequence {

		private GetNearestEdibleGhost getNearestEdibleGhost;
		private FoundGhost foundGhost;
		private MoveTowardsGhost moveTowardsGhost;

		public ChaseGhosts(
				final GetNearestEdibleGhost getNearestEdibleGhost,
				final FoundGhost foundGhost,
				final MoveTowardsGhost moveTowardsGhost) {
			this.getNearestEdibleGhost = getNearestEdibleGhost;
			this.foundGhost = foundGhost;
			this.moveTowardsGhost = moveTowardsGhost;
		}

		@Override
		public Constants.MOVE execute(Game game) {

			Constants.GHOST minGhost = getNearestEdibleGhost.execute(game);
			foundGhost.setMinGhost(minGhost);
			if (foundGhost.execute(game)) {
				moveTowardsGhost.setMinGhost(minGhost);
				return moveTowardsGhost.execute(game);
			}
			return null;
		}
	}

	// Eat Pills
	public class EatPills implements Sequence {

		private GetAvailablePills getAvailablePills;
		private ArePillsAvailable arePillsAvailable;
		private MoveTowardsNearestPill moveTowardsNearestPill;

		public EatPills(
				final GetAvailablePills getAvailablePills,
				final ArePillsAvailable arePillsAvailable,
				final MoveTowardsNearestPill moveTowardsNearestPill) {
			this.getAvailablePills = getAvailablePills;
			this.arePillsAvailable = arePillsAvailable;
			this.moveTowardsNearestPill = moveTowardsNearestPill;
		}

		@Override
		public Constants.MOVE execute(Game game) {
			int[] pills = game.getPillIndices();
			int[] powerPills = game.getPowerPillIndices();

			ArrayList<Integer> targets = new ArrayList<Integer>();

			getAvailablePills.setPills(pills);
			getAvailablePills.setTargets(targets);
			targets = getAvailablePills.execute(game);

			getAvailablePills.setPills(powerPills);
			getAvailablePills.setTargets(targets);
			targets = getAvailablePills.execute(game);

			arePillsAvailable.setTargets(targets);

			if (arePillsAvailable.execute(game)) {
				moveTowardsNearestPill.setTargets(targets);
				return moveTowardsNearestPill.execute(game);
			}
			return null;
		}
	}

	// Run away from ghosts
	public class RunAway implements Sequence {

		private GetGhostNear getGhostNear;
		private IsGhostTooClose isGhostTooClose;
		private MoveAwayFromGhost moveAwayFromGhost;

		public RunAway(final GetGhostNear getGhostNear,
				final IsGhostTooClose isGhostTooClose,
				final MoveAwayFromGhost moveAwayFromGhost) {
			this.getGhostNear = getGhostNear;
			this.isGhostTooClose = isGhostTooClose;
			this.moveAwayFromGhost = moveAwayFromGhost;
		}

		public Constants.MOVE execute(final Game game) {

			for (Constants.GHOST ghost : Constants.GHOST.values()) {
				getGhostNear.setGhost(ghost);
				int ghostLocation = getGhostNear.execute(game);
				if (ghostLocation != -1) {
					isGhostTooClose.setGhostLocation(ghostLocation);
					if (isGhostTooClose.execute(game)) {
						moveAwayFromGhost.setGhostLocation(ghostLocation);
						return moveAwayFromGhost.execute(game);
					}
				}
			}
			return null;
		}
	}

	// turns opposite direction
	public class TurnAround implements Sequence {

		private MoveInOppositeDirection moveInOppositeDirection;

		public TurnAround(MoveInOppositeDirection moveInOppositeDirection) {
			this.moveInOppositeDirection = moveInOppositeDirection;
		}

		@Override
		public Constants.MOVE execute(Game game) {
			return moveInOppositeDirection.execute(game);
		}

	}

	//////////////// Actions
	//////////////// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public class MoveAwayFromGhost implements Action {

		private int ghostLocation;

		public void setGhostLocation(int ghostLocation) {
			this.ghostLocation = ghostLocation;
		}

		public Constants.MOVE execute(Game game) {
			int current = game.getPacmanCurrentNodeIndex();
			return game.getNextMoveAwayFromTarget(current, ghostLocation, Constants.DM.PATH);
		}
	}

	public class MoveInOppositeDirection implements Action {

		@Override
		public Constants.MOVE execute(Game game) {
			return game.getPacmanLastMoveMade().opposite();
		}
	}

	public class MoveTowardsGhost implements Action {

		Constants.GHOST minGhost;

		public Constants.GHOST getMinGhost() {
			return minGhost;
		}

		public void setMinGhost(Constants.GHOST minGhost) {
			this.minGhost = minGhost;
		}

		@Override
		public Constants.MOVE execute(Game game) {
			int current = game.getPacmanCurrentNodeIndex();
			return game.getNextMoveTowardsTarget(current, game.getGhostCurrentNodeIndex(minGhost), Constants.DM.PATH);
		}
	}

	public class MoveTowardsNearestPill implements Action {

		ArrayList<Integer> targets;

		public ArrayList<Integer> getTargets() {
			return targets;
		}

		public void setTargets(ArrayList<Integer> targets) {
			this.targets = targets;
		}

		@Override
		public Constants.MOVE execute(Game game) {
			int current = game.getPacmanCurrentNodeIndex();

			int[] targetsArray = new int[targets.size()];
			for (int i = 0; i < targetsArray.length; i++) {
				targetsArray[i] = targets.get(i);
			}

			return game.getNextMoveTowardsTarget(
					current,
					game.getClosestNodeIndexFromNodeIndex(current, targetsArray, Constants.DM.PATH),
					Constants.DM.PATH);
		}
	}
	///////////////// Helpers
	///////////////// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// Gets pills
	public class GetAvailablePills {

		private int[] pills;
		private ArrayList<Integer> targets;

		public int[] getPills() {
			return pills;
		}

		public void setPills(int[] pills) {
			this.pills = pills;
		}

		public ArrayList<Integer> getTargets() {
			return targets;
		}

		public void setTargets(ArrayList<Integer> targets) {
			this.targets = targets;
		}

		public ArrayList<Integer> execute(Game game) {
			for (int i = 0; i < pills.length; i++) {
				Boolean pillStillAvailable = game.isPillStillAvailable(i);
				if (pillStillAvailable != null) {
					if (pillStillAvailable) {
						targets.add(pills[i]);
					}
				}
			}
			return targets;
		}
	}

	// gets nearest Ghosts
	public class GetGhostNear {

		private Constants.GHOST ghost;

		public Constants.GHOST getGhost() {
			return ghost;
		}

		public void setGhost(Constants.GHOST ghost) {
			this.ghost = ghost;
		}

		public int execute(Game game) {
			if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0) {
				return game.getGhostCurrentNodeIndex(ghost);
			}
			return -1;
		}
	}

	// gets nearest edible ghost
	public class GetNearestEdibleGhost {

		public Constants.GHOST execute(Game game) {
			int current = game.getPacmanCurrentNodeIndex();

			int minDistance = Integer.MAX_VALUE;
			Constants.GHOST minGhost = null;
			for (Constants.GHOST ghost : Constants.GHOST.values()) {
				if (game.getGhostEdibleTime(ghost) > 0) {
					int distance = game.getShortestPathDistance(current, game.getGhostCurrentNodeIndex(ghost));
					if (distance < minDistance) {
						minDistance = distance;
						minGhost = ghost;
					}
				}
			}
			return minGhost;
		}
	}

	/////////////// Decorators
	/////////////// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// checks if moves are available
	public class AreMovesAvailable implements Decorator {

		Constants.MOVE[] moves;

		public Constants.MOVE[] getMoves() {
			return moves;
		}

		public void setMoves(Constants.MOVE[] moves) {
			this.moves = moves;
		}

		@Override
		public boolean execute(Game game) {
			return (moves.length > 0);
		}
	}

	// checks if pills are available
	public class ArePillsAvailable implements Decorator {

		ArrayList<Integer> targets;

		public ArrayList<Integer> getTargets() {
			return targets;
		}

		public void setTargets(ArrayList<Integer> targets) {
			this.targets = targets;
		}

		@Override
		public boolean execute(Game game) {
			return !targets.isEmpty();
		}
	}

	// checks if ghosts are found
	public class FoundGhost implements Decorator {

		Constants.GHOST minGhost;

		public Constants.GHOST getMinGhost() {
			return minGhost;
		}

		public void setMinGhost(Constants.GHOST minGhost) {
			this.minGhost = minGhost;
		}

		@Override
		public boolean execute(Game game) {
			return (minGhost != null);
		}
	}

	// checks if ghosts are too close
	public class IsGhostTooClose implements Decorator {

		private static final int MIN_DISTANCE = 5;
		private int ghostLocation;

		public void setGhostLocation(int ghostLocation) {
			this.ghostLocation = ghostLocation;
		}

		public boolean execute(Game game) {
			int current = game.getPacmanCurrentNodeIndex();
			return (game.getShortestPathDistance(current, ghostLocation) < MIN_DISTANCE);
		}
	}
}
