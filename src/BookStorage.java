import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import javafx.scene.control.ListView;
import java.util.List;

public class BookStorage {
    private static final String FILE_NAME = "books.txt";

    /**
     * Saves the list of books to the file.
     *
     * @param bookList List of books to save.
     */
    private void saveBooksToFile(ListView<Book> bookList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Book book : bookList.getItems()) {
                // Save each book as: "Title","Author"
                writer.write("\"" + book.getTitle() + "\",\"" + book.getAuthor() + "\"");
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBooksFromFile(ListView<Book> bookList) {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Load each book by splitting into title and author
                    if (line.startsWith("\"") && line.endsWith("\"")) {
                        String[] parts = line.substring(1, line.length() - 1).split("\",\"");
                        if (parts.length == 2) {
                            String title = parts[0];
                            String author = parts[1];
                            bookList.getItems().add(new Book(title, author));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }}