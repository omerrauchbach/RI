package Part_2;

import Part_1.Document;
import Part_1.Parse;
import Part_1.Indexer;
import javafx.scene.control.Alert;
import sun.awt.Mutex;


import java.io.*;
import java.net.URL;
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
    private StringBuilder allLinesInQueries;
    private Mutex lockAddToAfterParse = new Mutex();
    public HashMap<String, String[]> queriesToSearchAndRank;
    private LinkedList<LinkedList<String>> relevantDocsForAll;


    //private Indexer indexer;

    public Searcher(String query, String stopWordsPath, String queryFilePath, boolean semantics) {
        this.parse = new Parse(false, stopWordsPath, true);
        this.queryText = query;
        this.stopWordsPath = stopWordsPath + "stop_words.txt";
        this.semantics = semantics;
        this.queryFilePath = queryFilePath;
        indexQuery = 1;
        this.queryFilePath = queryFilePath;
        queriesToSearchAndRank = new LinkedHashMap<>();
        relevantDocsForAll = new LinkedList<>();
        //indexer = Indexer.
    }

    public void processQuery() {

        Query query;
        String[] queryTextSplit = new String[1000]; //////////???????????
        Document doc;
        querySet = new LinkedList<>();


        if (queryText != null) { //only string. inputtt

            if(semantics)
                queryText = queryText + " " + addSemanticWords(queryText);

            queryTextSplit = queryText.split(" ");
            StringBuilder queryToProcess = new StringBuilder();
            int i = 0;
            while (i < queryTextSplit.length) {
                //queryTextSplit[i] = removeDelimiters(queryTextSplit[i]);
                queryTextSplit[i] = queryTextSplit[i].toLowerCase();
                queryToProcess.append(" ").append(queryTextSplit[i]); //adds to SB
                i++;
            }
            queryText = queryToProcess.toString(); //after all removals
            queryText = queryText.substring(1); //remove first extra blank.
            query = new Query(queryText);
            //createQueryDoc();
            doc = new Document();
            doc.setId("query" + indexQuery);
            doc.setText(queryText);
            querySet.add(doc);
            LinkedList newList = new LinkedList(querySet);
            querySet.clear();
            queryAfterParse = parse.parseDocs(newList);

            doc = queryAfterParse.poll(); //only one document in this return list.
            String[] toRank = prepareToRank(doc); //prepare String[] of query words to rank.

            queriesToSearchAndRank.put("query" + indexQuery, toRank);
        }

        else { // queries text file !!
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

        Ranker ranker = new Ranker(null); ////////////////// ????????????????????

  /*      // puts all the query terms into an array for rank
        HashMap<String, int[]> queryDic = query.getQueryTermDic();
        String[] queryTerms = new String[queryDic.size()];
        int i = 0;
        for (String queryWord : queryDic.keySet()) {
            queryTerms[i] = queryWord;
            i++;
        }*/

        relevantDocsForAll = ranker.rankAllQueries(queriesToSearchAndRank);
    }

    private String[] prepareToRank(Document doc) {
        if (doc != null) {
            String[] allTermsInQuery;
            String allText = doc.getTermDicAsString();
            allText = allText.substring(1);
            allTermsInQuery = allText.split(" ");
            return allTermsInQuery;
        }
        return null;
    }

    private void readQueryFile() {

        File queriesFile = new File(queryFilePath);
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
                int idQueryyy;
                String restOfQueries = allLinesInQueries.toString();
                //String id = allLinesInQueries.substring(allLinesInQueries.indexOf("<num>") + 2, endIndexId).trim();
                int startInd = allLinesInQueries.indexOf("<title>");
                Document[] allQueries = new Document[30];
                int numOfQueries = 0;

                while (startInd != -1) {
                    numOfQueries++;
                    idQueryyy = restOfQueries.indexOf(":")+2; //index!!! of Query number ID
                    idQuery = restOfQueries.substring(idQueryyy, restOfQueries.indexOf("<title>")-2); //number itself.
                    int endInd = restOfQueries.indexOf("<desc>", startInd)-5; //searches for "<desc>" from starts index
                    String currQuery = restOfQueries.substring(startInd+8, endInd); //query itself.

                    int endQuery = restOfQueries.indexOf("</top>", endInd);
                    restOfQueries = restOfQueries.substring(endQuery);

                    //set Id Query <num>"
                    if (semantics)
                        currQuery = currQuery + " " + addSemanticWords(currQuery);

                    allQueries[numOfQueries] = new Document();
                    allQueries[numOfQueries].setId(idQuery);
                    allQueries[numOfQueries].setText(currQuery);
                    querySet.add(allQueries[numOfQueries]);

                    startInd = restOfQueries.indexOf("<title>"); //continues to the next doc in file
                }

                //allLinesInQueries = new StringBuilder(); //initialize
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

    public void test(){
        processQuery();
    }

    public Queue<String> getRelevantDocs() {
        return relevantDocs;
    }

    private String addSemantics(String queryTerms) {
        String[] querySplitted = queryTerms.split(" ");
        StringBuilder semanticQuery = new StringBuilder();
        int i = 0;
        while (i < querySplitted.length) {
            querySplitted[i] = removeDelimiters(querySplitted[i]);
            String result = addSemanticWords(querySplitted[i]);

            if (result != null && !result.equals(""))
                semanticQuery.append(result); //2 more words.
            else
                semanticQuery.append(querySplitted[i]); //word itself. no changes.
            i++;
        }
        return querySplitted.toString();
    }

    public String addSemanticWords(String terms) {

        StringBuilder allWords = new StringBuilder();
        allWords.append(terms); //word itself.
        URL address;

        String[] termsSplit = terms.split(" "); //if it's more than 1 word.
        String toCheck = "";
        for (int i = 0; i < termsSplit.length; i++) {
            if (i != termsSplit.length - 1)
                toCheck = toCheck + termsSplit[i] + "+";
            else //last word.
                toCheck = toCheck + termsSplit[i];
        }
        try {
            address = new URL("https://api.datamuse.com/words?ml=" + toCheck);
            StringBuilder sb = new StringBuilder("{\"result\":");

            BufferedReader in = new BufferedReader(new InputStreamReader(address.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
            in.close();

            String allSynonyms = sb.toString();
            allSynonyms = allSynonyms.replaceAll(":|\\,|\\[|\\]|\\{|\\}"," ");
            allSynonyms = allSynonyms.replaceAll("\"", "");
            allSynonyms = allSynonyms.replaceAll("  ", " ");

            String toReturn = "";
            String[] toChoose = allSynonyms.split(" ");

            int startInd = allSynonyms.indexOf("word")+5;
            int only3words = 0;

                for (int i = 0; i < toChoose.length; i++) {
                    if (toChoose[i].equals("word")) {
                        toReturn = toReturn + " " + toChoose[i + 1];
                        allSynonyms = allSynonyms.substring(allSynonyms.indexOf("word") + 4);
                        toChoose = allSynonyms.split(" "); //again
                        only3words++;
                        i=0;
                        if (only3words == 3)
                            break;
                    }
                }

            return toReturn;

        } catch (IOException e) {
            return null;
        }
    }
}
