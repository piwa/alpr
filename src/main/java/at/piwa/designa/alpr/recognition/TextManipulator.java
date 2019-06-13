package at.piwa.designa.alpr.recognition;

import org.apache.commons.lang3.StringUtils;
import org.paukov.combinatorics3.Generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TextManipulator {


    public List<String> removeParticularCharacter(String word, String char1) {

        List<String> returnList = new ArrayList<>();

        int index = word.indexOf(char1);
        while (index >= 0) {

            StringBuilder builder = new StringBuilder(word);
            builder.deleteCharAt(index);
            returnList.add(builder.toString());

            index = word.indexOf(char1, index + 1);
        }
        return returnList;
    }

    public List<String> replaceCharacterAndCreateCombination(String text, String char1, String char2) {

        List<String> resultingList = new ArrayList<>();
        resultingList.add(text);

        String[] split = text.split(char1);

        if(split.length == 1) {
            return resultingList;
        }


        List<List<String>> cartesianLists = new ArrayList<>();

        for(int i = 0; i < split.length; i++ ) {
            List<String> cartesianEntry = new ArrayList<>();
            if(i < split.length - 1) {
                cartesianEntry.add(split[i].concat(char1));
                cartesianEntry.add(split[i].concat(char2));
            }
            else if(i == split.length-1 && text.endsWith(char1)) {
                cartesianEntry.add(split[i].concat(char1));
                cartesianEntry.add(split[i].concat(char2));
            }
            else {
                cartesianEntry.add(split[i]);
                cartesianEntry.add(split[i]);
            }

            cartesianLists.add(cartesianEntry);
        }

        Generator.cartesianProduct(cartesianLists.toArray(new List[0])).stream().forEach(p -> resultingList.add(StringUtils.join(((List) p).toArray())));



        return resultingList;
    }


    public List<String> replaceCharacterAndCreateCombinationBidirect(String s, String char1, String char2) {
        List<String> tempStrings = new ArrayList<>();
        tempStrings.addAll(replaceCharacterAndCreateCombination(s, char1, char2));
        tempStrings.addAll(replaceCharacterAndCreateCombination(s, char2, char1));
        return tempStrings;
    }
}
