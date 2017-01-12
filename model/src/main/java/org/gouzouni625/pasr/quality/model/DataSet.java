package org.gouzouni625.pasr.quality.model;

import lombok.NoArgsConstructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor
public class DataSet extends ArrayList<Sample> {

    public static DataSet load (Path container, Path transcriptions,
                                Pattern transcriptionPattern, Parser parser)
            throws FileNotFoundException {

        DataSet dataSet = new DataSet();

        Scanner scanner = new Scanner(new FileInputStream(transcriptions.toFile()));
        while (scanner.hasNextLine()) {
            Matcher matcher = transcriptionPattern.matcher(scanner.nextLine());

            if(matcher.matches()) {
                dataSet.add(new Sample(
                        container,
                        matcher.group(ID_PATTERN_GROUP_NAME),
                        parser.parse(matcher.group(CONTENT_PATTERN_GROUP_NAME))
                ));
            }
        }
        scanner.close();

        return dataSet;
    }

    public static final String ID_PATTERN_GROUP_NAME = "id";
    public static final String CONTENT_PATTERN_GROUP_NAME = "content";

}
