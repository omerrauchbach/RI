package Part_1;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Document {

    private String id;
    private String  docText;
    private int tfMax;
    public HashMap<String, int[]> termDic ;
    private String title;
    public HashMap<String, String> termPlacesInDoc;


    public Document(){

        termDic = new HashMap<>();
        termPlacesInDoc = new HashMap<>();
    }

    public int uniqueTerm(){
        return termDic.size();
    }

    public int getTfMax() {
        return calMaxTf();
    }

    public String getText() { return docText; }

    public String getPlaces(String term){

        String location = termPlacesInDoc.get(term);
        if(location != null)
            return location;
        else
            return "";
    }

    public String getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.docText = text;
    }

    private int calMaxTf(){

        int Max =0 ;
        for(Map.Entry<String, int[]> entry : termDic.entrySet()){

            if(entry.getValue()[0] > Max)
                Max = entry.getValue()[0];
        }
        tfMax = Max;
        return Max;

    }

    public int getLength(){
        int length =0 ;
        for(Map.Entry<String, int[]> entry : termDic.entrySet()){
            length = length + entry.getValue()[0];
        }
        return length;
    }

    public void clear(){
        this.docText = null;
    }

    @Override
    public String toString(){
        return id;
    }

}
