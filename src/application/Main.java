
package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;


public class Main extends Application {

	private final String NAME_AND_STUDENT_NUMBER = "Your name and student number here";
	private final String STAGE_TITLE = NAME_AND_STUDENT_NUMBER;

	BorderPane root;
	FlowPane canvasPane;
	FlowPane hudPane;
	Scene scene;
	Canvas canvas;
	GraphicsContext gc;

	HBox ioControlBox;
	HBox dbControlBox;

	Button btnLoad;
	Button btnSave;
	Button btnExit;
	Button btnClear;

	Button btnSaveToDB;
	Button btnLoadFromDB;
	ComboBox<User> usersComboBox;
	ComboBox<Integer> userImagesComboBox;
	Button btnGetUserImages;

	final double STAGE_WIDTH = 500;
	final double STAGE_HEIGHT = 600;

	final double HUD_WIDTH = STAGE_WIDTH;
	final double HUD_HEIGHT = 100;

	final double CANVAS_WIDTH = STAGE_WIDTH;
	final double CANVAS_HEIGHT = 500;

	List<Double> xList;
	List<Double> yList;

	private final int MAX_LIST_SIZE = 10000;

	ObservableList<User> users;
	ObservableList<Integer> userimage_idList;

	int user_id = 1;
	int userimage_id;

	public void layoutStage() {
		root = new BorderPane();
		scene = new Scene(root, STAGE_WIDTH, STAGE_HEIGHT);
		makeCanavsPane();
		makeHudPane();
		makeIOControls();
		makeDBControls();
	}
	public void makeCanavsPane() {
		canvasPane = new FlowPane();
		canvasPane.setMinWidth(CANVAS_WIDTH);
		canvasPane.setMinHeight(CANVAS_HEIGHT);
		canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
		canvasPane.getChildren().add(canvas);
	}
	public void makeHudPane() {
		hudPane = new FlowPane();
		hudPane.setOrientation(Orientation.VERTICAL);
		hudPane.setMinWidth(HUD_WIDTH);
		hudPane.setMinHeight(HUD_HEIGHT);
	}
	public void makeIOControls() {
		ioControlBox = new HBox();
		ioControlBox.setSpacing(10);
		ioControlBox.setMinWidth(HUD_WIDTH);
		ioControlBox.setAlignment(Pos.CENTER);

		btnLoad = new Button("load");
		btnLoad.setMaxWidth(Double.MAX_VALUE);
		ioControlBox.getChildren().add(btnLoad);

		btnSave = new Button("save");
		btnSave.setMaxWidth(Double.MAX_VALUE);
		ioControlBox.getChildren().add(btnSave);

		btnExit = new Button("exit");
		btnExit.setMaxWidth(Double.MAX_VALUE);
		ioControlBox.getChildren().add(btnExit);

		btnClear = new Button("clear");
		btnClear.setMaxWidth(Double.MAX_VALUE);
		ioControlBox.getChildren().add(btnClear);

		hudPane.getChildren().add(ioControlBox);
	}
	public void makeDBControls() {
		dbControlBox = new HBox();
		ioControlBox.setSpacing(10);
		ioControlBox.setMinWidth(HUD_WIDTH);
		ioControlBox.setAlignment(Pos.CENTER);

		btnSaveToDB = new Button("save to DB");
		btnSaveToDB.setMaxWidth(Double.MAX_VALUE);
		dbControlBox.getChildren().add(btnSaveToDB);

		btnLoadFromDB = new Button("load from DB");
		btnLoadFromDB.setMaxWidth(Double.MAX_VALUE);
		dbControlBox.getChildren().add(btnLoadFromDB);

		usersComboBox = new ComboBox<User>();

		dbControlBox.getChildren().add(usersComboBox);

		userImagesComboBox = new ComboBox<Integer>();
		bindUserImagesComboBox();

		dbControlBox.getChildren().add(userImagesComboBox);

		btnGetUserImages = new Button("Get from DB");
		btnGetUserImages.setMaxWidth(Double.MAX_VALUE);
		dbControlBox.getChildren().add(btnGetUserImages);

		hudPane.getChildren().add(dbControlBox);
	}
	public void bindUserImagesComboBox() {
		//userImagesComboBox.getItems().add(e);
		userImagesComboBox.setItems(userimage_idList);
		userImagesComboBox.getSelectionModel().selectFirst();
		userimage_id = userImagesComboBox.getSelectionModel().getSelectedItem();
		loadUserImageData();
		System.out.println("bindUserImagesComboBox userimage_id: " + userimage_id);
		userImagesComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> arg0, Integer arg1, Integer arg2) {
				System.out.println("bindUserImagesComboBox.changed");
				loadUserImageData();
				drawScreen();
			}
		});
	}
	@Override
	public void start(Stage primaryStage) {
		try {
			initData();
			layoutStage();
			gc = canvas.getGraphicsContext2D();
			root.setTop(hudPane);
			root.setBottom(canvasPane);
			addCanvasHandlers();
			addIOControlHandlers();
			addDBControlHandlers();
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
		System.out.println("start.selectedUserID :: " + user_id);
	}
	public void initData() {
		xList = new ArrayList<Double>();
		yList = new ArrayList<Double>();
		loadUserImageids();
		//		loadUserImageData();
	}
	public void addCoordToList(double x, double y) {
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
	public void addUsersComboHandlers() {
		usersComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<User>() {
			@Override
			public void changed(ObservableValue<? extends User> arg0, User arg1, User arg2) {
				if (arg2 != null) {
					System.out.println("addUsersComboHandlers.changed");

					//System.out.println("Selected user: " + arg2.getUser_id());
					user_id = arg2.getUser_id();
					System.out.println("addUsersComboHandlers.selectedUserID :: " + user_id);
					loadUserImageids();
					bindUserImagesComboBox();
				}
			}
		});
	}
	public void addCanvasHandlers() {
		canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				addCoordToList(arg0.getX(),arg0.getY());
				drawScreen();
			}
		});

		canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				addCoordToList(arg0.getX(),arg0.getY());
				drawScreen();
			}
		});
	}
	public void addDBControlHandlers() {
		btnSaveToDB.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (user_id > 0) {
					saveToDB();
				}
			}
		});

		btnLoadFromDB.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				loadFromDB();
			}
		});

		btnGetUserImages.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				loadUsers();
			}
		});

		//		usersComboBox.setOnAction(new EventHandler<ActionEvent>() {
		//			@Override
		//			public void handle(ActionEvent arg0) {
		//				System.out.println("CHANGED");
		//			}
		//		});

	}
	public void addIOControlHandlers() {
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
	public void saveToDB() {
		createNewImageData();
	}
	public void loadFromDB() {

	}
	public void saveImageToDB() {

	}
	private void loadUsers() {
		users = FXCollections.observableArrayList();

		DataBaseConnection dbcon = new DataBaseConnection();
		Connection connection = dbcon.getConnection();

		try {
			PreparedStatement preparedStatement = 
					connection.prepareStatement("select user_id, username from user");
			preparedStatement.execute();

			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				User user = new User();
				user.setUser_id(rs.getInt("user_id"));
				user.setUsername(rs.getString("username"));
				users.add(user);
			}
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	private void loadUserImageids() {
		userimage_idList = FXCollections.observableArrayList();
		DataBaseConnection dbcon = new DataBaseConnection();
		Connection connection = dbcon.getConnection();
		try {
			PreparedStatement preparedStatement = 
					connection.prepareStatement("select userimage_id from userimage where user_id = (?)");
			preparedStatement.setInt(1, user_id);
			preparedStatement.execute();

			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				userimage_idList.add(rs.getInt(1));
			}
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("userimage_idList.size(): " + userimage_idList.size());

	}
	private void loadUserImageData() {
		System.out.println("1 loadUserImageData");
		if (userimage_id == 0) return;
		System.out.println("2 loadUserImageData");
		xList.clear();
		yList.clear();
		DataBaseConnection dbcon = new DataBaseConnection();
		Connection connection = dbcon.getConnection();
		try {
			PreparedStatement preparedStatement = 
					connection.prepareStatement("select x, y from imagedata where userimage_id = (?)");
			preparedStatement.setInt(1, user_id);
			preparedStatement.execute();

			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				System.out.println("NEXT");
				xList.add((double) rs.getFloat(1));
				yList.add((double) rs.getFloat(2));
			}
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	private int createNewUser(String username) {
		DataBaseConnection dbcon = new DataBaseConnection();
		Connection connection = dbcon.getConnection();
		int newUserid = 0;
		try {
			PreparedStatement preparedStatement = 
					connection.prepareStatement("insert into user (username) values (?)",
							Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, username);
			preparedStatement.execute();
			ResultSet rskey = preparedStatement.getGeneratedKeys();
			if (rskey.next()) {
				newUserid = rskey.getInt(1);
			}
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return newUserid;
	}
	private int createNewUserImage(int user_id) {
		DataBaseConnection dbcon = new DataBaseConnection();
		Connection connection = dbcon.getConnection();
		int newImageid = 0;
		try {
			PreparedStatement preparedStatement = 
					connection.prepareStatement("insert into userimage (user_id) values (?)",
							Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setInt(1, user_id);
			preparedStatement.execute();
			ResultSet rskey = preparedStatement.getGeneratedKeys();
			if (rskey.next()) {
				newImageid = rskey.getInt(1);
			}
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return newImageid;
	}
	private void createNewImageData() {
		DataBaseConnection dbcon = new DataBaseConnection();
		Connection connection = dbcon.getConnection();
		try {
			Statement statement = connection.createStatement();
			for (int j = 0; j < xList.size(); j ++ ) {
				String query = "insert into imagedata (userimage_id,x,y) values('"
						+ user_id + "','" + xList.get(j) + "','" + yList.get(j)  +"')";
				statement.addBatch(query);
			}
			statement.executeBatch();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

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

//public void bindUsersCombo() {
//		usersComboBox.setItems(users);
//		//usersComboBox.getSelectionModel().selectFirst();
//		usersComboBox.setCellFactory(new Callback<ListView<User>,ListCell<User>>(){
//			@Override
//			public ListCell<User> call(ListView<User> l){
//				return new ListCell<User>(){
//					@Override
//					protected void updateItem(User item, boolean empty) {
//						super.updateItem(item, empty);
//						if (item == null || empty) {
//							//setGraphic(null);
//							System.out.println("bindUsersCombo.call.updateItem item == null");
//						} else {
//							setText(item.getUser_id() + "    " + item.getUsername());
//							System.out.println("bindUsersCombo.call.updateItem item not null");
//						}
//					}
//				};
//			}
//		});
//		addUsersComboHandlers();
//	}


//package application;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import javafx.animation.AnimationTimer;
//import javafx.application.Application;
//import javafx.beans.value.ChangeListener;
//import javafx.beans.value.ObservableValue;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.event.ActionEvent;
//import javafx.event.EventHandler;
//import javafx.geometry.Orientation;
//import javafx.geometry.Pos;
//import javafx.stage.Stage;
//import javafx.stage.StageStyle;
//import javafx.stage.WindowEvent;
//import javafx.scene.Scene;
//import javafx.scene.canvas.Canvas;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.control.Button;
//import javafx.scene.control.ComboBox;
//import javafx.scene.control.ListCell;
//import javafx.scene.control.ListView;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.FlowPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.paint.Color;
//import javafx.util.Callback;
//
//
//public class Main extends Application {
//
//	private final String NAME_AND_STUDENT_NUMBER = "Your name and student number here";
//	private final String STAGE_TITLE = NAME_AND_STUDENT_NUMBER;
//
//	BorderPane root;
//	FlowPane canvasPane;
//	FlowPane hudPane;
//	Scene scene;
//	Canvas canvas;
//	GraphicsContext gc;
//
//	Button btnLoad;
//	Button btnSave;
//	Button btnExit;
//	Button btnClear;
//
//	Button btnSaveToDB;
//	Button btnLoadFromDB;
//	ComboBox<User> usersComboBox;
//	ComboBox<Integer> imagesComboBox;
//
//	ObservableList<User> data;
//
//
//	Button btnGetImage;
//
//	final double STAGE_WIDTH = 500;
//	final double STAGE_HEIGHT = 600;
//
//	final double HUD_WIDTH = STAGE_WIDTH;
//	final double HUD_HEIGHT = 100;
//
//	final double CANVAS_WIDTH = STAGE_WIDTH;
//	final double CANVAS_HEIGHT = 500;
//
//	List<Double> xList;
//	List<Double> yList;
//
//	private final int MAX_LIST_SIZE = 10000;
//
//	ObservableList<User> users;
//	ObservableList<Integer> userimage_idList;
//	
//	int selectedUserID;
//	
//	public void layoutStage() {
//		
//	}
//	
//	@Override
//	public void start(Stage primaryStage) {
//		try {
//
//			initData();
//
//			root = new BorderPane();
//
//			canvasPane = new FlowPane();
//			canvasPane.setMinWidth(CANVAS_WIDTH);
//			canvasPane.setMinHeight(CANVAS_HEIGHT);
//
//			canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
//
//			gc = canvas.getGraphicsContext2D();
//			canvasPane.getChildren().add(canvas);
//
//			hudPane = new FlowPane();
//			hudPane.setOrientation(Orientation.VERTICAL);
//			hudPane.setMinWidth(HUD_WIDTH);
//			hudPane.setMinHeight(HUD_HEIGHT);
//
//			HBox iohb = new HBox();
//			iohb.setSpacing(10);
//			iohb.setMinWidth(HUD_WIDTH);
//			iohb.setAlignment(Pos.CENTER);
//
//			btnLoad = new Button("load");
//			btnLoad.setMaxWidth(Double.MAX_VALUE);
//			iohb.getChildren().add(btnLoad);
//
//			btnSave = new Button("save");
//			btnSave.setMaxWidth(Double.MAX_VALUE);
//			iohb.getChildren().add(btnSave);
//
//			btnExit = new Button("exit");
//			btnExit.setMaxWidth(Double.MAX_VALUE);
//			iohb.getChildren().add(btnExit);
//
//			btnClear = new Button("clear");
//			btnClear.setMaxWidth(Double.MAX_VALUE);
//			iohb.getChildren().add(btnClear);
//
//			HBox dbhb = new HBox();
//			iohb.setSpacing(10);
//			iohb.setMinWidth(HUD_WIDTH);
//			iohb.setAlignment(Pos.CENTER);
//
//			btnSaveToDB = new Button("save to DB");
//			btnSaveToDB.setMaxWidth(Double.MAX_VALUE);
//			dbhb.getChildren().add(btnSaveToDB);
//
//			btnLoadFromDB = new Button("load from DB");
//			btnLoadFromDB.setMaxWidth(Double.MAX_VALUE);
//			dbhb.getChildren().add(btnLoadFromDB);
//
//			usersComboBox = new ComboBox<User>();
//			usersComboBox.setItems(users);
//			//usersComboBox.getSelectionModel().selectFirst();
//			usersComboBox.setCellFactory(new Callback<ListView<User>,ListCell<User>>(){
//				@Override
//				public ListCell<User> call(ListView<User> l){
//					return new ListCell<User>(){
//						@Override
//						protected void updateItem(User item, boolean empty) {
//							super.updateItem(item, empty);
//							if (item == null || empty) {
//								setGraphic(null);
//							} else {
//								setText(item.getUser_id()+"    "+item.getUsername());
//								System.out.println("here");
//							}
//						}
//					} ;
//				}
//			});
//
//			usersComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<User>() {
//				@Override
//				public void changed(ObservableValue<? extends User> arg0, User arg1, User arg2) {
//					if (arg2 != null) {
//						System.out.println("Selected user: " + arg2.getUser_id());
//						selectedUserID = arg2.getUser_id();
//						loadUserImageids();
//						bindImagesComboBox();
//					}
//				}
//			});
//
//			dbhb.getChildren().add(usersComboBox);
//
//			imagesComboBox = new ComboBox<Integer>();
//			dbhb.getChildren().add(imagesComboBox);
//
//
//			btnGetImage = new Button("Get from DB");
//			btnGetImage.setMaxWidth(Double.MAX_VALUE);
//			dbhb.getChildren().add(btnGetImage);
//
//
//			hudPane.getChildren().add(iohb);
//			hudPane.getChildren().add(dbhb);
//
//			root.setTop(hudPane);
//			root.setBottom(canvasPane);
//
//			scene = new Scene(root, STAGE_WIDTH, STAGE_HEIGHT);
//
//
//			addHandlers();
//			drawScreen();
//
//			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
//
//			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
//
//				@Override
//				public void handle(WindowEvent event) {
//					saveToFile();
//					exitApp();
//				};
//
//			});
//
//			primaryStage.setResizable(false);
//			primaryStage.initStyle(StageStyle.UTILITY);
//			primaryStage.setTitle(STAGE_TITLE);
//			primaryStage.setScene(scene);
//			primaryStage.show();
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void bindImagesComboBox() {
//		imagesComboBox.setItems(userimage_idList);
//		imagesComboBox.getSelectionModel().selectFirst();
//		imagesComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
//			@Override
//			public void changed(ObservableValue<? extends Integer> arg0, Integer arg1, Integer arg2) {
//				System.out.println("imagesComboBox");
//			}
//		});
//	}
//
//	public void initData() {
//		xList = new ArrayList<Double>();
//		yList = new ArrayList<Double>();
//		loadUsers();
//		loadFromFile();
//	}
//
//	public void addCoord(double x, double y) {
//		xList.add(x);
//		yList.add(y);
//
//		if (xList.size() == (MAX_LIST_SIZE + 1)) {
//			xList.remove(0);
//			yList.remove(0);
//		}
//
//		System.out.println("xList size: " + xList.size());
//		System.out.println("yList size: " + yList.size());
//
//	}
//
//	public void drawScreen() {
//		gc.setFill(Color.BLACK);
//		gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
//		gc.setFill(Color.CHARTREUSE);
//
//		System.out.println("xList size: " + xList.size());
//		System.out.println("yList size: " + yList.size());
//
//		for (int x = 0; x < xList.size(); x ++) {
//			gc.fillRect(xList.get(x), yList.get(x), 10, 10);
//		}
//	}
//
//	public void addHandlers() {
//
//		canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
//
//			@Override
//			public void handle(MouseEvent arg0) {
//				addCoord(arg0.getX(),arg0.getY());
//				drawScreen();
//			}
//		});
//
//		canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
//
//			@Override
//			public void handle(MouseEvent arg0) {
//				addCoord(arg0.getX(),arg0.getY());
//				drawScreen();
//			}
//		});
//
//		btnLoad.setOnAction(new EventHandler<ActionEvent>() {
//
//			@Override
//			public void handle(ActionEvent event) {
//				loadFromFile();
//				drawScreen();
//			}
//		});
//
//		btnSave.setOnAction(new EventHandler<ActionEvent>() {
//
//			@Override
//			public void handle(ActionEvent event) {
//				saveToFile();
//			}
//		});
//
//		btnExit.setOnAction(new EventHandler<ActionEvent>() {
//
//			@Override
//			public void handle(ActionEvent event) {
//				saveToFile();
//				exitApp();
//
//			}
//		});
//
//		btnClear.setOnAction(new EventHandler<ActionEvent>() {
//
//			@Override
//			public void handle(ActionEvent event) {
//				clearCanvas();
//
//			}
//		});
//
//		btnSaveToDB.setOnAction(new EventHandler<ActionEvent>() {
//
//			@Override
//			public void handle(ActionEvent event) {
//				saveToDB();
//
//			}
//		});
//
//		btnLoadFromDB.setOnAction(new EventHandler<ActionEvent>() {
//
//			@Override
//			public void handle(ActionEvent event) {
//				loadFromDB();
//
//			}
//		});
//
//		btnGetImage.setOnAction(new EventHandler<ActionEvent>() {
//
//			@Override
//			public void handle(ActionEvent event) {
//				loadUsers();
//				
//			}
//		});
//
//		
//
//
//		usersComboBox.setOnAction(new EventHandler<ActionEvent>() {
//
//			@Override
//			public void handle(ActionEvent arg0) {
//				System.out.println("CHANGED");
//
//			}
//
//		});
//
//	}
//
//	public void saveToDB() {
//		createNewImageData(1, xList, yList);
//	}
//
//	public void loadFromDB() {
//
//	}
//
//	public void saveImageToDB() {
//
//
//
//	}
//
//	private void loadUsers() {
//
//		users = FXCollections.observableArrayList();
//
//		DataBaseConnection dbcon = new DataBaseConnection();
//		Connection connection = dbcon.getConnection();
//
//		try {
//			PreparedStatement preparedStatement = 
//					connection.prepareStatement("select user_id, username from user");
//			preparedStatement.execute();
//
//			ResultSet rs = preparedStatement.executeQuery();
//			while (rs.next()) {
//				User user = new User();
//				user.setUser_id(rs.getInt("user_id"));
//				user.setUsername(rs.getString("username"));
//				users.add(user);
//			}
//			connection.close();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	private void loadUserImageids() {
//
//		userimage_idList = FXCollections.observableArrayList();
//
//		DataBaseConnection dbcon = new DataBaseConnection();
//		Connection connection = dbcon.getConnection();
//
//		try {
//			PreparedStatement preparedStatement = 
//					connection.prepareStatement("select userimage_id from userimage where user_id = (?)");
//			preparedStatement.setInt(1, selectedUserID);
//			preparedStatement.execute();
//
//			ResultSet rs = preparedStatement.executeQuery();
//			while (rs.next()) {
//				userimage_idList.add(rs.getInt(1));
//			}
//			connection.close();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//		System.out.println("userimage_idList.size(): " + userimage_idList.size());
//
//	}
//
//	private void loadImageData(int userimage_id) {
//
//		xList.clear();
//		yList.clear();
//
//		DataBaseConnection dbcon = new DataBaseConnection();
//		Connection connection = dbcon.getConnection();
//
//		try {
//			PreparedStatement preparedStatement = 
//					connection.prepareStatement("select x, y from imagedata where userimage_id = (?)");
//			preparedStatement.setInt(1, userimage_id);
//			preparedStatement.execute();
//
//			ResultSet rs = preparedStatement.executeQuery();
//			while (rs.next()) {
//				System.out.println("NEXT");
//				xList.add((double) rs.getFloat(1));
//				yList.add((double) rs.getFloat(2));
//			}
//			connection.close();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	private int createNewUser(String username) {
//		DataBaseConnection dbcon = new DataBaseConnection();
//		Connection connection = dbcon.getConnection();
//		int newUserid = 0;
//		try {
//			PreparedStatement preparedStatement = 
//					connection.prepareStatement("insert into user (username) values (?)",
//							Statement.RETURN_GENERATED_KEYS);
//			preparedStatement.setString(1, username);
//			preparedStatement.execute();
//			ResultSet rskey = preparedStatement.getGeneratedKeys();
//			if (rskey.next()) {
//				newUserid = rskey.getInt(1);
//			}
//			connection.close();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return newUserid;
//	}
//
//	private int createNewUserImage(int user_id) {
//		DataBaseConnection dbcon = new DataBaseConnection();
//		Connection connection = dbcon.getConnection();
//		int newImageid = 0;
//		try {
//			PreparedStatement preparedStatement = 
//					connection.prepareStatement("insert into userimage (user_id) values (?)",
//							Statement.RETURN_GENERATED_KEYS);
//			preparedStatement.setInt(1, user_id);
//			preparedStatement.execute();
//			ResultSet rskey = preparedStatement.getGeneratedKeys();
//			if (rskey.next()) {
//				newImageid = rskey.getInt(1);
//			}
//			connection.close();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return newImageid;
//	}
//
//	private void createNewImageData(int userimage_id, List<Double> x, List<Double> y) {
//		DataBaseConnection dbcon = new DataBaseConnection();
//		Connection connection = dbcon.getConnection();
//
//		try {
//			Statement statement = connection.createStatement();
//
//			for (int j = 0; j < x.size(); j ++ ) {
//				String query = "insert into imagedata (userimage_id,x,y) values('"
//						+ userimage_id + "','" + x.get(j) + "','" + y.get(j)  +"')";
//				statement.addBatch(query);
//			}
//			statement.executeBatch();
//			statement.close();
//
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//	}
//
//
//
//	public void loadFromFile() {
//		ReadFromFile read = new ReadFromFile();
//		read.read();
//		convertArrayToList(read.getxArray(),read.getyArray());
//	}
//
//	private void convertArrayToList(Double xArray[], Double yArray[]) {
//
//		if (xArray != null && yArray != null) {
//			List<Double> newx = new ArrayList<Double>(Arrays.asList(xArray));
//			List<Double> newy = new ArrayList<Double>(Arrays.asList(yArray));
//
//			newx.removeAll(Collections.singleton(null));
//			newy.removeAll(Collections.singleton(null));
//
//			xList = newx;
//			yList = newy;
//		}
//
//	}
//
//	public void saveToFile() {
//		SaveToFile save = new SaveToFile();
//		Double[] xArray = xList.toArray(new Double[xList.size()]);
//		Double[] yArray = yList.toArray(new Double[yList.size()]);
//		save.save(xArray, yArray);
//	}
//
//	public void exitApp() {
//		System.exit(0);
//	}
//
//	public void clearCanvas() {
//		xList.clear();
//		yList.clear();
//		gc.setFill(Color.BLACK);
//		gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
//	}
//
//	public static void main(String[] args) {
//		launch(args);
//	}
//
//
//	public void gameLoop() {
//		AnimationTimer timer = new AnimationTimer() {
//
//			@Override
//			public void handle(long arg0) {
//				drawScreen();
//
//			}
//
//		};
//	}
//	
//	
//	//usersComboBox.getItems().addAll(usernameList);
//	//String selected = usersComboBox.getValue().to;
//
//
//	//				loadImageData(1);
//	//				drawScreen();
//	//				loadUsers();
//	//				loadUserImageids(1);
//	//				loadFromDB();
//	//				String username = usersComboBox.getValue();
//	//				int userimage_id = Integer.parseInt(imagesComboBox.getValue());
//	//				if (username != null && userimage_id > 0) {
//	//					createNewImageData(userimage_id);
//	//				}
//	
////	usersComboBox.valueProperty().addListener(new ChangeListener<String>() {
//	//			@Override
//	//			public void changed(ObservableValue<? extends String> observable,
//	//					String oldValue, String newValue) {
//	//				// TODO Auto-generated method stub
//	//				loadUserImageids(Integer.parseInt(newValue));
//	//				imagesComboBox.getItems().addAll(userimage_idList);
//	//
//	//
//	//			}
//	//
//	//		});
//}
//
//
