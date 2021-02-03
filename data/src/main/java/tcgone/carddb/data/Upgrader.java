package tcgone.carddb.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import tcgone.carddb.model.Set;
import tcgone.carddb.model.SetFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 *     1. read everything then check version field. if at least one version is not up to date;
 *       1. call upgrade methods in order, until the final one, for every stale object
 *       2. call setwriter and save the object to src/main/resources/cards
 *       3. exit with error. developer will restart the process.
 *     2. read convert_pio_to_yaml/*.json
 *       1. convert all files into yaml by using setwriter struct. it can save with an aux method. it will call upgrade() for every output
 *       2. delete the input files
 *       3. exit with error. developer will restart the process.
 *     3. read convert_yaml_to_impl/*.yaml
 *       1. convert all files into implementation templates via impltmplgenerator.
 *       2. delete the input files
 *       3. exit with error. developer will move the output files into tcgone-engine repository.
 *     4. read download_scans/*
 *       1. download all scans of given files (which format?)
 *       2. delete the input files
 *       3. exit with error. developer will upload the output files onto image server.
 *
 */

public class Upgrader {

  ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
  PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
  SetWriter setWriter = new SetWriter();
  public Upgrader() throws Exception {
    upgradeE1E2();
  }

  /**
   * @return whether an upgrade has been done
   * @throws IOException when there is an issue while upgrading it
   */
  boolean upgradeE1E2() throws IOException {
    // read set files
    Resource[] resources = resourceResolver.getResources("classpath:/cards/*.yaml");
    boolean flag=false;
    List<Set> sets=new ArrayList<>();
    for (Resource resource : resources) {
      flag=true;
      SetFile s1 = mapper.readValue(resource.getInputStream(), SetFile.class);
      s1.set.filename=resource.getFilename();
      s1.set.cards=s1.cards;
      s1.set.schema="E2";//EXPANSIONS2
      sets.add(s1.set);
    }
    //SAVENOW
    if (sets.size()>0) {
      setWriter.writeAll(sets,"output");
      System.out.println("EXPORTED "+sets.size()+" SETS");
    }
    return flag;
  }


  public static void main(String[] args) throws Exception {
    new Upgrader();
  }
}
