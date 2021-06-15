package sample;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.util.Pair;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Controller {

    @FXML
    private Canvas canvas;
    @FXML
    private TextField sizeX;
    @FXML
    private TextField sizeY;
    @FXML
    private TextField liczbaZiaren;
    @FXML
    private ChoiceBox warunkiBrzegowe;
    @FXML
    private ChoiceBox zarodkowanie;
    @FXML
    private ChoiceBox sasiedztwo;
    //MC:
    @FXML
    private TextField liczbaIteracjiMC;
    @FXML
    private TextField ktMC;
    @FXML
    private Canvas canvas2;
    @FXML
    private TextField parameterA;
    @FXML
    private TextField parameterB;
    @FXML
    private Canvas canvas3;

    static final double SIGMAZERO = 0;
    static final double ALFA = 1.9;
    static final double MI = 2.57E-10;
    static final double BETA = 8E+10;


    private Zarodkowanie typZarodkowania;
    private Sasiedztwo typSasiedztwa;
    private WarunekBrzegowy typWarunku;
    private int lZiaren = 0;
    private int screenSizeX = 500;
    private int screenSizeY = 500;
    private int cellSize = 1;
    private int _lIteracjiMC = 0;
    private int _ktMC = 0;
    private double _parametrA = 0;
    private double _parametrB = 0;
    private double time = 0;
    private double Ro = 1;
    private double Sigma = 3.91E+01;
    private double RoKrytyczne = 1;

    private List<Seed> ziarna = new ArrayList<>();
    private List<Seed> ziarnaRek = new ArrayList<>();
    List<String[]> dataLines = new ArrayList<>();

    ControllerHelper helperMethods = new ControllerHelper();


    //CA

    public void Initialize() {
        InitializeWarunkiBrzegowe();
        InitializeZarodkowanie();
        InitializeSasiedztwo();
        canvas.getGraphicsContext2D().setFill(Color.WHITE);
        OnMouseClick();
    }

    public void StartDrawing() {
        SetZarodkowanie();
        SetWarunek();
        SetSasiedztwo();
        if (typZarodkowania == Zarodkowanie.LOSOWE) {
            SetLiczbaZiaren();
            for (int i = 0; i < lZiaren; i++) {
                Seed ziarno = DrawCell();
                ziarna.add(ziarno);
            }
        }
    }

    public void Iterate() {
        StartIteration();
    }

    public void SetSize() {

        if (helperMethods.IsNumeric(sizeX.getText()) && helperMethods.IsNumeric(sizeX.getText())) {
            if (Integer.parseInt(sizeX.getText()) > 500 || Integer.parseInt(sizeX.getText()) > 500) {
                ControllerHelper.InvalidInputError();
            } else {
                screenSizeX = Integer.parseInt(sizeX.getText());
                screenSizeY = Integer.parseInt(sizeY.getText());
                double x = (double) 500 / (double) screenSizeX;
                double y = (double) 500 / (double) screenSizeY;
                cellSize = (int) x * (int) y;
                System.out.println("celsize:" + cellSize);
            }
        } else
            ControllerHelper.InvalidInputError();
    }

    public void Clear() {
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.getGraphicsContext2D().setFill(Color.WHITE);
        canvas2.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas2.getGraphicsContext2D().setFill(Color.WHITE);
        canvas3.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas3.getGraphicsContext2D().setFill(Color.WHITE);
        dataLines.clear();
        ziarna.clear();
        ziarnaRek.clear();
        time = 0;
        Ro = 1;
        Sigma = 3.91E+01;
        RoKrytyczne = 1;
    }

    public void OnMouseClick() {
        canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                DrawSingleCell((int) event.getSceneX() - 200, (int) event.getSceneY() - 20);
            }
        });
    }

    private void DrawSingleCell(int x, int y) {
        SetZarodkowanie();
        if (typZarodkowania == Zarodkowanie.WYKLIKANIE) {
            boolean isEmpty = true;
            int newX = 0;
            int newY = 0;
            if (x <= 500 && x >= 0)
                newX = ((int) ((double) x / (double) cellSize)) * cellSize;
            if (y <= 500 && y >= 0)
                newY = ((int) ((double) y / (double) cellSize)) * cellSize;
            for (Seed ziarno : ziarna
            ) {
                if (ziarno.x == newX && ziarno.y == newY) {
                    isEmpty = false;
                    break;
                }
            }
            if (isEmpty) {
                PixelWriter pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
                Color newColor = helperMethods.GetRandomColor();
                for (int i = 0; i < cellSize; i++) {
                    for (int j = 0; j < cellSize; j++) {
                        pixelWriter.setColor(newX + j, newY + i, newColor);
                    }
                }
                Seed ziarno = new Seed(newX, newY, newColor);
                ziarna.add(ziarno);
            }
        }
    }

    private void StartIteration() {
        if (typSasiedztwa == Sasiedztwo.VONNEUMANN)
            DrawSasiedztwoNeumann();
        else if (typSasiedztwa == Sasiedztwo.MOORE)
            DrawSasiedztwoMoore();
    }

    private void SetLiczbaZiaren() {

        if (helperMethods.IsNumeric(liczbaZiaren.getText())) {
            lZiaren = Integer.parseInt(liczbaZiaren.getText());
        } else
            ControllerHelper.InvalidInputError();
    }

    private void SetZarodkowanie() {
        if (zarodkowanie.getValue() != null) {
            switch (zarodkowanie.getValue().toString()) {
                case "Losowe" -> typZarodkowania = Zarodkowanie.LOSOWE;
                case "Jednorodne" -> typZarodkowania = Zarodkowanie.JEDNORODNE;
                case "Z promieniem" -> typZarodkowania = Zarodkowanie.PROMIEN;
                case "Wyklikanie" -> typZarodkowania = Zarodkowanie.WYKLIKANIE;
            }
            System.out.println(zarodkowanie.getValue().toString());
        }
    }

    private void SetWarunek() {
        if (warunkiBrzegowe.getValue() != null) {
            switch (warunkiBrzegowe.getValue().toString()) {
                case "Absorbujacy" -> typWarunku = WarunekBrzegowy.ABSORBUJACE;
                case "Periodyczny" -> typWarunku = WarunekBrzegowy.PERIODYCZNE;
            }
        }
    }

    private void SetSasiedztwo() {
        if (sasiedztwo.getValue() != null) {
            switch (sasiedztwo.getValue().toString()) {
                case "Von Neumann" -> typSasiedztwa = Sasiedztwo.VONNEUMANN;
                case "Moore" -> typSasiedztwa = Sasiedztwo.MOORE;
                case "Pent losowe" -> typSasiedztwa = Sasiedztwo.PENTLOSOWE;
                case "Z promieniem" -> typSasiedztwa = Sasiedztwo.PROMIEN;
                case "Heks losowe" -> typSasiedztwa = Sasiedztwo.HEKSLOSOWE;
                case "Heks Prawe" -> typSasiedztwa = Sasiedztwo.HEKSPRAWE;
                case "Heks lewe" -> typSasiedztwa = Sasiedztwo.HEKSLEWE;
            }
        }
    }

    private Seed DrawCell() {
        boolean isSearching = true;
        boolean findXY = true;

        int x = helperMethods.GetRandomInt(0, (500 / cellSize) - 1);
        int y = helperMethods.GetRandomInt(0, (500 / cellSize) - 1);
        x *= cellSize;
        y *= cellSize;

        WritableImage snap = canvas.snapshot(null, null);

        while (isSearching) {
            for (int i = 0; i < cellSize; i++) {
                for (int j = 0; j < cellSize; j++) {
                    int colorAtPoint = snap.getPixelReader().getArgb(x + i, y + j);
                    if (colorAtPoint != -1) {
                        findXY = false;
                        break;
                    }
                }
                if (!findXY)
                    break;
            }
            if (!findXY) {
                x = helperMethods.GetRandomInt(0, (500 / cellSize) - 1);
                y = helperMethods.GetRandomInt(0, (500 / cellSize) - 1);
                x *= cellSize;
                y *= cellSize;
                System.out.println(x + " " + y);
                findXY = true;
            } else
                isSearching = false;
        }


        PixelWriter pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
        Color newColor = helperMethods.GetRandomColor();
        for (int i = 0; i < cellSize; i++) {
            for (int j = 0; j < cellSize; j++) {
                pixelWriter.setColor(x + j, y + i, newColor);
            }
        }
        return new Seed(x, y, newColor);
    }

    private void DrawSasiedztwoNeumann() {
        WritableImage snap = canvas.snapshot(null, null);
        PixelWriter pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
        List<Seed> noweZiarna = new ArrayList<>();
        for (Seed ziarno : ziarna
        ) {
            int[] colorsAtPoints = new int[5];
            Color[] winningColors = new Color[5];
            colorsAtPoints[0] = snap.getPixelReader().getArgb(ziarno.x, ziarno.y);
            winningColors[0] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getRed() * 255),
                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getGreen() * 255),
                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getBlue() * 255));
            try {
                colorsAtPoints[1] = snap.getPixelReader().getArgb(ziarno.x - 1 * cellSize, ziarno.y);
                winningColors[1] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y).getRed() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y).getGreen() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y).getBlue() * 255));
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    colorsAtPoints[1] = snap.getPixelReader().getArgb(500 - 1 * cellSize, ziarno.y);
                    winningColors[1] = Color.rgb((int) (snap.getPixelReader().getColor(screenSizeX - 1 * cellSize, ziarno.y).getRed() * 255),
                            (int) (snap.getPixelReader().getColor(500 - 1 * cellSize, ziarno.y).getGreen() * 255),
                            (int) (snap.getPixelReader().getColor(500 - 1 * cellSize, ziarno.y).getBlue() * 255));
                }
            }
            try {
                colorsAtPoints[2] = snap.getPixelReader().getArgb(ziarno.x + 1 * cellSize, ziarno.y);
                winningColors[2] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y).getRed() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y).getGreen() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y).getBlue() * 255));
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    colorsAtPoints[2] = snap.getPixelReader().getArgb(0, ziarno.y);
                    winningColors[2] = Color.rgb((int) (snap.getPixelReader().getColor(0, ziarno.y).getRed() * 255),
                            (int) (snap.getPixelReader().getColor(0, ziarno.y).getGreen() * 255),
                            (int) (snap.getPixelReader().getColor(0, ziarno.y).getBlue() * 255));
                }
            }
            try {
                colorsAtPoints[3] = snap.getPixelReader().getArgb(ziarno.x, ziarno.y - 1 * cellSize);
                winningColors[3] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y - 1 * cellSize).getRed() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y - 1 * cellSize).getGreen() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y - 1 * cellSize).getBlue() * 255));
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    colorsAtPoints[3] = snap.getPixelReader().getArgb(ziarno.x, 500 - 1 * cellSize);
                    winningColors[3] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, 500 - 1 * cellSize).getRed() * 255),
                            (int) (snap.getPixelReader().getColor(ziarno.x, 500 - 1 * cellSize).getGreen() * 255),
                            (int) (snap.getPixelReader().getColor(ziarno.x, 500 - 1 * cellSize).getBlue() * 255));
                }
            }
            try {
                colorsAtPoints[4] = snap.getPixelReader().getArgb(ziarno.x, ziarno.y + 1 * cellSize);
                winningColors[4] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y + 1 * cellSize).getRed() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y + 1 * cellSize).getGreen() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y + 1 * cellSize).getBlue() * 255));
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    colorsAtPoints[4] = snap.getPixelReader().getArgb(ziarno.x, 0);
                    winningColors[4] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, 0).getRed() * 255),
                            (int) (snap.getPixelReader().getColor(ziarno.x, 0).getGreen() * 255),
                            (int) (snap.getPixelReader().getColor(ziarno.x, 0).getBlue() * 255));
                }
            }

            int modeIndex = helperMethods.mode(colorsAtPoints);
            System.out.println("coloor: " + winningColors[modeIndex]);

            try {
                if (snap.getPixelReader().getArgb(ziarno.x - 1 * cellSize, ziarno.y) == -1) {
                    for (int i = 0; i < cellSize; i++) {
                        for (int j = 0; j < cellSize; j++) {

                            pixelWriter.setColor(ziarno.x - 1 * cellSize + j, ziarno.y + i, winningColors[modeIndex]);
                        }
                    }
                    Seed newZiarno = new Seed(ziarno.x - 1 * cellSize, ziarno.y, winningColors[modeIndex]);
                    noweZiarna.add(newZiarno);
                    snap = canvas.snapshot(null, null);
                }

            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(500 - 1 * cellSize, ziarno.y) == -1) {
                        for (int i = 0; i < cellSize; i++) {
                            for (int j = 0; j < cellSize; j++) {

                                pixelWriter.setColor((500 - 1 * cellSize) + j, ziarno.y + i, winningColors[modeIndex]);

                            }
                        }
                        Seed newZiarno = new Seed(500 - 1 * cellSize, ziarno.y, winningColors[modeIndex]);
                        noweZiarna.add(newZiarno);
                        snap = canvas.snapshot(null, null);
                    }

                }

            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x + 1 * cellSize, ziarno.y) == -1) {
                    for (int i = 0; i < cellSize; i++) {
                        for (int j = 0; j < cellSize; j++) {
                            pixelWriter.setColor(ziarno.x + 1 * cellSize + j, ziarno.y + i, winningColors[modeIndex]);

                        }
                    }
                    Seed newZiarno = new Seed(ziarno.x + 1 * cellSize, ziarno.y, winningColors[modeIndex]);
                    noweZiarna.add(newZiarno);
                    snap = canvas.snapshot(null, null);
                }
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(0, ziarno.y) == -1) {
                        for (int i = 0; i < cellSize; i++) {
                            for (int j = 0; j < cellSize; j++) {

                                pixelWriter.setColor(j, ziarno.y + i, winningColors[modeIndex]);

                            }
                        }
                        Seed newZiarno = new Seed(0, ziarno.y, winningColors[modeIndex]);
                        noweZiarna.add(newZiarno);
                        snap = canvas.snapshot(null, null);
                    }
                }
            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x, ziarno.y - 1 * cellSize) == -1) {
                    for (int i = 0; i < cellSize; i++) {
                        for (int j = 0; j < cellSize; j++) {
                            pixelWriter.setColor(ziarno.x + j, ziarno.y - 1 * cellSize + i, winningColors[modeIndex]);

                        }
                    }
                    Seed newZiarno = new Seed(ziarno.x, ziarno.y - 1 * cellSize, winningColors[modeIndex]);
                    noweZiarna.add(newZiarno);
                    snap = canvas.snapshot(null, null);
                }
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(ziarno.x, 500 - 1 * cellSize) == -1) {
                        for (int i = 0; i < cellSize; i++) {
                            for (int j = 0; j < cellSize; j++) {
                                pixelWriter.setColor(ziarno.x + j, 500 - 1 * cellSize + i, winningColors[modeIndex]);

                            }
                        }
                        Seed newZiarno = new Seed(ziarno.x, 500 - 1 * cellSize, winningColors[modeIndex]);
                        noweZiarna.add(newZiarno);
                        snap = canvas.snapshot(null, null);
                    }
                }
            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x, ziarno.y + 1 * cellSize) == -1) {
                    for (int i = 0; i < cellSize; i++) {
                        for (int j = 0; j < cellSize; j++) {
                            pixelWriter.setColor(ziarno.x + j, ziarno.y + 1 * cellSize + i, winningColors[modeIndex]);

                        }
                    }
                    Seed newZiarno = new Seed(ziarno.x, ziarno.y + 1 * cellSize, winningColors[modeIndex]);
                    noweZiarna.add(newZiarno);
                    snap = canvas.snapshot(null, null);
                }
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(ziarno.x, 0) == -1) {
                        for (int i = 0; i < cellSize; i++) {
                            for (int j = 0; j < cellSize; j++) {
                                pixelWriter.setColor(ziarno.x + j, i, winningColors[modeIndex]);

                            }
                        }
                        Seed newZiarno = new Seed(ziarno.x, 0, winningColors[modeIndex]);
                        noweZiarna.add(newZiarno);
                        snap = canvas.snapshot(null, null);
                    }

                }
            }
        }
        ziarna = noweZiarna;
    }

    private void DrawSasiedztwoMoore() {
        WritableImage snap = canvas.snapshot(null, null);
        PixelWriter pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
        List<Seed> noweZiarna = new ArrayList<>();
        for (Seed ziarno : ziarna
        ) {
            int[] colorsAtPoints = new int[9];
            Color[] winningColors = new Color[9];
            colorsAtPoints[0] = snap.getPixelReader().getArgb(ziarno.x, ziarno.y);
            winningColors[0] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getRed() * 255),
                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getGreen() * 255),
                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getBlue() * 255));
            try {
                colorsAtPoints[1] = snap.getPixelReader().getArgb(ziarno.x - 1 * cellSize, ziarno.y);
                winningColors[1] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y).getRed() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y).getGreen() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y).getBlue() * 255));
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    colorsAtPoints[1] = snap.getPixelReader().getArgb(500 - 1 * cellSize, ziarno.y);
                    winningColors[1] = Color.rgb((int) (snap.getPixelReader().getColor(screenSizeX - 1 * cellSize, ziarno.y).getRed() * 255),
                            (int) (snap.getPixelReader().getColor(500 - 1 * cellSize, ziarno.y).getGreen() * 255),
                            (int) (snap.getPixelReader().getColor(500 - 1 * cellSize, ziarno.y).getBlue() * 255));
                }
            }
            try {
                colorsAtPoints[2] = snap.getPixelReader().getArgb(ziarno.x + 1 * cellSize, ziarno.y);
                winningColors[2] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y).getRed() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y).getGreen() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y).getBlue() * 255));
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    colorsAtPoints[2] = snap.getPixelReader().getArgb(0, ziarno.y);
                    winningColors[2] = Color.rgb((int) (snap.getPixelReader().getColor(0, ziarno.y).getRed() * 255),
                            (int) (snap.getPixelReader().getColor(0, ziarno.y).getGreen() * 255),
                            (int) (snap.getPixelReader().getColor(0, ziarno.y).getBlue() * 255));
                }
            }
            try {
                colorsAtPoints[3] = snap.getPixelReader().getArgb(ziarno.x, ziarno.y - 1 * cellSize);
                winningColors[3] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y - 1 * cellSize).getRed() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y - 1 * cellSize).getGreen() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y - 1 * cellSize).getBlue() * 255));
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    colorsAtPoints[3] = snap.getPixelReader().getArgb(ziarno.x, 500 - 1 * cellSize);
                    winningColors[3] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, 500 - 1 * cellSize).getRed() * 255),
                            (int) (snap.getPixelReader().getColor(ziarno.x, 500 - 1 * cellSize).getGreen() * 255),
                            (int) (snap.getPixelReader().getColor(ziarno.x, 500 - 1 * cellSize).getBlue() * 255));
                }
            }
            try {
                colorsAtPoints[4] = snap.getPixelReader().getArgb(ziarno.x, ziarno.y + 1 * cellSize);
                winningColors[4] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y + 1 * cellSize).getRed() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y + 1 * cellSize).getGreen() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y + 1 * cellSize).getBlue() * 255));
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    colorsAtPoints[4] = snap.getPixelReader().getArgb(ziarno.x, 0);
                    winningColors[4] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, 0).getRed() * 255),
                            (int) (snap.getPixelReader().getColor(ziarno.x, 0).getGreen() * 255),
                            (int) (snap.getPixelReader().getColor(ziarno.x, 0).getBlue() * 255));
                }
            }
            try {
                colorsAtPoints[5] = snap.getPixelReader().getArgb(ziarno.x + 1 * cellSize, ziarno.y + 1 * cellSize);
                winningColors[5] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y + 1 * cellSize).getRed() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y + 1 * cellSize).getGreen() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y + 1 * cellSize).getBlue() * 255));
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    try {
                        colorsAtPoints[5] = snap.getPixelReader().getArgb(ziarno.x + 1 * cellSize, 0);
                        winningColors[5] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, 0).getRed() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, 0).getGreen() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, 0).getBlue() * 255));
                    } catch (IndexOutOfBoundsException exception2) {
                        try {
                            colorsAtPoints[5] = snap.getPixelReader().getArgb(0, ziarno.y + 1 * cellSize);
                            winningColors[5] = Color.rgb((int) (snap.getPixelReader().getColor(0, ziarno.y + 1 * cellSize).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(0, ziarno.y + 1 * cellSize).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(0, ziarno.y + 1 * cellSize).getBlue() * 255));
                        } catch (IndexOutOfBoundsException exception3) {
                            colorsAtPoints[5] = snap.getPixelReader().getArgb(0, 0);
                            winningColors[5] = Color.rgb((int) (snap.getPixelReader().getColor(0, 0).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(0, 0).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(0, 0).getBlue() * 255));
                        }
                    }
                }
            }
            try {
                colorsAtPoints[6] = snap.getPixelReader().getArgb(ziarno.x - 1 * cellSize, ziarno.y + 1 * cellSize);
                winningColors[6] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y + 1 * cellSize).getRed() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y + 1 * cellSize).getGreen() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y + 1 * cellSize).getBlue() * 255));
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    try {
                        colorsAtPoints[6] = snap.getPixelReader().getArgb(ziarno.x - 1 * cellSize, 0);
                        winningColors[6] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, 0).getRed() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, 0).getGreen() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, 0).getBlue() * 255));
                    } catch (IndexOutOfBoundsException exception2) {
                        try {
                            colorsAtPoints[6] = snap.getPixelReader().getArgb(500 - cellSize, ziarno.y + 1 * cellSize);
                            winningColors[6] = Color.rgb((int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y + 1 * cellSize).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y + 1 * cellSize).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y + 1 * cellSize).getBlue() * 255));
                        } catch (IndexOutOfBoundsException exception3) {
                            colorsAtPoints[6] = snap.getPixelReader().getArgb(500 - cellSize, 0);
                            winningColors[6] = Color.rgb((int) (snap.getPixelReader().getColor(500 - cellSize, 0).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(500 - cellSize, 0).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(500 - cellSize, 0).getBlue() * 255));
                        }
                    }
                }

            }
            try {
                colorsAtPoints[7] = snap.getPixelReader().getArgb(ziarno.x - 1 * cellSize, ziarno.y - 1 * cellSize);
                winningColors[7] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y - 1 * cellSize).getRed() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y - 1 * cellSize).getGreen() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y - 1 * cellSize).getBlue() * 255));
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    try {
                        colorsAtPoints[7] = snap.getPixelReader().getArgb(ziarno.x - 1 * cellSize, 500 - cellSize);
                        winningColors[7] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, 500 - cellSize).getRed() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, 500 - cellSize).getGreen() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, 500 - cellSize).getBlue() * 255));
                    } catch (IndexOutOfBoundsException exception2) {
                        try {
                            colorsAtPoints[7] = snap.getPixelReader().getArgb(500 - cellSize, ziarno.y - 1 * cellSize);
                            winningColors[7] = Color.rgb((int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y - 1 * cellSize).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y - 1 * cellSize).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y - 1 * cellSize).getBlue() * 255));
                        } catch (IndexOutOfBoundsException exception3) {
                            colorsAtPoints[7] = snap.getPixelReader().getArgb(500 - cellSize, 500 - cellSize);
                            winningColors[7] = Color.rgb((int) (snap.getPixelReader().getColor(500 - cellSize, 500 - cellSize).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(500 - cellSize, 500 - cellSize).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(500 - cellSize, 500 - cellSize).getBlue() * 255));
                        }
                    }
                }

            }
            try {
                colorsAtPoints[8] = snap.getPixelReader().getArgb(ziarno.x + 1 * cellSize, ziarno.y - 1 * cellSize);
                winningColors[8] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y - 1 * cellSize).getRed() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y - 1 * cellSize).getGreen() * 255),
                        (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y - 1 * cellSize).getBlue() * 255));
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    try {
                        colorsAtPoints[8] = snap.getPixelReader().getArgb(ziarno.x + 1 * cellSize, 500 - cellSize);
                        winningColors[8] = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, 500 - cellSize).getRed() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, 500 - cellSize).getGreen() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, 500 - cellSize).getBlue() * 255));
                    } catch (IndexOutOfBoundsException exception2) {
                        try {
                            colorsAtPoints[8] = snap.getPixelReader().getArgb(0, ziarno.y - 1 * cellSize);
                            winningColors[8] = Color.rgb((int) (snap.getPixelReader().getColor(0, ziarno.y - 1 * cellSize).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(0, ziarno.y - 1 * cellSize).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(0, ziarno.y - 1 * cellSize).getBlue() * 255));
                        } catch (IndexOutOfBoundsException exception3) {
                            colorsAtPoints[8] = snap.getPixelReader().getArgb(0, 500 - cellSize);
                            winningColors[8] = Color.rgb((int) (snap.getPixelReader().getColor(0, 500 - cellSize).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(0, 500 - cellSize).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(0, 500 - cellSize).getBlue() * 255));
                        }
                    }
                }

            }

            int modeIndex = helperMethods.mode(colorsAtPoints);
            System.out.println("coloor: " + winningColors[modeIndex]);

            try {
                if (snap.getPixelReader().getArgb(ziarno.x - 1 * cellSize, ziarno.y) == -1) {
                    for (int i = 0; i < cellSize; i++) {
                        for (int j = 0; j < cellSize; j++) {

                            pixelWriter.setColor(ziarno.x - 1 * cellSize + j, ziarno.y + i, winningColors[modeIndex]);
                        }
                    }
                    Seed newZiarno = new Seed(ziarno.x - 1 * cellSize, ziarno.y, winningColors[modeIndex]);
                    noweZiarna.add(newZiarno);
                    snap = canvas.snapshot(null, null);
                }

            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(500 - 1 * cellSize, ziarno.y) == -1) {
                        for (int i = 0; i < cellSize; i++) {
                            for (int j = 0; j < cellSize; j++) {

                                pixelWriter.setColor((500 - 1 * cellSize) + j, ziarno.y + i, winningColors[modeIndex]);

                            }
                        }
                        Seed newZiarno = new Seed(500 - 1 * cellSize, ziarno.y, winningColors[modeIndex]);
                        noweZiarna.add(newZiarno);
                        snap = canvas.snapshot(null, null);
                    }

                }

            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x + 1 * cellSize, ziarno.y) == -1) {
                    for (int i = 0; i < cellSize; i++) {
                        for (int j = 0; j < cellSize; j++) {
                            pixelWriter.setColor(ziarno.x + 1 * cellSize + j, ziarno.y + i, winningColors[modeIndex]);

                        }
                    }
                    Seed newZiarno = new Seed(ziarno.x + 1 * cellSize, ziarno.y, winningColors[modeIndex]);
                    noweZiarna.add(newZiarno);
                    snap = canvas.snapshot(null, null);
                }
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(0, ziarno.y) == -1) {
                        for (int i = 0; i < cellSize; i++) {
                            for (int j = 0; j < cellSize; j++) {

                                pixelWriter.setColor(j, ziarno.y + i, winningColors[modeIndex]);

                            }
                        }
                        Seed newZiarno = new Seed(0, ziarno.y, winningColors[modeIndex]);
                        noweZiarna.add(newZiarno);
                        snap = canvas.snapshot(null, null);
                    }
                }
            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x, ziarno.y - 1 * cellSize) == -1) {
                    for (int i = 0; i < cellSize; i++) {
                        for (int j = 0; j < cellSize; j++) {
                            pixelWriter.setColor(ziarno.x + j, ziarno.y - 1 * cellSize + i, winningColors[modeIndex]);

                        }
                    }
                    Seed newZiarno = new Seed(ziarno.x, ziarno.y - 1 * cellSize, winningColors[modeIndex]);
                    noweZiarna.add(newZiarno);
                    snap = canvas.snapshot(null, null);
                }
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(ziarno.x, 500 - 1 * cellSize) == -1) {
                        for (int i = 0; i < cellSize; i++) {
                            for (int j = 0; j < cellSize; j++) {
                                pixelWriter.setColor(ziarno.x + j, 500 - 1 * cellSize + i, winningColors[modeIndex]);

                            }
                        }
                        Seed newZiarno = new Seed(ziarno.x, 500 - 1 * cellSize, winningColors[modeIndex]);
                        noweZiarna.add(newZiarno);
                        snap = canvas.snapshot(null, null);
                    }
                }
            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x, ziarno.y + 1 * cellSize) == -1) {
                    for (int i = 0; i < cellSize; i++) {
                        for (int j = 0; j < cellSize; j++) {
                            pixelWriter.setColor(ziarno.x + j, ziarno.y + 1 * cellSize + i, winningColors[modeIndex]);

                        }
                    }
                    Seed newZiarno = new Seed(ziarno.x, ziarno.y + 1 * cellSize, winningColors[modeIndex]);
                    noweZiarna.add(newZiarno);
                    snap = canvas.snapshot(null, null);
                }
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(ziarno.x, 0) == -1) {
                        for (int i = 0; i < cellSize; i++) {
                            for (int j = 0; j < cellSize; j++) {
                                pixelWriter.setColor(ziarno.x + j, i, winningColors[modeIndex]);

                            }
                        }
                        Seed newZiarno = new Seed(ziarno.x, 0, winningColors[modeIndex]);
                        noweZiarna.add(newZiarno);
                        snap = canvas.snapshot(null, null);
                    }

                }
            }
            //new
            try {
                if (snap.getPixelReader().getArgb(ziarno.x + 1 * cellSize, ziarno.y + 1 * cellSize) == -1) {
                    for (int i = 0; i < cellSize; i++) {
                        for (int j = 0; j < cellSize; j++) {
                            pixelWriter.setColor(ziarno.x + 1 * cellSize + j, ziarno.y + 1 * cellSize + i, winningColors[modeIndex]);

                        }
                    }
                    Seed newZiarno = new Seed(ziarno.x + 1 * cellSize, ziarno.y + 1 * cellSize, winningColors[modeIndex]);
                    noweZiarna.add(newZiarno);
                    snap = canvas.snapshot(null, null);
                }
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    try {
                        if (snap.getPixelReader().getArgb(ziarno.x + 1 * cellSize, 0) == -1) {
                            for (int i = 0; i < cellSize; i++) {
                                for (int j = 0; j < cellSize; j++) {
                                    pixelWriter.setColor(ziarno.x + 1 * cellSize + j, i, winningColors[modeIndex]);

                                }
                            }
                            Seed newZiarno = new Seed(ziarno.x + 1 * cellSize, 0, winningColors[modeIndex]);
                            noweZiarna.add(newZiarno);
                            snap = canvas.snapshot(null, null);
                        }
                    } catch (IndexOutOfBoundsException exception2) {
                        try {
                            if (snap.getPixelReader().getArgb(0, ziarno.y + cellSize) == -1) {
                                for (int i = 0; i < cellSize; i++) {
                                    for (int j = 0; j < cellSize; j++) {
                                        pixelWriter.setColor(j, ziarno.y + cellSize + i, winningColors[modeIndex]);

                                    }
                                }
                                Seed newZiarno = new Seed(0, ziarno.y + cellSize, winningColors[modeIndex]);
                                noweZiarna.add(newZiarno);
                                snap = canvas.snapshot(null, null);
                            }
                        } catch (IndexOutOfBoundsException exception3) {
                            if (snap.getPixelReader().getArgb(0, 0) == -1) {
                                for (int i = 0; i < cellSize; i++) {
                                    for (int j = 0; j < cellSize; j++) {
                                        pixelWriter.setColor(j, i, winningColors[modeIndex]);

                                    }
                                }
                                Seed newZiarno = new Seed(0, 0, winningColors[modeIndex]);
                                noweZiarna.add(newZiarno);
                                snap = canvas.snapshot(null, null);
                            }
                        }
                    }

                }
            }

            try {
                if (snap.getPixelReader().getArgb(ziarno.x - 1 * cellSize, ziarno.y + 1 * cellSize) == -1) {
                    for (int i = 0; i < cellSize; i++) {
                        for (int j = 0; j < cellSize; j++) {
                            pixelWriter.setColor(ziarno.x - 1 * cellSize + j, ziarno.y + 1 * cellSize + i, winningColors[modeIndex]);

                        }
                    }
                    Seed newZiarno = new Seed(ziarno.x - 1 * cellSize, ziarno.y + 1 * cellSize, winningColors[modeIndex]);
                    noweZiarna.add(newZiarno);
                    snap = canvas.snapshot(null, null);
                }
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    try {
                        if (snap.getPixelReader().getArgb(ziarno.x - 1 * cellSize, 0) == -1) {
                            for (int i = 0; i < cellSize; i++) {
                                for (int j = 0; j < cellSize; j++) {
                                    pixelWriter.setColor(ziarno.x - 1 * cellSize + j, i, winningColors[modeIndex]);

                                }
                            }
                            Seed newZiarno = new Seed(ziarno.x - 1 * cellSize, 0, winningColors[modeIndex]);
                            noweZiarna.add(newZiarno);
                            snap = canvas.snapshot(null, null);
                        }
                    } catch (IndexOutOfBoundsException exception2) {
                        try {
                            if (snap.getPixelReader().getArgb(500 - cellSize, ziarno.y + cellSize) == -1) {
                                for (int i = 0; i < cellSize; i++) {
                                    for (int j = 0; j < cellSize; j++) {
                                        pixelWriter.setColor(500 - cellSize + j, ziarno.y + cellSize + i, winningColors[modeIndex]);

                                    }
                                }
                                Seed newZiarno = new Seed(500 - cellSize, ziarno.y + cellSize, winningColors[modeIndex]);
                                noweZiarna.add(newZiarno);
                                snap = canvas.snapshot(null, null);
                            }
                        } catch (IndexOutOfBoundsException exception3) {
                            if (snap.getPixelReader().getArgb(500 - cellSize, 0) == -1) {
                                for (int i = 0; i < cellSize; i++) {
                                    for (int j = 0; j < cellSize; j++) {
                                        pixelWriter.setColor(500 - cellSize + j, i, winningColors[modeIndex]);

                                    }
                                }
                                Seed newZiarno = new Seed(500 - cellSize, 0, winningColors[modeIndex]);
                                noweZiarna.add(newZiarno);
                                snap = canvas.snapshot(null, null);
                            }
                        }
                    }

                }
            }

            try {
                if (snap.getPixelReader().getArgb(ziarno.x - 1 * cellSize, ziarno.y - 1 * cellSize) == -1) {
                    for (int i = 0; i < cellSize; i++) {
                        for (int j = 0; j < cellSize; j++) {
                            pixelWriter.setColor(ziarno.x - 1 * cellSize + j, ziarno.y - 1 * cellSize + i, winningColors[modeIndex]);

                        }
                    }
                    Seed newZiarno = new Seed(ziarno.x - 1 * cellSize, ziarno.y - 1 * cellSize, winningColors[modeIndex]);
                    noweZiarna.add(newZiarno);
                    snap = canvas.snapshot(null, null);
                }
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    try {
                        if (snap.getPixelReader().getArgb(ziarno.x - 1 * cellSize, 500 - cellSize) == -1) {
                            for (int i = 0; i < cellSize; i++) {
                                for (int j = 0; j < cellSize; j++) {
                                    pixelWriter.setColor(ziarno.x - 1 * cellSize + j, 500 - cellSize + i, winningColors[modeIndex]);

                                }
                            }
                            Seed newZiarno = new Seed(ziarno.x - 1 * cellSize, 500 - cellSize, winningColors[modeIndex]);
                            noweZiarna.add(newZiarno);
                            snap = canvas.snapshot(null, null);
                        }
                    } catch (IndexOutOfBoundsException exception2) {
                        try {
                            if (snap.getPixelReader().getArgb(500 - cellSize, ziarno.y - cellSize) == -1) {
                                for (int i = 0; i < cellSize; i++) {
                                    for (int j = 0; j < cellSize; j++) {
                                        pixelWriter.setColor(500 - cellSize + j, ziarno.y - cellSize + i, winningColors[modeIndex]);

                                    }
                                }
                                Seed newZiarno = new Seed(500 - cellSize, ziarno.y - cellSize, winningColors[modeIndex]);
                                noweZiarna.add(newZiarno);
                                snap = canvas.snapshot(null, null);
                            }
                        } catch (IndexOutOfBoundsException exception3) {
                            if (snap.getPixelReader().getArgb(500 - cellSize, 500 - cellSize) == -1) {
                                for (int i = 0; i < cellSize; i++) {
                                    for (int j = 0; j < cellSize; j++) {
                                        pixelWriter.setColor(500 - cellSize + j, 500 - cellSize + i, winningColors[modeIndex]);

                                    }
                                }
                                Seed newZiarno = new Seed(500 - cellSize, 500 - cellSize, winningColors[modeIndex]);
                                noweZiarna.add(newZiarno);
                                snap = canvas.snapshot(null, null);
                            }
                        }
                    }

                }
            }

            try {
                if (snap.getPixelReader().getArgb(ziarno.x + 1 * cellSize, ziarno.y - 1 * cellSize) == -1) {
                    for (int i = 0; i < cellSize; i++) {
                        for (int j = 0; j < cellSize; j++) {
                            pixelWriter.setColor(ziarno.x + 1 * cellSize + j, ziarno.y - 1 * cellSize + i, winningColors[modeIndex]);

                        }
                    }
                    Seed newZiarno = new Seed(ziarno.x + 1 * cellSize, ziarno.y - 1 * cellSize, winningColors[modeIndex]);
                    noweZiarna.add(newZiarno);
                    snap = canvas.snapshot(null, null);
                }
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    try {
                        if (snap.getPixelReader().getArgb(ziarno.x + 1 * cellSize, 500 - cellSize) == -1) {
                            for (int i = 0; i < cellSize; i++) {
                                for (int j = 0; j < cellSize; j++) {
                                    pixelWriter.setColor(ziarno.x + 1 * cellSize + j, 500 - cellSize + i, winningColors[modeIndex]);

                                }
                            }
                            Seed newZiarno = new Seed(ziarno.x + 1 * cellSize, 500 - cellSize, winningColors[modeIndex]);
                            noweZiarna.add(newZiarno);
                            snap = canvas.snapshot(null, null);
                        }
                    } catch (IndexOutOfBoundsException exception2) {
                        try {
                            if (snap.getPixelReader().getArgb(0, ziarno.y - cellSize) == -1) {
                                for (int i = 0; i < cellSize; i++) {
                                    for (int j = 0; j < cellSize; j++) {
                                        pixelWriter.setColor(j, ziarno.y - cellSize + i, winningColors[modeIndex]);

                                    }
                                }
                                Seed newZiarno = new Seed(0, ziarno.y - cellSize, winningColors[modeIndex]);
                                noweZiarna.add(newZiarno);
                                snap = canvas.snapshot(null, null);
                            }
                        } catch (IndexOutOfBoundsException exception3) {
                            if (snap.getPixelReader().getArgb(0, 500 - cellSize) == -1) {
                                for (int i = 0; i < cellSize; i++) {
                                    for (int j = 0; j < cellSize; j++) {
                                        pixelWriter.setColor(j, 500 - cellSize + i, winningColors[modeIndex]);

                                    }
                                }
                                Seed newZiarno = new Seed(0, 500 - cellSize, winningColors[modeIndex]);
                                noweZiarna.add(newZiarno);
                                snap = canvas.snapshot(null, null);
                            }
                        }
                    }

                }
            }
        }
        ziarna = noweZiarna;
    }

    private void InitializeWarunkiBrzegowe() {
        warunkiBrzegowe.getItems().add("Absorbujacy");
        warunkiBrzegowe.getItems().add("Periodyczny");
    }

    private void InitializeZarodkowanie() {
        zarodkowanie.getItems().add("Losowe");
        zarodkowanie.getItems().add("Jednorodne");
        zarodkowanie.getItems().add("Z promieniem");
        zarodkowanie.getItems().add("Wyklikanie");
    }

    private void InitializeSasiedztwo() {
        sasiedztwo.getItems().add("Von Neumann");
        sasiedztwo.getItems().add("Moore");
        sasiedztwo.getItems().add("Heks Prawe");
        sasiedztwo.getItems().add("Heks lewe");
        sasiedztwo.getItems().add("Heks losowe");
        sasiedztwo.getItems().add("Pent losowe");
        sasiedztwo.getItems().add("Z promieniem");
    }

    //Monte carlo

    public void StartMC() {
        SetLiczbaIteracjiMC();
        SetKTMC();
        GetAllSeedsFromCanvas();
        Collections.shuffle(ziarna);
        InitializeEnergy();
    }

    public void IterateMC() {
        if (_lIteracjiMC > 0) {
            System.out.println("iterate");
            canvas2.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            canvas2.getGraphicsContext2D().setFill(Color.WHITE);
            if (typSasiedztwa == Sasiedztwo.VONNEUMANN)
                DrawSasiedztwoNeumannMC();
            else if (typSasiedztwa == Sasiedztwo.MOORE)
                DrawSasiedztwoMooreMC();
            _lIteracjiMC--;
            liczbaIteracjiMC.setText(String.valueOf(_lIteracjiMC));
            InitializeEnergy();
        } else
            ControllerHelper.InvalidInputError();
    }

    private void SetLiczbaIteracjiMC() {

        if (helperMethods.IsNumeric(liczbaIteracjiMC.getText())) {
            _lIteracjiMC = Integer.parseInt(liczbaIteracjiMC.getText());
        } else
            ControllerHelper.InvalidInputError();
    }

    private void SetKTMC() {

        if (helperMethods.IsNumeric(ktMC.getText())) {
            int ktmcTemp = Integer.parseInt(ktMC.getText());
            if (ktmcTemp <= 6 && ktmcTemp >= 0.1)
                _ktMC = Integer.parseInt(ktMC.getText());
            else
                ControllerHelper.InvalidInputError();
        } else
            ControllerHelper.InvalidInputError();
    }

    private void DrawSasiedztwoNeumannMC() {
        WritableImage snap = canvas.snapshot(null, null);
        PixelWriter pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();

        for (Seed ziarno : ziarna
        ) {

            //losujemy inna komorke
            int selectedColorINT = 0;
            Color selectedColorCLR = Color.WHITE;
            int newEnergy = 0;

            switch (helperMethods.GetRandomInt(1, 4)) {
                case 1:
                    try {
                        selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y + cellSize);
                        selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y + cellSize).getRed() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y + cellSize).getGreen() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y + cellSize).getBlue() * 255));
                    } catch (IndexOutOfBoundsException exception) {
                        if (typWarunku == WarunekBrzegowy.ABSORBUJACE) {
                            System.out.println("outofbounds");
                            selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getBlue() * 255));
                        } else {
                            selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, 0);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, 0).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, 0).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, 0).getBlue() * 255));
                        }
                    }
                    break;
                case 2:
                    try {
                        selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y - cellSize);
                        selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y - cellSize).getRed() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y - cellSize).getGreen() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y - cellSize).getBlue() * 255));
                    } catch (IndexOutOfBoundsException exception) {
                        if (typWarunku == WarunekBrzegowy.ABSORBUJACE) {
                            System.out.println("outofbounds");
                            selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getBlue() * 255));
                        } else {
                            selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, 500 - cellSize);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, 500 - cellSize).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, 500 - cellSize).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, 500 - cellSize).getBlue() * 255));
                        }
                    }
                    break;
                case 3:
                    try {
                        selectedColorINT = snap.getPixelReader().getArgb(ziarno.x + cellSize, ziarno.y);
                        selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x + cellSize, ziarno.y).getRed() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x + cellSize, ziarno.y).getGreen() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x + cellSize, ziarno.y).getBlue() * 255));
                    } catch (IndexOutOfBoundsException exception) {
                        if (typWarunku == WarunekBrzegowy.ABSORBUJACE) {
                            System.out.println("outofbounds");
                            selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getBlue() * 255));
                        } else {
                            selectedColorINT = snap.getPixelReader().getArgb(0, ziarno.y);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(0, ziarno.y).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(0, ziarno.y).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(0, ziarno.y).getBlue() * 255));
                        }
                    }
                    break;
                default:
                    try {
                        selectedColorINT = snap.getPixelReader().getArgb(ziarno.x - cellSize, ziarno.y);
                        selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x - cellSize, ziarno.y).getRed() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x - cellSize, ziarno.y).getGreen() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x - cellSize, ziarno.y).getBlue() * 255));
                    } catch (IndexOutOfBoundsException exception) {
                        if (typWarunku == WarunekBrzegowy.ABSORBUJACE) {
                            System.out.println("outofbounds");
                            selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getBlue() * 255));
                        } else {
                            selectedColorINT = snap.getPixelReader().getArgb(500 - cellSize, ziarno.y);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y).getBlue() * 255));
                        }
                    }
                    break;

            }


            //obliczamy energie nowego stanu
            try {
                if (snap.getPixelReader().getArgb(ziarno.x - cellSize, ziarno.y) != selectedColorINT)
                    newEnergy += 1;
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(500 - cellSize, ziarno.y) != selectedColorINT)
                        newEnergy += 1;
                }
            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x + cellSize, ziarno.y) != selectedColorINT)
                    newEnergy += 1;
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(0, ziarno.y) != selectedColorINT)
                        newEnergy += 1;
                }
            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x, ziarno.y - cellSize) != selectedColorINT)
                    newEnergy += 1;
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(ziarno.x, 500 - cellSize) != selectedColorINT)
                        newEnergy += 1;
                }
            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x, ziarno.y + cellSize) != selectedColorINT)
                    newEnergy += 1;
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(ziarno.x, 0) != selectedColorINT)
                        newEnergy += 1;
                }
            }
            System.out.println("new " + newEnergy);

            //zmiana id
            double deltaE = newEnergy - ziarno.energia;
            if (deltaE <= 0 || Math.exp(-1 * (deltaE / (double) _ktMC)) == 1) {
                ziarno.energia = newEnergy;
                ziarno.color = selectedColorCLR;
                for (int i = 0; i < cellSize; i++) {
                    for (int j = 0; j < cellSize; j++) {
                        pixelWriter.setColor(ziarno.x + j, ziarno.y + i, selectedColorCLR);
                    }
                }
                snap = canvas.snapshot(null, null);
            }
            DrawEnergyMap(ziarno);


        }
    }

    private void DrawSasiedztwoMooreMC() {
        WritableImage snap = canvas.snapshot(null, null);
        PixelWriter pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();

        for (Seed ziarno : ziarna
        ) {
            //losujemy inna komorke
            int selectedColorINT = 0;
            Color selectedColorCLR = Color.WHITE;
            int newEnergy = 0;

            switch (helperMethods.GetRandomInt(1, 8)) {
                case 1:
                    try {
                        selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y + cellSize);
                        selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y + cellSize).getRed() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y + cellSize).getGreen() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y + cellSize).getBlue() * 255));
                    } catch (IndexOutOfBoundsException exception) {
                        if (typWarunku == WarunekBrzegowy.ABSORBUJACE) {
                            System.out.println("outofbounds");
                            selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getBlue() * 255));
                        } else {
                            selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, 0);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, 0).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, 0).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, 0).getBlue() * 255));
                        }
                    }
                    break;
                case 2:
                    try {
                        selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y - cellSize);
                        selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y - cellSize).getRed() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y - cellSize).getGreen() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y - cellSize).getBlue() * 255));
                    } catch (IndexOutOfBoundsException exception) {
                        if (typWarunku == WarunekBrzegowy.ABSORBUJACE) {
                            System.out.println("outofbounds");
                            selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getBlue() * 255));
                        } else {
                            selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, 500 - cellSize);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, 500 - cellSize).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, 500 - cellSize).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, 500 - cellSize).getBlue() * 255));
                        }
                    }
                    break;
                case 3:
                    try {
                        selectedColorINT = snap.getPixelReader().getArgb(ziarno.x + cellSize, ziarno.y);
                        selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x + cellSize, ziarno.y).getRed() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x + cellSize, ziarno.y).getGreen() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x + cellSize, ziarno.y).getBlue() * 255));
                    } catch (IndexOutOfBoundsException exception) {
                        if (typWarunku == WarunekBrzegowy.ABSORBUJACE) {
                            System.out.println("outofbounds");
                            selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getBlue() * 255));
                        } else {
                            selectedColorINT = snap.getPixelReader().getArgb(0, ziarno.y);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(0, ziarno.y).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(0, ziarno.y).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(0, ziarno.y).getBlue() * 255));
                        }
                    }
                    break;
                case 4:
                    try {
                        selectedColorINT = snap.getPixelReader().getArgb(ziarno.x - cellSize, ziarno.y);
                        selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x - cellSize, ziarno.y).getRed() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x - cellSize, ziarno.y).getGreen() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x - cellSize, ziarno.y).getBlue() * 255));
                    } catch (IndexOutOfBoundsException exception) {
                        if (typWarunku == WarunekBrzegowy.ABSORBUJACE) {
                            System.out.println("outofbounds");
                            selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getBlue() * 255));
                        } else {
                            selectedColorINT = snap.getPixelReader().getArgb(500 - cellSize, ziarno.y);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y).getBlue() * 255));
                        }
                    }
                    break;
                case 5:
                    try {
                        selectedColorINT = snap.getPixelReader().getArgb(ziarno.x + 1 * cellSize, ziarno.y + 1 * cellSize);
                        selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y + 1 * cellSize).getRed() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y + 1 * cellSize).getGreen() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y + 1 * cellSize).getBlue() * 255));
                    } catch (IndexOutOfBoundsException exception) {
                        if (typWarunku == WarunekBrzegowy.ABSORBUJACE) {
                            System.out.println("outofbounds");
                            selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getBlue() * 255));
                        } else {
                            try {
                                selectedColorINT = snap.getPixelReader().getArgb(ziarno.x + 1 * cellSize, 0);
                                selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, 0).getRed() * 255),
                                        (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, 0).getGreen() * 255),
                                        (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, 0).getBlue() * 255));
                            } catch (IndexOutOfBoundsException exception2) {
                                try {
                                    selectedColorINT = snap.getPixelReader().getArgb(0, ziarno.y + 1 * cellSize);
                                    selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(0, ziarno.y + 1 * cellSize).getRed() * 255),
                                            (int) (snap.getPixelReader().getColor(0, ziarno.y + 1 * cellSize).getGreen() * 255),
                                            (int) (snap.getPixelReader().getColor(0, ziarno.y + 1 * cellSize).getBlue() * 255));
                                } catch (IndexOutOfBoundsException exception3) {
                                    selectedColorINT = snap.getPixelReader().getArgb(0, 0);
                                    selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(0, 0).getRed() * 255),
                                            (int) (snap.getPixelReader().getColor(0, 0).getGreen() * 255),
                                            (int) (snap.getPixelReader().getColor(0, 0).getBlue() * 255));
                                }
                            }
                        }
                    }
                    break;
                case 6:
                    try {
                        selectedColorINT = snap.getPixelReader().getArgb(ziarno.x - 1 * cellSize, ziarno.y + 1 * cellSize);
                        selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y + 1 * cellSize).getRed() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y + 1 * cellSize).getGreen() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y + 1 * cellSize).getBlue() * 255));
                    } catch (IndexOutOfBoundsException exception) {
                        if (typWarunku == WarunekBrzegowy.ABSORBUJACE) {
                            System.out.println("outofbounds");
                            selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getBlue() * 255));
                        } else {
                            try {
                                selectedColorINT = snap.getPixelReader().getArgb(ziarno.x - 1 * cellSize, 0);
                                selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, 0).getRed() * 255),
                                        (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, 0).getGreen() * 255),
                                        (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, 0).getBlue() * 255));
                            } catch (IndexOutOfBoundsException exception2) {
                                try {
                                    selectedColorINT = snap.getPixelReader().getArgb(500 - cellSize, ziarno.y + 1 * cellSize);
                                    selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y + 1 * cellSize).getRed() * 255),
                                            (int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y + 1 * cellSize).getGreen() * 255),
                                            (int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y + 1 * cellSize).getBlue() * 255));
                                } catch (IndexOutOfBoundsException exception3) {
                                    selectedColorINT = snap.getPixelReader().getArgb(500 - cellSize, 0);
                                    selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(500 - cellSize, 0).getRed() * 255),
                                            (int) (snap.getPixelReader().getColor(500 - cellSize, 0).getGreen() * 255),
                                            (int) (snap.getPixelReader().getColor(500 - cellSize, 0).getBlue() * 255));
                                }
                            }
                        }

                    }
                    break;
                case 7:
                    try {
                        selectedColorINT = snap.getPixelReader().getArgb(ziarno.x - 1 * cellSize, ziarno.y - 1 * cellSize);
                        selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y - 1 * cellSize).getRed() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y - 1 * cellSize).getGreen() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, ziarno.y - 1 * cellSize).getBlue() * 255));
                    } catch (IndexOutOfBoundsException exception) {
                        if (typWarunku == WarunekBrzegowy.ABSORBUJACE) {
                            System.out.println("outofbounds");
                            selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getBlue() * 255));
                        } else {
                            try {
                                selectedColorINT = snap.getPixelReader().getArgb(ziarno.x - 1 * cellSize, 500 - cellSize);
                                selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, 500 - cellSize).getRed() * 255),
                                        (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, 500 - cellSize).getGreen() * 255),
                                        (int) (snap.getPixelReader().getColor(ziarno.x - 1 * cellSize, 500 - cellSize).getBlue() * 255));
                            } catch (IndexOutOfBoundsException exception2) {
                                try {
                                    selectedColorINT = snap.getPixelReader().getArgb(500 - cellSize, ziarno.y - 1 * cellSize);
                                    selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y - 1 * cellSize).getRed() * 255),
                                            (int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y - 1 * cellSize).getGreen() * 255),
                                            (int) (snap.getPixelReader().getColor(500 - cellSize, ziarno.y - 1 * cellSize).getBlue() * 255));
                                } catch (IndexOutOfBoundsException exception3) {
                                    selectedColorINT = snap.getPixelReader().getArgb(500 - cellSize, 500 - cellSize);
                                    selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(500 - cellSize, 500 - cellSize).getRed() * 255),
                                            (int) (snap.getPixelReader().getColor(500 - cellSize, 500 - cellSize).getGreen() * 255),
                                            (int) (snap.getPixelReader().getColor(500 - cellSize, 500 - cellSize).getBlue() * 255));
                                }
                            }
                        }

                    }
                    break;
                default:
                    try {
                        selectedColorINT = snap.getPixelReader().getArgb(ziarno.x + 1 * cellSize, ziarno.y - 1 * cellSize);
                        selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y - 1 * cellSize).getRed() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y - 1 * cellSize).getGreen() * 255),
                                (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, ziarno.y - 1 * cellSize).getBlue() * 255));
                    } catch (IndexOutOfBoundsException exception) {
                        if (typWarunku == WarunekBrzegowy.ABSORBUJACE) {
                            System.out.println("outofbounds");
                            selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y);
                            selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getRed() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getGreen() * 255),
                                    (int) (snap.getPixelReader().getColor(ziarno.x, ziarno.y).getBlue() * 255));
                        } else {
                            try {
                                selectedColorINT = snap.getPixelReader().getArgb(ziarno.x + 1 * cellSize, 500 - cellSize);
                                selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, 500 - cellSize).getRed() * 255),
                                        (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, 500 - cellSize).getGreen() * 255),
                                        (int) (snap.getPixelReader().getColor(ziarno.x + 1 * cellSize, 500 - cellSize).getBlue() * 255));
                            } catch (IndexOutOfBoundsException exception2) {
                                try {
                                    selectedColorINT = snap.getPixelReader().getArgb(0, ziarno.y - 1 * cellSize);
                                    selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(0, ziarno.y - 1 * cellSize).getRed() * 255),
                                            (int) (snap.getPixelReader().getColor(0, ziarno.y - 1 * cellSize).getGreen() * 255),
                                            (int) (snap.getPixelReader().getColor(0, ziarno.y - 1 * cellSize).getBlue() * 255));
                                } catch (IndexOutOfBoundsException exception3) {
                                    selectedColorINT = snap.getPixelReader().getArgb(0, 500 - cellSize);
                                    selectedColorCLR = Color.rgb((int) (snap.getPixelReader().getColor(0, 500 - cellSize).getRed() * 255),
                                            (int) (snap.getPixelReader().getColor(0, 500 - cellSize).getGreen() * 255),
                                            (int) (snap.getPixelReader().getColor(0, 500 - cellSize).getBlue() * 255));
                                }
                            }
                        }

                    }
                    break;
            }


            //obliczamy energie nowego stanu
            try {
                if (snap.getPixelReader().getArgb(ziarno.x - cellSize, ziarno.y) != selectedColorINT)
                    newEnergy += 1;
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(500 - cellSize, ziarno.y) != selectedColorINT)
                        newEnergy += 1;
                }
            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x + cellSize, ziarno.y) != selectedColorINT)
                    newEnergy += 1;
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(0, ziarno.y) != selectedColorINT)
                        newEnergy += 1;
                }
            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x, ziarno.y - cellSize) != selectedColorINT)
                    newEnergy += 1;
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(ziarno.x, 500 - cellSize) != selectedColorINT)
                        newEnergy += 1;
                }
            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x, ziarno.y + cellSize) != selectedColorINT)
                    newEnergy += 1;
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(ziarno.x, 0) != selectedColorINT)
                        newEnergy += 1;
                }
            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x + cellSize, ziarno.y + cellSize) != selectedColorINT)
                    newEnergy += 1;
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    try {
                        if (snap.getPixelReader().getArgb(ziarno.x + cellSize, 0) != selectedColorINT)
                            newEnergy += 1;
                    } catch (IndexOutOfBoundsException exception2) {
                        try {
                            if (snap.getPixelReader().getArgb(0, ziarno.y + cellSize) != selectedColorINT)
                                newEnergy += 1;
                        } catch (IndexOutOfBoundsException exception3) {
                            if (snap.getPixelReader().getArgb(0, 0) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                                newEnergy += 1;
                        }
                    }
                }
            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x - cellSize, ziarno.y + cellSize) != selectedColorINT)
                    ziarno.energia += 1;
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    try {
                        if (snap.getPixelReader().getArgb(ziarno.x - cellSize, 0) != selectedColorINT)
                            newEnergy += 1;
                    } catch (IndexOutOfBoundsException exception2) {
                        try {
                            if (snap.getPixelReader().getArgb(500 - cellSize, ziarno.y + cellSize) != selectedColorINT)
                                newEnergy += 1;
                        } catch (IndexOutOfBoundsException exception3) {
                            if (snap.getPixelReader().getArgb(500 - cellSize, 0) != selectedColorINT)
                                newEnergy += 1;
                        }
                    }
                }
            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x - cellSize, ziarno.y - cellSize) != selectedColorINT)
                    newEnergy += 1;
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    try {
                        if (snap.getPixelReader().getArgb(ziarno.x - cellSize, 500 - cellSize) != selectedColorINT)
                            newEnergy += 1;
                    } catch (IndexOutOfBoundsException exception2) {
                        try {
                            if (snap.getPixelReader().getArgb(500 - cellSize, ziarno.y - cellSize) != selectedColorINT)
                                newEnergy += 1;
                        } catch (IndexOutOfBoundsException exception3) {
                            if (snap.getPixelReader().getArgb(500 - cellSize, 500 - cellSize) != selectedColorINT)
                                newEnergy += 1;
                        }
                    }
                }
            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x + cellSize, ziarno.y - cellSize) != selectedColorINT)
                    newEnergy += 1;
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    try {
                        if (snap.getPixelReader().getArgb(ziarno.x + cellSize, 500 - cellSize) != selectedColorINT)
                            newEnergy += 1;
                    } catch (IndexOutOfBoundsException exception2) {
                        try {
                            if (snap.getPixelReader().getArgb(0, ziarno.y - cellSize) != selectedColorINT)
                                newEnergy += 1;
                        } catch (IndexOutOfBoundsException exception3) {
                            if (snap.getPixelReader().getArgb(0, 500 - cellSize) != selectedColorINT)
                                newEnergy += 1;
                        }
                    }
                }
            }

            System.out.println("new " + newEnergy);

            //zmiana id
            double deltaE = newEnergy - ziarno.energia;
            if (deltaE <= 0 || Math.exp(-1 * (deltaE / (double) _ktMC)) == 1) {
                ziarno.energia = newEnergy;
                ziarno.color = selectedColorCLR;
                for (int i = 0; i < cellSize; i++) {
                    for (int j = 0; j < cellSize; j++) {
                        pixelWriter.setColor(ziarno.x + j, ziarno.y + i, selectedColorCLR);
                    }
                }
                snap = canvas.snapshot(null, null);
            }
            DrawEnergyMap(ziarno);


        }
    }

    private void GetAllSeedsFromCanvas() {
        ziarna.clear();
        WritableImage snap = canvas.snapshot(null, null);
        for (int y = 0; y < 500; y += cellSize) {
            for (int x = 0; x < 500; x += cellSize) {
                Color colorAtPoint = Color.rgb((int) (snap.getPixelReader().getColor(x, y).getRed() * 255),
                        (int) (snap.getPixelReader().getColor(x, y).getGreen() * 255),
                        (int) (snap.getPixelReader().getColor(x, y).getBlue() * 255));
                Seed ziarno = new Seed(x, y, colorAtPoint);
                ziarna.add(ziarno);
            }
        }
    }

    private void DrawEnergyMap(Seed ziarno) {
        PixelWriter pixelWriter = canvas2.getGraphicsContext2D().getPixelWriter();
        Color newColor = Color.WHITE;
        for (int i = 0; i < ziarno.energia; i++)
            newColor = newColor.darker();
        for (int i = 0; i < cellSize; i++) {
            for (int j = 0; j < cellSize; j++) {
                pixelWriter.setColor(ziarno.x + j, ziarno.y + i, newColor);
            }
        }
    }

    private void InitializeEnergy() {
        WritableImage snap = canvas.snapshot(null, null);
        for (Seed ziarno : ziarna
        ) {
            ziarno.energia = 0;
            try {
                if (snap.getPixelReader().getArgb(ziarno.x - cellSize, ziarno.y) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                    ziarno.energia += 1;
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(500 - cellSize, ziarno.y) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                        ziarno.energia += 1;
                }
            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x + cellSize, ziarno.y) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                    ziarno.energia += 1;
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(0, ziarno.y) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                        ziarno.energia += 1;
                }
            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x, ziarno.y - cellSize) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                    ziarno.energia += 1;
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(ziarno.x, 500 - cellSize) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                        ziarno.energia += 1;
                }
            }
            try {
                if (snap.getPixelReader().getArgb(ziarno.x, ziarno.y + cellSize) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                    ziarno.energia += 1;
            } catch (IndexOutOfBoundsException exception) {
                if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                    System.out.println("outofbounds");
                else {
                    if (snap.getPixelReader().getArgb(ziarno.x, 0) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                        ziarno.energia += 1;
                }
            }
            if (typSasiedztwa == Sasiedztwo.MOORE) {
                try {
                    if (snap.getPixelReader().getArgb(ziarno.x + cellSize, ziarno.y + cellSize) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                        ziarno.energia += 1;
                } catch (IndexOutOfBoundsException exception) {
                    if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                        System.out.println("outofbounds");
                    else {
                        try {
                            if (snap.getPixelReader().getArgb(ziarno.x + cellSize, 0) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                                ziarno.energia += 1;
                        } catch (IndexOutOfBoundsException exception2) {
                            try {
                                if (snap.getPixelReader().getArgb(0, ziarno.y + cellSize) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                                    ziarno.energia += 1;
                            } catch (IndexOutOfBoundsException exception3) {
                                if (snap.getPixelReader().getArgb(0, 0) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                                    ziarno.energia += 1;
                            }
                        }
                    }
                }
                try {
                    if (snap.getPixelReader().getArgb(ziarno.x - cellSize, ziarno.y + cellSize) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                        ziarno.energia += 1;
                } catch (IndexOutOfBoundsException exception) {
                    if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                        System.out.println("outofbounds");
                    else {
                        try {
                            if (snap.getPixelReader().getArgb(ziarno.x - cellSize, 0) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                                ziarno.energia += 1;
                        } catch (IndexOutOfBoundsException exception2) {
                            try {
                                if (snap.getPixelReader().getArgb(500 - cellSize, ziarno.y + cellSize) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                                    ziarno.energia += 1;
                            } catch (IndexOutOfBoundsException exception3) {
                                if (snap.getPixelReader().getArgb(500 - cellSize, 0) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                                    ziarno.energia += 1;
                            }
                        }
                    }
                }
                try {
                    if (snap.getPixelReader().getArgb(ziarno.x - cellSize, ziarno.y - cellSize) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                        ziarno.energia += 1;
                } catch (IndexOutOfBoundsException exception) {
                    if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                        System.out.println("outofbounds");
                    else {
                        try {
                            if (snap.getPixelReader().getArgb(ziarno.x - cellSize, 500 - cellSize) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                                ziarno.energia += 1;
                        } catch (IndexOutOfBoundsException exception2) {
                            try {
                                if (snap.getPixelReader().getArgb(500 - cellSize, ziarno.y - cellSize) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                                    ziarno.energia += 1;
                            } catch (IndexOutOfBoundsException exception3) {
                                if (snap.getPixelReader().getArgb(500 - cellSize, 500 - cellSize) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                                    ziarno.energia += 1;
                            }
                        }
                    }
                }
                try {
                    if (snap.getPixelReader().getArgb(ziarno.x + cellSize, ziarno.y - cellSize) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                        ziarno.energia += 1;
                } catch (IndexOutOfBoundsException exception) {
                    if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                        System.out.println("outofbounds");
                    else {
                        try {
                            if (snap.getPixelReader().getArgb(ziarno.x + cellSize, 500 - cellSize) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                                ziarno.energia += 1;
                        } catch (IndexOutOfBoundsException exception2) {
                            try {
                                if (snap.getPixelReader().getArgb(0, ziarno.y - cellSize) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                                    ziarno.energia += 1;
                            } catch (IndexOutOfBoundsException exception3) {
                                if (snap.getPixelReader().getArgb(0, 500 - cellSize) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                                    ziarno.energia += 1;
                            }
                        }
                    }
                }
            }
            System.out.println("aktualna " + ziarno.energia);
            DrawEnergyMap(ziarno);
        }
    }

    //Rekrystalizacja

    public void StartRek() {
        GetAllSeedsFromCanvas();
        SetParametrA();
        SetParametrB();
        RoKrytyczne = (4.21584E+12) / (screenSizeX * screenSizeY);
        System.out.println("ro krytyczne: " + RoKrytyczne);
        dataLines.add(new String[]
                { "Time",  "Ro", "Sigma", "deltaRo"});
        dataLines.add(new String[]
                { String.valueOf(time), String.valueOf(Ro), String.valueOf(Sigma), "0"});
    }

    public void IterateRek() {

        time += 0.001;
        List<Seed> noweZiarnaRek = new ArrayList<>();
        PixelWriter pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
        WritableImage snap = canvas.snapshot(null, null);
        double newRo = CalculateRo();
        double deltaRo = newRo - Ro;
        double nextRo=0;
        System.out.println("delta ro: " + deltaRo / (screenSizeX * screenSizeY));
        for (Seed ziarno : ziarna
        ) {
            ziarno.ro += deltaRo / (screenSizeX * screenSizeY);
            if (typSasiedztwa == Sasiedztwo.VONNEUMANN) {
                if (ziarno.ro > RoKrytyczne && ZarodkowanieNeumann(ziarno,snap)) {
                    ziarno.zarekrystalizowany = true;
                    ziarno.color = helperMethods.GetRandomRedColor();
                    ziarno.ro = 0;
                    for (int i = 0; i < cellSize; i++) {
                        for (int j = 0; j < cellSize; j++) {
                            pixelWriter.setColor(ziarno.x + j, ziarno.y + i, ziarno.color);
                        }
                    }
                    DrawGestosc(ziarno, ziarno.color);
                    noweZiarnaRek.add(ziarno);
                }
                if (!ziarno.zarekrystalizowany && PrzejscieNeumann(ziarno)) {
                    ziarno.zarekrystalizowany = true;
                    ziarno.color =GetColorNeumann(ziarno);
                    ziarno.ro = 0;
                    for (int i = 0; i < cellSize; i++) {
                        for (int j = 0; j < cellSize; j++) {
                            pixelWriter.setColor(ziarno.x + j, ziarno.y + i, ziarno.color);
                        }
                    }
                    DrawGestosc(ziarno, ziarno.color);
                    noweZiarnaRek.add(ziarno);
                }
            }
            else if(typSasiedztwa==Sasiedztwo.MOORE){
                if (ziarno.ro > RoKrytyczne && ZarodkowanieMoore(ziarno,snap)) {
                    ziarno.zarekrystalizowany = true;
                    ziarno.color = helperMethods.GetRandomRedColor();
                    ziarno.ro = 0;
                    for (int i = 0; i < cellSize; i++) {
                        for (int j = 0; j < cellSize; j++) {
                            pixelWriter.setColor(ziarno.x + j, ziarno.y + i, ziarno.color);
                        }
                    }
                    DrawGestosc(ziarno, ziarno.color);
                    noweZiarnaRek.add(ziarno);
                }
                if (!ziarno.zarekrystalizowany && PrzejscieMoore(ziarno)) {
                    ziarno.zarekrystalizowany = true;
                    ziarno.color =GetColorMoore(ziarno);
                    ziarno.ro = 0;
                    for (int i = 0; i < cellSize; i++) {
                        for (int j = 0; j < cellSize; j++) {
                            pixelWriter.setColor(ziarno.x + j, ziarno.y + i, ziarno.color);
                        }
                    }
                    DrawGestosc(ziarno, ziarno.color);
                    noweZiarnaRek.add(ziarno);
                }
            }
            nextRo+=ziarno.ro;
        }

        ziarnaRek=noweZiarnaRek;
        Ro = nextRo;
        Sigma = CalculateSigma(Ro);
        dataLines.add(new String[]
                { String.valueOf(time), String.valueOf(Ro), String.valueOf(Sigma),String.valueOf(deltaRo)});
    }

    private void SetParametrA() {
        if (helperMethods.IsNumeric(parameterA.getText())) {
            _parametrA = Double.parseDouble(parameterA.getText());
        } else
            ControllerHelper.InvalidInputError();
    }

    private void SetParametrB() {
        if (helperMethods.IsNumeric(parameterB.getText())) {
            _parametrB = Double.parseDouble(parameterB.getText());
        } else
            ControllerHelper.InvalidInputError();
    }

    private double CalculateRo() {
        return _parametrA / _parametrB + (1 - _parametrA / _parametrB) *  Math.exp(-_parametrB * time);
    }

    private double CalculateSigma(double ro) {
        return SIGMAZERO +ALFA*BETA*MI*Math.sqrt(ro);
    }

    private boolean ZarodkowanieNeumann(Seed ziarno,WritableImage snap) {

        int selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y);
        try {
            if (snap.getPixelReader().getArgb(ziarno.x - cellSize, ziarno.y) != selectedColorINT)
                return true;
        } catch (IndexOutOfBoundsException exception) {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if (snap.getPixelReader().getArgb(500 - cellSize, ziarno.y) != selectedColorINT)
                    return true;
            }
        }
        try {
            if (snap.getPixelReader().getArgb(ziarno.x + cellSize, ziarno.y) != selectedColorINT)
                return true;
        } catch (IndexOutOfBoundsException exception) {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if (snap.getPixelReader().getArgb(0, ziarno.y) != selectedColorINT)
                    return true;
            }
        }
        try {
            if (snap.getPixelReader().getArgb(ziarno.x, ziarno.y - cellSize) != selectedColorINT)
                return true;
        } catch (IndexOutOfBoundsException exception) {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if (snap.getPixelReader().getArgb(ziarno.x, 500 - cellSize) != selectedColorINT)
                    return true;
            }
        }
        try {
            if (snap.getPixelReader().getArgb(ziarno.x, ziarno.y + cellSize) != selectedColorINT)
                return true;
        } catch (IndexOutOfBoundsException exception) {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if (snap.getPixelReader().getArgb(ziarno.x, 0) != selectedColorINT)
                    return true;
            }
        }

        return false;
    }

    private boolean ZarodkowanieMoore(Seed ziarno,WritableImage snap) {

        int selectedColorINT = snap.getPixelReader().getArgb(ziarno.x, ziarno.y);
        try {
            if (snap.getPixelReader().getArgb(ziarno.x - cellSize, ziarno.y) != selectedColorINT)
                return true;
        } catch (IndexOutOfBoundsException exception) {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if (snap.getPixelReader().getArgb(500 - cellSize, ziarno.y) != selectedColorINT)
                    return true;
            }
        }
        try {
            if (snap.getPixelReader().getArgb(ziarno.x + cellSize, ziarno.y) != selectedColorINT)
                return true;
        } catch (IndexOutOfBoundsException exception) {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if (snap.getPixelReader().getArgb(0, ziarno.y) != selectedColorINT)
                    return true;
            }
        }
        try {
            if (snap.getPixelReader().getArgb(ziarno.x, ziarno.y - cellSize) != selectedColorINT)
                return true;
        } catch (IndexOutOfBoundsException exception) {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if (snap.getPixelReader().getArgb(ziarno.x, 500 - cellSize) != selectedColorINT)
                    return true;
            }
        }
        try {
            if (snap.getPixelReader().getArgb(ziarno.x, ziarno.y + cellSize) != selectedColorINT)
                return true;
        } catch (IndexOutOfBoundsException exception) {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if (snap.getPixelReader().getArgb(ziarno.x, 0) != selectedColorINT)
                    return true;
            }
        }
        try {
            if (snap.getPixelReader().getArgb(ziarno.x + cellSize, ziarno.y + cellSize) != selectedColorINT)
                return true;
        } catch (IndexOutOfBoundsException exception) {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                try {
                    if (snap.getPixelReader().getArgb(ziarno.x + cellSize, 0) != selectedColorINT)
                        return true;
                } catch (IndexOutOfBoundsException exception2) {
                    try {
                        if (snap.getPixelReader().getArgb(0, ziarno.y + cellSize) != selectedColorINT)
                            return true;
                    } catch (IndexOutOfBoundsException exception3) {
                        if (snap.getPixelReader().getArgb(0, 0) != snap.getPixelReader().getArgb(ziarno.x, ziarno.y))
                            return true;
                    }
                }
            }
        }
        try {
            if (snap.getPixelReader().getArgb(ziarno.x - cellSize, ziarno.y + cellSize) != selectedColorINT)
                return true;
        } catch (IndexOutOfBoundsException exception) {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                try {
                    if (snap.getPixelReader().getArgb(ziarno.x - cellSize, 0) != selectedColorINT)
                        return true;
                } catch (IndexOutOfBoundsException exception2) {
                    try {
                        if (snap.getPixelReader().getArgb(500 - cellSize, ziarno.y + cellSize) != selectedColorINT)
                            return true;
                    } catch (IndexOutOfBoundsException exception3) {
                        if (snap.getPixelReader().getArgb(500 - cellSize, 0) != selectedColorINT)
                            return true;
                    }
                }
            }
        }
        try {
            if (snap.getPixelReader().getArgb(ziarno.x - cellSize, ziarno.y - cellSize) != selectedColorINT)
                return true;
        } catch (IndexOutOfBoundsException exception) {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                try {
                    if (snap.getPixelReader().getArgb(ziarno.x - cellSize, 500 - cellSize) != selectedColorINT)
                        return true;
                } catch (IndexOutOfBoundsException exception2) {
                    try {
                        if (snap.getPixelReader().getArgb(500 - cellSize, ziarno.y - cellSize) != selectedColorINT)
                            return true;
                    } catch (IndexOutOfBoundsException exception3) {
                        if (snap.getPixelReader().getArgb(500 - cellSize, 500 - cellSize) != selectedColorINT)
                            return true;
                    }
                }
            }
        }
        try {
            if (snap.getPixelReader().getArgb(ziarno.x + cellSize, ziarno.y - cellSize) != selectedColorINT)
                return true;
        } catch (IndexOutOfBoundsException exception) {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                try {
                    if (snap.getPixelReader().getArgb(ziarno.x + cellSize, 500 - cellSize) != selectedColorINT)
                        return true;
                } catch (IndexOutOfBoundsException exception2) {
                    try {
                        if (snap.getPixelReader().getArgb(0, ziarno.y - cellSize) != selectedColorINT)
                            return true;
                    } catch (IndexOutOfBoundsException exception3) {
                        if (snap.getPixelReader().getArgb(0, 500 - cellSize) != selectedColorINT)
                            return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean PrzejscieNeumann(Seed ziarno) {
        if(ziarnaRek.size()==0)
            return false;
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y) != null) {
            if ( (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                    || helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y).ro>=ziarno.ro ) {
                return false;
            }

        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if ((helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                        || helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y).ro>=ziarno.ro ) {
                    return false;
                }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y).zarekrystalizowany && !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                    || helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y).ro>=ziarno.ro) {
                return false;
            }
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if ((helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y).zarekrystalizowany && !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                        || helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y).ro>=ziarno.ro) {
                    return false;
                }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize).zarekrystalizowany && !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                    || helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize).ro>=ziarno.ro) {
                return false;
            }
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if ((helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize).zarekrystalizowany && !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                        || helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize).ro>=ziarno.ro) {
                    return false;
                }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                    || helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize).ro>ziarno.ro) {
                return false;
            }
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if ((helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                        || helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize).ro>ziarno.ro) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean PrzejscieMoore(Seed ziarno) {
        if(ziarnaRek.size()==0)
            return false;
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y) != null) {
            if ( (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                    || helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y).ro>=ziarno.ro ) {
                return false;
            }

        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if ((helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                        || helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y).ro>=ziarno.ro ) {
                    return false;
                }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y).zarekrystalizowany && !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                    || helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y).ro>=ziarno.ro) {
                return false;
            }
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if ((helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y).zarekrystalizowany && !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                        || helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y).ro>=ziarno.ro) {
                    return false;
                }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize).zarekrystalizowany && !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                    || helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize).ro>=ziarno.ro) {
                return false;
            }
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if ((helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize).zarekrystalizowany && !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                        || helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize).ro>=ziarno.ro) {
                    return false;
                }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                    || helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize).ro>ziarno.ro) {
                return false;
            }
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if ((helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize)))
                        || helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize).ro>ziarno.ro) {
                    return false;
                }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y + cellSize) != null){
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y + cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y + cellSize)))
                    || helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y + cellSize).ro>ziarno.ro)
                return false;
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if (helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, 0)!=null)
                    if ((helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, 0).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, 0)))
                    || helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, 0).ro>ziarno.ro)
                        return false;
                else {
                        if (helperMethods.GetSeedByXY(ziarna, 0, ziarno.y + cellSize)!=null)
                        if ((helperMethods.GetSeedByXY(ziarna, 0, ziarno.y + cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, 0, ziarno.y + cellSize)))
                    || helperMethods.GetSeedByXY(ziarna, 0, ziarno.y + cellSize).ro>ziarno.ro)
                            return false;
                    else {
                        if ((helperMethods.GetSeedByXY(ziarna, 0, 0).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, 0, 0)))
                    || helperMethods.GetSeedByXY(ziarna, 0, 0).ro>ziarno.ro)
                            return false;
                    }
                }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y + cellSize) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y + cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y + cellSize)))
                    || helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y + cellSize).ro>ziarno.ro)
                return false;
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, 0)!=null)
                    if ((helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, 0).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, 0)))
                    || helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, 0).ro>ziarno.ro)
                        return false;
                else {
                        if (helperMethods.GetSeedByXY(ziarna, 500 - cellSize, ziarno.y + cellSize)!=null)
                        if ((helperMethods.GetSeedByXY(ziarna, 500 - cellSize, ziarno.y + cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, 500 - cellSize, ziarno.y + cellSize)))
                    || helperMethods.GetSeedByXY(ziarna, 500 - cellSize, ziarno.y + cellSize).ro>ziarno.ro)
                            return false;
                    else {
                        if ((helperMethods.GetSeedByXY(ziarna, 500 - cellSize, 0).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, 500 - cellSize, 0)))
                    || helperMethods.GetSeedByXY(ziarna, 500 - cellSize, 0).ro>ziarno.ro)
                            return false;
                    }
                }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y - cellSize) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y - cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y - cellSize)))
                    || helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y - cellSize).ro>ziarno.ro)
                return false;
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, 500 - cellSize)!=null)
                    if ((helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, 500 - cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, 500 - cellSize)))
                    || helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, 500 - cellSize).ro>ziarno.ro)
                        return false;
                else {
                    if (helperMethods.GetSeedByXY(ziarna, 500 - cellSize, ziarno.y - cellSize)!=null)
                        if ((helperMethods.GetSeedByXY(ziarna, 500 - cellSize, ziarno.y - cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, 500 - cellSize, ziarno.y - cellSize)))
                    || helperMethods.GetSeedByXY(ziarna, 500 - cellSize, ziarno.y - cellSize).ro>ziarno.ro)
                            return false;
                    else {
                        if ((helperMethods.GetSeedByXY(ziarna, 500 - cellSize, 500 - cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, 500 - cellSize, 500 - cellSize)))
                    || helperMethods.GetSeedByXY(ziarna, 500 - cellSize, 500 - cellSize).ro>ziarno.ro)
                            return false;
                    }
                }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y - cellSize) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y - cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y - cellSize)))
                    || helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y - cellSize).ro>ziarno.ro)
                return false;
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if (helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, 500 - cellSize)!=null)
                    if ((helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, 500 - cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, 500 - cellSize)))
                    || helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, 500 - cellSize).ro>ziarno.ro)
                        return false;
                else {
                    if (helperMethods.GetSeedByXY(ziarna, 0, ziarno.y - cellSize)!=null)
                        if ((helperMethods.GetSeedByXY(ziarna, 0, ziarno.y - cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, 0, ziarno.y - cellSize)))
                    || helperMethods.GetSeedByXY(ziarna, 0, ziarno.y - cellSize).ro>ziarno.ro)
                            return false;
                    else {
                        if ((helperMethods.GetSeedByXY(ziarna, 0, 500 - cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, 0, 500 - cellSize)))
                    || helperMethods.GetSeedByXY(ziarna, 0, 500 - cellSize).ro>ziarno.ro)
                            return false;
                    }
                }
            }
        }
        return true;
    }

    private Color GetColorNeumann(Seed ziarno) {

        if (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y) != null) {
            if ( (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y).zarekrystalizowany&& ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                 ) {
                return (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)).color;
            }

        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if ((helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y).zarekrystalizowany&& ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                ) {
                    return (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)).color;
                }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y).zarekrystalizowany && ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
            ) {
                return (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)).color;
            }
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if ((helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y).zarekrystalizowany && ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                ) {
                    return (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)).color;
                }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize).zarekrystalizowany && ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
            ) {
                return (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)).color;
            }
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if ((helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize).zarekrystalizowany && ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                ) {
                    return (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)).color;
                }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize).zarekrystalizowany&& ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
            ) {
                return (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)).color;
            }
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if ((helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize).zarekrystalizowany&& ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                ) {
                    return (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)).color;
                }
            }
        }
        return  helperMethods.GetRandomRedColor();
    }

    private Color GetColorMoore(Seed ziarno) {

        if (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y) != null) {
            if ( (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
            ) {
                return helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y).color;
            }

        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if ((helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                ) {
                    return helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y).color;
                }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y).zarekrystalizowany && !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
            ) {
                return helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y).color;
            }
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if ((helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y).zarekrystalizowany && !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                ) {
                    return helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y).color;
                }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize).zarekrystalizowany && !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
            ) {
                return helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize).color;
            }
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if ((helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize).zarekrystalizowany && !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
                ) {
                    return helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y - cellSize).color;
                }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y)))
            ) {
                return helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize).color;
            }
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if ((helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize)))
                ) {
                    return helperMethods.GetSeedByXY(ziarna, ziarno.x, ziarno.y + cellSize).color;
                }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y + cellSize) != null){
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y + cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y + cellSize)))
            )
                return helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y + cellSize).color;
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if (helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, 0)!=null)
                    if ((helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, 0).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, 0)))
                    )
                        return helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, 0).color;
                    else {
                        if (helperMethods.GetSeedByXY(ziarna, 0, ziarno.y + cellSize)!=null)
                            if ((helperMethods.GetSeedByXY(ziarna, 0, ziarno.y + cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, 0, ziarno.y + cellSize)))
                            )
                                return helperMethods.GetSeedByXY(ziarna, 0, ziarno.y + cellSize).color;
                            else {
                                if ((helperMethods.GetSeedByXY(ziarna, 0, 0).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, 0, 0)))
                                )
                                    return helperMethods.GetSeedByXY(ziarna, 0, 0).color;
                            }
                    }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y + cellSize) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y + cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y + cellSize)))
            )
                return helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y + cellSize).color;
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, 0)!=null)
                    if ((helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, 0).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, 0)))
                    )
                        return helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, 0).color;
                    else {
                        if (helperMethods.GetSeedByXY(ziarna, 500 - cellSize, ziarno.y + cellSize)!=null)
                            if ((helperMethods.GetSeedByXY(ziarna, 500 - cellSize, ziarno.y + cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, 500 - cellSize, ziarno.y + cellSize)))
                            )
                                return helperMethods.GetSeedByXY(ziarna, 500 - cellSize, ziarno.y + cellSize).color;
                            else {
                                if ((helperMethods.GetSeedByXY(ziarna, 500 - cellSize, 0).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, 500 - cellSize, 0)))
                                )
                                    return helperMethods.GetSeedByXY(ziarna, 500 - cellSize, 0).color;
                            }
                    }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y - cellSize) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y - cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y - cellSize)))
            )
                return helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, ziarno.y - cellSize).color;
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if (helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, 500 - cellSize)!=null)
                    if ((helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, 500 - cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, 500 - cellSize)))
                    )
                        return helperMethods.GetSeedByXY(ziarna, ziarno.x - cellSize, 500 - cellSize).color;
                    else {
                        if (helperMethods.GetSeedByXY(ziarna, 500 - cellSize, ziarno.y - cellSize)!=null)
                            if ((helperMethods.GetSeedByXY(ziarna, 500 - cellSize, ziarno.y - cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, 500 - cellSize, ziarno.y - cellSize)))
                            )
                                return helperMethods.GetSeedByXY(ziarna, 500 - cellSize, ziarno.y - cellSize).color;
                            else {
                                if ((helperMethods.GetSeedByXY(ziarna, 500 - cellSize, 500 - cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, 500 - cellSize, 500 - cellSize)))
                                )
                                    return helperMethods.GetSeedByXY(ziarna, 500 - cellSize, 500 - cellSize).color;
                            }
                    }
            }
        }
        if (helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y - cellSize) != null) {
            if ((helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y - cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y - cellSize)))
            )
                return helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, ziarno.y - cellSize).color;
        } else {
            if (typWarunku == WarunekBrzegowy.ABSORBUJACE)
                System.out.println("outofbounds");
            else {
                if (helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, 500 - cellSize)!=null)
                    if ((helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, 500 - cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, 500 - cellSize)))
                    )
                        return helperMethods.GetSeedByXY(ziarna, ziarno.x + cellSize, 500 - cellSize).color;
                    else {
                        if (helperMethods.GetSeedByXY(ziarna, 0, ziarno.y - cellSize)!=null)
                            if ((helperMethods.GetSeedByXY(ziarna, 0, ziarno.y - cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, 0, ziarno.y - cellSize)))
                            )
                                return helperMethods.GetSeedByXY(ziarna, 0, ziarno.y - cellSize).color;
                            else {
                                if ((helperMethods.GetSeedByXY(ziarna, 0, 500 - cellSize).zarekrystalizowany&& !ziarnaRek.contains(helperMethods.GetSeedByXY(ziarna, 0, 500 - cellSize)))
                                )
                                    return helperMethods.GetSeedByXY(ziarna, 0, 500 - cellSize).color;
                            }
                    }
            }
        }
        return  helperMethods.GetRandomRedColor();
    }

    private void DrawGestosc(Seed ziarno, Color color) {
        PixelWriter pixelWriter = canvas3.getGraphicsContext2D().getPixelWriter();
        for (int i = 0; i < cellSize; i++) {
            for (int j = 0; j < cellSize; j++) {
                pixelWriter.setColor(ziarno.x + j, ziarno.y + i, color);
            }
        }
    }

    public void ExportToCSV() throws IOException{
        File csvOutputFile = new File("wartosci.csv");
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            dataLines.stream()
                    .map(this::convertToCSV)
                    .forEach(pw::println);
        }
        System.out.println("Exported to csv");
    }

    private String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

}
