package application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;


public class Main extends Application {

	private final String NAME_AND_STUDENT_NUMBER = "Your name and student number here";
	private final String STAGE_TITLE = NAME_AND_STUDENT_NUMBER;

	BorderPane root;
	FlowPane canvasPane;
	FlowPane hudPane;
	Scene scene;
	Canvas canvas;
	GraphicsContext gc;

	Button btnLoad;
	Button btnSave;
	Button btnExit;
	Button btnClear;

	final double STAGE_WIDTH = 500;
	final double STAGE_HEIGHT = 600;

	final double HUD_WIDTH = STAGE_WIDTH;
	final double HUD_HEIGHT = 100;

	final double CANVAS_WIDTH = STAGE_WIDTH;
	final double CANVAS_HEIGHT = 500;

	List<Double> xList;
	List<Double> yList;

	private final int MAX_LIST_SIZE = 10000;

	@Override
	public void start(Stage primaryStage) {
		try {
			root = new BorderPane();

			canvasPane = new FlowPane();
			canvasPane.setMinWidth(CANVAS_WIDTH);
			canvasPane.setMinHeight(CANVAS_HEIGHT);

			canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);

			gc = canvas.getGraphicsContext2D();
			canvasPane.getChildren().add(canvas);

			hudPane = new FlowPane();
			hudPane.setMinWidth(HUD_WIDTH);
			hudPane.setMinHeight(HUD_HEIGHT);

			HBox hb = new HBox();
			hb.setSpacing(10);
			hb.setMinWidth(HUD_WIDTH);
			hb.setMinHeight(HUD_HEIGHT);
			hb.setAlignment(Pos.CENTER);

			btnLoad = new Button("load");
			btnLoad.setMaxWidth(Double.MAX_VALUE);
			hb.getChildren().add(btnLoad);

			btnSave = new Button("save");
			btnSave.setMaxWidth(Double.MAX_VALUE);
			hb.getChildren().add(btnSave);

			btnExit = new Button("exit");
			btnExit.setMaxWidth(Double.MAX_VALUE);
			hb.getChildren().add(btnExit);

			btnClear = new Button("clear");
			btnClear.setMaxWidth(Double.MAX_VALUE);
			hb.getChildren().add(btnClear);

			hudPane.getChildren().add(hb);

			root.setTop(hudPane);
			root.setBottom(canvasPane);

			scene = new Scene(root, STAGE_WIDTH, STAGE_HEIGHT);

			initStuff();
			addHandlers();
			drawScreen();

			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent event) {
					saveToFile();
					exitApp();
				};

			});

			primaryStage.setResizable(false);
			primaryStage.initStyle(StageStyle.UTILITY);
			primaryStage.setTitle(STAGE_TITLE);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void initStuff() {
		xList = new ArrayList<Double>();
		yList = new ArrayList<Double>();
		loadFromFile();
	}

	public void addCoord(double x, double y) {
		xList.add(x);
		yList.add(y);

		if (xList.size() == (MAX_LIST_SIZE + 1)) {
			xList.remove(0);
			yList.remove(0);
		}

		System.out.println("xList size: " + xList.size());
		System.out.println("yList size: " + yList.size());

	}

	public void drawScreen() {
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
		gc.setFill(Color.CHARTREUSE);

		System.out.println("xList size: " + xList.size());
		System.out.println("yList size: " + yList.size());

		for (int x = 0; x < xList.size(); x ++) {
			gc.fillRect(xList.get(x), yList.get(x), 10, 10);
		}
	}

	public void addHandlers() {

		canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				addCoord(arg0.getX(),arg0.getY());
				drawScreen();
			}
		});

		canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				addCoord(arg0.getX(),arg0.getY());
				drawScreen();
			}
		});

		btnLoad.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				loadFromFile();
				drawScreen();
			}
		});

		btnSave.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				saveToFile();
			}
		});

		btnExit.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				saveToFile();
				exitApp();

			}
		});

		btnClear.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				clearCanvas();

			}
		});

	}

	public void loadFromFile() {
		ReadFromFile read = new ReadFromFile();
		read.read();
		convertArrayToList(read.getxArray(),read.getyArray());
	}

	private void convertArrayToList(Double xArray[], Double yArray[]) {

		if (xArray != null && yArray != null) {
			List<Double> newx = new ArrayList<Double>(Arrays.asList(xArray));
			List<Double> newy = new ArrayList<Double>(Arrays.asList(yArray));

			newx.removeAll(Collections.singleton(null));
			newy.removeAll(Collections.singleton(null));

			xList = newx;
			yList = newy;
		}

	}

	public void saveToFile() {
		SaveToFile save = new SaveToFile();
		Double[] xArray = xList.toArray(new Double[xList.size()]);
		Double[] yArray = yList.toArray(new Double[yList.size()]);
		save.save(xArray, yArray);
	}

	public void exitApp() {
		System.exit(0);
	}

	public void clearCanvas() {
		xList.clear();
		yList.clear();
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
	}

	public static void main(String[] args) {
		launch(args);
	}


	public void gameLoop() {
		AnimationTimer timer = new AnimationTimer() {

			@Override
			public void handle(long arg0) {
				drawScreen();

			}

		};
	}
}
