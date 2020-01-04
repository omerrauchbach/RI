package Part_2;

import Part_1.Parse;
import Part_1.Indexer;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class Searcher implements Runnable {


    private String queryText;
    private Query query;
    private String stopWordsPath;
    private boolean semantics;
    private Queue<String> relevantDocs;
    private Queue<String> queryAfterParse;

    //private Indexer indexer;

    public Searcher(String query, String stopWordsPath, boolean semantics) {
        this.queryText = query;
        this.stopWordsPath = stopWordsPath;
        this.semantics = semantics;
        //indexer = Indexer.
    }

    public void processQuery() {

        Query query;
        String[] queryTextSplit = new String[1000]; //////////???????????

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

        } else { // queries text file !! ????????????????????????
            query = new Query("");
        }

        Parse parse = new Parse(false, stopWordsPath, true); /////????
        parse.parseQuery(queryText);
        Ranker ranker = new Ranker();

        // turn all the query terms into an array for ranking
        HashMap<String, int[]> queryDic = query.getQueryTermDic();
        String[] queryTerms = new String[queryDic.size()];
        int i = 0;
        for (String queryWord : queryDic.keySet()) {
            queryTerms[i] = queryWord;
            i++;
        }

        relevantDocs = ranker.rank(queryTerms);
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
