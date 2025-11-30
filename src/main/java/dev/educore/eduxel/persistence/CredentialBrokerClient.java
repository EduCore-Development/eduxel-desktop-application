package dev.educore.eduxel.persistence;

import dev.educore.eduxel.config.ClientConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CredentialBrokerClient {
    public DbCredentials fetchCredentials(ClientConfig cfg) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(cfg.getHost(), cfg.getPort()), 3000);
            socket.setSoTimeout(3000);

            String secret = cfg.getSecretPlain();
            OutputStream os = socket.getOutputStream();
            os.write((secret + "\n").getBytes(StandardCharsets.UTF_8));
            os.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String line = br.readLine();
            if (line == null) throw new IOException("No response from broker");
            if (!line.startsWith("OK;")) {
                throw new IOException("Broker rejected secret or invalid response: " + line);
            }
            String data = line.substring(3);
            Map<String, String> kv = parseKeyValues(data);
            String host = kv.getOrDefault("HOST", "localhost");
            int port = Integer.parseInt(kv.getOrDefault("PORT", "3306"));
            String user = kv.getOrDefault("USER", "");
            String pass = kv.getOrDefault("PASS", "");
            String db = kv.getOrDefault("DB", "");
            return new DbCredentials(host, port, user, pass, db);
        }
    }

    private Map<String, String> parseKeyValues(String data) {
        Map<String, String> map = new HashMap<>();
        String[] parts = data.split(";");
        for (String p : parts) {
            int idx = p.indexOf('=');
            if (idx > 0) {
                String k = p.substring(0, idx);
                String v = p.substring(idx + 1);
                map.put(k, v);
            }
        }
        return map;
    }
}
