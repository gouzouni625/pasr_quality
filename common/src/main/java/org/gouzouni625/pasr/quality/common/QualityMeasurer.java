package org.gouzouni625.pasr.quality.common;

import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.gouzouni625.pasr.quality.model.DataSet;
import org.gouzouni625.pasr.quality.model.Partition;
import org.gouzouni625.pasr.quality.model.Quality;
import org.gouzouni625.pasr.quality.model.Sample;
import org.pasr.asr.ASRConfiguration;
import org.pasr.asr.recognizers.StreamSpeechRecognizer;
import org.pasr.database.processes.AcousticModelProcess;
import org.pasr.database.processes.LanguageModelProcess;
import org.pasr.model.asr.dictionary.Dictionary;
import org.pasr.model.text.Corpus;
import org.pasr.model.text.WordSequence;
import org.pasr.postp.correctors.Corrector;
import org.pasr.postp.detectors.POSDetector;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class QualityMeasurer {

    public QualityMeasurer (DataSet dataSet, Partition partition, File outputDirectory) {
        this.outputDirectory = outputDirectory;
        if(!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }

        int numberOfTrainingSamples = (int)(dataSet.size() * partition.getTrain());

        trainingSamples = new ArrayList<>();
        testingSamples = new ArrayList<>();

        Random random = new Random(System.currentTimeMillis());
        for(int i = 0;i < numberOfTrainingSamples;i++) {
            int chosen = random.nextInt(dataSet.size());

            trainingSamples.add(dataSet.remove(chosen));
        }

        dataSet.forEach(testingSamples :: add);

        this.init();
    }

    public QualityMeasurer (DataSet trainingDataSet, DataSet testingDataSet, File outputDirectory) {
        this.outputDirectory = outputDirectory;
        if(!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }

        trainingSamples = trainingDataSet;
        testingSamples = testingDataSet;

        this.init();
    }

    private void init () {
        asrConfiguration = ASRConfigurationImpl.fromJson(
                this.getClass().getResourceAsStream(
                        "/default-asr-configuration.json"
                )
        );
    }

    public Quality measure(
            boolean adaptLanguageModel,
            boolean adaptAcousticModel,
            boolean applyCorrector
    ) throws IOException, InterruptedException {
        if (adaptLanguageModel) {
            asrConfiguration.setLanguageModelPath(createLanguageModel());
            asrConfiguration.setDictionaryPath(createDictionary());
        }

        if (adaptAcousticModel) {
            asrConfiguration.setAcousticModelPath(createAcousticModel());
        }

        evaluate(applyCorrector);

        return null;
    }

    private Path createLanguageModel() throws IOException, InterruptedException {
        File languageModelDirectory = new File(outputDirectory, "language-model");
        if(!languageModelDirectory.exists()) {
            languageModelDirectory.mkdir();
        }

        File languageModelInputFile = new File(languageModelDirectory, "sentences.text");
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
                languageModelInputFile
        )));
        for(Sample sample : testingSamples) {
            printWriter.println("<s> " + sample.getContent() + " </s>");
        }
        printWriter.close();

        File languageModelOutputFile = new File(languageModelDirectory, "language-model.lm");

        new LanguageModelProcess(
                languageModelInputFile, languageModelOutputFile, 3,
                LanguageModelProcessConfigurationImpl.fromJson(
                        this.getClass().getResourceAsStream(
                                "/language-model-process-configuration.json"
                        )
                )
        ).startAndWaitFor();

        return languageModelOutputFile.toPath();
    }

    private Path createDictionary() throws IOException {
        File dictionaryDirectory = new File(outputDirectory, "dictionary");
        if(!dictionaryDirectory.exists()) {
            dictionaryDirectory.mkdir();
        }

        File dictionaryFile = new File(dictionaryDirectory, "dictionary.dict");

        List<WordSequence> wordSequenceList = new ArrayList<>();
        for (Sample sample : testingSamples) {
            wordSequenceList.add(new WordSequence(sample.getContent()));
        }

        Corpus corpus = new Corpus(wordSequenceList);

        Dictionary defaultDictionary = Dictionary.createFromStream(
                new FileInputStream(asrConfiguration.getDictionaryPath().toString())
        );

        Dictionary dictionary = corpus.process(defaultDictionary);

        FileOutputStream fileOutputStream = new FileOutputStream(dictionaryFile);
        dictionary.exportToStream(fileOutputStream);
        fileOutputStream.close();

        return dictionaryFile.toPath();
    }

    private Path createAcousticModel() throws IOException, InterruptedException {
        File acousticModelDirectory = new File(outputDirectory, "acoustic-model");
        if(!acousticModelDirectory.exists()) {
            acousticModelDirectory.mkdir();
        }

        File ids = new File(acousticModelDirectory, "ids");
        File transcriptions = new File(acousticModelDirectory, "transcriptions");

        PrintWriter fileIdsPrintWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(ids)
        ));
        PrintWriter transcriptionsPrintWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(transcriptions)
        ));
        for(Sample sample : trainingSamples) {
            fileIdsPrintWriter.println(sample.getId());
            transcriptionsPrintWriter.println("<s> " + sample.getContent() + " </s> (" + sample.getId() + ")");

            FileUtils.copyFile(
                    sample.getWavPath().toFile(),
                    new File(acousticModelDirectory, sample.getId() + ".wav")
            );
        }
        transcriptionsPrintWriter.close();
        fileIdsPrintWriter.close();

        new AcousticModelProcess(
                acousticModelDirectory,
                acousticModelDirectory,
                transcriptions.toPath(),
                ids.toPath(),
                AcousticModelProcessConfigurationImpl.fromJson(
                        this.getClass().getResourceAsStream(
                                "/acoustic-model-process-configuration.json"
                        )
                ),
                asrConfiguration
        ).startAndWaitFor();

        return Paths.get(acousticModelDirectory.toPath().toString(),
                asrConfiguration.getAcousticModelPath().getFileName().toString());
    }

    private void evaluate (boolean applyCorrector) throws IOException, InterruptedException {
        File evaluationDirectory = new File(outputDirectory, "evaluation");
        if(!evaluationDirectory.exists()) {
            evaluationDirectory.mkdir();
        }

        File evaluationOutputFile = new File(evaluationDirectory, "aligned.text");

        // Save the configuration used inside the output directory
        PrintWriter configurationPrintWriter = new PrintWriter(new FileOutputStream(
                new File(outputDirectory, "configuration.info")
        ));
        new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Path.class, new PathGsonTypeAdapter())
                .create()
                .toJson(asrConfiguration, configurationPrintWriter);
        configurationPrintWriter.close();

        StreamSpeechRecognizer streamSpeechRecognizer = new StreamSpeechRecognizer(asrConfiguration);

        File transcriptionFile = new File(evaluationDirectory, "transcription.text");
        File hypothesisFile = new File(evaluationDirectory, "hypothesis.text");

        PrintWriter hypothesisPrintWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(hypothesisFile)
        ));
        PrintWriter transcriptionPrintWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(transcriptionFile)
        ));
        for (Sample sample : testingSamples) {
            String hypothesis = streamSpeechRecognizer.recognize(
                    new FileInputStream(sample.getWavPath().toString())
            );
            sample.setHypothesis(hypothesis);

            hypothesisPrintWriter.println(hypothesis + " (" + sample.getId() + ")");

            transcriptionPrintWriter.println(sample.getContent() + " (" + sample.getId() + ")");
        }
        transcriptionPrintWriter.close();
        hypothesisPrintWriter.close();

        new WordAlignProcess(
                transcriptionFile, hypothesisFile, evaluationOutputFile,
                EvaluationConfigurationImpl.fromJson(
                        this.getClass().getResourceAsStream(
                                "/evaluation-configuration.json"
                        )
                )
        ).startAndWaitFor();

        if (!applyCorrector) {
            return;
        }

        List<WordSequence> wordSequenceList = new ArrayList<>();
        for (Sample sample : testingSamples) {
            wordSequenceList.add(new WordSequence(sample.getContent()));
        }

        Corpus corpus = new Corpus(wordSequenceList);

        Dictionary defaultDictionary = Dictionary.createFromStream(
                new FileInputStream(asrConfiguration.getDictionaryPath().toString())
        );

        Corrector corrector = new Corrector(corpus, defaultDictionary);
        corrector.addDetector(new POSDetector(corpus));

        File correctedOutputFile = new File(evaluationDirectory, "corrected.text");

        PrintWriter correctedPrintWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(correctedOutputFile)
        ));
        for (Sample sample : testingSamples) {
            String corrected = corrector.correct(sample.getHypothesis());
            sample.setCorrected(corrected);

            correctedPrintWriter.println(corrected + " (" + sample.getId() + ")");
        }
        correctedPrintWriter.close();

        File correctedEvaluationOutputFile = new File(evaluationDirectory, "corrected-aligned.text");

        new WordAlignProcess(
                transcriptionFile, correctedOutputFile, correctedEvaluationOutputFile,
                EvaluationConfigurationImpl.fromJson(
                        this.getClass().getResourceAsStream(
                                "/evaluation-configuration.json"
                        )
                )
        ).startAndWaitFor();
    }

    private final File outputDirectory;

    private final List<Sample> trainingSamples;
    private final List<Sample> testingSamples;

    private ASRConfiguration asrConfiguration;

}

