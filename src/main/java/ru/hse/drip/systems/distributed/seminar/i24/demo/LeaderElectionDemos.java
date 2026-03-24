package ru.hse.drip.systems.distributed.seminar.i24.demo;

import ru.hse.drip.systems.distributed.seminar.i24.common.Log;
import ru.hse.drip.systems.distributed.seminar.i24.common.RankedNode;
import ru.hse.drip.systems.distributed.seminar.i24.leader.*;

import java.util.List;
import java.util.Set;

public class LeaderElectionDemos {
    public static void runAll() {
        demoBully();
        demoNextInLine();
        demoLeaderCandidates();
        demoInvitation();
        demoRing();
    }

    public static void demoBully() {
        Log.title("Leader election #1: bully algorithm");
        List<RankedNode> nodes = sampleNodes();
        nodes.get(5).fail();
        RankedNode initiator = nodes.get(2);
        new BullyElection().electLeader(initiator, nodes)
            .ifPresent(leader -> Log.line("initiator=" + initiator.getId() + " -> new leader rank=" + leader.getRank()));
    }

    public static void demoNextInLine() {
        Log.title("Leader election #2.1: next-in-line failover");
        List<RankedNode> nodes = sampleNodes();
        nodes.get(5).fail();
        new NextInLineBullyElection().failover(List.of(5, 4), nodes)
            .ifPresent(leader -> Log.line("leader chosen from fallback list: node=" + leader.getId()));
    }

    public static void demoLeaderCandidates() {
        Log.title("Leader election #2.2: leader candidates");
        List<RankedNode> nodes = sampleNodes();
        nodes.get(5).fail();
        new LeaderCandidatesElection().electFromCandidates(nodes, Set.of(1, 2))
            .ifPresent(leader -> Log.line("candidate-subset leader: node=" + leader.getId() + ", rank=" + leader.getRank()));
    }

    public static void demoInvitation() {
        Log.title("Leader election #3: invitation algorithm");
        InvitationAlgorithm.Group left = new InvitationAlgorithm.Group("G1", List.of(
            new RankedNode(1, 1), new RankedNode(2, 2)));
        InvitationAlgorithm.Group right = new InvitationAlgorithm.Group("G2", List.of(
            new RankedNode(3, 3), new RankedNode(4, 4)));
        InvitationAlgorithm.Group merged = new InvitationAlgorithm().merge(left, right);
        merged.leader().ifPresent(leader -> Log.line("merged group=" + merged.getName() + ", leader=node " + leader.getId()));
    }

    public static void demoRing() {
        Log.title("Leader election #4: ring algorithm");
        List<RankedNode> nodes = sampleNodes();
        nodes.get(5).fail();
        int maxRank = new RingElection().elect(nodes, 3).orElse(-1);
        Log.line("ring election from node=3 -> max rank among alive nodes = " + maxRank);
    }

    private static List<RankedNode> sampleNodes() {
        return List.of(
            new RankedNode(1, 1),
            new RankedNode(2, 2),
            new RankedNode(3, 3),
            new RankedNode(4, 4),
            new RankedNode(5, 5),
            new RankedNode(6, 6)
        );
    }
}
