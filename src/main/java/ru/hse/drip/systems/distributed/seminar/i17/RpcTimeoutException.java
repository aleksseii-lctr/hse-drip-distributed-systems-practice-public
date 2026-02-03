package ru.hse.drip.systems.distributed.seminar.i17;

public class RpcTimeoutException extends Exception {
    public RpcTimeoutException(String message) {
        super(message);
    }
}
