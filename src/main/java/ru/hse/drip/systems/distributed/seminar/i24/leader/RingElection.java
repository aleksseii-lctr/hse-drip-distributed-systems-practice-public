package ru.hse.drip.systems.distributed.seminar.i24.leader;

import ru.hse.drip.systems.distributed.seminar.i24.common.RankedNode;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class RingElection {
    public OptionalInt elect(List<RankedNode> ring, int initiatorId) {
        List<Integer> seenRanks = new ArrayList<>();
        int start = indexOf(ring, initiatorId);
        if (start == -1) {
            return OptionalInt.empty();
        }
        for (int offset = 0; offset < ring.size(); offset++) {
            RankedNode node = ring.get((start + offset) % ring.size());
            if (node.isAlive()) {
                seenRanks.add(node.getRank());
            }
        }
        return seenRanks.stream().mapToInt(Integer::intValue).max();
    }

    private int indexOf(List<RankedNode> ring, int nodeId) {
        for (int i = 0; i < ring.size(); i++) {
            if (ring.get(i).getId() == nodeId) {
                return i;
            }
        }
        return -1;
    }
}
