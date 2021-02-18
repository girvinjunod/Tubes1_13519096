package za.co.entelect.challenge.command;

import za.co.entelect.challenge.enums.Direction;

public class SelectCommand implements Command {

    private final int worm_id;
    private final Direction direction;


    public SelectCommand(int worm_id, Direction direction) {

        this.worm_id = worm_id;
        this.direction = direction;
    }

    @Override
    public String render() {
        return String.format("select %d; shoot %s", worm_id, direction.name());
    }
}
