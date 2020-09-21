package com.o4x.musical.imageloader.model;

import java.util.Objects;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AudioFileCover {
    public final String title;
    public final String filePath;

    public AudioFileCover(String title, String filePath) {
        this.title = title;
        this.filePath = filePath;
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
        return Objects.hash(filePath);
    }
}
