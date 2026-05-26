package console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dialogs.AboutDialog;
import dialogs.CalculationDialog;
import dialogs.OpenSignalDialog;
import dialogs.OperationsDialog;
import dialogs.RecordDialog;
import dialogs.SourceDialog;
import dialogs.TransformationsDialog;
import dialogs.WindowingDialog;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import signals.Signal;

public class Console {
    private final MenuBar menuBar;
    private final ToolBar toolBar = new ToolBar();
    private final TextArea consoleText;
    private final TextField consoleInput;
    private final CommandLineParser parser;
    private final Map<String, Signal> signals;
    private final Stage stage;
    private final ComboBox<String> viewSignal = new ComboBox<String>();
    // Flag to avoid duplicate handling when both Action and Hidden events fire
    private boolean viewSignalActionHandled = false;
    private boolean newSignal = false;

    public Console(VBox v, Stage stage) {
        this.stage = stage;
        menuBar = new MenuBar();
        consoleText = new TextArea();
        consoleInput = new TextField();
        signals = new LinkedHashMap<String, Signal>();
        parser = new CommandLineParser(signals, this);

        Menu menuFile = new Menu("File");
        MenuItem menuItemOpen = new MenuItem("Open");
        MenuItem menuItemExit = new MenuItem("Exit");
        menuFile.getItems().add(menuItemOpen);
        menuFile.getItems().add(menuItemExit);

        menuItemOpen.setOnAction(e -> {
            new OpenSignalDialog(parser);
        });
        menuItemExit.setOnAction(e -> {
            System.exit(0);
        });

        Menu menuCommands = new Menu("Commands");
        MenuItem menuSources = new MenuItem("Sources");
        MenuItem menuWindowing = new MenuItem("Windowing");
        MenuItem menuCalculations = new MenuItem("Math operations");
        MenuItem menuTransformations = new MenuItem("Transformations");
        MenuItem menuOtherOperations = new MenuItem("Other operations");
        menuCommands.getItems().add(menuSources);
        menuCommands.getItems().add(menuWindowing);
        menuCommands.getItems().add(menuCalculations);
        menuCommands.getItems().add(menuTransformations);
        menuCommands.getItems().add(menuOtherOperations);

        menuSources.setOnAction(e -> {
            new SourceDialog(parser);
        });
        menuWindowing.setOnAction(e -> {
            new WindowingDialog(parser);
        });
        menuCalculations.setOnAction(e -> {
            new CalculationDialog(parser, signals);
        });
        menuTransformations.setOnAction(e -> {
            new TransformationsDialog(parser, signals);
        });
        menuOtherOperations.setOnAction(e -> {
            new OperationsDialog(parser, signals);
        });

        Menu menuHelp = new Menu("Help");
        MenuItem menuAbout = new MenuItem("About");
        menuHelp.getItems().add(menuAbout);

        menuAbout.setOnAction(e -> {
            new AboutDialog();
        });

        menuBar.getMenus().add(menuFile);
        menuBar.getMenus().add(menuCommands);
        menuBar.getMenus().add(menuHelp);

        Button recordButton = new Button("REC");

        recordButton.setOnAction(event -> {
            new RecordDialog(parser);
        });

        // Add a label on the left side of the ComboBox to indicate its purpose
        Label viewSignalLabel = new Label("View:");

        // Add a text label and ComboBox for viewing signals
        viewSignal.setPromptText("Signal");

        viewSignal.setOnAction(e -> {
            // A new signal is already displayed, so we skip handling this event to avoid
            // duplicate calls to showSignal.
            if (newSignal) {
                return;
            }
            // Mark that action was handled so the onHidden handler doesn't duplicate work
            viewSignalActionHandled = true;
            Signal s = signals.get(viewSignal.getValue());
            parser.showSignal(s, true);
        });

        // Also trigger when the popup is hidden (this covers selecting the currently
        // selected item which does not change the ComboBox value and therefore
        // doesn't fire the Action event). We skip the call if the Action event
        // already handled the selection to avoid duplicate calls.
        viewSignal.setOnHidden(e -> {
            if (viewSignalActionHandled) {
                // reset the flag and skip duplicate handling
                viewSignalActionHandled = false;
                return;
            }
            String val = viewSignal.getValue();
            if (val != null) {
                Signal s = signals.get(val);
                parser.showSignal(s, true);
            }
        });

        // Add buttons to the ToolBar
        toolBar.getItems().add(recordButton);
        toolBar.getItems().add(viewSignalLabel);
        toolBar.getItems().add(viewSignal);

        consoleText.setEditable(false);
        consoleInput.setPromptText("Enter command here");

        consoleInput.setOnAction(e -> {
            String inputText = consoleInput.getText();
            // consoleText.appendText("> " + inputText + "\n");
            parser.parseCommand(inputText);
            consoleInput.clear();
            // Put focus back to console
            stage.requestFocus();
        });

        v.getChildren().add(menuBar);
        v.getChildren().add(toolBar);
        VBox.setVgrow(consoleText, Priority.ALWAYS);
        v.getChildren().add(consoleText);
        v.getChildren().add(consoleInput);

    }

    public void print(String message) {
        consoleText.appendText(message);
    }

    public void println(String message) {
        consoleText.appendText(message + "\n");
    }

    public void show() {
        stage.show();
    }

    public void addSignal(Signal s) {
        if (s == null || s.getName() == null) {
            return;
        }
        if (viewSignal.getItems().contains(s.getName())) {
            return; // Avoid adding duplicate signal names to the ComboBox
        }
        newSignal = true; // Mark that we're adding a new signal to avoid handling the Action event
        viewSignal.getItems().add(s.getName());
        // Sorting would fire a show signal.
        viewSignal.getItems().sort(String::compareTo); // Keep the ComboBox items sorted alphabetically
        // Setting the value would fire a show signal.
        viewSignal.setValue(s.getName()); // Automatically select the newly added signal
        newSignal = false; // Reset the flag after adding the signal
    }

    public void setSignal(String s) {
        if (s == null) {
            return;
        }
        newSignal = true; // Mark that we're setting a signal to avoid handling the Action event
        viewSignal.setValue(s);
        newSignal = false; // Reset the flag after setting the signal
    }

    public void removeSignal(String s) {
        if (s == null) {
            return;
        }
        viewSignal.getItems().remove(s);
    }
}
