package org.gouzouni625.pasr.quality.common;


import org.pasr.database.processes.Process;

import java.io.File;
import java.io.IOException;

public class WordAlignProcess extends Process {

    public WordAlignProcess (File transcriptionsFile, File hypothesisFile, File outputFile,
                             EvaluationConfiguration evaluationConfiguration) throws IOException {

        setErrorRedirectionFile(new File(outputFile.getParentFile(), "error.log"));

        add(new ProcessBuilder(
                evaluationConfiguration.getWordAlignPath().toString(),
                transcriptionsFile.toPath().toString(),
                hypothesisFile.toPath().toString()
        ).redirectOutput(outputFile));
    }

}
