package com.o4x.musical.glide.audiocover;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AudioFileCover {
    public final String filePath;

    public AudioFileCover(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public int hashCode() {
        return Math.abs((filePath.getBytes().length + filePath.hashCode()));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || obj.getClass() != this.getClass()) return false;

        AudioFileCover compare = (AudioFileCover) obj;

        try {
            return (compare.filePath.equals(this.filePath) && compare.filePath.getBytes().length == this.filePath.getBytes().length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
