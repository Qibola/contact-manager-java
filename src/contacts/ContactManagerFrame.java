package contacts;

import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/** The main window: a table of contacts with a status line underneath. */
public class ContactManagerFrame extends JFrame {
    private final ContactTableModel model;
    private final JTable table;
    private final JLabel status = new JLabel();

    public ContactManagerFrame(Path file, List<Contact> contacts) {
        super("Contact Manager - " + file.getFileName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        model = new ContactTableModel(contacts);
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        updateStatus();

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        setSize(640, 400);
        setLocationRelativeTo(null);
    }

    private void updateStatus() {
        int n = model.getRowCount();
        status.setText(n + (n == 1 ? " contact" : " contacts"));
    }
}
