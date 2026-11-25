package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import oshi.SystemInfo;
import oshi.hardware.NetworkIF;

@Service
public class NetworkMonitoringService {

    private long previousReceived;
    private long previousSent;
    private long previousSampleTime;

    public synchronized NetworkMonitoringResponse getNetworkStats() {

        SystemInfo systemInfo = new SystemInfo();

        long totalReceived = 0;
        long totalSent = 0;
        int activeInterfaces = 0;

        List<NetworkInterfaceData> interfaces = new ArrayList<>();

        for (NetworkIF networkIF :
                systemInfo.getHardware().getNetworkIFs()) {

            networkIF.updateAttributes();
            if (networkIF.getIfOperStatus()
                    != NetworkIF.IfOperStatus.UP) {
                continue;
            }

            String displayName = networkIF.getDisplayName();

            if (displayName == null) {
                continue;
            }

            String normalizedName = displayName.toLowerCase();

            if (normalizedName.contains("hyper-v")
                    || normalizedName.contains("virtual")
                    || normalizedName.contains("wfp")
                    || normalizedName.contains("qos")
                    || normalizedName.contains("lightweight filter")
                    || normalizedName.contains("filter driver")) {
                continue;
            }
            long received = networkIF.getBytesRecv();
            long sent = networkIF.getBytesSent();

            totalReceived += received;
            totalSent += sent;
            activeInterfaces++;

            interfaces.add(new NetworkInterfaceData(
                    networkIF.getName(),
                    networkIF.getDisplayName(),
                    received,
                    sent
            ));
        }

        long currentTime = System.nanoTime();

        double receiveSpeed = 0;
        double transmitSpeed = 0;

        if (previousSampleTime > 0) {

            double elapsedSeconds =
                    (currentTime - previousSampleTime) / 1_000_000_000.0;

            if (elapsedSeconds > 0) {
                receiveSpeed = Math.max(0,
                        (totalReceived - previousReceived) / elapsedSeconds);

                transmitSpeed = Math.max(0,
                        (totalSent - previousSent) / elapsedSeconds);
            }
        }

        previousReceived = totalReceived;
        previousSent = totalSent;
        previousSampleTime = currentTime;

        return new NetworkMonitoringResponse(
                totalReceived,
                totalSent,
                receiveSpeed,
                transmitSpeed,
                activeInterfaces,
                interfaces
        );
    }

    public record NetworkInterfaceData(
            String name,
            String displayName,
            long bytesReceived,
            long bytesSent
    ) {}

    public record NetworkMonitoringResponse(
            long totalBytesReceived,
            long totalBytesSent,
            double receiveBytesPerSecond,
            double transmitBytesPerSecond,
            int activeInterfaceCount,
            List<NetworkInterfaceData> interfaces
    ) {}
}
