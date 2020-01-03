package Controller;

import Part_1.Document;
import Part_1.Indexer;
import Part_1.Parse;
import Part_1.ReadFile;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Controller {

    public TextField documentPath ;
    public TextField postingPath;
    public Button start;
    public Button browse_Doc;
    public Button browse_posting;
    public Button reset;
    public Button displayInv;
    public Button loadInv;
    public CheckBox stemming;
    public String docPath= "" ;
    public String postingPathSaved = "" ;
    public boolean alreadyIndexedWithStemming = false;
    public boolean alreadyIndexedWithoutStemming = false;
    private boolean startsIndexing = false;
    public long startTime;
    public String loadDicPath;
    public boolean stemm = false;
    public static BlockingQueue<Document> currChunk = new LinkedBlockingQueue<>(5000);
    ReadFile rd ;

    public void onStart(){

 /*       loadInv.setVisible(false);
        displayInv.setVisible(false);
        browse_Doc.setVisible(false);
        browse_posting.setVisible(false);*/

        docPath = documentPath.getText();
        postingPathSaved = this.postingPath.getText();
        stemm = this.stemming.isSelected();
        String infoToDisplay = "";

        if(docPath.equals("") || postingPathSaved.equals("")){
            displayError("You have to fill the two paths");
        }else{

            if (stemm)
                deleteFiles(postingPathSaved+"\\stemming");
            else
                deleteFiles(postingPathSaved+"\\nonStemming");


            rd = new ReadFile(docPath ,stemm  ,postingPathSaved);
            try {
                ReadFile.stopParser = false;
                Parse.stopIndexer =false;

                startTime = System.nanoTime();
                startsIndexing = true;
                rd.start();

                if (stemm)
                    alreadyIndexedWithStemming = true;
                else
                    alreadyIndexedWithoutStemming = true;
            }
            catch(Exception e){
                e.printStackTrace();
            }finally {
 /*               loadInv.setDisable(false);
                displayInv.setDisable(false);
                browse_Doc.setDisable(false);
                browse_posting.setDisable(false);*/
                double totalTimeInSeconds = (System.nanoTime() - startTime) * Math.pow(10, -9);
                int numOfDocs = rd.getNumOfDoc();
                int numOfUniqueTerms = rd.getNumOFTerm();
                infoToDisplay = "Total documents indexed: " + numOfDocs + "\nNumber of unique " +
                        "terms in the corpus: " + numOfUniqueTerms + "\nTotal process' running time: "
                        + totalTimeInSeconds + " seconds";

                displayInfo(infoToDisplay);
                startsIndexing = false;
               /* try {
                    sortByValue();
                    onlyNumbers();


                } catch (IOException e){
                    e.printStackTrace();
            }*/

                System.out.println("Done!!!!!!!");

            }
        }
    }


    public void onBrowseDoc(){ Browse(documentPath); }

    public void onBrowsePosting(){ Browse(postingPath); }

    private void Browse(TextField text){
        JButton open = new JButton();
        JFileChooser jc = new JFileChooser();
        jc.setCurrentDirectory(new File("."));
        jc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(jc.showOpenDialog(open) == JFileChooser.APPROVE_OPTION){
            text.setText(jc.getSelectedFile().getAbsolutePath());
        }
    }

    public void onReset(){

        postingPathSaved = this.postingPath.getText();
        if (!startsIndexing && ((alreadyIndexedWithStemming || alreadyIndexedWithoutStemming) || !postingPathSaved.equals(""))) {

            documentPath.setText("");
            postingPath.setText("");

            stemming.setSelected(false);

            File dir = new File(postingPathSaved);
            File[] dirFiles = dir.listFiles();
            if (dirFiles != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to reset?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.YES){
                    deleteFiles(postingPathSaved+"\\stemming");
                    deleteFiles(postingPathSaved+"\\nonStemming");
                }


            }

            alreadyIndexedWithStemming = false;
            alreadyIndexedWithoutStemming = false;
            rd = null;
            ReadFile.stopParser = false;
            Parse.stopIndexer = false;
            documentPath.clear();
            postingPath.clear();
            docPath = null;
            postingPath = new TextField();
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("No data to be reset!");
            alert.show();
        }
    }

    public void onDisplayInv(){

        ObservableList<Map.Entry<String, Integer>> invertedList = getObservableList();
        Stage stage = new Stage();
        stage.setTitle("Dictionary");

        TableColumn<Map.Entry<String, Integer>, String> tokenCol = new TableColumn<>("term");
        tokenCol.setMinWidth(200);
        tokenCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));

        TableColumn<Map.Entry<String, Integer> , Integer> numCol = new TableColumn<>("total shows");
        numCol.setMinWidth(100);
        numCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getValue()).asObject());

        TableView table = new TableView<>();
        table.setItems(invertedList);
        table.getColumns().addAll(tokenCol, numCol);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(table);

        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.show();
    }

    public void onLoadInv() throws IOException {

        postingPathSaved = this.postingPath.getText();
        stemm = this.stemming.isSelected();

        if (postingPathSaved.equals("")) {
            displayError("You have to fill the posting path to load a dictionary!");
        }

         else { //correct posting files  path.

            if (new File(postingPathSaved + "\\stemming").exists() && stemm) {
                loadDicPath = postingPathSaved + "\\stemming";
                //rd = new ReadFile(docPath, stemm, postingPathSaved);
            }
            else if (new File(postingPathSaved + "\\nonStemming").exists() && !stemm) {
                loadDicPath = postingPathSaved + "\\nonStemming";
                //rd = new ReadFile(docPath, stemm, postingPathSaved);
            }
            else {
                displayError("No files to load from the given path!");
                return;
            }

            ///////////////////////////////////////////////////////////////// ?????????
            rd = new ReadFile(docPath, stemm, postingPathSaved);

            File dir = new File(loadDicPath); //which dir to load from (stemm or not).
            File[] dirFiles = dir.listFiles();
            if (dirFiles != null) {
                for (File fileInDir : dirFiles) { //each text file.
                    if (fileInDir != null)
                        load(fileInDir.getName());
                }
                displayInfo("Dictionary was successfully loaded to memory.");

            }
        }
    }

    private ObservableList<Map.Entry<String, Integer>> getObservableList(){

        ObservableList<Map.Entry<String, Integer>> invertedList = FXCollections.observableArrayList();
        TreeMap<String,int[]> sortedList = new TreeMap<>(new Comparator<String>(){

            @Override
            public int compare(String s1, String s2) {
                int result = s1.compareToIgnoreCase(s2);
                if( result == 0 )
                    result = s1.compareTo(s2);
                return result;
            }
        });
        sortedList.putAll(rd.indexer.termDic);
        for(Map.Entry<String, int[]> entry : sortedList.entrySet()){
            Map.Entry<String, Integer> newEntry = new Map.Entry<String, Integer>() {
                @Override
                public String getKey() {
                    return entry.getKey();
                }

                @Override
                public Integer getValue() {
                    return entry.getValue()[1];
                }

                @Override
                public Integer setValue(Integer value) {
                    return null;
                }
            };
            invertedList.add(newEntry);
        }
        return invertedList;
    }

    private List<String> getInvAsList(){

        List<String> InvList = new ArrayList<>();
        TreeMap<String,int[]> sortedList = new TreeMap<>(new Comparator<String>(){

            @Override
            public int compare(String s1, String s2) {
                int result = s1.compareToIgnoreCase(s2);
                if( result == 0 )
                    result = s1.compareTo(s2);
                return result;
            }
        });
        sortedList.putAll(rd.indexer.termDic);
        for(Map.Entry<String, int[]> entry: sortedList.entrySet()) {
            int[] value = entry.getValue();
            InvList.add(entry.getKey() + "," + value[0] + "," + value[1]) ;
        }

        return InvList;
    }

    private void deleteFiles(String pathToDelete){


        File dir = new File(pathToDelete);
        if(dir.exists()) {
            File[] dirFiles = dir.listFiles();
            if (dirFiles != null) {
                for (File fileInDir : dirFiles) {
                    if (fileInDir != null)
                        fileInDir.delete();
                }
                dir.delete();
            }
        }
    }

    public void sortByValue() throws IOException {

        List<Map.Entry<String, int[]> > list =
                new LinkedList<Map.Entry<String, int[]> >(rd.indexer.termDic.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, int[]> >() {
            public int compare(Map.Entry<String, int[]> o1,
                               Map.Entry<String, int[]> o2)
            {
                return (o2.getValue()[1])-(o1.getValue()[1]);
            }
        });
        FileWriter pw = new FileWriter(postingPathSaved+"\\wordsAllWords.txt", false);
        Iterator it = list.iterator();
        int counter = 0;

        for(Map.Entry<String, int[]> entry  : list){

            if(counter < 10) {
                pw.write(entry.getKey() + "-" + entry.getValue()[1] + "\r\n");
                counter++;
            }else {
                break;
            }
        }
        pw.close();
        System.out.println("finish!!");

    }
    private void displayError(String error){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(error);
        alert.show();
    }

    private void displayInfo(String info){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(info);
        alert.show();
    }

    private void load (String fileNameToLoadFrom) {

        String line1;
        String term;
        int totalShows = 0;
        int numOfDocs = 0;
        int[] termData = new int[2];
        String[] allDocsInfo = null;
        String currFilePath = loadDicPath + "\\" + fileNameToLoadFrom ;
        try {
            Scanner scanner = new Scanner((new File(currFilePath)));
            while (scanner.hasNextLine()) {

                String test = scanner.nextLine();
                line1 = test;
                term = "";
                if(line1.contains("insurer|FBHUMMENE") || line1.contains("FB3,20199,20213,23203"))
                    continue;
                try {
                    term = line1.substring(0, line1.indexOf("|")); // only term itself, with no other data.
                }
                catch (Exception e){
                    //System.out.println();
                }
                line1 = line1.substring(line1.indexOf("|")+1); //without term itself.

                totalShows = 0;
                numOfDocs =0;


                /*
                allDocsInfo = new String[line1.length()]; //initailiaze
                if (!line1.contains("|")) { //only one doc in list.
                    numOfDocs = 1;
                    totalShows = Integer.parseInt(line1.substring(line1.indexOf(":") + 1, line1.indexOf(";")));
                    line1 = null; //finish
                }
                */


                String[] lineChar = line1.split("\\|");
                numOfDocs += lineChar.length;
                for(String s : lineChar) {
                    String[] dot = s.split("[:;]+");
                    try {
                        totalShows += Integer.parseInt(dot[1]);
                    }
                    catch (Exception e){
                        //System.out.println();
                    }
                }

                    /*
                    allDocsInfo = line1.split(":|\\;|\\|"); //each cell is a different doc and its values
                    numOfDocs++;
                    totalShows = totalShows + Integer.parseInt(allDocsInfo[1]);
                    line1 = line1.substring(line1.indexOf("|") + 1); // next docID of term and info...
                    if (!line1.contains("|")) {
                        numOfDocs++;
                        allDocsInfo = line1.split(":|\\;|\\|"); //each cell is a different doc and its values
                        try {
                            totalShows = totalShows + Integer.parseInt(allDocsInfo[1]);
                            break;
                        }
                        catch (Exception e){
                            System.out.println();
                        }

                    }
                    */


                termData[0] = numOfDocs;
                termData[1] = totalShows;


                if(term.length() == 0)
                    continue;
                if(Character.isDigit(term.charAt(0))){

                    if(rd.indexer.termDic.containsKey(term)) {
                        int[] savedTermData;
                        savedTermData = rd.indexer.termDic.get(term);
                        int[] updateTermInfo = new int[3];
                        updateTermInfo[0] = savedTermData[0] + termData[0]; // adds 1 to curr # of docs
                        updateTermInfo[1] = savedTermData[1] + termData[1]; //#shows total == adds num of appearences in specific doc. !!!
                        rd.indexer.termDic.replace(term, updateTermInfo);
                    }else{
                        rd.indexer.termDic.put(term,termData);
                    }

                }
                else if(rd.indexer.termDic.containsKey(term)){
                    int[] savedTermData;
                    savedTermData = rd.indexer.termDic.get(term);
                    int[] updateTermInfo = new int[3];
                    updateTermInfo[0] = savedTermData[0] + termData[0]; // adds 1 to curr # of docs
                    updateTermInfo[1] = savedTermData[1] + termData[1]; //#shows total == adds num of appearences in specific doc. !!!
                    rd.indexer.termDic.replace(term, updateTermInfo);

                }
                else if(rd.indexer.termDic.containsKey(term.toUpperCase()))
                {
                    int[] savedTermData = rd.indexer.termDic.get(term.toUpperCase());
                    int[] updateTermInfo = new int[3];
                    updateTermInfo[0] = savedTermData[0] + termData[0]; // adds 1 to curr # of docs
                    updateTermInfo[1] = savedTermData[1] + termData[1]; //#shows total == adds num of appearences in specific doc. !!!
                    rd.indexer.termDic.replace(term, updateTermInfo);
                }else if( rd.indexer.termDic.containsKey(term.toLowerCase())){

                    int[] savedTermData = rd.indexer.termDic.get(term.toLowerCase());
                    int[] updateTermInfo = new int[3];
                    updateTermInfo[0] = savedTermData[0] + termData[0]; // adds 1 to curr # of docs
                    updateTermInfo[1] = savedTermData[1] + termData[1]; //#shows total == adds num of appearences in specific doc. !!!
                    rd.indexer.termDic.replace(term.toLowerCase(), updateTermInfo);

                }else{
                    rd.indexer.termDic.put(term,termData);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
    private boolean alreadyIndexedAll() {
        return alreadyIndexedWithStemming && alreadyIndexedWithoutStemming;
    }
}