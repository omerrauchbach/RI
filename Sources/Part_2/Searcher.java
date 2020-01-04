package Part_2;

import Part_1.Parse;
import Part_1.Indexer;


import java.util.HashSet;
import java.util.Queue;

public class Searcher {


    private String query;
    private String stopWordsPath;
    private boolean semantics;
    private Queue<String> relevantDocuments;

    public Searcher(String query, String stopWordsPath, boolean semantics) {
        this.query = query;
        this.stopWordsPath = stopWordsPath;
        this.semantics = semantics;
    }

    public void processQuery(){
        Query query;
        Parse parse = new Parse(stopWordsPath, true, Query Indexer.isDictionaryStemmed); /////
        parse.parseDocs();
        Ranker ranker = new Ranker();

        // turn all the query terms in to an array for ranking
        HashMap<String, int[]> queryDictionary = queryObject.getQueryTermDictionary();
        String[] queryTerms = new String[queryDictionary.size()];
        Set<String> queryKeys = queryDictionary.keySet();
        int i = 0;
        for (String query : queryKeys) {
            queryTerms[i] = query;
            i++;
        }

        relevantDocuments = ranker.rank(queryTerms);


    }
}
