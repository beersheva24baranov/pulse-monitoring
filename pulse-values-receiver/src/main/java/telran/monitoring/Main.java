package telran.monitoring;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.logging.Level;


public class Main {
    private static final int PORT = 5000;
    private static final int MAX_SIZE = 1500;

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        setupLogger();

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            logger.info("UDP-сервер запущен на порту " + PORT);
            byte[] buffer = new byte[MAX_SIZE];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Получаем строку с данными
                String receivedData = new String(packet.getData(), 0, packet.getLength()).trim();
                logger.finest("Получено JSON: " + receivedData);

                // Обрабатываем JSON вручную
                processSensorData(receivedData);

                // Отправляем обратно
                byte[] responseData = receivedData.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(
                        responseData, responseData.length, packet.getAddress(), packet.getPort());

                socket.send(responsePacket);
                logger.info("Отправлено обратно: " + receivedData);
            }
        } catch (Exception e) {
            logger.severe("Ошибка сервера: " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    private static void processSensorData(String json) {
        try {
            // Парсим JSON вручную
            long patientId = extractLong(json, "patientId");
            int value = extractInt(json, "value");
            long timestamp = extractLong(json, "timestamp");

            if (value > 230) {
                logger.severe("Критическое значение пульса: patientId=" + patientId + ", value=" + value);
            } else if (value > 220) {
                logger.warning("Высокое значение пульса: patientId=" + patientId + ", value=" + value);
            }
        } catch (Exception e) {
            logger.warning("Ошибка обработки JSON: " + e.getMessage());
        }
    }

    private static long extractLong(String json, String key) throws Exception {
        String value = extractValue(json, key);
        return Long.parseLong(value);
    }

    private static int extractInt(String json, String key) throws Exception {
        String value = extractValue(json, key);
        return Integer.parseInt(value);
    }

    private static String extractValue(String json, String key) throws Exception {
        int keyIndex = json.indexOf("\"" + key + "\":");
        if (keyIndex == -1) throw new Exception("Ключ " + key + " не найден в JSON");

        int start = json.indexOf(":", keyIndex) + 1;
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        if (end == -1) throw new Exception("Ошибка парсинга JSON");

        return json.substring(start, end).replaceAll("[^0-9]", "").trim();
    }

    private static void setupLogger() {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
    }
}