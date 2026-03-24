package ru.hse.drip.systems.distributed.seminar.i24.common;

public class RankedNode {
    private final int id;
    private final int rank;
    private boolean alive = true;

    public RankedNode(int id, int rank) {
        this.id = id;
        this.rank = rank;
    }

    public int getId() {
        return id;
    }

    public int getRank() {
        return rank;
    }

    public boolean isAlive() {
        return alive;
    }

    public void fail() {
        this.alive = false;
    }

    public void recover() {
        this.alive = true;
    }

    @Override
    public String toString() {
        return "Node{" + "id=" + id + ", rank=" + rank + ", alive=" + alive + '}';
    }
}
