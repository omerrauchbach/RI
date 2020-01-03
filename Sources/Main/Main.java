package Main;


import Part_1.Indexer;
import Part_1.Parse;
import Part_1.ReadFile;
import Part_1.Document;
import Part_1.Writer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class Main extends Application {

   // Indexer indexer = new Indexer(false, null);

    public static void main(String[] args) {

        //onlyNumbers();
        launch(args);
    }

//    public void onlyNumbers() throws IOException {
//        Iterator it = indexer.termDic.entrySet().iterator();
//        int counter = 0;
//        while (it.hasNext()){
//            Map.Entry pair = (Map.Entry) it.next();
//            String word=(String)pair.getKey();
//            if(word.length()>0 && Character.isDigit(word.charAt(0))) {
//                counter++;
//            }
//        }
//        System.out.println(counter);
//    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent mainWindow = FXMLLoader.load(getClass().getResource("/Main.fxml"));
        primaryStage.setScene(new Scene(mainWindow , 600, 400));
        primaryStage.show();
    }
}

/*
public class Main{

    public static void main(String[] args) {
        ReadFile rf = new ReadFile("C:\\Users\\omer\\Desktop");
        rf.readInsideAllFiles();
        Parse parse_test = new Parse(false, null);
        parse_test.start();

    }


}
*/

/**
 *              ReadFile rf = new ReadFile("C:\\Users\\omer\\Desktop");
 *         rf.readInsideAllFiles();
 *         Parse parse_test = new Parse();
 *         parse_test.start();
 *
 *         for(Map.Entry<String, int[]> entry : parse_test.termDic.entrySet()) {
 *             String key = entry.getKey();
 *             int[] value = entry.getValue();
 *             System.out.println(key+","+value[0]);
 */
