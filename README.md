# contact-manager-java

A small desktop contact manager written in Java Swing. Contacts (name, phone,
email) are stored in a plain CSV file so you can open them in any spreadsheet.

## Requirements

- JDK 17 or newer (`javac` and `java` on your PATH). On macOS: `brew install openjdk`.

## Build & run

```bash
javac -d out src/contacts/*.java
java -cp out contacts.Main            # opens a window on ./contacts.csv
java -cp out contacts.Main other.csv  # or a file of your choice
```

The main window shows your contacts in a table (Name / Phone / Email) with a
count in the status line. Use **Add...** to create a contact, and **Edit...**
(or double-click a row) to change one, and **Delete** (after a confirmation) to
remove one. Type in the **Search** box to filter the table by name, phone or
email (case-insensitive); the status line then shows "Showing N of M contacts".
The list starts sorted by name; click any column header to sort by it, and
click again to reverse (sorting ignores case and never changes the CSV order).
Phone and email are optional, but when filled in the dialog checks them: a
phone needs at least 7 digits and only `+ - . ( )` or spaces, and an email must
look like `name@example.com`.

Menus: **File** has Find (Cmd/Ctrl+F jumps to the search box) and Quit
(Cmd/Ctrl+Q); **Contact** has Add (Cmd/Ctrl+N), Edit (Cmd/Ctrl+E) and Delete
(Cmd/Ctrl+Backspace), the same actions as the toolbar buttons.

Every change is saved straight back to the CSV file. With no display (`-Djava.awt.headless=true`) it
prints the contacts to the terminal instead.

## Tests

The tests are plain Java with no external dependencies:

```bash
javac -d out src/contacts/*.java test/contacts/*.java
java -cp out contacts.ContactStoreTest
java -cp out contacts.ContactTableModelTest
java -Djava.awt.headless=true -cp out contacts.ContactDialogTest
```

## Project layout

```
src/contacts/Contact.java       # the Contact model (name, phone, email)
src/contacts/ContactStore.java  # CSV load/save with proper quoting
src/contacts/ContactTableModel.java   # adapts the contact list to a JTable + search matching
src/contacts/ContactManagerFrame.java # main window: menus + toolbar + search + sortable JTable + status line
src/contacts/ContactDialog.java       # modal add/edit dialog + phone/email validation
src/contacts/Main.java          # entry point (opens the window on the Swing EDT)
test/contacts/ContactStoreTest.java
test/contacts/ContactTableModelTest.java
test/contacts/ContactDialogTest.java
```

## CSV format

```
name,phone,email
Ada Lovelace,555-0100,ada@example.com
"Hopper, Grace",555-0102,grace@example.com
```

Fields with commas, quotes or newlines are quoted; quotes inside a field are doubled.

## Roadmap

- [x] Scaffold: README + Contact class + CSV load/save (with tests)
- [x] Main window: JFrame + JTable showing the loaded contacts
- [x] Add/edit dialog wired to the table, saving back to CSV
- [x] Delete contacts + search/filter box
- [x] Column sorting + input validation (email/phone) + menus and polish
