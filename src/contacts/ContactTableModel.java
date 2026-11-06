package contacts;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Adapts a list of contacts to a JTable: one row per contact, with
 * Name / Phone / Email columns. Cells are not edited in place; changes go
 * through ContactDialog and then addContact / updateContact.
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
