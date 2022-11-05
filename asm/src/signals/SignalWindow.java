package signals;

import java.util.Arrays;
import java.util.Map;

import console.CommandLineParser;
import dialogs.SaveSignalDialog;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class SignalWindow {

    public static final int SIGNAL_HEIGHT = 250; /* Number of pixels vertical for signal */
    public static final int BODE_SIGNAL_HEIGHT = 200; /* Number of pixels vertical for signal */
    public static final double PH_MAG = 3.0; /* Ratio phase/magnitude for bode diagram */
    public static final int LEFT_XOFFS = 100; /* Margin left */
    public static final int RIGHT_XOFFS = 50; /* Margin right */
    public static final int BOTTOM_YOFFS = 50; /* Margin bottom */
    public static final int TOP_YOFFS = 50; /* Margin top */
    public static final double PIXEL_DIST = 100.0;
    public static final int USHRT_MAX = 65535;
    public static final int PHASE_MAX = 200;
    public static final int PHASE_MIN = -200;

    private final Signal signal;
    private final MenuBar menuBar = new MenuBar();
    private final ToolBar toolBar = new ToolBar();
    private final ComboBox<String> viewMode = new ComboBox<String>();
    private final ScrollPane scrollPane = new ScrollPane();
    private boolean viewModeReact = true;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Stage stage = new Stage();
    private boolean bon = false;
    private int screenWidth;
    VBox root = new VBox();
    Scene scene;

    public SignalWindow(Signal s, CommandLineParser parser, Map<String, Signal> signals) {
        this.signal = s;
        this.signal.setWindow(this);

        Menu menuFile = new Menu("File");
        MenuItem menuItemSave = new MenuItem("Save");
        MenuItem menuItemExit = new MenuItem("Exit");
        menuFile.getItems().add(menuItemSave);
        menuFile.getItems().add(menuItemExit);

        Menu menuView = new Menu("View");
        MenuItem menuItemConsole = new MenuItem("Console");
        menuView.getItems().add(menuItemConsole);

        menuItemSave.setOnAction(e -> {
            new SaveSignalDialog(parser, signals, s.getName());
        });
        menuItemExit.setOnAction(e -> {
            System.exit(0);
        });
        menuItemConsole.setOnAction(e -> {
            parser.showConsole();
        });
        menuBar.getMenus().add(menuFile);
        menuBar.getMenus().add(menuView);

        Button zoomInButton = new Button("+");
        Button zoomOutButton = new Button("-");

        zoomInButton.setOnAction(event -> {
            double hscale = s.getHScale() / 0.9;
            if (canRender(hscale)) {
                s.setHScale(hscale);
                this.show(bon);
            }
        });
        zoomOutButton.setOnAction(event -> {
            s.setHScale(s.getHScale() * 0.9);
            this.show(bon);
        });

        // Add buttons to the ToolBar
        toolBar.getItems().add(zoomInButton);
        toolBar.getItems().add(zoomOutButton);
        if (signal.getDataDomain() == Signal.TIME || signal.getDataDomain() == Signal.FREQ) {
            toolBar.getItems().add(viewMode);
        }
        viewMode.setOnAction(e -> {
            if (viewModeReact) {
                if (viewMode.getValue().equals("real")) {
                    signal.setMode(Signal.REAL_M);
                    this.show(bon);
                }
                if (viewMode.getValue().equals("imag")) {
                    signal.setMode(Signal.IMAG_M);
                    this.show(bon);
                }
                if (viewMode.getValue().equals("bode")) {
                    signal.setMode(Signal.BODE_M);
                    this.show(bon);
                }
            }
        });
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        screenWidth = (int) screenBounds.getWidth();
        System.out.println("screen width " + screenWidth);

        double sceneWidth = getCanvasWidth() > screenWidth * 0.8 ? screenWidth * 0.8 : getCanvasWidth();
        // TODO : get actual height of menu bar.
        // menuBar.getheight() returns 0.0. Now adding 50.
        // toolBar.getHeight() idem
        scene = new Scene(root, sceneWidth, getCanvasHeight() + 50 + 50);
        canvas = new Canvas(getCanvasWidth(), getCanvasHeight());
        gc = canvas.getGraphicsContext2D();

        root.getChildren().add(menuBar);
        root.getChildren().add(toolBar);
        root.getChildren().add(scrollPane);
        scrollPane.setPrefSize(sceneWidth, signal.getWindowHeight());
        scrollPane.setContent(canvas);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        this.show(bon);
    }

    public void show(boolean bon) {
        this.bon = bon;

        viewModeReact = false;
        viewMode.getItems().clear();
        if (signal.getDataType() == Signal.REAL || signal.getDataType() == Signal.COMP) {
            viewMode.getItems().add("real");
        }
        if (signal.getDataType() == Signal.IMAG || signal.getDataType() == Signal.COMP) {
            viewMode.getItems().add("imag");
        }
        if (signal.getDataDomain() == Signal.FREQ) {
            viewMode.getItems().add("bode");
        }

        String mode;
        switch (signal.getMode()) {
        case Signal.REAL_M:
            mode = "  Real";
            viewMode.getSelectionModel().select("real");
            break;
        case Signal.IMAG_M:
            mode = "  Imaginary";
            viewMode.getSelectionModel().select("imag");
            break;
        case Signal.MAGN_M:
            mode = "  Magnitude";
            break;
        case Signal.BODE_M:
            mode = "  Bode";
            viewMode.getSelectionModel().select("bode");
            break;
        case Signal.PHAS_M:
            mode = "  Phase";
            break;
        default:
            mode = "";
            break;
        }
        switch (signal.getDataDomain()) {
        case Signal.TIME:
            mode += " (Time)";
            break;
        case Signal.FREQ:
            mode += " (Frequency)";
            break;
        default:
            break;
        }

        viewModeReact = true;

        canvas.setWidth(getCanvasWidth());
        canvas.setHeight(getCanvasHeight());
        signal.setWindowWidth((int) canvas.getWidth());
        signal.setWindowHeight((int) canvas.getHeight());
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        switch (this.signal.getMode()) {
        case Signal.REAL_M:
        case Signal.IMAG_M:
            if (this.signal.getDataDomain() == Signal.TIME)
                genSignalBackground(this.signal, gc);
            if (this.signal.getDataDomain() == Signal.FREQ)
                gen_freq_background(this.signal, gc);
            genSignalPlot(this.signal, gc);
            break;
        case Signal.BODE_M:
            genBodeBackground(this.signal, gc);
            genBodePlot(this.signal, gc);
            break;
        case Signal.MAGN_M:
            gen_magnitude_background(this.signal, gc);
            gen_magnitude_plot(this.signal, gc);
            break;
        case Signal.PHAS_M:
            gen_phase_background(this.signal, gc);
            gen_phase_plot(this.signal, gc);
            break;
        case Signal.HIST_M:
            gen_hist_background(this.signal, gc);
            gen_hist_plot(this.signal, gc);
            break;
        default:
            break;
        }

        stage.setScene(scene);
        stage.setTitle("Signal " + this.signal.getName() + " " + mode);
        if (canvas.getWidth() < stage.getWidth()
                || (canvas.getWidth() > stage.getWidth() && canvas.getWidth() < screenWidth * 0.8))
            stage.setWidth(canvas.getWidth());
        stage.show();
    }

    private int getCanvasWidth(double newHScale) {
        return (int) (newHScale * signal.getDataLength() + LEFT_XOFFS + RIGHT_XOFFS);
    }

    private int getCanvasWidth() {
        return (int) getCanvasWidth(signal.getHScale());
    }

    private int getCanvasHeight() {
        if (signal.getMode() == Signal.BODE_M) {
            return BODE_SIGNAL_HEIGHT + (int) (BODE_SIGNAL_HEIGHT / PH_MAG) + 2 * TOP_YOFFS + BOTTOM_YOFFS;
        } else {
            return SIGNAL_HEIGHT + TOP_YOFFS + BOTTOM_YOFFS;
        }
    }

    public boolean canRender(double hscale) {
        // If the canvas becomes very large the rendering runs out of
        // its allocated VRAM space. This causes a NullPointerException.
        // Default limits may vary per OS. It is possible to allocate
        // more VRAM by command line options.
        // For now limit the canvas size. On Windows 10 I noticed problems
        // above 2.9M pixels.
        return getCanvasWidth(hscale) * getCanvasHeight() < 2900000;
    }

    public void close() {
        stage.close();
    }

    private double getRecordMax(Signal s) {
        int offset = (s.getChannel() * s.getDataRecords() + s.getRecord()) * s.getDataLength();
        double[] data = (s.getMode() == Signal.IMAG_M) ? s.getImagData() : s.getRealData();
        double max = data[offset];
        for (int i = 1; i < s.getDataLength(); i++) {
            if (data[offset + i] > max)
                max = data[offset + i];
        }
        return max;
    }

    private double getRecordMin(Signal s) {
        int offset = (s.getChannel() * s.getDataRecords() + s.getRecord()) * s.getDataLength();
        double[] data = (s.getMode() == Signal.IMAG_M) ? s.getImagData() : s.getRealData();
        double min = data[offset];
        for (int i = 1; i < s.getDataLength(); i++) {
            if (data[offset + i] < min)
                min = data[offset + i];
        }
        return min;
    }

    private void genSignalBackground(Signal s, GraphicsContext gc) {
        int length = s.getWindowWidth() - LEFT_XOFFS - RIGHT_XOFFS;
        int maxy = s.getWindowHeight();
        int height = s.getWindowHeight() - TOP_YOFFS - BOTTOM_YOFFS;
        double distance = PIXEL_DIST;

        int nrOfTextLabels = (int) ((length / distance) + 1);
        double totalTime = (double) s.getDataLength() / (double) (s.getDataSampleRate() * 10);
        double deltaT = totalTime / (double) (nrOfTextLabels - 1);
        distance = (double) length / (double) (nrOfTextLabels - 1);

        double unit = 1.0;
        char unitStr = ' ';

        if (totalTime < (1.0)) {
            unit = 1e-3;
            unitStr = 'm';
        }
        if (totalTime < (1e-3)) {
            unit = 1e-6;
            unitStr = 'u';
        }
        if (totalTime < (1e-6)) {
            unit = 1e-9;
            unitStr = 'n';
        }
        if (totalTime < (1e-9)) {
            unit = 1e-11;
            unitStr = 'p';
        }

        gc.setStroke(Color.GRAY);
        double fontHeight = gc.getFont().getSize();
        gc.strokeText("--> Time [" + unitStr + "s]", length / 2, maxy - fontHeight);

        deltaT = deltaT / unit;

        /* print 0 x-axis, y-axis text */

        gc.strokeText("0", LEFT_XOFFS, maxy - 28);

        /* print record_nr and channel_nr */

        String ch = String.format("Rec: %d - Chan: %d", s.getRecord(), s.getChannel());
        gc.strokeText(ch, (LEFT_XOFFS + length - 200), (TOP_YOFFS - 10));

        for (int i = 1; i < nrOfTextLabels; i++) {
            double xValue = (deltaT * i);
            ch = String.format("%3.2f", xValue);
            gc.strokeText(ch, (int) (i * distance + LEFT_XOFFS - 25), (int) (maxy - 28));
        }

        /* draw vertical grid */
        gc.setStroke(Color.GRAY);
        for (int i = 1; i < nrOfTextLabels; i++)
            gc.strokeLine(LEFT_XOFFS + i * distance, (TOP_YOFFS), LEFT_XOFFS + i * distance, (height + TOP_YOFFS));

        /* draw the x and y axis in black (+ up and right) */
        gc.setStroke(Color.BLACK);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, LEFT_XOFFS, maxy - BOTTOM_YOFFS);
        gc.strokeLine(length + LEFT_XOFFS, TOP_YOFFS, length + LEFT_XOFFS, maxy - BOTTOM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, (maxy - BOTTOM_YOFFS), length + LEFT_XOFFS, maxy - BOTTOM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, length + LEFT_XOFFS, TOP_YOFFS);

        double Minimum = getRecordMin(s);
        double Maximum = getRecordMax(s);
        if ((Maximum <= 1) && (Maximum >= 0.0))
            Maximum = 1;
        if ((Minimum >= -1) && (Minimum <= 0.0))
            Minimum = -1;

        s.setMinimum(Minimum);
        s.setMaximum(Maximum);

        nrOfTextLabels = 9;
        distance = (int) (-height / (nrOfTextLabels - 1));

        double Max_abs = (double) Math.max(Math.abs(Maximum), Math.abs(Minimum)); /* Absolute maximum */
        double deltaY = Max_abs * 2 / (nrOfTextLabels - 1);
        s.setVScale(Max_abs / (height / 2.0));

        gc.setStroke(Color.GRAY);
        if (Max_abs >= USHRT_MAX) {
            for (int i = 0; i < nrOfTextLabels; i++) {
                double yValue = Max_abs - deltaY * i;
                ch = String.format("%8.2g", yValue);
                gc.strokeText(ch, (LEFT_XOFFS - 95), (int) -(i * distance - TOP_YOFFS - 5));
            }
        }

        if (Max_abs < USHRT_MAX) {
            for (int i = 0; i < nrOfTextLabels; i++) {
                double yValue = Max_abs - deltaY * i;
                ch = String.format("%5.2f", yValue);
                gc.strokeText(ch, (LEFT_XOFFS - 95), (int) -(i * distance - TOP_YOFFS - 5));
            }
        }

        gc.setStroke(Color.BLACK);
        for (int i = 1; i < nrOfTextLabels - 1; i++)
            gc.strokeLine(LEFT_XOFFS + 1, (int) -(i * distance - TOP_YOFFS), length + LEFT_XOFFS - 1,
                    (int) -(i * distance - TOP_YOFFS));
    }

    private void gen_freq_background(Signal s, GraphicsContext gc) {
        int length, height, nrOfTextLabels, sampleRate;
        double delta_f, delta_y, Max_abs;
        double unit, distance;

        String nm = "Signal " + s.getName();

        if (s.getMode() == Signal.REAL_M)
            nm += "  Real (Freq)";
        else
            nm += "  Imaginary (Freq)";

        length = s.getWindowWidth() - LEFT_XOFFS - RIGHT_XOFFS; /* Length horizontal axis */
        height = s.getWindowHeight() - TOP_YOFFS - BOTTOM_YOFFS; /* Length vertical axis */
        distance = PIXEL_DIST; /* Number of pixels between to numbers */
        sampleRate = s.getDataSampleRate() * 10; /* Samplerate in Hz */
        int maxy = s.getWindowHeight();

        gc.setStroke(Color.RED);
        double fontHeight = gc.getFont().getSize();
        gc.strokeText(nm, (length / 2), fontHeight);

        nrOfTextLabels = (int) ((length / distance) + 1); /* Amount of numbers horizontal axis */
        delta_f = (double) (sampleRate) / (double) (nrOfTextLabels - 1); /* freq between two numbers */
        distance = (double) (length) / (double) (nrOfTextLabels - 1); /* Number of pixels between two numbers */

        unit = 1e3; /* Choose unit 1000 (kHz) */
        delta_f = delta_f / unit;

        gc.setStroke(Color.GRAY);
        gc.strokeText("--> Frequency [kHz]", (length / 2), maxy - fontHeight);

        /* print 0 x-axis text */
        gc.strokeText("0", LEFT_XOFFS, maxy - 28);

        for (int i = 1; i < nrOfTextLabels; i++) {
            double xValue = delta_f * i;
            gc.strokeText(String.format("%3.2f", xValue), (int) (i * distance + LEFT_XOFFS - 25), (int) (maxy - 28));
        }

        /* print record_nr and channel_nr */
        String ch = String.format("Rec: %d - Chan: %d", s.getRecord(), s.getChannel());
        gc.strokeText(ch, LEFT_XOFFS + length - 200, TOP_YOFFS - 10);

        /* draw vertical grid */
        for (int i = 1; i < nrOfTextLabels; i++)
            gc.strokeLine(LEFT_XOFFS + i * distance, TOP_YOFFS, LEFT_XOFFS + i * distance, height + TOP_YOFFS);

        /* draw the x and y axis in black (+ top and right) */
        gc.setStroke(Color.BLACK);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, LEFT_XOFFS, maxy - BOTTOM_YOFFS);
        gc.strokeLine(length + LEFT_XOFFS, TOP_YOFFS, length + LEFT_XOFFS, maxy - BOTTOM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, (maxy - BOTTOM_YOFFS), length + LEFT_XOFFS, maxy - BOTTOM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, length + LEFT_XOFFS, TOP_YOFFS);

        double Minimum = getRecordMin(s);
        double Maximum = getRecordMax(s);

        s.setMaximum(Maximum);
        s.setMinimum(Minimum);

        nrOfTextLabels = 9;
        distance = (int) (-height / (nrOfTextLabels - 1));

        Max_abs = (double) Math.max(Math.abs(Maximum), Math.abs(Minimum)); /* Absolute maximum */
        if (Max_abs == 0.0)
            Max_abs = 1;
        delta_y = Max_abs * 2 / (nrOfTextLabels - 1);

        s.setVScale(Max_abs / (height / 2));

        gc.setStroke(Color.GRAY);
        if (Max_abs >= USHRT_MAX) {
            for (int i = 0; i < nrOfTextLabels; i++) {
                double yValue = (int) (Max_abs - delta_y * i);
                ch = String.format("%8.2g", yValue);
                gc.strokeText(ch, (LEFT_XOFFS - 95), (int) -(i * distance - TOP_YOFFS - 5));
            }
        }

        if (Max_abs < USHRT_MAX) {
            for (int i = 0; i < nrOfTextLabels; i++) {
                double yValue = Max_abs - delta_y * i;
                ch = String.format("%5.2f", yValue);
                gc.strokeText(ch, (LEFT_XOFFS - 95), (int) -(i * distance - TOP_YOFFS - 5));
            }
        }

        gc.setStroke(Color.BLACK);
        for (int i = 1; i < nrOfTextLabels - 1; i++)

            gc.strokeLine(LEFT_XOFFS + 1, (int) -(i * distance - TOP_YOFFS), length + LEFT_XOFFS - 1,
                    (int) -(i * distance - TOP_YOFFS));
    }

    private void genSignalPlot(Signal s, GraphicsContext gc) {
        int nul_offs, height, x1, x2, y1, y2;
        double vscale, hscale;
        double[] data; /* pointer to data to display */
        double[] temp; /* temporary copy for scaling */

        vscale = s.getVScale();
        hscale = s.getHScale();
        height = s.getWindowHeight() - TOP_YOFFS - BOTTOM_YOFFS;

        temp = new double[s.getDataLength()];

        data = s.getRealData(); /* display real data */

        if (s.getMode() == Signal.IMAG_M) {
            data = s.getImagData(); /* display imaginary data */
        }

        /* Which record to display? */

        int offset = (s.getChannel() * s.getDataRecords() + s.getRecord()) * s.getDataLength();

        // System.out.println(String.format("Signal %s, Channel %d, Record
        // %d\n",s.getName(),s.getChannel(),s.getRecord()));

        for (int i = 0; i < s.getDataLength(); i++) {
            temp[i] = data[offset + i];
        }

        for (int i = 0; i < s.getDataLength(); i++) {
            temp[i] = (temp[i] / vscale);
        }
        switch (s.getDataDomain()) {
        case Signal.TIME:
            gc.setStroke(Color.RED);
            nul_offs = height / 2 + TOP_YOFFS;

            if (!bon) {
                for (int i = 0; i < s.getDataLength() - 1; i++) {
                    x1 = (int) (hscale * i + LEFT_XOFFS);
                    x2 = (int) (hscale * (i + 1) + LEFT_XOFFS);
                    y1 = (int) -(temp[i] - nul_offs);
                    y2 = (int) -(temp[i + 1] - nul_offs);
                    // System.out.println(String.format(" %d %d %d %d",x1,y1,x2,y2));
                    gc.strokeLine(x1, y1, x2, y2);
                }
            } else {
                for (int i = 0; i < s.getDataLength(); i++) {
                    gc.strokeLine((int) (hscale * i + LEFT_XOFFS), (int) (nul_offs), (int) (hscale * i + LEFT_XOFFS),
                            (int) -(temp[i] - nul_offs));
                }
            }
            break;

        case Signal.FREQ:
            gc.setStroke(Color.GREEN);
            nul_offs = (int) (TOP_YOFFS + (s.getMaximum() / s.getVScale()));

            if (!bon) {
                for (int i = 0; i < (s.getDataLength() - 1); i++) {
                    x1 = (int) (hscale * i + LEFT_XOFFS);
                    x2 = (int) (hscale * (i + 1) + LEFT_XOFFS);
                    gc.strokeLine(x1, (int) -(temp[i] - nul_offs), x2, (int) -(temp[i + 1] - nul_offs));
                }
            } else
                for (int i = 0; i < (s.getDataLength()); i++) {
                    gc.strokeLine((int) (hscale * i + LEFT_XOFFS), (int) (nul_offs), (int) (hscale * i + LEFT_XOFFS),
                            (int) -(temp[i] - nul_offs));
                }

            break;
        case Signal.MAGN:
            gc.setStroke(Color.RED);
            nul_offs = height / 2 + TOP_YOFFS;

            if (!bon) {
                for (int i = 0; i < (s.getDataLength() - 1); i++) {
                    gc.strokeLine((int) (hscale * i + LEFT_XOFFS), (int) -(temp[i] - nul_offs),
                            (int) (hscale * (i + 1) + LEFT_XOFFS), (int) -(temp[i + 1] - nul_offs));
                }
            } else {
                for (int i = 0; i < (s.getDataLength()); i++) {
                    gc.strokeLine((int) (hscale * i + LEFT_XOFFS), (int) (nul_offs), (int) (hscale * i + LEFT_XOFFS),
                            (int) -(temp[i] - nul_offs));
                }
            }
            break;

        default:
            break;
        }

    }

    private void genBodeBackground(Signal s, GraphicsContext gc) {
        int magHeight, phaseHeight, nrOfText, sampleRate;
        int mag_top_offs;
        int delta_ph;
        double deltaFreq, delta_db, Max_abs;
        double unit, distance;

        String nm = "Signal " + s.getName() + "   BodeDiagram";

        magHeight = BODE_SIGNAL_HEIGHT;
        phaseHeight = (int) (BODE_SIGNAL_HEIGHT / PH_MAG);
        mag_top_offs = phaseHeight + 2 * TOP_YOFFS;

        int length = s.getWindowWidth() - LEFT_XOFFS - RIGHT_XOFFS; /* Length horizontal axis */
        int maxy = s.getWindowHeight();
        /* Horizontal axis + text */
        gc.setStroke(Color.RED);
        double fontHeight = gc.getFont().getSize();
        gc.strokeText(nm, length / 2, fontHeight);

        distance = PIXEL_DIST; /* Nr of pixels between two numbers */
        sampleRate = s.getDataSampleRate() * 10; /* Sample rate in Hz */

        nrOfText = (int) ((length / distance) + 1); /* Nr of numbers horizontal axis */
        deltaFreq = (double) (sampleRate) / (double) (nrOfText - 1); /* freq between two numbers */
        distance = (double) (length) / (double) (nrOfText - 1); /* Nr of pixels between two numbers */

        unit = 1e3; /* Choose unit 1000(kHz) */
        deltaFreq = deltaFreq / unit;

        gc.setStroke(Color.GRAY);
        gc.strokeText("--> Frequency [kHz]", length / 2, maxy - fontHeight);

        // print 0 x-as text
        gc.strokeText("0", LEFT_XOFFS, maxy - 28);

        for (int i = 1; i < nrOfText; i++) {
            double xValue = (deltaFreq * i);
            String ch = String.format("%3.2f", xValue);
            gc.strokeText(ch, (int) (i * distance + LEFT_XOFFS - 25), (int) (maxy - 28));
        }

        // print record_nr and channel_nr
        String ch = String.format("          Chan: %d", s.getChannel());
        gc.strokeText(ch, LEFT_XOFFS + length - 200, TOP_YOFFS - 10);

        // draw vertical grid
        gc.setStroke(Color.BLACK);
        for (int i = 1; i < nrOfText; i++) {
            gc.strokeLine(LEFT_XOFFS + i * distance, (TOP_YOFFS), LEFT_XOFFS + i * distance, (phaseHeight + TOP_YOFFS));
            gc.strokeLine(LEFT_XOFFS + i * distance, (mag_top_offs), LEFT_XOFFS + i * distance,
                    (mag_top_offs + magHeight));
        }

        // draw the x and y axis in black (+ up and right)
        gc.strokeLine(LEFT_XOFFS, mag_top_offs, LEFT_XOFFS, (maxy - BOTTOM_YOFFS));
        gc.strokeLine(length + LEFT_XOFFS, mag_top_offs, length + LEFT_XOFFS, (maxy - BOTTOM_YOFFS));
        gc.strokeLine(LEFT_XOFFS, maxy - BOTTOM_YOFFS, length + LEFT_XOFFS, (maxy - BOTTOM_YOFFS));
        gc.strokeLine(LEFT_XOFFS, mag_top_offs, length + LEFT_XOFFS, mag_top_offs);

        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, LEFT_XOFFS, TOP_YOFFS + phaseHeight);
        gc.strokeLine(length + LEFT_XOFFS, TOP_YOFFS, length + LEFT_XOFFS, TOP_YOFFS + phaseHeight);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, length + LEFT_XOFFS, TOP_YOFFS);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS + phaseHeight, length + LEFT_XOFFS, TOP_YOFFS + phaseHeight);

        // Determine VSCALE w.r.t. Maximum and minimum for the phase
        s.setMaximum(PHASE_MAX);
        s.setMinimum(PHASE_MIN);

        Max_abs = s.getMaximum() - s.getMinimum(); // difference between minimum and maximum
        nrOfText = 5;
        distance = (int) (-phaseHeight / (nrOfText - 1));
        delta_ph = (int) (Max_abs / (nrOfText - 1)); // delta is 20 degrees

        s.setVScale(Max_abs / (double) (phaseHeight));
        // System.out.println(String.format("MA: %g,phase_hoogte %d, Window_V:
        // %g\n",Max_abs,phaseHeight,s.getVScale()));

        gc.setStroke(Color.GRAY);
        for (int i = 0; i < nrOfText; i++) {
            int ph = (int) (s.getMaximum() - delta_ph * i);
            ch = String.format("%7d", ph);
            gc.strokeText(ch, (LEFT_XOFFS - 80), (int) -(i * distance - TOP_YOFFS - 5));
        }

        gc.strokeText("Degrees", (LEFT_XOFFS), (TOP_YOFFS - 10));

        gc.setStroke(Color.BLACK);
        for (int i = 1; i < nrOfText - 1; i++) {
            gc.strokeLine(LEFT_XOFFS + 1, -(i * distance - TOP_YOFFS), length + LEFT_XOFFS - 1,
                    -(i * distance - TOP_YOFFS));
        }

        // Determine VSCALE w.r.t. Maximum and minimum for magnitude
        double[] real_data = s.getRealData(); // display real data
        double[] imag_data = s.getImagData(); // display imaginary data

        double[] temp = new double[s.getDataLength()];
        for (int i = 0; i < s.getDataLength(); i++) {
            temp[i] = 10.0 * Math.log10(Math.sqrt(Math.pow(real_data[i], 2.0) + Math.pow(imag_data[i], 2.0)));
        }

        double Minimum, Maximum; /* min and max values of a record */
        Minimum = Maximum = temp[0]; // Determine Min and Max

        for (int i = 0; i < s.getDataLength(); i++) {
            if (temp[i] > Maximum)
                Maximum = temp[i];
            if (temp[i] < Minimum)
                Minimum = temp[i];
        }
        // Cut very low values at -100 dB.
        if (Minimum < -100.0) {
            Minimum = -100.0;
        }

        s.setMaximum(((int) (Maximum / 10.0) + 1.0) * 10.0);
        s.setMinimum(((int) (Minimum / 10.0) - 1.0) * 10.0);

        Max_abs = s.getMaximum() - s.getMinimum(); // difference minimum and maximum
        nrOfText = (int) (Max_abs / 10.0) + 1;
        distance = -magHeight / (nrOfText - 1);
        delta_db = 10;

        s.setVScale(Max_abs / (magHeight));
        // System.out.println(String.format("max %f min %f, vscale %f", s.getMaximum(),
        // s.getMinimum(),s.getVScale()));
        // System.out.println(String.format("Max_abs %f", Max_abs));

        gc.setStroke(Color.GRAY);
        for (int i = 0; i < nrOfText; i++) {
            int yValue = (int) (s.getMaximum() - delta_db * i);
            ch = String.format("%7d", yValue);
            gc.strokeText(ch, (LEFT_XOFFS - 80), (int) -(i * distance - mag_top_offs - 5));
        }

        gc.strokeText("dB", (LEFT_XOFFS), (mag_top_offs - 10));

        gc.setStroke(Color.BLACK);
        for (int i = 1; i < nrOfText - 1; i++) {
            gc.strokeLine(LEFT_XOFFS + 1, -(i * distance - mag_top_offs), length + LEFT_XOFFS - 1,
                    -(i * distance - mag_top_offs));
        }
    }

    private void genBodePlot(Signal s, GraphicsContext gc) {
        int nul_offs, height, phase_height, mag_top_offs, Max_abs;
        int x1, y1, x2, y2;
        int data_nr, offs;
        double hscale;

        hscale = s.getHScale();
        height = s.getWindowHeight() - TOP_YOFFS - BOTTOM_YOFFS;
        int mid_type = s.getAverageType();

        phase_height = (int) (BODE_SIGNAL_HEIGHT / PH_MAG);
        mag_top_offs = phase_height + 2 * TOP_YOFFS;

        // generate magnitude part

        double[] real_data = s.getRealData();
        double[] imag_data = s.getImagData();

        offs = s.getChannel() * s.getDataRecords() * s.getDataLength();

        // System.out.println(String.format("Signal %s, Channel
        // %d\n",s.getName(),s.getChannel()));

        double[] cumulative1 = new double[s.getDataLength()];
        double[] cumulative2 = new double[s.getDataLength()];

        if (mid_type == 0) {
            for (int j = 0; j < s.getDataRecords(); j++) {
                for (int i = 0; i < s.getDataLength(); i++) {
                    data_nr = offs + i + j * s.getDataLength();
                    // magnitude
                    double temp = Math.sqrt(Math.pow(real_data[data_nr], 2) + Math.pow(imag_data[data_nr], 2));
                    /* Add up the magnitudes */
                    cumulative1[i] = cumulative1[i] + temp;
                }
            }

            for (int i = 0; i < s.getDataLength(); i++) {
                cumulative1[i] = 10.0 * Math.log10(cumulative1[i] / s.getDataRecords());
                // Cut very low values at -100 dB.
                if (cumulative1[i] < -100.0)
                    cumulative1[i] = -100.0;
                cumulative1[i] = cumulative1[i] / s.getVScale();
            }
        }

        if (mid_type == 1) {
            for (int j = 0; j < s.getDataRecords(); j++)
                for (int i = 0; i < s.getDataLength(); i++) {
                    /* Add up the records */
                    cumulative1[i] = cumulative1[i] + (real_data[offs + i + j * s.getDataLength()]);
                    cumulative2[i] = cumulative2[i] + (imag_data[offs + i + j * s.getDataLength()]);
                }
            for (int i = 0; i < s.getDataLength(); i++) {
                /* Deel door het aantal records */
                cumulative1[i] = cumulative1[i] / (double) (s.getDataRecords());
                cumulative2[i] = cumulative2[i] / (double) (s.getDataRecords());
                cumulative1[i] = 10 * Math.log10(Math.sqrt(Math.pow(cumulative1[i], 2) + Math.pow(cumulative2[i], 2)));
                // Cut very low values at -100 dB.
                if (cumulative1[i] < -100.0)
                    cumulative1[i] = -100.0;
                cumulative1[i] = cumulative1[i] / s.getVScale();
            }

        }

        nul_offs = (int) (mag_top_offs + (s.getMaximum() / s.getVScale()));

        gc.setStroke(Color.GREEN);
        if (!bon) {
            for (int i = 0; i < (s.getDataLength() - 1); i++) {
                x1 = (int) (hscale * i + LEFT_XOFFS);
                y1 = (int) -(cumulative1[i] - nul_offs);
                x2 = (int) (hscale * (i + 1) + LEFT_XOFFS);
                y2 = (int) -(cumulative1[i + 1] - nul_offs);
                gc.strokeLine(x1, y1, x2, y2);
            }
        } else {
            for (int i = 0; i < (s.getDataLength()); i++) {
                gc.strokeLine((int) (hscale * i + LEFT_XOFFS), (int) (height + TOP_YOFFS),
                        (int) (hscale * i + LEFT_XOFFS), (int) -(cumulative1[i] - nul_offs));
            }
        }

        /* generate phase part */

        s.setMaximum(PHASE_MAX);
        s.setMinimum(PHASE_MIN);

        Max_abs = (int) (s.getMaximum() - s.getMinimum()); /* difference between minimum and maximum */
        s.setVScale(Max_abs / (double) (phase_height));

        // System.out.println(String.format("Max: %d , Vscale: %f , ph_h %d \n",
        // Max_abs, s.getVScale(), phase_height));

        for (int i = 0; i < s.getDataLength(); i++)
            cumulative1[i] = 0;

        if (mid_type == 0) {
            for (int j = 0; j < s.getDataRecords(); j++) {
                for (int i = 0; i < s.getDataLength(); i++) {
                    data_nr = offs + i + j * s.getDataLength();
                    double temp = (180.0 / Math.PI) * Math.atan2(imag_data[data_nr], real_data[data_nr]);
                    cumulative1[i] = cumulative1[i] + temp;
                }
            }

            for (int i = 0; i < s.getDataLength(); i++)
                cumulative1[i] = (cumulative1[i] / s.getDataRecords()) / s.getVScale();

        }

        if (mid_type == 1) {
            for (int j = 0; j < s.getDataRecords(); j++)
                for (int i = 0; i < s.getDataLength(); i++) {
                    double temp = (real_data[offs + i + j * s.getDataLength()]);
                    cumulative1[i] = cumulative1[i] + temp;
                    temp = (imag_data[offs + i + j * s.getDataLength()]);
                    cumulative2[i] = cumulative2[i] + temp;
                }
            for (int i = 0; i < s.getDataLength(); i++) {
                cumulative1[i] = cumulative1[i] / s.getDataRecords();
                cumulative2[i] = cumulative2[i] / s.getDataRecords();

                cumulative1[i] = (180.0 / Math.PI) * Math.atan2(cumulative2[i], cumulative1[i]) / s.getVScale();
            }

        }

        nul_offs = (int) (TOP_YOFFS + (s.getMaximum() / s.getVScale()));

        if (!bon) {
            for (int i = 0; i < (s.getDataLength() - 1); i++) {
                x1 = (int) (hscale * i + LEFT_XOFFS);
                y1 = (int) -(cumulative1[i] - nul_offs);
                x2 = (int) (hscale * (i + 1) + LEFT_XOFFS);
                y2 = (int) -(cumulative1[i + 1] - nul_offs);
                gc.strokeLine(x1, y1, x2, y2);
            }
        } else {
            for (int i = 0; i < (s.getDataLength()); i++) {
                x1 = (int) (hscale * i + LEFT_XOFFS);
                y1 = (int) (nul_offs);
                x2 = (int) (hscale * i + LEFT_XOFFS);
                y2 = (int) -(cumulative1[i] - nul_offs);
                gc.strokeLine(x1, y1, x2, y2);
            }
        }
    }

    private void gen_magnitude_background(Signal s, GraphicsContext gc) {
        int length, height, nrOfTextLabels, sampleRate;
        double delta_f, delta_db, Max_abs;
        double unit, distance;
        double Minimum, Maximum; /* min and max values of record */

        String nm = "Signal " + s.getName() + "   Magnitude (Freq)";

        length = s.getWindowWidth() - LEFT_XOFFS - RIGHT_XOFFS; /* Length horizontal axis */
        height = s.getWindowHeight() - TOP_YOFFS - BOTTOM_YOFFS; /* Length vertical axis */
        int maxy = s.getWindowHeight();
        gc.setStroke(Color.RED);
        double fontHeight = gc.getFont().getSize();
        gc.strokeText(nm, (length / 2), fontHeight);

        distance = PIXEL_DIST; /* Number of pixels between two numbers */
        sampleRate = s.getDataSampleRate() * 10; /* Samplerate in Hz */

        nrOfTextLabels = (int) ((length / distance) + 1); /* Amount of numbers horizontal axis */
        delta_f = (double) (sampleRate) / (double) (nrOfTextLabels - 1); /* freq between two numbers */
        distance = (double) (length) / (double) (nrOfTextLabels - 1); /* Amount of pixels between two numbers */

        unit = 1e3; /* Choose unit 1000 (kHz) */
        delta_f = delta_f / unit;

        gc.setStroke(Color.GRAY);
        gc.strokeText("--> Frequency [kHz]", (length / 2), maxy - fontHeight);

        /* print 0 x-axis text */
        gc.strokeText("0", LEFT_XOFFS, maxy - 28);

        for (int i = 1; i < nrOfTextLabels; i++) {
            double xValue = (delta_f * i);
            String ch = String.format("%3.2f", xValue);
            gc.strokeText(ch, (int) (i * distance + LEFT_XOFFS - 25), (int) (maxy - 28));
        }

        /* print record_nr and channel_nr */

        String ch = String.format("          Chan: %d", s.getChannel());
        gc.strokeText(ch, LEFT_XOFFS + length - 200, TOP_YOFFS - 10);

        // draw vertical grid
        for (int i = 1; i < nrOfTextLabels; i++)
            gc.strokeLine(LEFT_XOFFS + i * distance, TOP_YOFFS, LEFT_XOFFS + i * distance, height + TOP_YOFFS);

        /* draw the x and y axis in black (+ top and right) */
        gc.setStroke(Color.BLACK);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, LEFT_XOFFS, maxy - BOTTOM_YOFFS);
        gc.strokeLine(length + LEFT_XOFFS, TOP_YOFFS, length + LEFT_XOFFS, maxy - BOTTOM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, maxy - BOTTOM_YOFFS, length + LEFT_XOFFS, maxy - BOTTOM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, length + LEFT_XOFFS, TOP_YOFFS);

        /* Determine VSCALE with Maximum en minimum */

        double[] real_data = s.getRealData(); /* display real data */
        double[] imag_data = s.getImagData(); /* display imaginary data */
        int dataLength = s.getDataLength();
        double[] temp = new double[dataLength];

        if (s.getLog() == 1)
            for (int i = 0; i < dataLength; i++) {
                temp[i] = 10 * Math.log10(Math.sqrt(Math.pow(real_data[i], 2) + Math.pow(imag_data[i], 2)));
            }
        else
            for (int i = 0; i < dataLength; i++) {
                temp[i] = (Math.sqrt(Math.pow(real_data[i], 2) + Math.pow(imag_data[i], 2)));
            }

        Minimum = Maximum = temp[0]; /* Determine Min and Max */

        for (int i = 1; i < dataLength; i++) {
            if (temp[i] > Maximum)
                Maximum = temp[i];
            if (temp[i] < Minimum)
                Minimum = temp[i];
        }
        if (s.getLog() == 1) {
            // Cut very low values at -100 dB.
            if (Minimum < -100.0) {
                Minimum = -100.0;
            }
        }

        if (s.getLog() == 1) {
            s.setMaximum(((int) (Maximum / 10) + 1) * 10);
            s.setMinimum(((int) (Minimum / 10) - 1) * 10);
            Max_abs = s.getMaximum() - s.getMinimum(); /* difference between minimum and maximum */
            nrOfTextLabels = (int) (Max_abs / 10) + 1;
            delta_db = 10;
        } else {
            s.setMaximum(Maximum);
            s.setMinimum(0);
            if (s.getMaximum() <= 1)
                s.setMaximum(1);
            Max_abs = s.getMaximum();
            nrOfTextLabels = 9;
            delta_db = Max_abs / (nrOfTextLabels - 1);
        }

        distance = -height / (nrOfTextLabels - 1);

        s.setVScale(Max_abs / (height));

        gc.setStroke(Color.GRAY);
        for (int i = 0; i < nrOfTextLabels; i++) {
            int yValue = (int) (s.getMaximum() - delta_db * i);
            ch = String.format("%7d", yValue);
            gc.strokeText(ch, (LEFT_XOFFS - 80), (int) -(i * distance - TOP_YOFFS - 5));
        }

        if (s.getLog() == 1) {
            gc.strokeText("dB", LEFT_XOFFS, TOP_YOFFS - 10);
        }

        for (int i = 1; i < nrOfTextLabels - 1; i++) {
            gc.strokeLine(LEFT_XOFFS + 1, -(i * distance - TOP_YOFFS), length + LEFT_XOFFS - 1,
                    -(i * distance - TOP_YOFFS));
        }

    }

    private void gen_magnitude_plot(Signal s, GraphicsContext gc) {
        int nul_offs, height;
        double hscale;

        hscale = s.getHScale();
        height = s.getWindowHeight() - TOP_YOFFS - BOTTOM_YOFFS;

        double[] real_data = s.getRealData(); /* display real data */

        /* Calculate the address of the channel to show. */

        double temp[] = new double[s.getDataLength()];
        int offset = (s.getChannel() * s.getDataRecords() + s.getRecord()) * s.getDataLength();

        if (s.getLog() == 1) {
            for (int i = 0; i < s.getDataLength(); i++) {
                temp[i] = (10 * Math.log10(real_data[offset + i]));
                // Cut very low values at -100 dB.
                if (temp[i] < -100.0)
                    temp[i] = -100.0;
                temp[i] = temp[i] / s.getVScale();
            }
        } else {
            for (int i = 0; i < s.getDataLength(); i++)
                temp[i] = ((real_data[offset + i])) / s.getVScale();
        }

        nul_offs = (int) (TOP_YOFFS + (s.getMaximum() / s.getVScale()));

        gc.setStroke(Color.GREEN);
        if (!bon) {
            for (int i = 0; i < (s.getDataLength() - 1); i++) {
                gc.strokeLine((int) (hscale * i + LEFT_XOFFS), (int) -(temp[i] - nul_offs),
                        (int) (hscale * (i + 1) + LEFT_XOFFS), (int) -(temp[i + 1] - nul_offs));
            }
        } else {
            for (int i = 0; i < s.getDataLength(); i++) {
                gc.strokeLine((int) (hscale * i + LEFT_XOFFS), (int) (height + TOP_YOFFS),
                        (int) (hscale * i + LEFT_XOFFS), (int) -(temp[i] - nul_offs));
            }
        }
    }

    private void gen_phase_background(Signal s, GraphicsContext gc) {
        int length, height, nrOfTextLabels, sampleRate;
        double delta, delta_f, Max_abs;
        double unit, distance;

        String nm = "Signal " + s.getName() + "   Phase";

        length = s.getWindowWidth() - LEFT_XOFFS - RIGHT_XOFFS; /* Length horizontal axis */
        height = s.getWindowHeight() - TOP_YOFFS - BOTTOM_YOFFS; /* Length vertical axis */
        distance = PIXEL_DIST; /* Number of pixels between to numbers */
        sampleRate = s.getDataSampleRate() * 10; /* Samplerate in Hz */
        int maxy = s.getWindowHeight();

        gc.setStroke(Color.RED);
        double fontHeight = gc.getFont().getSize();
        gc.strokeText(nm, (length / 2), fontHeight);

        nrOfTextLabels = (int) ((length / distance) + 1); /* Amount of numbers horizontal axis */
        delta_f = (double) (sampleRate) / (double) (nrOfTextLabels - 1); /* freq between two numbers */
        distance = (double) (length) / (double) (nrOfTextLabels - 1); /* Number of pixels between two numbers */

        unit = 1e3; /* Choose unit 1000 (kHz) */
        delta_f = delta_f / unit;

        gc.setStroke(Color.GRAY);
        gc.strokeText("--> Frequency [kHz]", (length / 2), maxy - fontHeight);

        /* print 0 x-axis text */
        gc.strokeText("0", LEFT_XOFFS, maxy - 28);

        for (int i = 1; i < nrOfTextLabels; i++) {
            double xValue = (delta_f * i);
            String ch = String.format("%3.2f", xValue);
            gc.strokeText(ch, (int) (i * distance + LEFT_XOFFS - 25), (int) (maxy - 28));
        }

        /* print record_nr and channel_nr */
        String ch = String.format("          Chan: %d", s.getChannel());
        gc.strokeText(ch, (LEFT_XOFFS + length - 200), (TOP_YOFFS - 10));

        /* draw vertical grid */
        for (int i = 1; i < nrOfTextLabels; i++)
            gc.strokeLine(LEFT_XOFFS + i * distance, TOP_YOFFS, LEFT_XOFFS + i * distance, height + TOP_YOFFS);

        /* draw the x and y axis in black (+ top and right) */
        gc.setStroke(Color.BLACK);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, LEFT_XOFFS, maxy - BOTTOM_YOFFS);
        gc.strokeLine(length + LEFT_XOFFS, TOP_YOFFS, length + LEFT_XOFFS, maxy - BOTTOM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, (maxy - BOTTOM_YOFFS), length + LEFT_XOFFS, maxy - BOTTOM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, length + LEFT_XOFFS, TOP_YOFFS);

        /* Determine VSCALE with Maximum and minimum */

        s.setMaximum(PHASE_MAX);
        s.setMinimum(PHASE_MIN);

        Max_abs = s.getMaximum() - s.getMinimum(); /* difference minimum and maximum */
        nrOfTextLabels = 11;
        distance = -height / (nrOfTextLabels - 1);
        delta = Max_abs / (nrOfTextLabels - 1); /* delta is 20 degrees */

        s.setVScale(Max_abs / height);

        gc.setStroke(Color.GRAY);
        for (int i = 0; i < nrOfTextLabels; i++) {
            int yValue = (int) (s.getMaximum() - delta * i);
            ch = String.format("%7d", yValue);
            gc.strokeText(ch, (LEFT_XOFFS - 80), (int) -(i * distance - TOP_YOFFS - 5));
        }

        gc.strokeText("Degrees", LEFT_XOFFS, TOP_YOFFS - 10);

        for (int i = 1; i < nrOfTextLabels - 1; i++) {
            gc.strokeLine(LEFT_XOFFS + 1, -(i * distance - TOP_YOFFS), length + LEFT_XOFFS - 1,
                    -(i * distance - TOP_YOFFS));
        }
    }

    private void gen_phase_plot(Signal s, GraphicsContext gc) {
        int nul_offs;
        double hscale;

        hscale = s.getHScale();
        double[] temp = new double[s.getDataRecords() * s.getDataLength()];
        double[] real_data = s.getRealData(); /* display real data */

        /*
         * Calculate the address of the channel to show. In the phase domain channels
         * have always 1 record.
         */

        int offset = s.getChannel() * s.getRecord() * s.getDataLength();

        // System.out.println(String.format("Signal: %s, Channel:
        // %d\n",s.getName(),s.getChannel()));
        // System.out.println(String.format("VSCALE : %g",s.getVScale()));

        for (int i = 0; i < s.getDataLength(); i++)
            temp[i] = (real_data[i + offset]) / s.getVScale();

        nul_offs = (int) (TOP_YOFFS + (s.getMaximum() / s.getVScale()));

        gc.setStroke(Color.GREEN);
        if (!bon) {
            for (int i = 0; i < s.getDataLength() - 1; i++) {
                gc.strokeLine((int) (hscale * i + LEFT_XOFFS), (int) -(temp[i] - nul_offs),
                        (int) (hscale * (i + 1) + LEFT_XOFFS), (int) -(temp[i + 1] - nul_offs));
            }
        } else {
            for (int i = 0; i < s.getDataLength(); i++) {
                gc.strokeLine((int) (hscale * i + LEFT_XOFFS), (int) (nul_offs), (int) (hscale * i + LEFT_XOFFS),
                        (int) -(temp[i] - nul_offs));
            }
        }
    }

    private void gen_hist_background(Signal s, GraphicsContext gc) {
        int length, height, nrOfTextLabels;
        double delta_a, delta_n, Max_abs;
        double xValue, distance;
        int maxy = s.getWindowHeight();

        String nm = "Signal " + s.getName() + "   Histogram (Time)";

        length = s.getWindowWidth() - LEFT_XOFFS - RIGHT_XOFFS; /* Length horizontal axis */
        height = s.getWindowHeight() - TOP_YOFFS - BOTTOM_YOFFS; /* Length vertical axis */
        distance = PIXEL_DIST; /* Number of pixels between to numbers */

        gc.setStroke(Color.RED);
        double fontHeight = gc.getFont().getSize();
        gc.strokeText(nm, (length / 2), fontHeight);

        double min = getRecordMin(s);
        double max = getRecordMax(s);
        s.setMinimum(min);
        s.setMaximum(max);
        nrOfTextLabels = (int) ((length / distance) + 1); /* Amount of numbers horizontal axis */
        delta_a = (double) (max - min) / (double) (nrOfTextLabels - 1); /* ampl between two numbers */
        distance = (double) (length) / (double) (nrOfTextLabels - 1); /* Amount of pixels between two numbers */

        gc.setStroke(Color.GRAY);
        gc.strokeText("--> Magnitude", (length / 2), maxy - fontHeight);

        /* print 0 x-axis text */
        String ch = String.format("%5.1f", min);
        gc.strokeText(ch, LEFT_XOFFS, maxy - 28);

        int count = nrOfTextLabels - 1;

        xValue = (delta_a * count + min);
        ch = String.format("%5.1f", xValue);
        gc.strokeText(ch, (int) (count * distance + LEFT_XOFFS - 25), (int) (maxy - 28));

        /* print record_nr and channel_nr */

        ch = String.format("Chan: %d", s.getChannel());
        gc.strokeText(ch, (LEFT_XOFFS + length - 100), TOP_YOFFS - 10);

        /* draw vertical grid */

        for (int i = 1; i < nrOfTextLabels; i++)
            gc.strokeLine(LEFT_XOFFS + i * distance, TOP_YOFFS, LEFT_XOFFS + i * distance, height + TOP_YOFFS);

        /* draw the x and y axis in black (+ top and right) */
        gc.setStroke(Color.GRAY);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, LEFT_XOFFS, maxy - BOTTOM_YOFFS);
        gc.strokeLine(length + LEFT_XOFFS, TOP_YOFFS, length + LEFT_XOFFS, maxy - BOTTOM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, (maxy - BOTTOM_YOFFS), length + LEFT_XOFFS, maxy - BOTTOM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, length + LEFT_XOFFS, TOP_YOFFS);

        /* Determine VSCALE with Maximum and minimum */

        Max_abs = 100; /* difference between minimum and maximum in % */
        nrOfTextLabels = (int) 11;
        distance = -height / (nrOfTextLabels - 1);
        delta_n = 10;

        s.setVScale(s.getMaximum() / height);

        gc.setStroke(Color.GRAY);
        for (int i = 0; i < nrOfTextLabels; i++) {
            int yValue = (int) (100 - delta_n * i);
            ch = String.format("%7d", yValue);
            gc.strokeText(ch, (LEFT_XOFFS - 80), (int) -(i * distance - TOP_YOFFS - 5));
        }

        gc.strokeText("%%", LEFT_XOFFS, TOP_YOFFS - 10);

        for (int i = 1; i < nrOfTextLabels - 1; i++) {
            gc.strokeLine(LEFT_XOFFS + 1, -(i * distance - TOP_YOFFS), length + LEFT_XOFFS - 1,
                    -(i * distance - TOP_YOFFS));
        }

    }

    /**************************************************************************
     *
     * void gen_hist_plot(VENSTER *vp)
     *
     **************************************************************************/

    private void gen_hist_plot(Signal s, GraphicsContext gc) {
        double vscale = s.getVScale();
        double hscale = s.getHScale();
        int height = s.getWindowHeight() - TOP_YOFFS - BOTTOM_YOFFS;

        double[] cumulative = new double[s.getDataLength()];
        double[] data = s.getRealData(); /* display real data */

        /* Which record to display? */

        int offset = (s.getChannel() * s.getDataRecords() + s.getRecord()) * s.getDataLength();

        double[] temp = Arrays.copyOfRange(data, offset, offset + s.getDataLength());

        cumulative[0] = temp[0];

        for (int i = 0; i < s.getDataLength(); i++) {
            if (i > 0)
                cumulative[i] = cumulative[i - 1] + temp[i];
            temp[i] = (temp[i] / vscale);
        }

        double cscale = cumulative[s.getDataLength() - 1] / height;

        int nul_offs = height + TOP_YOFFS;

        gc.setStroke(Color.BLUE);
        for (int i = 0; i < s.getDataLength(); i++) {
            gc.strokeLine((int) (hscale * i + LEFT_XOFFS), nul_offs, (int) (hscale * i + LEFT_XOFFS),
                    (int) -(temp[i] - nul_offs));
        }

        gc.setStroke(Color.GREEN);
        for (int i = 0; i < (s.getDataLength() - 1); i++) {
            gc.strokeLine((int) (hscale * i + LEFT_XOFFS), (int) -((cumulative[i] / cscale) - nul_offs),
                    (int) (hscale * (i + 1) + LEFT_XOFFS), (int) -((cumulative[i + 1] / cscale) - nul_offs));
        }
    }

}