package ru.hse.drip.systems.distributed.seminar.i24.leader;

import ru.hse.drip.systems.distributed.seminar.i24.common.RankedNode;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LeaderCandidatesElection {
    public Optional<RankedNode> electFromCandidates(List<RankedNode> nodes, Set<Integer> candidateIds) {
        return nodes.stream()
            .filter(RankedNode::isAlive)
            .filter(node -> candidateIds.contains(node.getId()))
            .max(Comparator.comparingInt(RankedNode::getRank));
    }
}
