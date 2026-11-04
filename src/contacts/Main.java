package contacts;

import java.nio.file.Path;
import java.util.List;

/**
 * Entry point. For now it loads contacts.csv and prints them; the Swing
 * window arrives in the next roadmap step.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Path file = Path.of(args.length > 0 ? args[0] : "contacts.csv");
        List<Contact> contacts = new ContactStore(file).load();
        System.out.println(contacts.size() + " contact(s) in " + file);
        for (Contact c : contacts) {
            System.out.println("  " + c);
        }
    }
}
