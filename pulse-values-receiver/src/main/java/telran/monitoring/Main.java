package telran.monitoring;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import telran.monitoring.logging.*;
import telran.monitoring.api.SensorData;

public class Main {

    private static final int PORT = 5000;
    private static final int MAX_SIZE = 1500;
    private static final int WARNING_LOG_VALUE = 220;
    private static final int ERROR_LOG_VALUE = 230;
    private static final Logger logger = new LoggerStandard("receiver");

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[MAX_SIZE];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                handlePacket(packet, socket);
            }
        } catch (Exception e) {
            logger.log("error", "Socket error: " + e.getMessage());
        }
    }

    private static void handlePacket(DatagramPacket packet, DatagramSocket socket) {
        String jsonStr = new String(packet.getData(), 0, packet.getLength()).trim();
        logger.log("finest", "Received: " + jsonStr);

        try {
            SensorData sensorData = SensorData.of(jsonStr);
            logPulseValue(sensorData, WARNING_LOG_VALUE, ERROR_LOG_VALUE);
        } catch (Exception e) {
            logger.log("error", "Failed to parse sensor data: " + e.getMessage());
        }

        try {
            socket.send(packet);
        } catch (Exception e) {
            logger.log("error", "Failed to send response: " + e.getMessage());
        }
    }

    private static void logPulseValue(SensorData sensorData, int warningThreshold, int errorThreshold) {
        int value = sensorData.value();

        if (value >= warningThreshold && value <= errorThreshold) {
            logValue("warning", sensorData, warningThreshold);
        } else if (value > errorThreshold) {
            logValue("error", sensorData, errorThreshold);
        } else {
            logger.log("info", String.format("patient %d has normal pulse: %d", sensorData.patientId(), value));
        }
    }

    private static void logValue(String level, SensorData sensorData, int threshold) {
        logger.log(level, String.format("patient %d has pulse value greater than %d", sensorData.patientId(), threshold));
    }
}