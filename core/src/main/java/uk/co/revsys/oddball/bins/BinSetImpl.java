/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.revsys.oddball.bins;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import uk.co.revsys.oddball.bins.*;
import uk.co.revsys.oddball.cases.Case;
import uk.co.revsys.oddball.util.OddballException;
import uk.co.revsys.resource.repository.ResourceRepository;
import uk.co.revsys.resource.repository.model.Resource;

/**
 *
 * @author Andrew
 */
public class BinSetImpl implements BinSet{
    

    Map<String, Bin> bins = new HashMap<String, Bin>();

    private String name;
    private String binType;


    @Override
    public void addBin(Bin bin) {
        bins.put(bin.getLabel(),bin);
    }

   
    public Map<String, Bin> getBins() {
        return bins;
    }

    public String getName() {
        return name;
    }
    
    /**
     * @return the binType
     */
    public String getBinType() {
        return binType;
    }

    /**
     * @param binType the binType to set
     */
    public void setBinType(String binType) {
        this.binType = binType;
    }
    

    public static BinSet loadBinSet(String binSetName, ResourceRepository resourceRepository)throws OddballException{
        try{
            Resource resource = new Resource("", binSetName);
            InputStream inputStream = resourceRepository.read(resource);
            List<String> bins = IOUtils.readLines(inputStream);
            BinSet binSet = new BinSetImpl();
            binSet.setName(binSetName);
            for (String binStr : bins){
                System.out.println("binStr");
                System.out.println(binStr);
                String[] parsed = binStr.trim().split(":",2);
                if (parsed.length!=2){
                    break;
                }
                Bin bin = new BinImpl();
                bin.setLabel(parsed[0]);
                bin.setBinString(parsed[1], resourceRepository);
                binSet.addBin(bin);
            }
            return binSet;
        }
        catch (java.io.FileNotFoundException e){
            throw new OddballException("No Bin Set named "+binSetName+" in repository");
        }
        catch (java.io.IOException e){
            throw new OddballException("Bins could not be loaded");
        }
//        catch (Exception e){
//            e.printStackTrace();
//            throw new OddballException("Bins could not be loaded");
//        }
    }

    public Collection listBinLabels(){
        return bins.keySet();
    }    

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

}
