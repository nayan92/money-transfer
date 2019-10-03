package nayan92.moneytransfer.controller.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockManager {

    private final Map<Integer, Lock> locks = new HashMap<>();

    public synchronized Lock getLockForId(int id) {
        locks.putIfAbsent(id, new ReentrantLock());
        return locks.get(id);
    }

}
