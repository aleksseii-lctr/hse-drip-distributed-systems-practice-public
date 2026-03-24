package ru.hse.drip.systems.distributed.seminar.i24.leader;

import ru.hse.drip.systems.distributed.seminar.i24.common.RankedNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InvitationAlgorithm {
    public static class Group {
        private final String name;
        private final List<RankedNode> members;

        public Group(String name, List<RankedNode> members) {
            this.name = name;
            this.members = new ArrayList<>(members);
        }

        public String getName() {
            return name;
        }

        public List<RankedNode> getMembers() {
            return members;
        }

        public Optional<RankedNode> leader() {
            return members.stream()
                .filter(RankedNode::isAlive)
                .max(Comparator.comparingInt(RankedNode::getRank));
        }
    }

    public Group merge(Group left, Group right) {
        List<RankedNode> merged = new ArrayList<>();
        merged.addAll(left.getMembers());
        merged.addAll(right.getMembers());
        return new Group(left.getName() + "+" + right.getName(), merged);
    }
}
