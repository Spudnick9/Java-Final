import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.*;
import java.util.List;

public class BookCatalogueApp extends Application {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";
    private static final String FILE_NAME = "books.txt";

    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showLoginScreen();
    }

    private void showLoginScreen() {
        VBox loginLayout = new VBox(10);
        loginLayout.setPadding(new Insets(10));

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> checkLogin(usernameField.getText(), passwordField.getText()));

        loginLayout.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, loginButton);

        Scene loginScene = new Scene(loginLayout, 300, 200);
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    private void checkLogin(String username, String password) {
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            showCatalogueScreen();
        } else {
            Alert alert = new Alert(AlertType.ERROR, "Invalid credentials. Please try again.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void showCatalogueScreen() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label titleLabel = new Label("Book Catalogue");
        ListView<Book> bookList = new ListView<>();

        // Load books from file
        loadBooksFromFile(bookList);

        Button addButton = new Button("Add");
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");
        Button logoutButton = new Button("Logout");

        setupAddButton(addButton, bookList);
        setupEditButton(editButton, bookList);
        setupDeleteButton(deleteButton, bookList);
        setupLogoutButton(logoutButton);

        HBox buttonLayout = new HBox(10, addButton, editButton, deleteButton, logoutButton);
        buttonLayout.setPadding(new Insets(10, 0, 0, 0));

        layout.getChildren().addAll(titleLabel, bookList, buttonLayout);

        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Book Catalogue");
        primaryStage.show();
    }

    private void setupAddButton(Button addButton, ListView<Book> bookList) {
        addButton.setOnAction(e -> {
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Add Book");
            dialog.setHeaderText("Add a New Book");

            Label titleLabel = new Label("Book Name:");
            TextField titleField = new TextField();
            Label authorLabel = new Label("Author Name:");
            TextField authorField = new TextField();

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            grid.add(titleLabel, 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(authorLabel, 0, 1);
            grid.add(authorField, 1, 1);

            dialog.getDialogPane().setContent(grid);

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(button -> {
                if (button == ButtonType.OK) {
                    return new Pair<>(titleField.getText(), authorField.getText());
                }
                return null;
            });

            dialog.showAndWait().ifPresent(result -> {
                String bookName = result.getKey().trim();
                String authorName = result.getValue().trim();

                if (!bookName.isEmpty() && !authorName.isEmpty()) {
                    bookList.getItems().add(new Book(bookName, authorName));
                    saveBooksToFile(bookList);
                } else {
                    Alert alert = new Alert(AlertType.WARNING, "Both fields must be filled!", ButtonType.OK);
                    alert.showAndWait();
                }
            });
        });
    }

    private void setupEditButton(Button editButton, ListView<Book> bookList) {
        editButton.setOnAction(e -> {
            Book selectedBook = bookList.getSelectionModel().getSelectedItem();

            if (selectedBook == null) {
                Alert alert = new Alert(AlertType.WARNING, "Please select a book to edit.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Edit Book");
            dialog.setHeaderText("Edit Book Details");

            Label titleLabel = new Label("Book Name:");
            TextField titleField = new TextField(selectedBook.getTitle());
            Label authorLabel = new Label("Author Name:");
            TextField authorField = new TextField(selectedBook.getAuthor());

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            grid.add(titleLabel, 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(authorLabel, 0, 1);
            grid.add(authorField, 1, 1);

            dialog.getDialogPane().setContent(grid);

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(button -> {
                if (button == ButtonType.OK) {
                    return new Pair<>(titleField.getText(), authorField.getText());
                }
                return null;
            });

            dialog.showAndWait().ifPresent(result -> {
                String bookName = result.getKey().trim();
                String authorName = result.getValue().trim();

                if (!bookName.isEmpty() && !authorName.isEmpty()) {
                    selectedBook.setTitle(bookName);
                    selectedBook.setAuthor(authorName);
                    bookList.refresh();
                    saveBooksToFile(bookList);
                } else {
                    Alert alert = new Alert(AlertType.WARNING, "Both fields must be filled!", ButtonType.OK);
                    alert.showAndWait();
                }
            });
        });
    }

    private void setupDeleteButton(Button deleteButton, ListView<Book> bookList) {
        deleteButton.setOnAction(e -> {
            Book selectedBook = bookList.getSelectionModel().getSelectedItem();

            if (selectedBook != null) {
                bookList.getItems().remove(selectedBook);
                saveBooksToFile(bookList);
            } else {
                Alert alert = new Alert(AlertType.WARNING, "Please select a book to delete.", ButtonType.OK);
                alert.showAndWait();
            }
        });
    }

    private void setupLogoutButton(Button logoutButton) {
        logoutButton.setOnAction(e -> showLoginScreen());
    }

    private void saveBooksToFile(ListView<Book> bookList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Book book : bookList.getItems()) {
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
    }
}
