package ru.hse.drip.systems.distributed.seminar.i24.leader;

import ru.hse.drip.systems.distributed.seminar.i24.common.RankedNode;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class BullyElection {
    public Optional<RankedNode> electLeader(RankedNode initiator, List<RankedNode> nodes) {
        return nodes.stream()
            .filter(RankedNode::isAlive)
            .filter(node -> node.getRank() > initiator.getRank())
            .max(Comparator.comparingInt(RankedNode::getRank))
            .or(() -> nodes.stream()
                .filter(RankedNode::isAlive)
                .filter(node -> node.getId() == initiator.getId())
                .findFirst());
    }
}
