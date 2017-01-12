package org.gouzouni625.pasr.quality.common;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pasr.database.processes.AcousticModelProcessConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;


@Getter
@AllArgsConstructor
public class AcousticModelProcessConfigurationImpl implements AcousticModelProcessConfiguration {

    public static AcousticModelProcessConfigurationImpl fromJson(InputStream inputStream) {
        return new GsonBuilder()
                .registerTypeAdapter(Path.class, new PathGsonTypeAdapter())
                .create().fromJson(new InputStreamReader(inputStream),
                AcousticModelProcessConfigurationImpl.class);
    }

    private final Path sphinxFePath;
    private final Path bwPath;
    private final Path mllrSolvePath;
    private final Path mapAdaptPath;

}
