package github.o4x.musical.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import github.o4x.musical.misc.DialogAsyncTask;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TagUtil {

    private static final String TAG = TagUtil.class.getSimpleName();

    private final Activity activity;
    private final List<String> songPaths;
    private Tag tags;

    public TagUtil(@NonNull Activity activity, @NonNull List<String> songPaths) {
        this.activity = activity;
        this.songPaths = songPaths;
        createTags();
    }


    private void createTags() {
        String path = songPaths.get(0);
        try {
            tags = AudioFileIO.read(new File(path)).getTag();
        } catch (Exception e) {
            Log.e(TAG, "Could not read audio file " + path, e);
            tags = new AudioFile().getTag();
        }
    }

    @Nullable
    public String getSongTitle() {
        try {
            return tags.getFirst(FieldKey.TITLE);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    public String getAlbumTitle() {
        try {
            return tags.getFirst(FieldKey.ALBUM);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    public String getArtistName() {
        try {
            return tags.getFirst(FieldKey.ARTIST);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    public String getAlbumArtistName() {
        try {
            return tags.getFirst(FieldKey.ALBUM_ARTIST);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    public String getGenreName() {
        try {
            return tags.getFirst(FieldKey.GENRE);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    public String getSongYear() {
        try {
            return tags.getFirst(FieldKey.YEAR);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    public String getTrackNumber() {
        try {
            return tags.getFirst(FieldKey.TRACK);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    public String getDiscNumber() {
        try {
            return tags.getFirst(FieldKey.DISC_NO);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    public String getLyrics() {
        try {
            return tags.getFirst(FieldKey.LYRICS);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    public Bitmap getAlbumArt() {
        try {
            Artwork artworkTag = tags.getFirstArtwork();
            if (artworkTag != null) {
                byte[] artworkBinaryData = artworkTag.getBinaryData();
                return BitmapFactory.decodeByteArray(artworkBinaryData, 0, artworkBinaryData.length);
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static class ArtworkInfo {
        public final long albumId;
        public final Bitmap artwork;

        public ArtworkInfo(long albumId, Bitmap artwork) {
            this.albumId = albumId;
            this.artwork = artwork;
        }
    }

    public void writeValuesToFiles(@NonNull final Map<FieldKey, String> fieldKeyValueMap, @Nullable final ArtworkInfo artworkInfo) {
        Util.hideSoftKeyboard(activity);

        new WriteTagsAsyncTask(activity).execute(new WriteTagsAsyncTask.LoadingInfo(songPaths, fieldKeyValueMap, artworkInfo));
    }

    private static class WriteTagsAsyncTask extends DialogAsyncTask<WriteTagsAsyncTask.LoadingInfo, Integer, String[]> {
        Context applicationContext;

        public WriteTagsAsyncTask(Context context) {
            super(context);
            applicationContext = context;
        }

        @Override
        protected String[] doInBackground(WriteTagsAsyncTask.LoadingInfo... params) {
            try {
                WriteTagsAsyncTask.LoadingInfo info = params[0];

                Artwork artwork = null;
                File albumArtFile = null;
                if (info.artworkInfo != null && info.artworkInfo.artwork != null) {
                    try {
                        albumArtFile = MusicUtil.createAlbumArtFile().getCanonicalFile();
                        info.artworkInfo.artwork.compress(Bitmap.CompressFormat.PNG, 0, new FileOutputStream(albumArtFile));
                        artwork = ArtworkFactory.createArtworkFromFile(albumArtFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                int counter = 0;
                boolean wroteArtwork = false;
                boolean deletedArtwork = false;
                for (String filePath : info.filePaths) {
                    publishProgress(++counter, info.filePaths.size());
                    try {
                        AudioFile audioFile = AudioFileIO.read(new File(filePath));
                        Tag tag = audioFile.getTagAndConvertOrCreateAndSetDefault();

                        if (info.fieldKeyValueMap != null) {
                            for (Map.Entry<FieldKey, String> entry : info.fieldKeyValueMap.entrySet()) {
                                try {
                                    tag.setField(entry.getKey(), entry.getValue());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (info.artworkInfo != null) {
                            if (info.artworkInfo.artwork == null) {
                                tag.deleteArtworkField();
                                deletedArtwork = true;
                            } else if (artwork != null) {
                                tag.deleteArtworkField();
                                tag.setField(artwork);
                                wroteArtwork = true;
                            }
                        }

                        audioFile.commit();
                    } catch (@NonNull CannotReadException | IOException | CannotWriteException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
                        e.printStackTrace();
                    }
                }

                Context context = getContext();
                if (context != null) {
                    if (wroteArtwork) {
                        MusicUtil.insertAlbumArt(context, info.artworkInfo.albumId, albumArtFile.getPath());
                    } else if (deletedArtwork) {
                        MusicUtil.deleteAlbumArt(context, info.artworkInfo.albumId);
                    }
                }

                return info.filePaths.toArray(new String[info.filePaths.size()]);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(@NotNull String[] toBeScanned) {
            super.onPostExecute(toBeScanned);
            ScannerUtilKt.scanPaths(toBeScanned);
        }

        @Override
        protected void onCancelled(String[] toBeScanned) {
            super.onCancelled(toBeScanned);
            ScannerUtilKt.scanPaths(toBeScanned);
        }

        @Override
        protected Dialog createDialog(@NonNull Context context) {
            return null;
        }

        @Override
        protected void onProgressUpdate(@NonNull Dialog dialog, Integer... values) {
            super.onProgressUpdate(dialog, values);
//            ((MaterialDialog) dialog).setMaxProgress(values[1]);
//            ((MaterialDialog) dialog).setProgress(values[0]);
        }

        public static class LoadingInfo {
            public final Collection<String> filePaths;
            @Nullable
            public final Map<FieldKey, String> fieldKeyValueMap;
            @Nullable
            private ArtworkInfo artworkInfo;

            private LoadingInfo(Collection<String> filePaths, @Nullable Map<FieldKey, String> fieldKeyValueMap, @Nullable ArtworkInfo artworkInfo) {
                this.filePaths = filePaths;
                this.fieldKeyValueMap = fieldKeyValueMap;
                this.artworkInfo = artworkInfo;
            }
        }
    }
}
