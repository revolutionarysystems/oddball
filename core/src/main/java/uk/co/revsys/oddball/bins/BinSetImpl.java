package uk.co.revsys.oddball.bins;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import uk.co.revsys.resource.repository.ResourceRepository;
import uk.co.revsys.resource.repository.model.Resource;

/*
 * @author Andrew
 */
public class BinSetImpl implements BinSet {

    Map<String, Bin> bins = new HashMap<String, Bin>();

    private String name;
    private String binType;

    @Override
    public void addBin(Bin bin) {
        bins.put(bin.getLabel(), bin);
    }

    @Override
    public Map<String, Bin> getBins() {
        return bins;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * @return the binType
     */
    @Override
    public String getBinType() {
        return binType;
    }

    /**
     * @param binType the binType to set
     */
    @Override
    public void setBinType(String binType) {
        this.binType = binType;
    }

    public static BinSet loadBinSet(String binSetName, ResourceRepository resourceRepository) throws BinSetNotLoadedException {
        try {
            Resource resource = new Resource("", binSetName);
            InputStream inputStream = resourceRepository.read(resource);
            List<String> bins = IOUtils.readLines(inputStream);
            IOUtils.closeQuietly(inputStream);
            BinSet binSet = new BinSetImpl();
            binSet.setName(binSetName);
            for (String binStr : bins) {
                String[] parsed = binStr.trim().split(":", 2);
                if (parsed.length != 2) {
                    break;
                }
                Bin bin = new BinImpl();
                bin.setLabel(parsed[0]);
                bin.setBinString(parsed[1], resourceRepository);
                binSet.addBin(bin);
            }
            return binSet;
        } catch (IOException ex) {
            throw new BinSetNotLoadedException(binSetName, ex);
        }
    }

    @Override
    public Collection listBinLabels() {
        return bins.keySet();
    }

    /**
     * @param name the name to set
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

}
