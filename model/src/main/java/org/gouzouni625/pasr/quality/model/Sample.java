package org.gouzouni625.pasr.quality.model;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.nio.file.Paths;


@Getter
public class Sample {

    public Sample(Path container, String id, String content) {
        this.container = container;
        this.id = id;
        this.content = content;
    }

    public Path getWavPath() {
        return Paths.get(container.toString(), id + ".wav");
    }

    private final Path container;
    private final String id;
    private final String content;

    @Setter
    private String hypothesis;

    @Setter
    private String corrected;

}
