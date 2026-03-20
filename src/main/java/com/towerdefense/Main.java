package com.towerdefense;

import com.towerdefense.server.GameServer;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        GameServer.start(8080);
        new CountDownLatch(1).await();
    }
}
