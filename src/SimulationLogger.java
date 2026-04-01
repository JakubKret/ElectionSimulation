import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SimulationLogger {
    private PrintWriter fileWriter;
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss:SSSSSS");
    private final String prefix;

    public SimulationLogger(String filename, String prefix) {
        this.prefix = prefix;
        try {
            this.fileWriter = new PrintWriter(new FileWriter(filename, false));
        } catch (IOException e) {
            System.err.println("Błąd inicjalizacji loggera: " + e.getMessage());
        }
    }

    public synchronized void log(String message) {
        String timestamp = "[" + LocalTime.now().format(timeFormat) + "] ";
        String fullMessage = timestamp + prefix + " " + message;


        if (fileWriter != null) {
            fileWriter.println(fullMessage);
            fileWriter.flush();
        }
    }

    public void close() {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }
}