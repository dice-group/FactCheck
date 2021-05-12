package org.aksw.defacto.boa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RelativePredicates {
    public List<String> all(String input){
        List<String> returnVal = null;

        //returnVal = Arrays.asList(input,"");

        if(input.equals("award")){
            returnVal = Arrays.asList(input,"shared the","was awarded the","winners","along with","laureate the","winner","received the ","won the","recipient");
        }

        if(input.equals("birthPlace")){
            returnVal = Arrays.asList("birth place","was born in");
        }

        if(input.equals("death place")){
            returnVal = Arrays.asList("death place","");
        }

        if(returnVal==null){
            returnVal = new ArrayList<>();
            returnVal.add(input);
        }

    return returnVal;
    }
}
