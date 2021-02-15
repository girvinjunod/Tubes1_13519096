package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;

import java.util.*;
import java.util.stream.Collectors;

public class Bot {

    private final Random random;
    private final GameState gameState;
    private final Opponent opponent;
    private final MyWorm currentWorm;

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.opponent = gameState.opponents[0];
        this.currentWorm = getCurrentWorm(gameState);
    }

    private MyWorm getCurrentWorm(GameState gameState) {
        return Arrays.stream(gameState.myPlayer.worms)
                .filter(myWorm -> myWorm.id == gameState.currentWormId)
                .findFirst()
                .get();
    }

    //Di sini main nya
    public Command run() {
        //Penyerangan
        Worm enemyWorm = getFirstWormInRange();
        if (enemyWorm != null) {
            if (canBanana(enemyWorm)) {
                return new BananaCommand(enemyWorm.position.x, enemyWorm.position.y);
            }
            else if (canSnowball(enemyWorm)) {
                return new SnowballCommand(enemyWorm.position.x, enemyWorm.position.y);
            }
            else {
                Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
                return new ShootCommand(direction);
            }
        }

        //kalau gaada musuh, dia bergerak (Perpindahan)
        List<Cell> surroundingBlocks = null;

        /*if(gameState.currentRound<=40) {
            if (currentWorm.id == 1) {
                surroundingBlocks = get_cell_tujuan(currentWorm.position.x, currentWorm.position.y, 14, 18);
            } else if (currentWorm.id == 2) {
                surroundingBlocks = get_cell_tujuan(currentWorm.position.x, currentWorm.position.y, 18, 14);
            } else if (currentWorm.id == 3) {
                surroundingBlocks = get_cell_tujuan(currentWorm.position.x, currentWorm.position.y, 14, 14);
            }
        }else */if(gameState.currentRound<=90 ||
                ((currentWorm.position.x >= 15)
                && (currentWorm.position.x <= 17)
                && (currentWorm.position.y >= 15)
                && (currentWorm.position.y <= 17))){
            surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
        }else{
            surroundingBlocks = get_cell_tujuan(currentWorm.position.x, currentWorm.position.y, 16, 16);
        }

        if (surroundingBlocks.size() > 0) {
            int cellIdx = random.nextInt(surroundingBlocks.size());
            Cell block = surroundingBlocks.get(cellIdx);
            if (block.type == CellType.AIR) {
                return new MoveCommand(block.x, block.y);
            } else if (block.type == CellType.DIRT) {
                return new DigCommand(block.x, block.y);
            } else {
                return new DoNothingCommand();
            }
        }

        return new DoNothingCommand();
    }

    //Di bawah adalah implementasi function nya
    private Worm getFirstWormInRange() {

        Set<String> cells = constructFireDirectionLines(currentWorm.weapon.range)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        for (Worm enemyWorm : opponent.worms) {
            if (enemyWorm.health>0) {
                String enemyPosition = String.format("%d_%d", enemyWorm.position.x, enemyWorm.position.y);
                if (cells.contains(enemyPosition)) {
                    return enemyWorm;
                }
            }
        }

        return null;
    }

    private List<List<Cell>> constructFireDirectionLines(int range) {
        List<List<Cell>> directionLines = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            List<Cell> directionLine = new ArrayList<>();
            for (int directionMultiplier = 1; directionMultiplier <= range; directionMultiplier++) {

                int coordinateX = currentWorm.position.x + (directionMultiplier * direction.x);
                int coordinateY = currentWorm.position.y + (directionMultiplier * direction.y);

                if (!isValidCoordinate(coordinateX, coordinateY)) {
                    break;
                }

                if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, coordinateX, coordinateY) > range) {
                    break;
                }

                Cell cell = gameState.map[coordinateY][coordinateX];
                if (cell.type != CellType.AIR) {
                    break;
                }

                directionLine.add(cell);
            }
            directionLines.add(directionLine);
        }

        return directionLines;
    }

    private List<Cell> getSurroundingCells(int x, int y) {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j)) {
                    cells.add(gameState.map[j][i]);
                }
            }
        }
        return cells;
    }

    private List<Cell> get_cell_tujuan(int x, int y, int bx, int by) {
        ArrayList<Cell> cells = new ArrayList<>();
        int jarak = 999999;
        int temp_x = 0;
        int temp_y = 0;
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // Don't include the current position
                int a = euclideanDistance(i, j, bx,by);
                if (a < jarak){
                    jarak = a;
                    temp_x = i;
                    temp_y = j;
                }
            }
        }
        if (temp_x != x || temp_y != y && isValidCoordinate(temp_x, temp_y)) {
            cells.add(gameState.map[temp_y][temp_x]);
        }
        return cells;
    }

    private boolean canBanana(Worm target) {
        return (currentWorm.id==2)
                && currentWorm.bananaBombs.count > 0
                && euclideanDistance(currentWorm.position.x,
                                    currentWorm.position.y,
                                    target.position.x,
                                    target.position.y) <= currentWorm.bananaBombs.range
                && euclideanDistance(currentWorm.position.x,
                                    currentWorm.position.y,
                                    target.position.x,
                                    target.position.y) > currentWorm.bananaBombs.damageRadius * 0.75;
    }

    private boolean canSnowball(Worm target) {
        return (currentWorm.id==3)
                && currentWorm.snowballs.count > 0
                && target.roundsUntilUnfrozen == 0
                && euclideanDistance(currentWorm.position.x,
                                    currentWorm.position.y,
                                    target.position.x,
                                    target.position.y) <= currentWorm.snowballs.range
                && euclideanDistance(currentWorm.position.x,
                                    currentWorm.position.y,
                                    target.position.x,
                                    target.position.y) > currentWorm.snowballs.freezeRadius * Math.sqrt(2);
    }

    private int euclideanDistance(int aX, int aY, int bX, int bY) {
        return (int) (Math.sqrt(Math.pow(aX - bX, 2) + Math.pow(aY - bY, 2)));
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < gameState.mapSize
                && y >= 0 && y < gameState.mapSize;
    }

    private Direction resolveDirection(Position a, Position b) {
        StringBuilder builder = new StringBuilder();

        int verticalComponent = b.y - a.y;
        int horizontalComponent = b.x - a.x;

        if (verticalComponent < 0 && horizontalComponent==0) {
            builder.append('N');
        } else if (verticalComponent > 0 && horizontalComponent==0) {
            builder.append('S');
        } else if (horizontalComponent < 0 && verticalComponent==0) {
            builder.append('W');
        } else if (horizontalComponent > 0 && verticalComponent==0) {
            builder.append('E');
        } else if (horizontalComponent > 0 && verticalComponent>0) {
            if (horizontalComponent == verticalComponent){
                builder.append("SE");
            }
        } else if (horizontalComponent > 0) {
            if (horizontalComponent == -(verticalComponent)) {
                builder.append("NE");
            }
        }else if (horizontalComponent < 0 && verticalComponent > 0) {
            if (horizontalComponent == -verticalComponent) {
                builder.append("SW");
            }
        }else if (horizontalComponent < 0) {
            if (horizontalComponent == verticalComponent) {
                builder.append("NW");
            }
        }

        return Direction.valueOf(builder.toString());
    }
}
