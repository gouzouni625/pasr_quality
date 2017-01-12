package org.gouzouni625.pasr.quality.arctic;

import org.gouzouni625.ns.NumberSpeller;
import org.gouzouni625.pasr.quality.model.Parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ARCTICParser implements Parser {

    public ARCTICParser(final NumberSpeller numberSpeller) {
        this.numberSpeller = numberSpeller;
    }

    @Override
    public String parse (String string) {
        return replaceNumbers(string)
                .replaceAll("[.,-;]", " ")
                .replaceAll(" +", " ")
                .toLowerCase()
                .trim();
    }

    private String replaceNumbers (final String string) {
        String processedString = string;

        // Replace 4-digit numbers
        // The numbers with the most digits should be replaced first to avoid replacing them as
        // sequences of numbers with fewer digits.
        Matcher matcher = Pattern.compile("([0-9]{4})").matcher(processedString);
        while(matcher.find()) {
            processedString = processedString.replaceAll(
                    matcher.group(1),
                    numberSpeller.spell(
                            Integer.valueOf(matcher.group(1)),
                            NumberSpeller.Types.DATE
                    )
            );
        }

        // Replace 2-digit numbers
        matcher = Pattern.compile("([0-9]{2})").matcher(processedString);
        while(matcher.find()) {
            processedString = processedString.replaceAll(
                    matcher.group(1),
                    numberSpeller.spell(
                            Integer.valueOf(matcher.group(1)),
                            NumberSpeller.Types.NORMAL
                    )
            );
        }

        return processedString;
    }

    private final NumberSpeller numberSpeller;

}
