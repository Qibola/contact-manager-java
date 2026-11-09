package contacts;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.table.AbstractTableModel;

/**
 * Adapts a list of contacts to a JTable: one row per contact, with
 * Name / Phone / Email columns. Cells are not edited in place; changes go
 * through ContactDialog and then addContact / updateContact / removeContact.
 */
public class ContactTableModel extends AbstractTableModel {
    static final String[] COLUMNS = {"Name", "Phone", "Email"};

    private final List<Contact> contacts = new ArrayList<>();

    public ContactTableModel(List<Contact> initial) {
        contacts.addAll(initial);
    }

    /** Replaces every row, e.g. after reloading the CSV file. */
    public void setContacts(List<Contact> newContacts) {
        contacts.clear();
        contacts.addAll(newContacts);
        fireTableDataChanged();
    }

    /** A copy of the current rows, safe to hand to ContactStore.save. */
    public List<Contact> getContacts() {
        return new ArrayList<>(contacts);
    }

    public Contact getContactAt(int row) {
        return contacts.get(row);
    }

    /** Appends a contact and returns its row index. */
    public int addContact(Contact contact) {
        contacts.add(contact);
        int row = contacts.size() - 1;
        fireTableRowsInserted(row, row);
        return row;
    }

    /** Replaces the contact at the given row. */
    public void updateContact(int row, Contact contact) {
        contacts.set(row, contact);
        fireTableRowsUpdated(row, row);
    }

    /** Removes and returns the contact at the given row. */
    public Contact removeContact(int row) {
        Contact removed = contacts.remove(row);
        fireTableRowsDeleted(row, row);
        return removed;
    }

    /**
     * True if the contact's name, phone or email contains the query,
     * ignoring case and surrounding spaces. A blank query matches everyone.
     */
    public static boolean matches(Contact c, String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return true;
        for (String field : new String[] {c.getName(), c.getPhone(), c.getEmail()}) {
            if (field != null && field.toLowerCase(Locale.ROOT).contains(q)) return true;
        }
        return false;
    }

    @Override
    public int getRowCount() {
        return contacts.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return String.class;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Contact c = contacts.get(row);
        switch (column) {
            case 0: return c.getName();
            case 1: return c.getPhone();
            case 2: return c.getEmail();
            default: throw new IndexOutOfBoundsException("column " + column);
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
