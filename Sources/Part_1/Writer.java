package Part_1;

import Controller.Controller;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

import Part_1.Indexer;

public class Writer {

    private final String path ;
    private StringBuilder allTermsInPostingFile;
    public static int counterTest =0;

    public Writer(String path) {

        allTermsInPostingFile = new StringBuilder();
        this.path = path;

        File adFile = new File(path + "\\a-d.txt");
        File eiFile = new File(path + "\\e-i.txt");
        File jmFile = new File(path + "\\j-m.txt");
        File nqFile = new File(path + "\\n-q.txt");
        File rvFile = new File(path + "\\r-v.txt");
        File wzFile = new File(path + "\\w-z.txt");
        File nonLetterFile = new File(path + "\\nonLetter.txt");

        try {

            if (!adFile.exists()) {
                adFile.createNewFile();
            }

            if (!eiFile.exists()) {
                eiFile.createNewFile();
            }

            if (!jmFile.exists()) {
                jmFile.createNewFile();
            }

            if (!nqFile.exists()) {
                nqFile.createNewFile();
            }

            if (!rvFile.exists()) {
                rvFile.createNewFile();
            }
            if (!wzFile.exists()) {
                wzFile.createNewFile();
            }
            if (!rvFile.exists()) {
                nonLetterFile.createNewFile();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void createPostingFile( HashMap<String, String> ChunkTermDicDocs) {

        String ad = "" ;
        String ei= ""  ;
        String jm = "" ;
        String nq= "" ;
        String rv= "" ;
        String wz= "" ;
        String nonLetter = "" ;


        for(Map.Entry<String,String> entry : ChunkTermDicDocs.entrySet()){
            String value = entry.getValue();
            String key = entry.getKey();
            char charOfKey = key.charAt(0);


            if ((charOfKey>='a' && charOfKey<='d') || (charOfKey>='A' && charOfKey<='D'))
                ad = ad+key + "|" + value + "\n";
            else if ((charOfKey>='e' && charOfKey<='i') ||( charOfKey>='E' && charOfKey<='I'))
                ei = ei+key + "|" + value + "\n";
            else if ((charOfKey>='j' && charOfKey<='m') || (charOfKey>='J' && charOfKey<='M'))
                jm = jm+key + "|" + value + "\n";
            else if ((charOfKey>='n' && charOfKey<='q') || (charOfKey>='N' && charOfKey<='Q'))
                nq = nq+key + "|" + value + "\n";
            else if ((charOfKey>='r' && charOfKey<='v') || (charOfKey>='R' && charOfKey<='V'))
                rv = rv+key + "|" + value + "\n";
            else if ((charOfKey>='w' && charOfKey<='z') || (charOfKey>='W' && charOfKey<='Z'))
                wz = wz+key + "|" + value + "\n";
            else if(!(key.substring(0,1)).matches(".[a-zA-Z].")) {
                nonLetter = nonLetter + key + "|" + value + "\n";
            }
        }
        ChunkTermDicDocs.clear();

        try {
            PrintWriter out ;
            if (!ad.equals("")) {
                out = new PrintWriter(new FileWriter(path + "\\a-d.txt", true));
                out.append(ad);
                out.close();
            }
            if (!ei.equals("")) {
                out = new PrintWriter(new FileWriter(path + "\\e-i.txt", true));
                out.append(ei);
                out.close();
            }
            if (!jm.equals("")) {
                out = new PrintWriter(new FileWriter(path + "\\j-m.txt", true));
                out.append(jm);
                out.close();
            }
            if (!nq.equals("")) {
                out = new PrintWriter(new FileWriter(path + "\\n-q.txt", true));
                out.append(nq);
                out.close();
            }
            if (!rv.equals("")) {
                out = new PrintWriter(new FileWriter(path + "\\r-v.txt", true));
                out.append(rv);
                out.close();
            }
            if (!wz.equals("")) {
                out = new PrintWriter(new FileWriter(path + "\\w-z.txt", true));
                out.append(wz);
                out.close();
            }
            if (!nonLetter.equals("")) {
                out = new PrintWriter(new FileWriter(path + "\\nonLetter.txt", true));
                out.append(nonLetter);
                out.close();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        //System.out.println("Wrote this cuhnk to disk." + counterTest);
        counterTest++;

    }

    public void sortPostingFiles(){

        File dir = new File(path);
        File[] dirFiles = dir.listFiles(); //max - all 7 posting files.
        String name="";
        if (dirFiles != null) {
            for (File fileInDir : dirFiles) {
                name = fileInDir.getName();
                HashMap<String , String>  terms = new HashMap<>();
                if (fileInDir != null) {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileInDir)));
                        for (String currTermInfo; (currTermInfo = br.readLine()) != null; ) {

                            try {
                                currTermInfo.substring(0, currTermInfo.indexOf("|"));
                            }
                            catch (Exception e){
                                continue;
                            }
                            String key = currTermInfo.substring(0, currTermInfo.indexOf("|"));
                            if(terms.containsKey(key)){
                                terms.replace(key ,terms.get(key)+"|"+currTermInfo.substring(currTermInfo.indexOf("|")+1));
                            }else if(terms.containsKey(key.toUpperCase())){
                                String value = terms.remove(key.toUpperCase());
                                terms.put(key ,currTermInfo+"|"+value.substring(value.indexOf("|")+1));
                            }else if(terms.containsKey(key.toLowerCase())){
                                terms.replace(key.toLowerCase() ,terms.get(key.toLowerCase())+"|"+currTermInfo.substring(currTermInfo.indexOf("|")+1));
                            }else{
                                terms.put(key , currTermInfo);
                            }
                        }

                        File newPostFile = new File(path + "\\" + name );
                        PrintWriter out = new PrintWriter(newPostFile);
                        for(String line : terms.values()) //write all to a new file.
                            out.append(line +"\n");
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } //finish gets the whole file.
            }
        }
    }
}