package contacts;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Minimal dependency-free tests for ContactStore. Run with:
 *   java -cp out contacts.ContactStoreTest
 */
public class ContactStoreTest {
    private static int failures = 0;

    public static void main(String[] args) throws Exception {
        roundTripsSimpleContacts();
        roundTripsCommasQuotesAndNewlines();
        missingFileLoadsAsEmpty();
        rejectsMalformedRows();

        if (failures > 0) {
            System.out.println(failures + " test(s) FAILED");
            System.exit(1);
        }
        System.out.println("All tests passed");
    }

    static void roundTripsSimpleContacts() throws Exception {
        Path tmp = Files.createTempFile("contacts", ".csv");
        List<Contact> in = List.of(
                new Contact("Ada Lovelace", "555-0100", "ada@example.com"),
                new Contact("Alan Turing", "555-0101", "alan@example.com"));
        ContactStore store = new ContactStore(tmp);
        store.save(in);
        check("simple round trip", in.equals(store.load()));
        check("header written", Files.readAllLines(tmp).get(0).equals(ContactStore.HEADER));
        Files.delete(tmp);
    }

    static void roundTripsCommasQuotesAndNewlines() throws Exception {
        Path tmp = Files.createTempFile("contacts", ".csv");
        List<Contact> in = List.of(
                new Contact("Hopper, Grace", "555 \"work\"", "grace@example.com"),
                new Contact("Multi\nLine", "", ""));
        ContactStore store = new ContactStore(tmp);
        store.save(in);
        check("special characters round trip", in.equals(store.load()));
        Files.delete(tmp);
    }

    static void missingFileLoadsAsEmpty() throws Exception {
        Path missing = Path.of("does-not-exist-" + System.nanoTime() + ".csv");
        check("missing file is empty", new ContactStore(missing).load().isEmpty());
    }

    static void rejectsMalformedRows() throws Exception {
        Path tmp = Files.createTempFile("contacts", ".csv");
        Files.writeString(tmp, ContactStore.HEADER + "\nonly,two\n");
        try {
            new ContactStore(tmp).load();
            check("malformed row throws", false);
        } catch (java.io.IOException expected) {
            check("malformed row throws", true);
        }
        Files.delete(tmp);
    }

    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS " : "FAIL ") + name);
        if (!ok) failures++;
    }
}
