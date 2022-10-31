package signals;

import java.util.Arrays;
import java.util.Map;

import console.CommandLineParser;
import dialogs.SaveSignalDialog;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SignalWindow {

    public static final int SIGNAL_HOOGTE = 250; /* Aantal pixels verticaal voor signaal */
    public static final int BODE_SIGNAL_HOOGTE = 200; /* Aantal pixels verticaal voor signaal */
    public static final double PH_MAG = 3.0; /* Verhouding tussen phase/magn bij bodediagram */
    public static final int LEFT_XOFFS = 100; /* Marge links */
    public static final int RIGHT_XOFFS = 50; /* Marge rechts */
    public static final int BOTTEM_YOFFS = 50; /* Marge onder */
    public static final int TOP_YOFFS = 50; /* Marge boven */
    public static final double PIXEL_DIST = 100.0;
    public static final int USHRT_MAX = 65535;
    public static final int PHASE_MAX = 200;
    public static final int PHASE_MIN = -200;

    private final Signal signal;
    private final MenuBar menuBar = new MenuBar();
    private final ToolBar toolBar = new ToolBar();
    private final ComboBox<String> viewMode = new ComboBox<String>();
    private boolean viewModeReact = true;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Stage stage = new Stage();
    private boolean bon = false;

    public SignalWindow(Signal s, CommandLineParser parser, Map<String, Signal> signals) {
        this.signal = s;
        this.signal.setWindow(this);

        Menu menuFile = new Menu("File");
        MenuItem menuItemSave = new MenuItem("Save");
        menuFile.getItems().add(menuItemSave);

        Menu menuView = new Menu("View");
        MenuItem menuItemConsole = new MenuItem("Console");
        menuView.getItems().add(menuItemConsole);

        menuItemSave.setOnAction(e -> {
            new SaveSignalDialog(parser, signals, s.getName());
        });
        menuItemConsole.setOnAction(e -> {
            parser.showConsole();
        });
        menuBar.getMenus().add(menuFile);
        menuBar.getMenus().add(menuView);

        Button zoomInButton = new Button("+");
        Button zoomOutButton = new Button("-");

        zoomInButton.setOnAction(event -> {
            s.setHScale(s.getHScale() / 0.8);
            this.show(bon);
        });
        zoomOutButton.setOnAction(event -> {
            s.setHScale(s.getHScale() * 0.8);
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

        canvas = new Canvas(signal.getWindowWidth(), signal.getWindowHeight());
        gc = canvas.getGraphicsContext2D();
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
        VBox root = new VBox();

        signal.setWindowWidth((int) (signal.getHScale() * signal.getDataLength() + LEFT_XOFFS + RIGHT_XOFFS));
        if (signal.getMode() == Signal.BODE_M) {
            signal.setWindowHeight(
                    BODE_SIGNAL_HOOGTE + (int) (BODE_SIGNAL_HOOGTE / PH_MAG) + 2 * TOP_YOFFS + BOTTEM_YOFFS);
        } else {
            signal.setWindowHeight(SIGNAL_HOOGTE + TOP_YOFFS + BOTTEM_YOFFS);
        }
        // TODO : get actual height of menu bar.
        // menuBar.getheight() returns 0.0. Now adding 50.
        // toolBar.getHeight() idem
        Scene scene = new Scene(root, signal.getWindowWidth(),
                signal.getWindowHeight() + 50 + 50 );
        canvas.setWidth(signal.getWindowWidth());
        canvas.setHeight(signal.getWindowHeight());
        root.getChildren().add(menuBar);
        root.getChildren().add(toolBar);
        root.getChildren().add(canvas);

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
        stage.show();
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
        int height = s.getWindowHeight() - TOP_YOFFS - BOTTEM_YOFFS;
        double distance = PIXEL_DIST;

        int aantaltxt = (int) ((length / distance) + 1);
        double totalTime = (double) s.getDataLength() / (double) (s.getDataSampleRate() * 10);
        double deltaT = totalTime / (double) (aantaltxt - 1);
        distance = (double) length / (double) (aantaltxt - 1);

        double unit = 1.0;
        char eenhstr = ' ';

        if (totalTime < (1.0)) {
            unit = 1e-3;
            eenhstr = 'm';
        }
        if (totalTime < (1e-3)) {
            unit = 1e-6;
            eenhstr = 'u';
        }
        if (totalTime < (1e-6)) {
            unit = 1e-9;
            eenhstr = 'n';
        }
        if (totalTime < (1e-9)) {
            unit = 1e-11;
            eenhstr = 'p';
        }

        gc.setStroke(Color.GRAY);
        double fontHeight = gc.getFont().getSize();
        gc.strokeText("--> Time [" + eenhstr + "s]", length / 2, maxy - fontHeight);

        deltaT = deltaT / unit;

        /* print 0 x-as, y-as text */

        gc.strokeText("0", LEFT_XOFFS, maxy - 28);

        /* print record_nr en channel_nr */

        String ch = String.format("Rec: %d - Chan: %d", s.getRecord(), s.getChannel());
        gc.strokeText(ch, (LEFT_XOFFS + length - 200), (TOP_YOFFS - 10));

        for (int i = 1; i < aantaltxt; i++) {
            double xwaarde = (deltaT * i);
            ch = String.format("%3.2f", xwaarde);
            gc.strokeText(ch, (int) (i * distance + LEFT_XOFFS - 25), (int) (maxy - 28));
        }

        /* draw vertical grid */
        gc.setStroke(Color.GRAY);
        for (int i = 1; i < aantaltxt; i++)
            gc.strokeLine(LEFT_XOFFS + i * distance, (TOP_YOFFS), LEFT_XOFFS + i * distance, (height + TOP_YOFFS));

        /* draw the x en y axis in black (+ up and right) */
        gc.setStroke(Color.BLACK);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, LEFT_XOFFS, maxy - BOTTEM_YOFFS);
        gc.strokeLine(length + LEFT_XOFFS, TOP_YOFFS, length + LEFT_XOFFS, maxy - BOTTEM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, (maxy - BOTTEM_YOFFS), length + LEFT_XOFFS, maxy - BOTTEM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, length + LEFT_XOFFS, TOP_YOFFS);

        double Minimum = getRecordMin(s);
        double Maximum = getRecordMax(s);
        if ((Maximum <= 1) && (Maximum >= 0.0))
            Maximum = 1;
        if ((Minimum >= -1) && (Minimum <= 0.0))
            Minimum = -1;

        s.setMinimum(Minimum);
        s.setMaximum(Maximum);

        aantaltxt = 9;
        distance = (int) (-height / (aantaltxt - 1));

        double Max_abs = (double) Math.max(Math.abs(Maximum), Math.abs(Minimum)); /* Absolute maximum */
        double deltaY = Max_abs * 2 / (aantaltxt - 1);
        s.setVScale(Max_abs / (height / 2.0));

        gc.setStroke(Color.GRAY);
        if (Max_abs >= USHRT_MAX) {
            for (int tel = 0; tel < aantaltxt; tel++) {
                double y_waarde = Max_abs - deltaY * tel;
                ch = String.format("%8.2g", y_waarde);
                gc.strokeText(ch, (LEFT_XOFFS - 95), (int) -(tel * distance - TOP_YOFFS - 5));
            }
        }

        if (Max_abs < USHRT_MAX) {
            for (int tel = 0; tel < aantaltxt; tel++) {
                double y_waarde = Max_abs - deltaY * tel;
                ch = String.format("%5.2f", y_waarde);
                gc.strokeText(ch, (LEFT_XOFFS - 95), (int) -(tel * distance - TOP_YOFFS - 5));
            }
        }

        gc.setStroke(Color.BLACK);
        for (int i = 1; i < aantaltxt - 1; i++)
            gc.strokeLine(LEFT_XOFFS + 1, (int) -(i * distance - TOP_YOFFS), length + LEFT_XOFFS - 1,
                    (int) -(i * distance - TOP_YOFFS));
    }

    private void gen_freq_background(Signal s, GraphicsContext gc) {
        int lengte, hoogte, aantaltxt, sampler;
        double delta_f, delta_y, Max_abs;
        double eenheid, afstand;

        String nm = "Signal " + s.getName();

        if (s.getMode() == Signal.REAL_M)
            nm += "  Real (Freq)";
        else
            nm += "  Imaginary (Freq)";

        lengte = s.getWindowWidth() - LEFT_XOFFS - RIGHT_XOFFS; /* Lengte horizontale as */
        hoogte = s.getWindowHeight() - TOP_YOFFS - BOTTEM_YOFFS; /* Lengte verticale as */
        afstand = PIXEL_DIST; /* Aantal pixels tussen twee getallen */
        sampler = s.getDataSampleRate() * 10; /* Samplerate in Hz */
        int maxy = s.getWindowHeight();

        gc.setStroke(Color.RED);
        double fontHeight = gc.getFont().getSize();
        gc.strokeText(nm, (lengte / 2), fontHeight);

        aantaltxt = (int) ((lengte / afstand) + 1); /* Aantal getallen horizontale as */
        delta_f = (double) (sampler) / (double) (aantaltxt - 1); /* freq tussen twee getallen */
        afstand = (double) (lengte) / (double) (aantaltxt - 1); /* Aantal pixels tussen twee getallen */

        eenheid = 1e3; /* Kies voor de eenheid 1000 (kHz) */
        delta_f = delta_f / eenheid;

        gc.setStroke(Color.GRAY);
        gc.strokeText("--> Frequency [kHz]", (lengte / 2), maxy - fontHeight);

        /* print 0 x-as text */
        gc.strokeText("0", LEFT_XOFFS, maxy - 28);

        /* allocate data for xwaarde */
        for (int tel = 1; tel < aantaltxt; tel++) {
            double xwaarde = delta_f * tel;
            gc.strokeText(String.format("%3.2f", xwaarde), (int) (tel * afstand + LEFT_XOFFS - 25), (int) (maxy - 28));
        }

        /* print record_nr en channel_nr */
        String ch = String.format("Rec: %d - Chan: %d", s.getRecord(), s.getChannel());
        gc.strokeText(ch, LEFT_XOFFS + lengte - 200, TOP_YOFFS - 10);

        /* teken vertikaal grid */
        for (int tel = 1; tel < aantaltxt; tel++)
            gc.strokeLine(LEFT_XOFFS + tel * afstand, TOP_YOFFS, LEFT_XOFFS + tel * afstand, hoogte + TOP_YOFFS);

        /* teken de x en y as in zwart (+ boven en rechts) */
        gc.setStroke(Color.BLACK);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, LEFT_XOFFS, maxy - BOTTEM_YOFFS);
        gc.strokeLine(lengte + LEFT_XOFFS, TOP_YOFFS, lengte + LEFT_XOFFS, maxy - BOTTEM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, (maxy - BOTTEM_YOFFS), lengte + LEFT_XOFFS, maxy - BOTTEM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, lengte + LEFT_XOFFS, TOP_YOFFS);

        double Minimum = getRecordMin(s);
        double Maximum = getRecordMax(s);

        s.setMaximum(Maximum);
        s.setMinimum(Minimum);

        aantaltxt = 9;
        afstand = (int) (-hoogte / (aantaltxt - 1));

        Max_abs = (double) Math.max(Math.abs(Maximum), Math.abs(Minimum)); /* Absolute maximum */
        if (Max_abs == 0.0)
            Max_abs = 1;
        delta_y = Max_abs * 2 / (aantaltxt - 1);

        s.setVScale(Max_abs / (hoogte / 2));

        gc.setStroke(Color.GRAY);
        if (Max_abs >= USHRT_MAX) {
            for (int tel = 0; tel < aantaltxt; tel++) {
                double y_waarde = (int) (Max_abs - delta_y * tel);
                ch = String.format("%8.2g", y_waarde);
                gc.strokeText(ch, (LEFT_XOFFS - 95), (int) -(tel * afstand - TOP_YOFFS - 5));
            }
        }

        if (Max_abs < USHRT_MAX) {
            for (int tel = 0; tel < aantaltxt; tel++) {
                double y_waarde = Max_abs - delta_y * tel;
                ch = String.format("%5.2f", y_waarde);
                gc.strokeText(ch, (LEFT_XOFFS - 95), (int) -(tel * afstand - TOP_YOFFS - 5));
            }
        }

        gc.setStroke(Color.BLACK);
        for (int tel = 1; tel < aantaltxt - 1; tel++)

            gc.strokeLine(LEFT_XOFFS + 1, (int) -(tel * afstand - TOP_YOFFS), lengte + LEFT_XOFFS - 1,
                    (int) -(tel * afstand - TOP_YOFFS));
    }

    private void genSignalPlot(Signal s, GraphicsContext gc) {
        int nul_offs, hoogte, x1, x2, y1, y2;
        double vscale, hscale;
        double[] data; /* pointer to data to display */
        double[] temp; /* temporary copy for scaling */

        vscale = s.getVScale();
        hscale = s.getHScale();
        hoogte = s.getWindowHeight() - TOP_YOFFS - BOTTEM_YOFFS;

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
            nul_offs = hoogte / 2 + TOP_YOFFS;

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
            nul_offs = hoogte / 2 + TOP_YOFFS;

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
        int magHeight, phaseHeight, nrOfText, sampler;
        int mag_top_offs;
        int delta_ph;
        double deltaFreq, delta_db, Max_abs;
        double unit, distance;

        String nm = "Signal " + s.getName() + "   BodeDiagram";

        magHeight = BODE_SIGNAL_HOOGTE;
        phaseHeight = (int) (BODE_SIGNAL_HOOGTE / PH_MAG);
        mag_top_offs = phaseHeight + 2 * TOP_YOFFS;

        int length = s.getWindowWidth() - LEFT_XOFFS - RIGHT_XOFFS; /* Length horizontal axis */
        int maxy = s.getWindowHeight();
        /* Horizontal axis + text */
        gc.setStroke(Color.RED);
        double fontHeight = gc.getFont().getSize();
        gc.strokeText(nm, length / 2, fontHeight);

        distance = PIXEL_DIST; /* Nr of pixels between two numbers */
        sampler = s.getDataSampleRate() * 10; /* Sample rate in Hz */

        nrOfText = (int) ((length / distance) + 1); /* Nr of numbers horizontal axis */
        deltaFreq = (double) (sampler) / (double) (nrOfText - 1); /* freq between two numbers */
        distance = (double) (length) / (double) (nrOfText - 1); /* Nr of pixels between two numbers */

        unit = 1e3; /* Choose unit 1000(kHz) */
        deltaFreq = deltaFreq / unit;

        gc.setStroke(Color.GRAY);
        gc.strokeText("--> Frequency [kHz]", length / 2, maxy - fontHeight);

        // print 0 x-as text
        gc.strokeText("0", LEFT_XOFFS, maxy - 28);

        // allocate data for x value
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
        for (int tel = 1; tel < nrOfText; tel++) {
            gc.strokeLine(LEFT_XOFFS + tel * distance, (TOP_YOFFS), LEFT_XOFFS + tel * distance,
                    (phaseHeight + TOP_YOFFS));
            gc.strokeLine(LEFT_XOFFS + tel * distance, (mag_top_offs), LEFT_XOFFS + tel * distance,
                    (mag_top_offs + magHeight));
        }

        // draw the x and y axis in black (+ up and right)
        gc.strokeLine(LEFT_XOFFS, mag_top_offs, LEFT_XOFFS, (maxy - BOTTEM_YOFFS));
        gc.strokeLine(length + LEFT_XOFFS, mag_top_offs, length + LEFT_XOFFS, (maxy - BOTTEM_YOFFS));
        gc.strokeLine(LEFT_XOFFS, maxy - BOTTEM_YOFFS, length + LEFT_XOFFS, (maxy - BOTTEM_YOFFS));
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
        for (int tel = 1; tel < nrOfText - 1; tel++) {
            gc.strokeLine(LEFT_XOFFS + 1, -(tel * distance - mag_top_offs), length + LEFT_XOFFS - 1,
                    -(tel * distance - mag_top_offs));
        }

    }

    private void genBodePlot(Signal s, GraphicsContext gc) {
        int nul_offs, hoogte, phase_hoogte, mag_top_offs, Max_abs;
        int x1, y1, x2, y2;
        int data_nr, offs;
        double hscale;

        hscale = s.getHScale();
        hoogte = s.getWindowHeight() - TOP_YOFFS - BOTTEM_YOFFS;
        int mid_type = s.getAverageType();

        phase_hoogte = (int) (BODE_SIGNAL_HOOGTE / PH_MAG);
        mag_top_offs = phase_hoogte + 2 * TOP_YOFFS;

        // generate magnitude part

        double[] real_data = s.getRealData();
        double[] imag_data = s.getImagData();

        offs = s.getChannel() * s.getDataRecords() * s.getDataLength();

        // System.out.println(String.format("Signal %s, Channel
        // %d\n",s.getName(),s.getChannel()));

        double[] cum1 = new double[s.getDataLength()];
        double[] cum2 = new double[s.getDataLength()];

        if (mid_type == 0) {
            for (int j = 0; j < s.getDataRecords(); j++) {
                for (int i = 0; i < s.getDataLength(); i++) {
                    data_nr = offs + i + j * s.getDataLength();
                    // magnitude
                    double temp = Math.sqrt(Math.pow(real_data[data_nr], 2) + Math.pow(imag_data[data_nr], 2));
                    /* Tel de magnitudes bij elkaar op */
                    cum1[i] = cum1[i] + temp;
                }
            }

            for (int i = 0; i < s.getDataLength(); i++) {
                cum1[i] = 10.0 * Math.log10(cum1[i] / s.getDataRecords());
                // Cut very low values at -100 dB.
                if (cum1[i] < -100.0)
                    cum1[i] = -100.0;
                cum1[i] = cum1[i] / s.getVScale();
            }
        }

        if (mid_type == 1) {
            for (int j = 0; j < s.getDataRecords(); j++)
                for (int i = 0; i < s.getDataLength(); i++) {
                    /* Tel de records bij elkaar op */
                    cum1[i] = cum1[i] + (real_data[offs + i + j * s.getDataLength()]);
                    cum2[i] = cum2[i] + (imag_data[offs + i + j * s.getDataLength()]);
                }
            for (int i = 0; i < s.getDataLength(); i++) {
                /* Deel door het aantal records */
                cum1[i] = cum1[i] / (double) (s.getDataRecords());
                cum2[i] = cum2[i] / (double) (s.getDataRecords());
                cum1[i] = 10 * Math.log10(Math.sqrt(Math.pow(cum1[i], 2) + Math.pow(cum2[i], 2)));
                // Cut very low values at -100 dB.
                if (cum1[i] < -100.0)
                    cum1[i] = -100.0;
                cum1[i] = cum1[i] / s.getVScale();
            }

        }

        nul_offs = (int) (mag_top_offs + (s.getMaximum() / s.getVScale()));

        gc.setStroke(Color.GREEN);
        if (!bon) {
            for (int i = 0; i < (s.getDataLength() - 1); i++) {
                x1 = (int) (hscale * i + LEFT_XOFFS);
                y1 = (int) -(cum1[i] - nul_offs);
                x2 = (int) (hscale * (i + 1) + LEFT_XOFFS);
                y2 = (int) -(cum1[i + 1] - nul_offs);
                gc.strokeLine(x1, y1, x2, y2);
            }
        } else {
            for (int i = 0; i < (s.getDataLength()); i++) {
                gc.strokeLine((int) (hscale * i + LEFT_XOFFS), (int) (hoogte + TOP_YOFFS),
                        (int) (hscale * i + LEFT_XOFFS), (int) -(cum1[i] - nul_offs));
            }
        }

        /* genereer phase gedeelte */

        s.setMaximum(PHASE_MAX);
        s.setMinimum(PHASE_MIN);

        Max_abs = (int) (s.getMaximum() - s.getMinimum()); /* verschil tussen minimum en maximum */
        s.setVScale(Max_abs / (double) (phase_hoogte));

        // System.out.println(String.format("Max: %d , Vscale: %f , ph_h %d \n",
        // Max_abs, s.getVScale(), phase_hoogte));

        for (int i = 0; i < s.getDataLength(); i++)
            cum1[i] = 0;

        if (mid_type == 0) {
            for (int j = 0; j < s.getDataRecords(); j++) {
                for (int i = 0; i < s.getDataLength(); i++) {
                    data_nr = offs + i + j * s.getDataLength();
                    double temp = (180.0 / Math.PI) * Math.atan2(imag_data[data_nr], real_data[data_nr]);
                    cum1[i] = cum1[i] + temp;
                }
            }

            for (int i = 0; i < s.getDataLength(); i++)
                cum1[i] = (cum1[i] / s.getDataRecords()) / s.getVScale();

        }

        if (mid_type == 1) {
            for (int j = 0; j < s.getDataRecords(); j++)
                for (int i = 0; i < s.getDataLength(); i++) {
                    double temp = (real_data[offs + i + j * s.getDataLength()]);
                    cum1[i] = cum1[i] + temp;
                    temp = (imag_data[offs + i + j * s.getDataLength()]);
                    cum2[i] = cum2[i] + temp;
                }
            for (int i = 0; i < s.getDataLength(); i++) {
                cum1[i] = cum1[i] / s.getDataRecords();
                cum2[i] = cum2[i] / s.getDataRecords();

                cum1[i] = (180.0 / Math.PI) * Math.atan2(cum2[i], cum1[i]) / s.getVScale();
            }

        }

        nul_offs = (int) (TOP_YOFFS + (s.getMaximum() / s.getVScale()));

        if (!bon) {
            for (int i = 0; i < (s.getDataLength() - 1); i++) {
                x1 = (int) (hscale * i + LEFT_XOFFS);
                y1 = (int) -(cum1[i] - nul_offs);
                x2 = (int) (hscale * (i + 1) + LEFT_XOFFS);
                y2 = (int) -(cum1[i + 1] - nul_offs);
                gc.strokeLine(x1, y1, x2, y2);
            }
        } else {
            for (int i = 0; i < (s.getDataLength()); i++) {
                x1 = (int) (hscale * i + LEFT_XOFFS);
                y1 = (int) (nul_offs);
                x2 = (int) (hscale * i + LEFT_XOFFS);
                y2 = (int) -(cum1[i] - nul_offs);
                gc.strokeLine(x1, y1, x2, y2);
            }
        }
    }

    private void gen_magnitude_background(Signal s, GraphicsContext gc) {
        int lengte, hoogte, aantaltxt, sampler;
        double delta_f, delta_db, Max_abs;
        double eenheid, afstand;
        double Minimum, Maximum; /* min en max waarden van record */

        String nm = "Signal " + s.getName() + "   Magnitude (Freq)";

        lengte = s.getWindowWidth() - LEFT_XOFFS - RIGHT_XOFFS; /* Lengte horizontale as */
        hoogte = s.getWindowHeight() - TOP_YOFFS - BOTTEM_YOFFS; /* Lengte verticale as */
        int maxy = s.getWindowHeight();
        gc.setStroke(Color.RED);
        double fontHeight = gc.getFont().getSize();
        gc.strokeText(nm, (lengte / 2), fontHeight);

        afstand = PIXEL_DIST; /* Aantal pixels tussen twee getallen */
        sampler = s.getDataSampleRate() * 10; /* Samplerate in Hz */

        aantaltxt = (int) ((lengte / afstand) + 1); /* Aantal getallen horizontale as */
        delta_f = (double) (sampler) / (double) (aantaltxt - 1); /* freq tussen twee getallen */
        afstand = (double) (lengte) / (double) (aantaltxt - 1); /* Aantal pixels tussen twee getallen */

        eenheid = 1e3; /* Kies voor de eenheid 1000 (kHz) */
        delta_f = delta_f / eenheid;

        gc.setStroke(Color.GRAY);
        gc.strokeText("--> Frequency [kHz]", (lengte / 2), maxy - fontHeight);

        /* print 0 x-as text */
        gc.strokeText("0", LEFT_XOFFS, maxy - 28);

        for (int tel = 1; tel < aantaltxt; tel++) {
            double xwaarde = (delta_f * tel);
            String ch = String.format("%3.2f", xwaarde);
            gc.strokeText(ch, (int) (tel * afstand + LEFT_XOFFS - 25), (int) (maxy - 28));
        }

        /* print record_nr en channel_nr */

        String ch = String.format("          Chan: %d", s.getChannel());
        gc.strokeText(ch, LEFT_XOFFS + lengte - 200, TOP_YOFFS - 10);

        // teken vertikaal grid
        for (int tel = 1; tel < aantaltxt; tel++)
            gc.strokeLine(LEFT_XOFFS + tel * afstand, TOP_YOFFS, LEFT_XOFFS + tel * afstand, hoogte + TOP_YOFFS);

        /* teken de x en y as in zwart (+ boven en rechts) */
        gc.setStroke(Color.BLACK);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, LEFT_XOFFS, maxy - BOTTEM_YOFFS);
        gc.strokeLine(lengte + LEFT_XOFFS, TOP_YOFFS, lengte + LEFT_XOFFS, maxy - BOTTEM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, maxy - BOTTEM_YOFFS, lengte + LEFT_XOFFS, maxy - BOTTEM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, lengte + LEFT_XOFFS, TOP_YOFFS);

        /* Bepaal VSCALE m.b.v. Maximum en minimum */

        double[] real_data = s.getRealData(); /* display real data */
        double[] imag_data = s.getImagData(); /* display imaginary data */
        int dataLength = s.getDataLength();
        double[] temp = new double[dataLength];

        if (s.getLog() == 1)
            for (int tel = 0; tel < dataLength; tel++) {
                temp[tel] = 10 * Math.log10(Math.sqrt(Math.pow(real_data[tel], 2) + Math.pow(imag_data[tel], 2)));
            }
        else
            for (int tel = 0; tel < dataLength; tel++) {
                temp[tel] = (Math.sqrt(Math.pow(real_data[tel], 2) + Math.pow(imag_data[tel], 2)));
            }

        Minimum = Maximum = temp[0]; /* Min en Max bepalen */

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
            Max_abs = s.getMaximum() - s.getMinimum(); /* verschil tussen minimum en maximum */
            aantaltxt = (int) (Max_abs / 10) + 1;
            delta_db = 10;
        } else {
            s.setMaximum(Maximum);
            s.setMinimum(0);
            if (s.getMaximum() <= 1)
                s.setMaximum(1);
            Max_abs = s.getMaximum();
            aantaltxt = 9;
            delta_db = Max_abs / (aantaltxt - 1);
        }

        afstand = -hoogte / (aantaltxt - 1);

        s.setVScale(Max_abs / (hoogte));

        gc.setStroke(Color.GRAY);
        for (int tel = 0; tel < aantaltxt; tel++) {
            int ywaarde = (int) (s.getMaximum() - delta_db * tel);
            ch = String.format("%7d", ywaarde);
            gc.strokeText(ch, (LEFT_XOFFS - 80), (int) -(tel * afstand - TOP_YOFFS - 5));
        }

        if (s.getLog() == 1) {
            gc.strokeText("dB", LEFT_XOFFS, TOP_YOFFS - 10);
        }

        for (int tel = 1; tel < aantaltxt - 1; tel++) {
            gc.strokeLine(LEFT_XOFFS + 1, -(tel * afstand - TOP_YOFFS), lengte + LEFT_XOFFS - 1,
                    -(tel * afstand - TOP_YOFFS));
        }

    }

    private void gen_magnitude_plot(Signal s, GraphicsContext gc) {
        int nul_offs, hoogte;
        double hscale;

        hscale = s.getHScale();
        hoogte = s.getWindowHeight() - TOP_YOFFS - BOTTEM_YOFFS;

        double[] real_data = s.getRealData(); /* display real data */

        /* Bereken het adres van het channel dat je wil zien. */

        double temp[] = new double[s.getDataLength()];
        int offset = (s.getChannel() * s.getDataRecords() + s.getRecord()) * s.getDataLength();

        /*
         * asm_wprintf("Signal %s, Channel %d\n",DataSignalName(vp),Window_Channel(vp));
         */

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
                gc.strokeLine((int) (hscale * i + LEFT_XOFFS), (int) (hoogte + TOP_YOFFS),
                        (int) (hscale * i + LEFT_XOFFS), (int) -(temp[i] - nul_offs));
            }
        }
    }

    private void gen_phase_background(Signal s, GraphicsContext gc) {
        int lengte, hoogte, aantaltxt, sampler;
        double delta, delta_f, Max_abs;
        double eenheid, afstand;

        String nm = "Signal " + s.getName() + "   Phase";

        lengte = s.getWindowWidth() - LEFT_XOFFS - RIGHT_XOFFS; /* Lengte horizontale as */
        hoogte = s.getWindowHeight() - TOP_YOFFS - BOTTEM_YOFFS; /* Lengte verticale as */
        afstand = PIXEL_DIST; /* Aantal pixels tussen twee getallen */
        sampler = s.getDataSampleRate() * 10; /* Samplerate in Hz */
        int maxy = s.getWindowHeight();

        gc.setStroke(Color.RED);
        double fontHeight = gc.getFont().getSize();
        gc.strokeText(nm, (lengte / 2), fontHeight);

        aantaltxt = (int) ((lengte / afstand) + 1); /* Aantal getallen horizontale as */
        delta_f = (double) (sampler) / (double) (aantaltxt - 1); /* freq tussen twee getallen */
        afstand = (double) (lengte) / (double) (aantaltxt - 1); /* Aantal pixels tussen twee getallen */

        eenheid = 1e3; /* Kies voor de eenheid 1000 (kHz) */
        delta_f = delta_f / eenheid;

        gc.setStroke(Color.GRAY);
        gc.strokeText("--> Frequency [kHz]", (lengte / 2), maxy - fontHeight);

        /* print 0 x-as text */
        gc.strokeText("0", LEFT_XOFFS, maxy - 28);

        /* allocate data for xwaarde */
        for (int tel = 1; tel < aantaltxt; tel++) {
            double xwaarde = (delta_f * tel);
            String ch = String.format("%3.2f", xwaarde);
            gc.strokeText(ch, (int) (tel * afstand + LEFT_XOFFS - 25), (int) (maxy - 28));
        }

        /* print record_nr en channel_nr */

        String ch = String.format("          Chan: %d", s.getChannel());
        gc.strokeText(ch, (LEFT_XOFFS + lengte - 200), (TOP_YOFFS - 10));

        /* teken vertikaal grid */
        for (int tel = 1; tel < aantaltxt; tel++)
            gc.strokeLine(LEFT_XOFFS + tel * afstand, TOP_YOFFS, LEFT_XOFFS + tel * afstand, hoogte + TOP_YOFFS);

        /* teken de x en y as in zwart (+ boven en rechts) */
        gc.setStroke(Color.BLACK);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, LEFT_XOFFS, maxy - BOTTEM_YOFFS);
        gc.strokeLine(lengte + LEFT_XOFFS, TOP_YOFFS, lengte + LEFT_XOFFS, maxy - BOTTEM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, (maxy - BOTTEM_YOFFS), lengte + LEFT_XOFFS, maxy - BOTTEM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, lengte + LEFT_XOFFS, TOP_YOFFS);

        /* Bepaal VSCALE m.b.v. Maximum en minimum */

        s.setMaximum(PHASE_MAX);
        s.setMinimum(PHASE_MIN);

        Max_abs = s.getMaximum() - s.getMinimum(); /* verschil tussen minimum en maximum */
        aantaltxt = 11;
        afstand = -hoogte / (aantaltxt - 1);
        delta = Max_abs / (aantaltxt - 1); /* delta is 20 graden */

        s.setVScale(Max_abs / hoogte);

        gc.setStroke(Color.GRAY);
        for (int tel = 0; tel < aantaltxt; tel++) {
            int ywaarde = (int) (s.getMaximum() - delta * tel);
            ch = String.format("%7d", ywaarde);
            gc.strokeText(ch, (LEFT_XOFFS - 80), (int) -(tel * afstand - TOP_YOFFS - 5));
        }

        gc.strokeText("Degrees", LEFT_XOFFS, TOP_YOFFS - 10);

        for (int tel = 1; tel < aantaltxt - 1; tel++) {
            gc.strokeLine(LEFT_XOFFS + 1, -(tel * afstand - TOP_YOFFS), lengte + LEFT_XOFFS - 1,
                    -(tel * afstand - TOP_YOFFS));
        }
    }

    private void gen_phase_plot(Signal s, GraphicsContext gc) {
        int nul_offs;
        double hscale;

        hscale = s.getHScale();
        double[] temp = new double[s.getDataRecords() * s.getDataLength()];
        double[] real_data = s.getRealData(); /* display real data */

        /*
         * Bereken het adres van het channel dat je wil zien. In het fase domein
         * bevatten channels altijd maar 1 record
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
        int lengte, hoogte, aantaltxt;
        double delta_a, delta_n, Max_abs;
        double xwaarde, afstand;
        int maxy = s.getWindowHeight();

        String nm = "Signal " + s.getName() + "   Histogram (Time)";

        lengte = s.getWindowWidth() - LEFT_XOFFS - RIGHT_XOFFS; /* Lengte horizontale as */
        hoogte = s.getWindowHeight() - TOP_YOFFS - BOTTEM_YOFFS; /* Lengte verticale as */
        afstand = PIXEL_DIST; /* Aantal pixels tussen twee getallen */

        gc.setStroke(Color.RED);
        double fontHeight = gc.getFont().getSize();
        gc.strokeText(nm, (lengte / 2), fontHeight);

        double min = getRecordMin(s);
        double max = getRecordMax(s);
        s.setMinimum(min);
        s.setMaximum(max);
        aantaltxt = (int) ((lengte / afstand) + 1); /* Aantal getallen horizontale as */
        delta_a = (double) (max - min) / (double) (aantaltxt - 1); /* ampl tussen twee getallen */
        afstand = (double) (lengte) / (double) (aantaltxt - 1); /* Aantal pixels tussen twee getallen */

        gc.setStroke(Color.GRAY);
        gc.strokeText("--> Magnitude", (lengte / 2), maxy - fontHeight);

        /* print 0 x-as text */
        String ch = String.format("%5.1f", min);
        gc.strokeText(ch, LEFT_XOFFS, maxy - 28);

        /*
         * for(tel=1;tel < aantaltxt;tel++) {
         */
        int tel = aantaltxt - 1;

        /*
         * xwaarde[tel]=(delta_a*tel+DataRealMin(vp)); sprintf(ch,"%5.1f",xwaarde[tel]);
         */

        xwaarde = (delta_a * tel + min);
        ch = String.format("%5.1f", xwaarde);
        gc.strokeText(ch, (int) (tel * afstand + LEFT_XOFFS - 25), (int) (maxy - 28));

        /* } */

        /* print record_nr en channel_nr */

        ch = String.format("Chan: %d", s.getChannel());
        gc.strokeText(ch, (LEFT_XOFFS + lengte - 100), TOP_YOFFS - 10);

        /* teken vertikaal grid */

        for (int i = 1; i < aantaltxt; i++)
            gc.strokeLine(LEFT_XOFFS + i * afstand, TOP_YOFFS, LEFT_XOFFS + i * afstand, hoogte + TOP_YOFFS);

        /* teken de x en y as in zwart (+ boven en rechts) */
        gc.setStroke(Color.GRAY);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, LEFT_XOFFS, maxy - BOTTEM_YOFFS);
        gc.strokeLine(lengte + LEFT_XOFFS, TOP_YOFFS, lengte + LEFT_XOFFS, maxy - BOTTEM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, (maxy - BOTTEM_YOFFS), lengte + LEFT_XOFFS, maxy - BOTTEM_YOFFS);
        gc.strokeLine(LEFT_XOFFS, TOP_YOFFS, lengte + LEFT_XOFFS, TOP_YOFFS);

        /* Bepaal VSCALE m.b.v. Maximum en minimum */

        double[] real_data = s.getRealData(); /* display real data */

        Max_abs = 100; /* verschil tussen minimum en maximum in % */
        aantaltxt = (int) 11;
        afstand = -hoogte / (aantaltxt - 1);
        delta_n = 10;

        s.setVScale(s.getMaximum() / hoogte);

        gc.setStroke(Color.GRAY);
        for (int i = 0; i < aantaltxt; i++) {
            int ywaarde = (int) (100 - delta_n * i);
            ch = String.format("%7d", ywaarde);
            gc.strokeText(ch, (LEFT_XOFFS - 80), (int) -(i * afstand - TOP_YOFFS - 5));
        }

        gc.strokeText("%%", LEFT_XOFFS, TOP_YOFFS - 10);

        for (tel = 1; tel < aantaltxt - 1; tel++) {
            gc.strokeLine(LEFT_XOFFS + 1, -(tel * afstand - TOP_YOFFS), lengte + LEFT_XOFFS - 1,
                    -(tel * afstand - TOP_YOFFS));
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
        int hoogte = s.getWindowHeight() - TOP_YOFFS - BOTTEM_YOFFS;

        double[] cum = new double[s.getDataLength()];
        double[] data = s.getRealData(); /* display real data */

        /* Welk record displayen? */

        int offset = (s.getChannel() * s.getDataRecords() + s.getRecord()) * s.getDataLength();

        double[] temp = Arrays.copyOfRange(data, offset, offset + s.getDataLength());

        cum[0] = temp[0];

        for (int i = 0; i < s.getDataLength(); i++) {
            if (i > 0)
                cum[i] = cum[i - 1] + temp[i];
            temp[i] = (temp[i] / vscale);
        }

        double cscale = cum[s.getDataLength() - 1] / hoogte;

        int nul_offs = hoogte + TOP_YOFFS;

        gc.setStroke(Color.BLUE);
        for (int i = 0; i < s.getDataLength(); i++) {
            gc.strokeLine((int) (hscale * i + LEFT_XOFFS), nul_offs, (int) (hscale * i + LEFT_XOFFS),
                    (int) -(temp[i] - nul_offs));
        }

        gc.setStroke(Color.GREEN);
        for (int i = 0; i < (s.getDataLength() - 1); i++) {
            gc.strokeLine((int) (hscale * i + LEFT_XOFFS), (int) -((cum[i] / cscale) - nul_offs),
                    (int) (hscale * (i + 1) + LEFT_XOFFS), (int) -((cum[i + 1] / cscale) - nul_offs));
        }
    }

}