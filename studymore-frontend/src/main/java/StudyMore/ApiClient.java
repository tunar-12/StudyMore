package StudyMore;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.Duration;

public class ApiClient {

    private static final String BASE      = "http://localhost:8080/api";
    private static final String AUTH_BASE = "http://localhost:8080";
    //private static final String BASE      = "https://studymore-production.up.railway.app/api";
    //private static final String AUTH_BASE = "https://studymore-production.up.railway.app";
    
    // Updated to include the connection timeout originally present in SyncClient
    private static final HttpClient HTTP  = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static String get(String path) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + path))
                    .header("Content-Type", "application/json")
                    .GET().build();
            HttpResponse<String> response = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("GET " + path + " → " + response.statusCode() + " " + response.body());
            return response.body();
        } catch (Exception e) {
            System.err.println("GET failed: " + e.getMessage());
            return "[]";
        }
    }

    public static String post(String path, String body) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + path))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(body)).build();
            HttpResponse<String> response = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("POST " + path + " → " + response.statusCode() + " " + response.body());
            return response.body();
        } catch (Exception e) {
            System.err.println("POST failed: " + e.getMessage());
            return "{}";
        }
    }

    public static String postAuth(String path, String body) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(AUTH_BASE + path))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(body)).build();
            HttpResponse<String> response = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("POST_AUTH " + path + " → " + response.statusCode() + " " + response.body());
            return response.body();
        } catch (Exception e) {
            System.err.println("POST_AUTH failed: " + e.getMessage());
            return "{}";
        }
    }

    public static String put(String path) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + path))
                    .header("Content-Type", "application/json")
                    .PUT(BodyPublishers.noBody()).build();
            HttpResponse<String> response = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("PUT " + path + " → " + response.statusCode() + " " + response.body());
            return response.body();
        } catch (Exception e) {
            System.err.println("PUT failed: " + e.getMessage());
            return "{}";
        }
    }

    public static JSONObject sync(long userId, JSONObject payload) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/sync/" + userId))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("SYNC /sync/" + userId + "-" + response.statusCode());
                return new JSONObject(response.body());
            } else {
                System.err.println("Sync failed — HTTP " + response.statusCode() + ": " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Sync network error: " + e.getMessage());
            return null;
        }
    }
}