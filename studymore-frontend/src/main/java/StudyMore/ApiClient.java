package StudyMore;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;

public class ApiClient {

    private static final String BASE      = "https://studymore-production.up.railway.app/api";
    private static final String AUTH_BASE = "https://studymore-production.up.railway.app";
    private static final HttpClient HTTP  = HttpClient.newHttpClient();

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
}