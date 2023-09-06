/**
 * 
 */
package org.aksw.defacto.config;

import org.ini4j.Ini;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefactoConfig {

	private Ini defactoConfig;
    
    public static String DEFACTO_DATA_DIR;

    public DefactoConfig(Ini config) {
        System.out.println("ini size : "+config.size());
        this.defactoConfig =  config;
        DEFACTO_DATA_DIR = this.defactoConfig.get("eval", "data-directory");
    }
    
    /**
     * returns boolean values from the config file
     * 
     * @param section
     * @param key
     * @return
     */
    public boolean getBooleanSetting(String section, String key) {
        
        return Boolean.valueOf(defactoConfig.get(section, key));
    }
    
    /**
     * returns string values from defacto config
     * 
     * @param section
     * @param key
     * @return
     */
    public String getStringSetting(String section, String key) {
        
        return defactoConfig.get(section, key);
    }

    /**
     * this should overwrite a config setting, TODO make sure that it does
     * 
     * @param section
     * @param value
     */
    public void setStringSetting(String section, String key, String value) {

        this.defactoConfig.put(section, key, value);
    }

    /**
     * returns integer values for defacto setting
     * 
     * @param section
     * @param key
     * @return
     */
    public Integer getIntegerSetting(String section, String key) {

        return Integer.valueOf(this.defactoConfig.get(section, key));
    }

    /**
     * returns double values from the config
     * 
     * @param section
     * @param key
     * @return
     */
    public Double getDoubleSetting(String section, String key) {

        return Double.valueOf(this.defactoConfig.get(section, key));
    }

    /**
     * returns array values from the config
     * the value should be with space spited
     * @param key
     * @return
     */
    public String[] getArray(String section,String key){
        String ll = this.defactoConfig.get(section,key);
        String[] results = ll.split(" ");
        return results;
    }
}
