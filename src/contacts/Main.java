package contacts;

import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * Entry point. Loads the CSV file and opens the main window. On a headless
 * machine (no display) it falls back to printing the contacts.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Path file = Path.of(args.length > 0 ? args[0] : "contacts.csv");
        List<Contact> contacts = new ContactStore(file).load();

        if (GraphicsEnvironment.isHeadless()) {
            System.out.println(contacts.size() + " contact(s) in " + file);
            for (Contact c : contacts) {
                System.out.println("  " + c);
            }
            return;
        }

        // Swing components must be created on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> new ContactManagerFrame(file, contacts).setVisible(true));
    }
}
