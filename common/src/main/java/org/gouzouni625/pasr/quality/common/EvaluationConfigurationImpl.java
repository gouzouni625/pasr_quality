package org.gouzouni625.pasr.quality.common;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;


@Getter
@Setter
@AllArgsConstructor
public class EvaluationConfigurationImpl implements EvaluationConfiguration {

    public static EvaluationConfigurationImpl fromJson(InputStream inputStream) {
        return new GsonBuilder()
                .registerTypeAdapter(Path.class, new PathGsonTypeAdapter())
                .create().fromJson(new InputStreamReader(inputStream),
                        EvaluationConfigurationImpl.class);
    }

    private Path wordAlignPath;

}
