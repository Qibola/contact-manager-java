# contact-manager-java

A small desktop contact manager written in Java Swing. Contacts (name, phone,
email) are stored in a plain CSV file so you can open them in any spreadsheet.

## Requirements

- JDK 17 or newer (`javac` and `java` on your PATH). On macOS: `brew install openjdk`.

## Build & run

```bash
javac -d out src/contacts/*.java
java -cp out contacts.Main            # reads ./contacts.csv
java -cp out contacts.Main other.csv  # or a file of your choice
```

## Tests

The tests are plain Java with no external dependencies:

```bash
javac -d out src/contacts/*.java test/contacts/*.java
java -cp out contacts.ContactStoreTest
```

## Project layout

```
src/contacts/Contact.java       # the Contact model (name, phone, email)
src/contacts/ContactStore.java  # CSV load/save with proper quoting
src/contacts/Main.java          # entry point
test/contacts/ContactStoreTest.java
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
- [ ] Main window: JFrame + JTable showing the loaded contacts
- [ ] Add/edit dialog wired to the table, saving back to CSV
- [ ] Delete contacts + search/filter box
- [ ] Column sorting + input validation (email/phone) + menus and polish
