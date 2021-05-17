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

        if(input.equals("starring")){
            returnVal = Arrays.asList("starring","start","prima","acted in","played in","Has played","played a role");
        }

        if(input.equals("almaMater")){
            returnVal = Arrays.asList("seminary","organization","lyceum","halls of knowledge","halls of ivy","brainery","association","alma Mater","graduated From","institute");

        }

        if(input.equals("imports")){
            returnVal = Arrays.asList("imports","import","Importation");
        }
        if(input.equals("gender")){
            returnVal = Arrays.asList("gender","has gender","sex","sexuality","is");
        }

        if(input.equals("instrument")){
            returnVal = Arrays.asList("instrument","has Musical Role");
        }

        if(input.equals("location")){
            returnVal = Arrays.asList("location","is located","is located in","placed","placed in");
        }

        if(input.equals("editing")){
            returnVal = Arrays.asList("editing","edited");
        }

        if(input.equals("combatant")){
            returnVal = Arrays.asList("combatant","participatedIn");
        }

        if(input.equals("currency")){
            returnVal = Arrays.asList("hasCurrency","currency");
        }

        if(input.equals("influences")){
            returnVal = Arrays.asList("influences","dependence","attachment","cohesion","attachment");
        }

        if(input.equals("workplaces")){
            returnVal = Arrays.asList("workplaces","work at");
        }

        if(input.equals("doctoralAdvisor")){
            returnVal = Arrays.asList("doctoral Advisor","has Academic Advisor");
        }

        if(input.equals("language")){
            returnVal = Arrays.asList("language","has Official Language");
        }

        if(input.equals("office")){
            returnVal = Arrays.asList("office","is Politician Of");
        }

        if(input.equals("capital")){
            returnVal = Arrays.asList("capital","has Capital");
        }

        if(input.equals("knownFor")){
            returnVal = Arrays.asList("known For","is Known For");
        }

        if(input.equals("child")){
            returnVal = Arrays.asList("child","hasChild");
        }

        if(input.equals("nationality")){
            returnVal = Arrays.asList("nationality","is Citizen Of");
        }

        if(input.equals("website")){
            returnVal = Arrays.asList("website","has Website");
        }

        if(input.equals("affiliation")){
            returnVal = Arrays.asList("affiliation","Affiliated","play","part","member","federating","drive","ride");
        }

        if(input.equals("director")){
            returnVal = Arrays.asList("director","directed");
        }

        if(input.equals("team")){
            returnVal = Arrays.asList("team","play","playing","join","part","member","federating","drive","ride");
        }

        if(input.equals("writer")){
            returnVal = Arrays.asList("writer","created","by","write","wrote","writing","creat");
        }

        if(input.equals("leader")){
            returnVal = Arrays.asList("leader","is Leader Of");
        }

        if(input.equals("mainInterests")){
            returnVal = Arrays.asList("mainInterests","main Interests");
        }

        if(input.equals("exports")){
            returnVal = Arrays.asList("exports");
        }

        if(input.equals("owner")){
            returnVal = Arrays.asList("owns","owner");
        }

        if(input.equals("spouse")){
            returnVal = Arrays.asList("spouse","is Married To");
        }

        if(input.equals("place")){
            returnVal = Arrays.asList("place","happenedIn");
        }

        if(input.equals("birthPlace")){
            returnVal = Arrays.asList("birthPlace","birth Place","born","from");
        }

        if(input.equals("musicComposer")){
            returnVal = Arrays.asList("music Composer","composer","wrote Music For");
        }

        if(input.equals("live")){
            returnVal = Arrays.asList("live","live in");
        }

        if(input.equals("deathPlace")){
            returnVal = Arrays.asList("deathPlace","");
        }


        if(returnVal==null){
            returnVal = new ArrayList<>();
            returnVal.add(input);
        }

    return returnVal;
    }
}
