package contacts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and saves contacts as a CSV file with a header row:
 * name,phone,email
 *
 * Fields containing commas, quotes or newlines are wrapped in double
 * quotes, and embedded quotes are doubled (RFC 4180 style).
 */
public class ContactStore {
    static final String HEADER = "name,phone,email";

    private final Path file;

    public ContactStore(Path file) {
        this.file = file;
    }

    /** Reads all contacts. A missing file is treated as an empty list. */
    public List<Contact> load() throws IOException {
        List<Contact> contacts = new ArrayList<>();
        if (!Files.exists(file)) return contacts;

        String text = Files.readString(file, StandardCharsets.UTF_8);
        List<List<String>> rows = parse(text);
        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (i == 0 && String.join(",", row).equals(HEADER)) continue;
            if (row.size() == 1 && row.get(0).isEmpty()) continue; // blank line
            if (row.size() != 3) {
                throw new IOException("Line " + (i + 1) + ": expected 3 fields, got " + row.size());
            }
            contacts.add(new Contact(row.get(0), row.get(1), row.get(2)));
        }
        return contacts;
    }

    /** Overwrites the file with the given contacts. */
    public void save(List<Contact> contacts) throws IOException {
        try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            out.write(HEADER);
            out.newLine();
            for (Contact c : contacts) {
                out.write(escape(c.getName()) + "," + escape(c.getPhone()) + "," + escape(c.getEmail()));
                out.newLine();
            }
        }
    }

    static String escape(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /** Splits CSV text into rows of fields, honouring quoted fields. */
    static List<List<String>> parse(String text) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(ch);
                }
            } else if (ch == '"') {
                inQuotes = true;
            } else if (ch == ',') {
                row.add(field.toString());
                field.setLength(0);
            } else if (ch == '\n' || ch == '\r') {
                if (ch == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') i++;
                row.add(field.toString());
                field.setLength(0);
                rows.add(row);
                row = new ArrayList<>();
            } else {
                field.append(ch);
            }
        }
        if (field.length() > 0 || !row.isEmpty()) {
            row.add(field.toString());
            rows.add(row);
        }
        return rows;
    }
}
