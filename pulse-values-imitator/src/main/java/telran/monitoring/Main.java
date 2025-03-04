package telran.monitoring;
import java.net.*;
import java.util.*;
import telran.monitoring.api.SensorData;
import telran.monitoring.logging.Logger;
import telran.monitoring.logging.LoggerStandard;
import java.util.stream.IntStream;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class Main {
    static Logger logger = new LoggerStandard("imitator");

    static final int MIN_PULSE_VALUE = 40;
    static final int MAX_PULSE_VALUE = 240;
    static final long TIMEOUT_SEND = 500;
    static final int TIMEOUT_RESPONSE = 1000;
    static final String DEFAULT_HOST = "localhost";
    static final int DEFAULT_PORT = 5000;
    static final int DEFAULT_N_PATIENTS = 10;
    static final int DEFAULT_N_PACKETS = 50;

    // Параметры скачков
    static final double JUMP_PROB = 0.1; // 10% вероятность скачка
    static final double MIN_JUMP_PERCENT = 10; // Мин. 10% от текущего значения
    static final double MAX_JUMP_PERCENT = 100; // Макс. 100% от текущего значения
    static final double JUMP_POSITIVE_PROB = 0.7; // 70% вероятность положительного скачка

    static DatagramSocket socket = null;
    static Map<Long, Integer> lastPulseValues = new HashMap<>(); // Запоминаем последний пульс пациентов

    public static void main(String[] args) {
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT_RESPONSE);
            IntStream.rangeClosed(1, DEFAULT_N_PACKETS).forEach(Main::send);
        } catch (SocketException e) {
            System.err.println("Error creating socket: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    static void send(int i) {
        SensorData sensor = getRandomSensorData(i);
        String jsonStr = sensor.toString();
        try {
            udpSend(jsonStr);
        } catch (Exception e) {
            logger.log("severe", "Error sending packet: " + e.getMessage());
        }
    }

    private static void udpSend(String jsonStr) throws Exception {
        logger.log("finest", "Data to be sent: " + jsonStr);
        byte[] bufferSend = jsonStr.getBytes();
        DatagramPacket packet = new DatagramPacket(bufferSend, bufferSend.length,
                InetAddress.getByName(DEFAULT_HOST), DEFAULT_PORT);
        socket.send(packet);

        try {
            socket.receive(packet);
            String receivedData = new String(packet.getData(), 0, packet.getLength());
            if (!jsonStr.equals(receivedData)) {
                throw new Exception("Received packet doesn't match the sent one");
            }
        } catch (SocketTimeoutException e) {
            logger.log("warning", "Response timeout: no data received");
        }
    }

    private static SensorData getRandomSensorData(int i) {
        long patientId = getRandomNumber(1, DEFAULT_N_PATIENTS);
        int pulseValue = getRealisticPulseValue(patientId);
        long timestamp = System.currentTimeMillis();
        return new SensorData(patientId, pulseValue, timestamp);
    }

    private static int getRealisticPulseValue(long patientId) {
        int lastPulse = lastPulseValues.getOrDefault(patientId, getRandomNumber(MIN_PULSE_VALUE, MAX_PULSE_VALUE));

        if (Math.random() < JUMP_PROB) {
            int jumpPercent = getRandomNumber((int) MIN_JUMP_PERCENT, (int) MAX_JUMP_PERCENT);
            int jumpValue = (lastPulse * jumpPercent) / 100;

            if (Math.random() < JUMP_POSITIVE_PROB) {
                lastPulse = Math.min(MAX_PULSE_VALUE, lastPulse + jumpValue);
            } else {
                lastPulse = Math.max(MIN_PULSE_VALUE, lastPulse - jumpValue);
            }
        }

        lastPulseValues.put(patientId, lastPulse);
        return lastPulse;
    }

    private static int getRandomNumber(int minValue, int maxValue) {
        return ThreadLocalRandom.current().nextInt(minValue, maxValue + 1);
    }
}
