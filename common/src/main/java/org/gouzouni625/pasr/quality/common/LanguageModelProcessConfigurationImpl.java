package org.gouzouni625.pasr.quality.common;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pasr.database.processes.LanguageModelProcessConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

@Getter
@AllArgsConstructor
public class LanguageModelProcessConfigurationImpl implements LanguageModelProcessConfiguration {

    public static LanguageModelProcessConfigurationImpl fromJson(InputStream inputStream) {
        return new GsonBuilder()
                .registerTypeAdapter(Path.class, new PathGsonTypeAdapter())
                .create().fromJson(new InputStreamReader(inputStream),
                LanguageModelProcessConfigurationImpl.class);
    }

    private final Path text2wfreqPath;
    private final Path wfreq2vocabPath;
    private final Path text2idngramPath;
    private final Path idngram2lmPath;

}
