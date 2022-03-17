package github.o4x.musical.imageloader.model;

/**
 * Used to define the artist cover
 */
public class AlbumCover {

    private long id;
    private String filePath;

    public AlbumCover(long id, String filePath) {

        this.filePath = filePath;
        this.id = id;
    }

    public long getId() {

        return id;
    }

    public void setId(int year) {

        this.id = year;
    }

    public String getFilePath() {

        return filePath;
    }

    public void setFilePath(String filePath) {

        this.filePath = filePath;
    }
}
