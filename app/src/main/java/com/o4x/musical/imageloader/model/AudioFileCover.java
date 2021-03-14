package com.o4x.musical.imageloader.model;

import java.util.Objects;

public class AudioFileCover {

    public final String title;
    public final String filePath;
    public final Long dateModified;

    public AudioFileCover(String title, String filePath, Long dateModified) {
        this.title = title;
        this.filePath = filePath;
        this.dateModified = dateModified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioFileCover that = (AudioFileCover) o;
        return filePath.equals(that.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath) + dateModified.hashCode();
    }
}
