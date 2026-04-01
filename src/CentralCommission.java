import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CentralCommission {
    private static final int PORT = 4444;
    private final int expectedStations;

    private final ConcurrentHashMap<String, AtomicInteger> globalResults;
    private final AtomicInteger receivedReports = new AtomicInteger(0);
//    private final SimulationLogger logger;
    private final long startTime;

    public static void main(String[] args) {
        int stations = 3;
        if (args.length > 0) {
            stations = Integer.parseInt(args[0]);
        }
        CentralCommission server = new CentralCommission(stations);
        server.startServer();
    }

    public CentralCommission(int expectedStations) {
        this.expectedStations = expectedStations;
        this.startTime = System.currentTimeMillis();
        //this.logger = new SimulationLogger("log_centrala.txt", "[CENTRALA]");
        globalResults = new ConcurrentHashMap<>();
        for (String c : ElectionConfig.CANDIDATES) globalResults.put(c, new AtomicInteger(0));
    }

    public void startServer() {
        logToBoth("Serwer startuje (PID: " + ProcessHandle.current().pid() + "). Oczekuje na " + expectedStations + " komisji");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {


            for (int i = 0; i < expectedStations; i++) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        logToBoth("Zaakceptowano wszystkie polaczenia. Czekam na przetworzenie danych");

        while (receivedReports.get() < expectedStations) {
            try { Thread.sleep(100); } catch (InterruptedException e) {}
        }

        logToBoth("Przetworzono wszystkie " + expectedStations + " raportow.");
        printFinalResults();

//        logger.close();
        System.exit(0);
    }

    private void handleClient(Socket socket) {
        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
            int id = ois.readInt();
            Map<String, Integer> results = (Map<String, Integer>) ois.readObject();

            results.forEach((k, v) -> globalResults.get(k).addAndGet(v));

            synchronized (receivedReports) {
                int count = receivedReports.incrementAndGet();
//                logger.log("Odebrano raport z komisji " + id + " (Razem: " + count + "/" + expectedStations + ")");
            }

        } catch (Exception e) {
//            logger.log("Blad: " + e.getMessage());
        }
    }

    public void printFinalResults() {
        long duration = System.currentTimeMillis() - startTime;

        System.out.println();
        logToBoth("==========================================");
        logToBoth("   OFICJALNE WYNIKI (KOMISJA CENTRALNA)   ");
        logToBoth("==========================================");

        int totalVotes = 0;
        for (String candidate : ElectionConfig.CANDIDATES) {
            int count = globalResults.get(candidate).get();
            totalVotes += count;
            logToBoth(String.format(" %-15s : %d glosow", candidate, count));
        }

        logToBoth("------------------------------------------");
        logToBoth(" LACZNIE GLOSOW  : " + totalVotes);
        logToBoth(" CZAS CALKOWITY  : " + duration + " ms");
        logToBoth("==========================================");

        int maxScore = globalResults.values().stream()
                .mapToInt(AtomicInteger::get)
                .max()
                .orElse(0);

        String winners = globalResults.entrySet().stream()
                .filter(entry -> entry.getValue().get() == maxScore)
                .map(Map.Entry::getKey)
                .map(String::toUpperCase)
                .collect(Collectors.joining(", "));

        if (winners.contains(",")) {
            logToBoth(" WYNIK WYBORÓW   : REMIS (" + winners + ")");
        } else {
            logToBoth(" ZWYCIEZCA       : " + winners);
        }
        logToBoth("==========================================");
        System.out.println();
    }

    private void logToBoth(String msg) {
//        logger.log(msg);
        System.out.println(msg);
    }
}