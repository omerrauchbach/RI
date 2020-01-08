package Part_1;


import sun.awt.Mutex;

import java.util.*;
import java.io.File;

import java.util.concurrent.*;


public class Indexer{

    public  static HashMap<String, int[]> allDocuments = new HashMap<>(); // 0 - maxTF, 1- #uniqueTerms, 2- length of doc
    public static  HashMap<String , int[]> termDic = new HashMap<>();// 0 - #docs, 1- #showsTotal, 2- line in posting
    public static   String filePath ; //get it from parse. !
    HashMap<String, int[]> littleDic = new HashMap<>() ;
    HashMap<String, String> ChunkTermDicDocs = new HashMap<>();
    ExecutorService poolIndexer =  Executors.newFixedThreadPool(500);
    Writer wr  ;
    private int indexIndexer = 0;
    Mutex chuncklock = new Mutex();


    public Indexer(boolean stemming ,String postingPath ){


        if(stemming)
            filePath = postingPath + "\\stemming";
        else
            filePath = postingPath + "\\nonStemming";

        new File(filePath).mkdir();
        wr = new Writer(filePath);
    }


    public void indexAll(LinkedList<Document> listDoc) {

        String allInfoOfTermForPosting = "";
        while (!listDoc.isEmpty()) {

            Document currDoc = listDoc.poll();
            int[] docInfo = new int[3];

            /////handle tmpDicDoc /////////////
            docInfo[0] = currDoc.getTfMax();
            docInfo[1] = currDoc.uniqueTerm(); //how many unique terms.
            docInfo[2] = currDoc.getLength();
            allDocuments.put(currDoc.getId(), docInfo); //adds current doc to docs dic.

            for (Map.Entry<String, int[]> entry : currDoc.termDic.entrySet()) {

                int[] currTermInfo = entry.getValue();
                String key = entry.getKey();
                boolean notNum = !key.matches(".\\d.");

                if (notNum && !key.chars().anyMatch(Character::isUpperCase) && littleDic.containsKey(key.toUpperCase())) {
                    littleDic.put(key, littleDic.remove(key.toUpperCase()));
                }

                if (littleDic.containsKey(key)) {

                    int[] savedTermData = littleDic.get(key);
                    int[] updateTermInfo = new int[2];
                    updateTermInfo[0] = savedTermData[0] + 1; // adds 1 to curr # of docs
                    updateTermInfo[1] = savedTermData[1] + currTermInfo[0]; //#shows total == adds num of appearences in specific doc. !!!
                    littleDic.replace(key, updateTermInfo); //replaces values in little dic

                    allInfoOfTermForPosting = ChunkTermDicDocs.get(key) + "|" + currDoc.getId() + ":" + currTermInfo[0] + ";" + currDoc.getPlaces(key);
                    chuncklock.lock();
                    ChunkTermDicDocs.replace(key, allInfoOfTermForPosting); // updates info for posting.
                    chuncklock.unlock();

                } else { //first time of this term in chunk.

                    int[] termInfo = new int[2];
                    termInfo[0] = 1; // first doc in list
                    termInfo[1] = currTermInfo[0]; //num of appearances in specific doc. !!!
                    littleDic.put(key, termInfo); //first doc in list, for posting!
                    indexIndexer++;

                    allInfoOfTermForPosting = currDoc.getId() + ":" + termInfo[1] + ";" + currDoc.getPlaces(key);
                    chuncklock.lock();
                    ChunkTermDicDocs.put(key, allInfoOfTermForPosting); //info for posting.
                    chuncklock.unlock();

                    if(indexIndexer % 5000 == 0){

                        HashMap<String, int[]> littleDicSend = new HashMap<>(littleDic) ;
                        littleDic.clear();
                        chuncklock.lock();
                        HashMap<String, String> ChunkTermDicDocsSend = new HashMap<>(ChunkTermDicDocs);
                        ChunkTermDicDocs.clear();
                        chuncklock.unlock();
                        updateTermDic(littleDicSend);
                        littleDicSend.clear();
                        //System.out.println("indexer out: "+indexIndexer);
                        try {
                            poolIndexer.execute(() -> {
                                wr.createPostingFile(ChunkTermDicDocsSend);
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    private void updateTermDic(HashMap<String, int[]> littleDic){

        for(Map.Entry<String, int[]> entry : littleDic.entrySet()){

            String key = entry.getKey();
            int[] value = entry.getValue();
            if(Character.isDigit(key.charAt(0))){

                if(termDic.containsKey(key)) {
                    int[] savedTermData;
                    savedTermData = termDic.get(key);
                    int[] updateTermInfo = new int[3];
                    updateTermInfo[0] = savedTermData[0] + value[0]; // adds 1 to curr # of docs
                    updateTermInfo[1] = savedTermData[1] + value[1]; //#shows total == adds num of appearences in specific doc. !!!
                    termDic.replace(key, updateTermInfo);
                }else{
                    termDic.put(key,value);
                }
            }
            else if(termDic.containsKey(key)){
                int[] savedTermData;
                savedTermData = termDic.get(key);
                int[] updateTermInfo = new int[3];
                updateTermInfo[0] = savedTermData[0] + value[0]; // adds 1 to curr # of docs
                updateTermInfo[1] = savedTermData[1] + value[1]; //#shows total == adds num of appearences in specific doc. !!!
                termDic.replace(key, updateTermInfo);

            }
            else if(termDic.containsKey(key.toUpperCase()))
            {
                int[] savedTermData = termDic.get(key.toUpperCase());
                int[] updateTermInfo = new int[3];
                updateTermInfo[0] = savedTermData[0] + value[0]; // adds 1 to curr # of docs
                updateTermInfo[1] = savedTermData[1] + value[1]; //#shows total == adds num of appearences in specific doc. !!!
                termDic.replace(key, updateTermInfo);
            }else if( termDic.containsKey(key.toLowerCase())){

                int[] savedTermData = termDic.get(key.toLowerCase());
                int[] updateTermInfo = new int[3];
                updateTermInfo[0] = savedTermData[0] + value[0]; // adds 1 to curr # of docs
                updateTermInfo[1] = savedTermData[1] + value[1]; //#shows total == adds num of appearences in specific doc. !!!
                termDic.replace(key.toLowerCase(), updateTermInfo);

            }else{
                termDic.put(key,value);
            }
        }
    }

    private TreeMap<String , int[]> newTree(){

        return new TreeMap<>(new Comparator<String>(){

            @Override
            public int compare(String s1, String s2) {
                int result = s1.compareToIgnoreCase(s2);
                if( result == 0 )
                    result = s1.compareTo(s2);
                return result;
            }
        });
    }

    public void shutDown(){

        poolIndexer.shutdown();

        try {
            poolIndexer.awaitTermination(30, TimeUnit.MINUTES);
            wr.createPostingFile(ChunkTermDicDocs);
            updateTermDic(littleDic);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}