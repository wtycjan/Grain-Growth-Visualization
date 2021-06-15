package sample;

import javafx.scene.paint.Color;
import javafx.util.Pair;

public class Seed {
    public int x;
    public int y;
    public Color color;
    public int energia=0;
    public double ro = 0;
    public boolean zarekrystalizowany= false;

    public Seed(int x, int y, Color col)
    {
        this.x=x;
        this.y=y;
        this.color=col;
    }
}
