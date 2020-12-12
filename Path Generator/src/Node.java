import java.util.LinkedHashSet;
import java.util.Set;

public class Node {
    private Set<Node> neighbours;
    private int row, col;
    private boolean visited;
    private int type;

    Node(int row, int col, int type) {
        this.row= row;
        this.col= col;
        this.type= type;
        neighbours= new LinkedHashSet<>();
        visited= false;

    }

    public Set<Node> getNeighbours() {
        return neighbours;
    }

    public int getX() {
        return (int) ((row + 0.5) * R.TILE_SIZE);
    }

    public int getY() {
        return (int) ((col + 0.5) * R.TILE_SIZE);
    }

    public void visit() {
        visited= true;
    }

}