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
            returnVal = Arrays.asList("instrument","has Musical Role","tool","device");
        }

        if(input.equals("location")){
            returnVal = Arrays.asList("location","located","placed","position","place","site");
        }

        if(input.equals("editing")){
            returnVal = Arrays.asList("editing","edited","edit","redaction");
        }

        if(input.equals("combatant")){
            returnVal = Arrays.asList("combatant","participatedIn","warrior","champion","war","hero");
        }

        if(input.equals("currency")){
            returnVal = Arrays.asList("hasCurrency","currency");
        }

        if(input.equals("influences")){
            returnVal = Arrays.asList("influences","dependence","attachment","cohesion","attachment","effect");
        }

        if(input.equals("workplaces")){
            returnVal = Arrays.asList("workplaces","work at","location","located","placed","position","place","site");
        }

        if(input.equals("doctoralAdvisor")){
            returnVal = Arrays.asList("doctoral Advisor","has Academic Advisor");
        }

        if(input.equals("language")){
            returnVal = Arrays.asList("language","has Official Language","speak","speaking");
        }

        if(input.equals("office")){
            returnVal = Arrays.asList("office","Politician","member","leader");
        }

        if(input.equals("capital")){
            returnVal = Arrays.asList("capital","has Capital");
        }

        if(input.equals("child")){
            returnVal = Arrays.asList("child","has Child","kid","son","daughter","progeny");
        }

        if(input.equals("nationality")){
            returnVal = Arrays.asList("nationality","Citizen","born","citizenship");
        }

        if(input.equals("website")){
            returnVal = Arrays.asList("website","has Website","site");
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
            returnVal = Arrays.asList("leader", "manager","chief", "head","principal","commander","captain","boss","governor");
        }

        if(input.equals("mainInterests")){
            returnVal = Arrays.asList("mainInterests","main Interests");
        }

        if(input.equals("exports")){
            returnVal = Arrays.asList("exports");
        }

        if(input.equals("owner")){
            returnVal = Arrays.asList("owns","owner","has","have");
        }

        if(input.equals("spouse")){
            returnVal = Arrays.asList("spouse","Married","partner","couple","engage");
        }

        if(input.equals("place")){
            returnVal = Arrays.asList("place","happenedIn","at");
        }

        if(input.equals("birthPlace")){
            returnVal = Arrays.asList("birthPlace","birth Place","born","from","grow");
        }


        if(input.equals("musicComposer")){
            returnVal = Arrays.asList("music Composer","composer","wrote Music For","melodist","symphonist","songwriter","songster","writer","tunesmith","songsmith");
        }

        if(input.equals("live")){
            returnVal = Arrays.asList("live","live in");
        }

        if(input.equals("residence")){
            returnVal = Arrays.asList("live","live in","residence","home","address","place","seat");
        }

        if(input.equals("deathPlace")){
            returnVal = Arrays.asList("deathPlace","die","death place","died","passed away","buried","kill","killed","terror","got shot","exhumed");
        }

        if(input.equals("producer")){
            returnVal = Arrays.asList("producer", "manufacturer","maker", "builder","creator","manager","farmer","fabricator");
        }

        if(input.equals("nationality")){
            returnVal = Arrays.asList("nationality", "born","death", "die");
        }

        if(input.equals("knownFor")){
            returnVal = Arrays.asList("known", "work","accepted", "established","noted","acknowledged");
        }

        if(input.equals("workInstitutions")){
            returnVal = Arrays.asList("work", "Institution","institute","place","workplace");
        }

        if(input.equals("officialLanguage")){
            returnVal = Arrays.asList("language","has Official Language","speak","speaking");
        }

        if(input.equals("architect")){
            returnVal = Arrays.asList("designer", "planner", "builder", "draughtsman", "building consultant");
        }

        if(input.equals("artist")){
            returnVal = Arrays.asList("creator", "originator", "designer", "producer", "fine artist", "old master");
        }

        if(input.equals("author")){
            returnVal = Arrays.asList("writer", "wordsmith", "novelist", "dramatist", "playwright", "screenwriter", "scriptwriter", "poet", "essayist", "biographer", "journalist", "columnist");
        }

        if(input.equals("commander")){
            returnVal = Arrays.asList("eader", "head", "boss", "chief", "director", "manager", "controller", "master", "commander-in-chief", "headman");
        }

        if(input.equals("governor")){
            returnVal = Arrays.asList("governor","leader", "manager","chief", "head","principal","commander","captain","boss");
        }

        //productionCompany
        if(input.equals("productionCompany")){
            returnVal = Arrays.asList("production company","manufacture","company","construction","creation");
        }
        //academicDiscipline
        if(input.equals("academicDiscipline")){
            returnVal = Arrays.asList("academic discipline","field of study","study","field","discipline");
        }
        //chancellor
        if(input.equals("chancellor")){
            returnVal = Arrays.asList("chancellor","leader","president","principal","dean","master","chief");
        }
        //city
        if(input.equals("city")){
            returnVal = Arrays.asList("city","location","address","position");
        }

        if(returnVal==null){
            returnVal = new ArrayList<>();
            returnVal.add(input);
        }

    return returnVal;
    }
}
