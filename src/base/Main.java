package base;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) {

        SharedData sharedData = new SharedData();
        Thread clientThread = new Thread(new Client(sharedData));
        Thread serverThread = new Thread(new Server(sharedData));

        System.out.println("Starting client and server...");

        clientThread.start();
        serverThread.start();

        try {
            clientThread.join();
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Client and server have been shut down.");
    }
}

class Server implements Runnable {

    private static final String[] someData = new String[]{"A", "B", "C", "D", "E", "STOP"};
    private SharedData sharedData;

    Server(SharedData sharedData) {
        this.sharedData = sharedData;
    }

    @Override
    public void run() {
        int i = 0;
        while (i < someData.length) {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(500, 2000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sharedData.addData(someData[i++]);
        }
    }
}

class Client implements Runnable {

    private SharedData sharedData;

    Client(SharedData sharedData) {
        this.sharedData = sharedData;
    }

    @Override
    public void run() {
        for (Optional<String> data = sharedData.getData(); data.isPresent(); data = sharedData.getData()) {
            if ("STOP".equals(data.get())) {
                break;
            } else {
                System.out.println(data.get());
            }
        }
    }
}

class SharedData {
    private volatile boolean addData = true;
    private volatile List<String> data = new ArrayList<>();

    Optional<String> getData() {
        synchronized (this) {
            while (addData) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Optional<String> dataToSendBack = data.isEmpty() ? Optional.empty() : Optional.of(data.remove(0));
            addData = true;
            notifyAll();
            return dataToSendBack;
        }
    }

    void addData(String str) {
        synchronized (this) {
            while (!addData) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            data.add(str);
            addData = false;
            notifyAll();
        }
    }
}