package ru.hse.drip.systems.distributed.seminar.i17;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Scenarios {

    public static void runAll(Path dir) throws Exception {
        System.out.println("=== Scenario 1: Happy path ===");
        scenarioHappy(dir.resolve("s1"));
        System.out.println();

        System.out.println("=== Scenario 2: Abort because insufficient funds ===");
        scenarioAbortInsufficientFunds(dir.resolve("s2"));
        System.out.println();

        System.out.println("=== Scenario 3: Drop COMMIT to one participant, coordinator retries ===");
        scenarioDropCommitRetry(dir.resolve("s3"));
        System.out.println();

        System.out.println("=== Scenario 4: Coordinator crash after decision, then recovery ===");
        scenarioCoordinatorCrashRecovery(dir.resolve("s4"));
        System.out.println();

        System.out.println("=== Scenario 5: Participant crashes on COMMIT after PREPARED, then restart + recovery ===");
        scenarioParticipantCrashOnCommitRecovery(dir.resolve("s5"));
        System.out.println();
    }

    private static void scenarioHappy(Path dir) throws Exception {
        Env env = Env.boot(dir, 123);
        env.shardA.setBalance("alice", 200);
        env.shardB.setBalance("bob", 10);

        Decision d = env.coordinator.run(transferPlan(env, 100));
        System.out.println("Decision: " + d);
        System.out.println("alice=" + env.shardA.getBalance("alice") + " bob=" + env.shardB.getBalance("bob"));
    }

    private static void scenarioAbortInsufficientFunds(Path dir) throws Exception {
        Env env = Env.boot(dir, 124);
        env.shardA.setBalance("alice", 50);
        env.shardB.setBalance("bob", 10);

        Decision d = env.coordinator.run(transferPlan(env, 100));
        System.out.println("Decision: " + d);
        System.out.println("alice=" + env.shardA.getBalance("alice") + " bob=" + env.shardB.getBalance("bob"));
    }

    private static void scenarioDropCommitRetry(Path dir) throws Exception {
        Env env = Env.boot(dir, 125);
        env.shardA.setBalance("alice", 200);
        env.shardB.setBalance("bob", 10);

        // Drop first COMMIT sent to shardB to simulate message loss.
        env.net.dropFirst(UnreliableNetwork.MsgType.COMMIT, env.pB.id(), 1);

        Decision d = env.coordinator.run(transferPlan(env, 100));
        System.out.println("Decision: " + d);
        System.out.println("alice=" + env.shardA.getBalance("alice") + " bob=" + env.shardB.getBalance("bob"));
    }

    private static void scenarioCoordinatorCrashRecovery(Path dir) throws Exception {
        Env env = Env.boot(dir, 126);
        env.shardA.setBalance("alice", 200);
        env.shardB.setBalance("bob", 10);

        // Drop COMMIT to shardB to simulate that coordinator logged decision but didn't deliver to B.
        env.net.dropFirst(UnreliableNetwork.MsgType.COMMIT, env.pB.id(), 100);

        Decision d = env.coordinator.run(transferPlan(env, 100));
        System.out.println("Decision (from first run): " + d);
        System.out.println("After crash moment: alice=" + env.shardA.getBalance("alice") + " bob=" + env.shardB.getBalance("bob"));

        // "Restart" coordinator with same log, heal network, and recover.
        env.net.dropFirst(UnreliableNetwork.MsgType.COMMIT, env.pB.id(), 0);

        Coordinator restarted = new Coordinator("coord",
                new FileDurableLog(dir.resolve("coord.log")),
                env.net, 3, 5);

        restarted.recoverAndFinish();
        System.out.println("After recovery: alice=" + env.shardA.getBalance("alice") + " bob=" + env.shardB.getBalance("bob"));
    }

    private static void scenarioParticipantCrashOnCommitRecovery(Path dir) throws Exception {
        Env env = Env.boot(dir, 127);
        env.shardA.setBalance("alice", 200);
        env.shardB.setBalance("bob", 10);

        // ShardB will crash on first COMMIT delivery (after it was PREPARED).
        env.net.crashOnFirst(UnreliableNetwork.MsgType.COMMIT, env.pB.id());

        Decision d = env.coordinator.run(transferPlan(env, 100));
        System.out.println("Decision: " + d);
        System.out.println("After first run: alice=" + env.shardA.getBalance("alice") + " bob=" + env.shardB.getBalance("bob")
                + " (shardB crashed=" + env.pB.isCrashed() + ")");

        // Restart participant B (new server instance, same log). It should load PREPARED from log.
        env.pB = Env.restartParticipantB(dir, env);
        env.net.register(env.pB);

        // Coordinator restart / recovery resends decision
        Coordinator restarted = new Coordinator("coord",
                new FileDurableLog(dir.resolve("coord.log")),
                env.net, 3, 5);
        restarted.recoverAndFinish();

        System.out.println("After participant restart + recovery: alice=" + env.shardA.getBalance("alice") + " bob=" + env.shardB.getBalance("bob"));
    }

    private static TxPlan transferPlan(Env env, long amount) {
        Map<String, String> ops = new LinkedHashMap<>();
        ops.put(env.pA.id(), "DEBIT alice " + amount);
        ops.put(env.pB.id(), "CREDIT bob " + amount);
        return new TxPlan(ops);
    }

    /**
     * Small environment builder for scenarios.
     */
    static final class Env {
        final Path dir;
        final UnreliableNetwork net;
        final BankShard shardA;
        final BankShard shardB;
        ParticipantServer pA;
        ParticipantServer pB;
        Coordinator coordinator;

        private Env(Path dir, UnreliableNetwork net, BankShard shardA, BankShard shardB) {
            this.dir = dir;
            this.net = net;
            this.shardA = shardA;
            this.shardB = shardB;
        }

        static Env boot(Path dir, long seed) throws Exception {
            java.nio.file.Files.createDirectories(dir);
            UnreliableNetwork net = new UnreliableNetwork(seed);
            net.setDuplicateProbability(0.2); // show idempotency matters

            BankShard shardA = new BankShard("shardA");
            BankShard shardB = new BankShard("shardB");

            ParticipantServer pA = new ParticipantServer("shardA", shardA, new FileDurableLog(dir.resolve("shardA.log")));
            ParticipantServer pB = new ParticipantServer("shardB", shardB, new FileDurableLog(dir.resolve("shardB.log")));

            net.register(pA);
            net.register(pB);

            Coordinator coord = new Coordinator("coord", new FileDurableLog(dir.resolve("coord.log")), net,
                    3, 5);

            Env env = new Env(dir, net, shardA, shardB);
            env.pA = pA;
            env.pB = pB;
            env.coordinator = coord;
            return env;
        }

        static ParticipantServer restartParticipantB(Path dir, Env env) throws Exception {
            // "process restart": new server instance, same log
            ParticipantServer newPB = new ParticipantServer("shardB", env.shardB, new FileDurableLog(dir.resolve("shardB.log")));
            newPB.setCrashed(false);
            return newPB;
        }
    }
}
