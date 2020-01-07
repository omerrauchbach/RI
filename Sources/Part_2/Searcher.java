package Part_2;

import Part_1.Document;
import Part_1.Parse;
import Part_1.Indexer;
import javafx.scene.control.Alert;
import sun.awt.Mutex;


import java.io.*;
import java.util.*;

public class Searcher implements Runnable {


    private String queryText;
    private Query query;
    private String stopWordsPath;
    private String queryFilePath;
    private boolean semantics;
    private Queue<String> relevantDocs;
    public Queue<Document> querySet;
    public LinkedList<Document> queryAfterParse = new LinkedList<>();
    int indexQuery;
    private Parse parse;
    private String queryPath;
    private StringBuilder allLinesInQueries;
    private Mutex lockAddToAfterParse = new Mutex();
    public HashMap<String, String[]> queriesToSearchAndRank;
    private String queryFilePath;

    //private Indexer indexer;

    public Searcher(String query, String stopWordsPath, String queryFilePath, boolean semantics) {
        this.parse = new Parse(false, stopWordsPath, true);
        this.queryText = query;
        this.stopWordsPath = stopWordsPath;
        this.semantics = semantics;
        this.queryFilePath = queryFilePath;
        indexQuery = 1;
        this.queryFilePath = queryFilePath;
        queriesToSearchAndRank =new LinkedHashMap<>();
        //indexer = Indexer.
    }

    public void processQuery() {

        Query query;
        String[] queryTextSplit = new String[1000]; //////////???????????
        Document doc;
        querySet = new LinkedList<>();

        if (queryText != null) { //only string. inputtt
            queryTextSplit = queryText.split(" ");
            StringBuilder queryToProcess = new StringBuilder();
            int i = 0;
            while (i < queryTextSplit.length) {
                queryTextSplit[i] = removeDelimiters(queryTextSplit[i]);
                queryTextSplit[i] = queryTextSplit[i].toLowerCase();
                queryToProcess.append(" ").append(queryTextSplit[i]); //adds to SB
                i++;
            }
            queryText = queryToProcess.toString(); //after all removals
            query = new Query(queryText);
            //createQueryDoc();
            doc = new Document();
            doc.setId("query" + indexQuery);
            doc.setText(queryText);
            querySet.add(doc);
            LinkedList newList = new LinkedList(querySet);
            querySet.clear();
            queryAfterParse = parse.parseDocs(newList);

            doc = queryAfterParse.getFirst(); //only one document in this return list.
            String[] toRank = prepareToRank(doc); //prepare String[] of query words to rank.

            queriesToSearchAndRank.put("query" + indexQuery, toRank);
        }

        else { // queries text file !! ????????????????????????
            query = new Query("");
            readQueryFile();
            Document currQuery = new Document();

            while (!queryAfterParse.isEmpty()) { //each query is a separate doc in list.
               // currQuery = new Document();
                currQuery = queryAfterParse.poll();
                String[] toRank = prepareToRank(currQuery); //prepare String[] of query words to rank.
                queriesToSearchAndRank.put("query" + currQuery.getId(), toRank);
            }
        }

        Ranker ranker = new Ranker();

  /*      // puts all the query terms into an array for rank
        HashMap<String, int[]> queryDic = query.getQueryTermDic();
        String[] queryTerms = new String[queryDic.size()];
        int i = 0;
        for (String queryWord : queryDic.keySet()) {
            queryTerms[i] = queryWord;
            i++;
        }*/

        relevantDocs = ranker.rank(queriesToSearchAndRank);
    }

    private String[] prepareToRank(Document doc) {
        if (doc != null) {
            String[] allTermsInQuery = new String[doc.getLength()];
            String allText = doc.getText();
            allTermsInQuery = allText.split(" ");
            return allTermsInQuery;
        }
        return null;
    }

    private void readQueryFile() {

        File queriesFile = new File(queryPath);
        //System.out.println("ReadFile");
        if (queriesFile != null) {
            allLinesInQueries = new StringBuilder();
            try {
                BufferedReader myBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(queriesFile)));
                for (String currLine; (currLine = myBufferedReader.readLine()) != null; )
                    allLinesInQueries.append(currLine + System.lineSeparator());

                //createDoc();
                String text;
                String idQuery;
                //String id = allLinesInQueries.substring(allLinesInQueries.indexOf("<num>") + 2, endIndexId).trim();
                int startInd = allLinesInQueries.indexOf("<title>");
                while (startInd != -1) {
                    idQuery = String.valueOf(allLinesInQueries.indexOf("<num>")+2); //Query number ID
                    int endInd = allLinesInQueries.indexOf("<desc>", startInd); //searches for "<desc>" from starts index
                    String currQuery = allLinesInQueries.substring(startInd+1, endInd); //query itself.

                    //set Id Query <num>"
                    Document newQueryDoc = new Document();
                    newQueryDoc.setId(idQuery);
                    newQueryDoc.setText(currQuery);
                    querySet.add(newQueryDoc);

                    startInd = allLinesInQueries.indexOf("<title>", endInd); //continues to the next doc in file
                }

                allLinesInQueries = new StringBuilder(); //initialize
                myBufferedReader.close();

                LinkedList newList = new LinkedList(querySet);
                querySet.clear();

                try {
                    LinkedList<Document> AfterParseQueries = parse.parseDocs(newList);
                    lockAddToAfterParse.lock();      ///??????????????????????/
                    queryAfterParse.addAll(AfterParseQueries);
                    lockAddToAfterParse.unlock();      ///??????????????????????/
                   // parseIndexrList++;
                    //System.out.println("to Parser: " +parseIndexrList);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in folder path");
            alert.show();
        }
        //stopReadFile = true;
    }



    private void createQuery(){

    }

    private String removeDelimiters(String word) {
        if (!word.equals("")) {
            int length = word.length();

            // removes delimiter from the beginning of the word
            if (word.charAt(0) > 'z' || word.charAt(0) < 'A' || (word.charAt(0) > 'Z' && word.charAt(0) < 'a')) {
                return removeDelimiters(word.substring(1));
            }

            // removes delimiter from the end of the word
            if (word.charAt(length - 1) > 'z' || word.charAt(length - 1) < 'A' || (word.charAt(length - 1) > 'Z' && word.charAt(length - 1) < 'a'))
                return removeDelimiters(word.substring(0, length - 1));

            return word;
        }
        return word;
    }

    @Override
    public void run() {
        
    }

    public Queue<String> getRelevantDocs() {
        return relevantDocs;
    }
}
