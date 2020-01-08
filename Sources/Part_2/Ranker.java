package Part_2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import Part_1.Document;
import Part_1.Indexer;

public class Ranker {


    String path ;
    int avgLengthOfDoc;
    int numberOfDoc;
    Set<String> setOfDoc ;



    Ranker(String path){
        this.path = path;
        avgLengthOfDoc= getAvgLengthOfDoc();
        setOfDoc = new HashSet<>();
    }

    List<String> rank(Query q){

        Queue<String> ans = new LinkedList<>();
        HashMap queryHashMap = q.getQueryTermDic();
        String[] terms =(String[])queryHashMap.keySet().toArray();
        this.avgLengthOfDoc = getAvgLengthOfDoc();
        this.numberOfDoc = Indexer.allDocuments.size();
        HashMap<String,Double> allDocRank =new HashMap<>();


        LinkedList<HashMap<String,int[][]>> docsData = new LinkedList<>();
        for(int indexQuery = 0 ;  indexQuery < terms.length ; indexQuery++) {
            String term = terms[indexQuery];
            docsData.push(getDataForTerm(term));
        }


        LinkedList<int[][]> valuesList = new LinkedList<>();
       for(String docId : setOfDoc ){
           for(int indexQuery = 0 ;  indexQuery < queryHashMap.size() ; indexQuery++){
               HashMap<String,int[][]> dataForQuery =  docsData.get(indexQuery);
               valuesList.push(dataForQuery.get(docId));
           }
           Double rankedDoc =rankQueryDoc(valuesList);
           allDocRank.put(docId,rankedDoc);
       }


        allDocRank.entrySet().stream().sorted((k1, k2) -> -k1.getValue().compareTo(k2.getValue()));

       int index =0;
       List<String> topValues = new LinkedList<>();
       for(Map.Entry<String,Double> entry : allDocRank.entrySet() ){
           if(index == 50)
               break;
           else
               topValues.add(entry.getKey());

       }


        return topValues;

    }


    String getFilePath(String term) {


        char charOfKey = term.charAt(0);


        if ((charOfKey >= 'a' && charOfKey <= 'd') || (charOfKey >= 'A' && charOfKey <= 'D'))
            return path + "\\a-d.txt";
        else if ((charOfKey >= 'e' && charOfKey <= 'i') || (charOfKey >= 'E' && charOfKey <= 'I'))
            return path + "\\e-i.txt";
        else if ((charOfKey >= 'j' && charOfKey <= 'm') || (charOfKey >= 'J' && charOfKey <= 'M'))
            return path + "\\j-m.txt";
        else if ((charOfKey >= 'n' && charOfKey <= 'q') || (charOfKey >= 'N' && charOfKey <= 'Q'))
            return path + "\\n-q.txt";
        else if ((charOfKey >= 'r' && charOfKey <= 'v') || (charOfKey >= 'R' && charOfKey <= 'V'))
            return path + "\\r-v.txt";
        else if ((charOfKey >= 'w' && charOfKey <= 'z') || (charOfKey >= 'W' && charOfKey <= 'Z'))
            return path + "\\w-z.txt";
        else if (!(term.substring(0, 1)).matches(".[a-zA-Z]."))
            return path + "\\nonLetter.txt";
        else
            return null;

    }


    HashMap<String,int[][]> getDataForTerm(String term ){

        File file = new File(path);
        HashMap<String,int[][]> docs = new LinkedHashMap<>();
        try{
        BufferedReader myBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        for (String currLine; (currLine = myBufferedReader.readLine()) != null; ){
            int indexTermFromLine = currLine.indexOf("|");
            if(currLine.substring(0,indexTermFromLine).equals(term)) {
                int[] location = null;
                int[] dataTermDoc =null;
                String docId=null;
                String withOutTerm = currLine.substring(currLine.indexOf("|") + 1);
                String[] splitLine = withOutTerm.split("\\|");
                for(String docsLine : splitLine){
                    String[] docSplit = docsLine.split("[:;]+");
                    docId = docSplit[0]; /// doc name
                    setOfDoc.add(docId);
                    dataTermDoc = new int[3];
                    location = getLocation(docSplit[2]);
                    int[] docData = Indexer.allDocuments.get(docId);
                    int[] termData = Indexer.termDic.get(term);
                    dataTermDoc[0] = termData[0]; // # df
                    dataTermDoc[1] = Integer.parseInt(docSplit[1]); //tf
                    dataTermDoc[2] = docData[2]; //docLength
                }

                int[][] pushData = new int[2][];
                pushData[0] = location;
                pushData[1] = dataTermDoc;
                docs.put(docId,pushData);
            }


        }


        }
        catch (Exception e){

        }
        return docs;
    }


    private int getAvgLengthOfDoc(){

        Queue<int[]> allDoc = new LinkedList<>(Indexer.allDocuments.values());
        int sum = 0;
        for(int[] data : allDoc)
            sum+=data[2];
        return sum/allDoc.size();


    }


    private int[] getLocation(String location){

        String[] split = location.split(",");
        int[] afterSplit = new int[split.length];
        int i=0;
        for(String num: split){
            try {
                afterSplit[i] = Integer.parseInt(num);
                i++;
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        return afterSplit;
    }

    /**
     *                         0     1     2      3    4
     * @param data order: numOfDocs,df,avgLength,tf,docLength
     * @return
     */
    private double rankTermDoc(int[] data){

        double b = 0.6;
        double k =2;
        double denominator = data[3] +k*((1-b)+((b*data[4])/data[4]));
        double numerator = (k+1)*data[3];
        double log = Math.log10((data[0]-data[1])/data[1]);
        return (numerator/denominator)*log;
    }

    private double rankQueryDoc(LinkedList<int[][]> dataQuery){

        Double rank = 0.0;
        for(int[][] data : dataQuery ){

            double rankPerTerm = rankTermDoc(data[1]);
            double addToRank = rankPerTerm*0.1;

            ///// location //////

            ////title////



            rank+=rankPerTerm;
        }



        return rank;
    }






}
