package com.o4x.musical.glide.artistimage;

import com.o4x.musical.model.Album;

/**
 * Used to define the artist cover
 */
public class AlbumCover {

    private int year;

    private String filePath;

    public AlbumCover(int year, String filePath) {

        this.filePath = filePath;
        this.year = year;
    }

    public int getYear() {

        return year;
    }

    public void setYear(int year) {

        this.year = year;
    }

    public String getFilePath() {

        return filePath;
    }

    public void setFilePath(String filePath) {

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

        AlbumCover compare = (AlbumCover) obj;

        try {
            return (compare.filePath.equals(this.filePath) && compare.filePath.getBytes().length == this.filePath.getBytes().length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
