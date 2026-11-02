import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class TimetableServer {

    static String[] days = {
        "Monday", "Tuesday", "Wednesday",
        "Thursday", "Friday", "Saturday", "Sunday"
    };

    static DateTimeFormatter timeFormat =
        DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(
            System.getenv().getOrDefault("PORT", "8080"));

        HttpServer server = HttpServer.create(
            new InetSocketAddress(port), 0);

        // Receives activity details and generates the timetable.
        server.createContext("/api/generate", exchange -> {
            try {
                if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    send(exchange, 405,
                        "{\"error\":\"Use POST to generate timetable.\"}",
                        "application/json; charset=UTF-8");
                    return;
                }

                String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8);

                Map<String, String> form = new HashMap<>();

                for (String pair : body.split("&")) {
                    if (pair.isEmpty()) continue;

                    String[] p = pair.split("=", 2);

                    form.put(
                        URLDecoder.decode(p[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(
                            p.length > 1 ? p[1] : "",
                            StandardCharsets.UTF_8)
                    );
                }

                send(exchange, 200, generate(form),
                    "application/json; charset=UTF-8");

            } catch (Exception e) {
                String message = e.getMessage() == null
                    ? "Invalid input" : e.getMessage();

                send(exchange, 400,
                    "{\"error\":" + json(message) + "}",
                    "application/json; charset=UTF-8");
            }
        });

        // Opens the HTML, CSS and JavaScript files.
        server.createContext("/", exchange -> {

            String path = exchange.getRequestURI().getPath();

            if (path.equals("/")) path = "/index.html";

            Path web = Paths.get("web").toAbsolutePath().normalize();
            Path file = web.resolve(path.substring(1)).normalize();

            if (!file.startsWith(web) || !Files.isRegularFile(file)) {
                send(exchange, 404, "File not found",
                    "text/plain; charset=UTF-8");
                return;
            }

            String name = file.toString().toLowerCase(Locale.ENGLISH);
            String type = "application/octet-stream";

            if (name.endsWith(".html"))
                type = "text/html; charset=UTF-8";

            else if (name.endsWith(".css"))
                type = "text/css; charset=UTF-8";

            else if (name.endsWith(".js"))
                type = "application/javascript; charset=UTF-8";

            exchange.getResponseHeaders().set("Cache-Control", "no-store");

            send(exchange, 200, Files.readString(file), type);
        });

        server.start();

        System.out.println(
            "Smart Timetable Generator running at http://localhost:" + port);
    }


    // Automatically generates the timetable.

    static String generate(Map<String, String> form) {

        String type = required(form, "timetableType");

        LocalTime start = parseTime(required(form, "startTime"));
        LocalTime end = parseTime(required(form, "endTime"));

        if (!end.isAfter(start))
            throw new IllegalArgumentException(
                "End time must be later than start time.");

        int available = (int) Duration.between(start, end).toMinutes();

        int breakTime = number(required(form, "breakMinutes"));

        if (breakTime < 0 || breakTime > 180)
            throw new IllegalArgumentException(
                "Break must be between 0 and 180 minutes.");

        String[] selectedDays = required(form, "days").split(",");

        int count = number(required(form, "activityCount"));

        if (count < 1 || count > 50)
            throw new IllegalArgumentException(
                "Add between 1 and 50 activities.");

        ArrayList<Activity> activities = new ArrayList<>();

        for (int i = 0; i < count; i++) {

            String name = required(form, "activityName" + i);

            double hours;

            try {
                hours = Double.parseDouble(
                    required(form, "duration" + i));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "Enter a valid duration for " + name + ".");
            }

            int duration = (int) Math.round(hours * 60);

            if (duration < 15 || duration % 15 != 0)
                throw new IllegalArgumentException(
                    "Use durations in 15-minute steps.");

            String priority = required(form, "priority" + i);

            if (!priority.equals("High")
                    && !priority.equals("Medium")
                    && !priority.equals("Low"))
                throw new IllegalArgumentException(
                    "Choose High, Medium or Low priority for " + name + ".");

            activities.add(
                new Activity(name, duration, priority));
        }

        // High priority first; longer activities first within each priority.
        activities.sort((a, b) -> {
            int p = priorityRank(b.priority) - priorityRank(a.priority);
            return p != 0 ? p : b.duration - a.duration;
        });

        Map<String, Integer> used = new LinkedHashMap<>();

        for (String day : selectedDays) {
            day = day.trim();

            if (!Arrays.asList(days).contains(day))
                throw new IllegalArgumentException("Invalid day: " + day);

            if (!used.containsKey(day))
                used.put(day, 0);
        }

        StringBuilder scheduled = new StringBuilder();
        StringBuilder unscheduled = new StringBuilder();

        for (Activity a : activities) {

            String bestDay = null;
            int least = Integer.MAX_VALUE;

            for (String day : used.keySet()) {

                int load = used.get(day);
                int breakBefore = load == 0 ? 0 : breakTime;

                if (load + breakBefore + a.duration <= available
                        && load < least) {

                    bestDay = day;
                    least = load;
                }
            }

            if (bestDay == null) {

                if (unscheduled.length() > 0)
                    unscheduled.append(",");

                unscheduled.append("{\"name\":")
                    .append(json(a.name))
                    .append(",\"reason\":\"Not enough free time\"}");

            } else {

                int load = used.get(bestDay);

                LocalTime activityStart =
                    start.plusMinutes(
                        load + (load == 0 ? 0 : breakTime));

                LocalTime activityEnd =
                    activityStart.plusMinutes(a.duration);

                if (scheduled.length() > 0)
                    scheduled.append(",");

                scheduled.append("{\"name\":")
                    .append(json(a.name))
                    .append(",\"day\":")
                    .append(json(bestDay))
                    .append(",\"start\":")
                    .append(json(activityStart.format(timeFormat)))
                    .append(",\"end\":")
                    .append(json(activityEnd.format(timeFormat)))
                    .append(",\"duration\":")
                    .append(a.duration)
                    .append(",\"priority\":")
                    .append(json(a.priority))
                    .append("}");

                used.put(bestDay,
                    load + a.duration +
                    (load == 0 ? 0 : breakTime));
            }
        }

        return "{\"type\":" + json(type) +
            ",\"scheduled\":[" + scheduled +
            "],\"unscheduled\":[" + unscheduled + "]}";
    }


    // Converts AM/PM time into Java time.

    static LocalTime parseTime(String value) {

        try {
            return LocalTime.parse(
                value.trim().toUpperCase(Locale.ENGLISH),
                timeFormat);

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                "Enter time like 9:00 AM or 5:00 PM.");
        }
    }


    static int number(String value) {

        try {
            return Integer.parseInt(value);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Enter a valid whole number.");
        }
    }


    static String required(Map<String, String> form, String key) {

        String value = form.get(key);

        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(
                "Please fill in " + key + ".");

        return value.trim();
    }


    // Gives High priority the highest ranking.

    static int priorityRank(String p) {

        if (p.equals("High")) return 3;
        if (p.equals("Medium")) return 2;

        return 1;
    }


    // Correctly formats text for JSON.

    static String json(String value) {

        StringBuilder out = new StringBuilder("\"");

        for (char c : value.toCharArray()) {

            if (c == '"' || c == '\\')
                out.append('\\');

            if (c == '\n')
                out.append("\\n");
            else if (c == '\r')
                out.append("\\r");
            else if (c == '\t')
                out.append("\\t");
            else
                out.append(c);
        }

        return out.append('"').toString();
    }


    // Sends the webpage or JSON response to the browser.

    static void send(
            HttpExchange exchange,
            int status,
            String text,
            String type) throws IOException {

        byte[] data = text.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", type);

        exchange.sendResponseHeaders(status, data.length);

        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }


    static class Activity {

        String name;
        int duration;
        String priority;

        Activity(String name, int duration, String priority) {
            this.name = name;
            this.duration = duration;
            this.priority = priority;
        }
    }
}