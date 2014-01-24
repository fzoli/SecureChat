package org.dyndns.fzoli.ui;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.DefaultFormatter;

/**
 * Reguláris kifejezésre illeszkedő szövegformázó.
 */
public class RegexPatternFormatter extends DefaultFormatter {

    protected Matcher matcher;

    public RegexPatternFormatter(Pattern regex) {
        setOverwriteMode(false);
        matcher = regex.matcher(""); // Matcher inicializálása a regexhez
    }

    @Override
    public Object stringToValue(String string) throws ParseException {
        if (string == null)
            return null;
        matcher.reset(string); // Matcher szövegének beállítása

        if (!matcher.matches()) // Ha nem illeszkedik a szöveg, kivételt dob
            throw new ParseException("does not match regex", 0);

        // Ha a szöveg illeszkedett, akkor vissza lehet térni vele
        return super.stringToValue(string);
    }

}