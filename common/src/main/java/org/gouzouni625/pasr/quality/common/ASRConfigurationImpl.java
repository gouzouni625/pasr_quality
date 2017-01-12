package org.gouzouni625.pasr.quality.common;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.pasr.asr.ASRConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;


@Getter
@Setter
@AllArgsConstructor
public class ASRConfigurationImpl implements ASRConfiguration {

    public static ASRConfigurationImpl fromJson(InputStream inputStream) {
        return new GsonBuilder()
                .registerTypeAdapter(Path.class, new PathGsonTypeAdapter())
                .create().fromJson(new InputStreamReader(inputStream),
                ASRConfigurationImpl.class);
    }

    private Path acousticModelPath;
    private Path languageModelPath;
    private Path dictionaryPath;

}
