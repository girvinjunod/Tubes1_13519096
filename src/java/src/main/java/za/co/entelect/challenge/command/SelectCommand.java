package za.co.entelect.challenge.command;

public class SelectCommand implements Command {

    private final int x;
    private final int y;

    public SelectCommand(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String render() {
        return String.format("dig %d %d", x, y);
    }
}
