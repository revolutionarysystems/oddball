/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.aggregator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Andrew
 */
public class LevelledCollationAccumulator implements PropertyAccumulator, Comparator{
    
    Map<String, CollationEntry> collation;
    String separator = ".";

    public LevelledCollationAccumulator() {
        this.collation = new HashMap<String, CollationEntry>();
    }
                
    @Override
    public int compare(Object o1, Object o2){
        return ((Integer)((HashMap)o2).get("count"))-((Integer)((HashMap)o1).get("count"));
//        return ((String)((HashMap)o2).get("value")).compareTo((String)((HashMap)o1).get("value"));
    }

    
    private void incorporate(String property, Map<String, CollationEntry> coll, String separator, boolean reverse){
        String trunk = property;
        String branch = "";
        if (property.contains(separator)){
            if (!reverse){
                branch = property.substring(property.indexOf(separator)+1);
                trunk = property.substring(0, property.indexOf(separator));
            } else {
                branch = property.substring(0, property.lastIndexOf(separator));
                trunk = property.substring(property.lastIndexOf(separator)+1);
            }
                    
        }
        CollationEntry ce = coll.get(trunk);
        if (ce==null){
            ce =  new CollationEntry();
            coll.put(trunk,ce);
        }
        ce.incrementCount();
        Map<String, CollationEntry> subColl = ce.getSubCollations();
//        System.out.println(prefix);
//        System.out.println(remainder);
        if (branch.length()>0){
            incorporate(branch, subColl, separator, reverse);
        }
    }
    
    @Override
    public void accumulateProperty(Object property){
        if (property!=null){
            property=(property.toString()).replace("\"","");
            String separator = ".";
            if (options.containsKey("separator")){
                separator = options.get("separator");
            }
            incorporate((String) property, collation, separator, options.containsKey("order")&&options.get("order").equals("reverse"));
        }
//        System.out.println("collation");
//        System.out.println(collation.toString());
    }

//    public void removeProperty(Object property){
//        if (property!=null){
//            property=((String)property).replace("\"","");
//        }
//        if (collation.containsKey(property)){
//            if ((Integer)collation.get(property)>1){
//                collation.put(property, ((Integer)collation.get(property))-1);
//            } else {
//                collation.remove(property);
//            }
//        }
//    }

    
    @Override
    public Map assessProperty(Object property){
        Map<Object, Object> results = new HashMap<Object, Object>();
        int total = 0;
        for (Object item:collation.keySet()){
            total+=(Integer)collation.get(item).getCount();
        }
        results.put("value", property);
        String separator = ".";
        if (options.containsKey("separator")){
            separator = options.get("separator");
        }
        String[] propParts = ((String)property).split(separator.replace(".", "\\."));
        
        Map<String, CollationEntry> subColl = collation;
        String prefix = "";
        for (String propPart: propParts){
            Map<String, Object> match = new HashMap<String, Object>();
            if (subColl.containsKey(propPart)){
                match.put("count", subColl.get(propPart).getCount());
                match.put("proportion", ((Integer)subColl.get(propPart).getCount())/(float)total);
                subColl = subColl.get(propPart).getSubCollations();
                prefix=prefix+propPart+separator;
            } else {
                match.put("count", 0);
                match.put("proportion", (float)0.0);
                break;
            }
            results.put(prefix.substring(0, prefix.length()-1), match);
        }
        
//        if (collation.containsKey(property)){
//            results.put("count", collation.get(property));
//            results.put("proportion", ((Integer)collation.get(property))/(float)total);
//        } else {
//            results.put("count", 0);
//            results.put("proportion", (float)0.0);
//        }
        return results;
    }

    
    private Map recursiveResults(Map<String, CollationEntry> collation){
        Map<Object, Object> results = new HashMap<Object, Object>();
        ArrayList<Map<String, Object>> innerResults = new ArrayList<Map<String, Object>> ();
        int total= 0;
        double totalInfo = 0;
        
        for (Object item:collation.keySet()){
            Map<String, Object> cell = new HashMap<String, Object>();
            cell.put("value", item);
            cell.put("count", collation.get(item).getCount());
            total+=(Integer)collation.get(item).getCount();
            innerResults.add(cell);
            Map<String, CollationEntry> subColls = recursiveResults(collation.get(item).getSubCollations());
            cell.put("distribution", subColls.get("distribution"));
            cell.put("information", subColls.get("information"));
        }
        Collections.sort(innerResults, this);
        if (innerResults.size()>0){
            int maxCount = (Integer) innerResults.get(0).get("count");
            for (Map<String, Object> entry: innerResults){
                entry.put("relFreq", ((Integer)entry.get("count")*1.0)/maxCount);
                double absFreq = ((Integer)entry.get("count")*1.0)/total;
                entry.put("absFreq", absFreq);
                double info = -Math.log(absFreq)/Math.log(2) * absFreq;
                totalInfo+=info;
                entry.put("info", -Math.log(absFreq)/Math.log(2));
            }
        }
        results.put("distribution", innerResults);
        results.put("information", totalInfo);
        return results;
    }
    
    @Override
    public Map readOffResults(){
        return recursiveResults(collation);
//        Map<Object, Object> results = new HashMap<Object, Object>();
//        ArrayList<Map<String, Object>> innerResults = new ArrayList<Map<String, Object>> ();
//        int total= 0;
//        for (Object item:collation.keySet()){
//            Map<String, Object> cell = new HashMap<String, Object>();
//            cell.put("value", item);
//            cell.put("count", collation.get(item).getCount());
//            total+=(Integer)collation.get(item).getCount();
//            innerResults.add(cell);
//        }
//        Collections.sort(innerResults, this);
//        if (innerResults.size()>0){
//            int maxCount = (Integer) innerResults.get(0).get("count");
//            for (Map<String, Object> entry: innerResults){
//                entry.put("relFreq", ((Integer)entry.get("count")*1.0)/maxCount);
//                double absFreq = ((Integer)entry.get("count")*1.0)/total;
//                entry.put("absFreq", absFreq);
//            }
//        }
//        results.put("distribution", innerResults);
//        return results;
    }
    
    private Map<String,String> options;
    public void setOptions(Map<String, String> options){
        this.options = options;
    }
    
//    int totalCount= 0;
//    double totalInfo= 0;
    Map<String, Double> itemInfo = new HashMap<String, Double>();


    
}
