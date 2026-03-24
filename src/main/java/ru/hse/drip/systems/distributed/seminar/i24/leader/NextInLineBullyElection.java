package ru.hse.drip.systems.distributed.seminar.i24.leader;

import ru.hse.drip.systems.distributed.seminar.i24.common.RankedNode;

import java.util.List;
import java.util.Optional;

public class NextInLineBullyElection {
    public Optional<RankedNode> failover(List<Integer> alternates, List<RankedNode> nodes) {
        for (Integer candidateId : alternates) {
            for (RankedNode node : nodes) {
                if (node.getId() == candidateId && node.isAlive()) {
                    return Optional.of(node);
                }
            }
        }
        return Optional.empty();
    }
}
