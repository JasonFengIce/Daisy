/*****************************************************************************
 * Util.java
 * ****************************************************************************
 * Copyright © 2011-2014 VLC authors and VideoLAN
 * <p/>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package tv.ismar.daisy.core.vlc;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;
import org.videolan.libvlc.util.AndroidUtil;
import tv.ismar.daisy.VodApplication;

import java.io.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Util {
    public final static String TAG = "VLC/Util";
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    public static final String ACTION_SCAN_START = "org.videolan.vlc.gui.ScanStart";
    public static final String ACTION_SCAN_STOP = "org.videolan.vlc.gui.ScanStop";


    public static int convertPxToDp(int px) {
        DisplayMetrics metrics = VodApplication.getAppResources().getDisplayMetrics();
        float logicalDensity = metrics.density;
        int dp = Math.round(px / logicalDensity);
        return dp;
    }

    public static int convertDpToPx(int dp) {
        return Math.round(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                        VodApplication.getAppResources().getDisplayMetrics())
        );
    }

    public static String readAsset(String assetName, String defaultS) {
        InputStream is = null;
        BufferedReader r = null;
        try {
            is = VodApplication.getAppResources().getAssets().open(assetName);
            r = new BufferedReader(new InputStreamReader(is, "UTF8"));
            StringBuilder sb = new StringBuilder();
            String line = r.readLine();
            if (line != null) {
                sb.append(line);
                line = r.readLine();
                while (line != null) {
                    sb.append('\n');
                    sb.append(line);
                    line = r.readLine();
                }
            }
            return sb.toString();
        } catch (IOException e) {
            return defaultS;
        } finally {
            close(is);
            close(r);
        }
    }

    /**
     * Get a resource id from an attribute id.
     *
     * @param context
     * @param attrId
     * @return the resource id
     */
    public static int getResourceFromAttribute(Context context, int attrId) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attrId});
        int resId = a.getResourceId(0, 0);
        a.recycle();
        return resId;
    }

    /**
     * Get a color id from an attribute id.
     *
     * @param context
     * @param attrId
     * @return the color id
     */
    public static int getColorFromAttribute(Context context, int attrId) {
        return VodApplication.getAppResources().getColor(getResourceFromAttribute(context, attrId));
    }

    /**
     * Set the alignment mode of the specified TextView with the desired align
     * mode from preferences.
     * <p/>
     * See @array/audio_title_alignment_values
     *
     * @param alignMode Align mode as read from preferences
     * @param t         Reference to the textview
     */
    public static void setAlignModeByPref(int alignMode, TextView t) {
        if (alignMode == 1)
            t.setEllipsize(TruncateAt.END);
        else if (alignMode == 2)
            t.setEllipsize(TruncateAt.START);
        else if (alignMode == 3) {
            t.setEllipsize(TruncateAt.MARQUEE);
            t.setMarqueeRepeatLimit(-1);
            t.setSelected(true);
        }
    }

    /**
     * Generate a value suitable for use in setId
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public static void actionScanStart() {
        Intent intent = new Intent();
        intent.setAction(ACTION_SCAN_START);
        VodApplication.getAppContext().sendBroadcast(intent);
    }

    public static void actionScanStop() {
        Intent intent = new Intent();
        intent.setAction(ACTION_SCAN_STOP);
        VodApplication.getAppContext().sendBroadcast(intent);
    }

    private static class DialogCallback implements PlaybackService.Client.Callback {
        private final ProgressDialog dialog;
        final private PlaybackService.Client mClient;
        final private Runnable mRunnable;

        private interface Runnable {
            void run(PlaybackService service);
        }

        private DialogCallback(Context context, Runnable runnable) {
            mClient = new PlaybackService.Client(context, this);
            mRunnable = runnable;
            this.dialog = ProgressDialog.show(
                    context,
                    "loading" + "…",
                    "please_wait", true);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mClient.disconnect();
                }
            });
            mClient.connect();
        }

        @Override
        public void onConnected(PlaybackService service) {
            mRunnable.run(service);
            dialog.dismiss();
        }

        @Override
        public void onDisconnected() {
            dialog.dismiss();
        }
    }

    public static void openMedia(final Context context, final MediaWrapper media) {
        if (media == null)
            return;
        if (media.getType() == MediaWrapper.TYPE_VIDEO) {
//            VideoPlayerActivity.start(context, media.getUri(), media.getTitle());
            Log.d(TAG, "openMedia");
        } else if (media.getType() == MediaWrapper.TYPE_AUDIO) {
            new DialogCallback(context, new DialogCallback.Runnable() {
                @Override
                public void run(PlaybackService service) {
                    service.load(media);
                }
            });
        }
    }

    public static void openList(final Context context, final List<MediaWrapper> list, final int position) {
        new DialogCallback(context, new DialogCallback.Runnable() {
            @Override
            public void run(PlaybackService service) {
                service.load(list, position);
            }
        });
    }

    public static void openStream(final Context context, final String uri) {
        new DialogCallback(context, new DialogCallback.Runnable() {
            @Override
            public void run(PlaybackService service) {
                service.loadLocation(uri);
            }
        });
    }

    private static String getMediaString(Context ctx, int id) {
        if (ctx != null)
            return ctx.getResources().getString(id);
        else {
//            switch (id) {
//                case R.string.unknown_artist:
//                    return "Unknown Artist";
//                case R.string.unknown_album:
//                    return "Unknown Album";
//                case R.string.unknown_genre:
//                    return "Unknown Genre";
//                default:
//                    return "";
//            }
            return "";
        }
    }

    public static String getMediaArtist(Context ctx, MediaWrapper media) {
        final String artist = media.getArtist();
        return artist != null ? artist : "unknown_artist";
    }

    public static String getMediaReferenceArtist(Context ctx, MediaWrapper media) {
        final String artist = media.getReferenceArtist();
        return artist != null ? artist : "unknown_artist";
    }

    public static String getMediaAlbumArtist(Context ctx, MediaWrapper media) {
        final String albumArtist = media.getAlbumArtist();
        return albumArtist != null ? albumArtist : "unknown_artist";
    }

    public static String getMediaAlbum(Context ctx, MediaWrapper media) {
        final String album = media.getAlbum();
        return album != null ? album : "unknown_album";

    }

    public static String getMediaGenre(Context ctx, MediaWrapper media) {
        final String genre = media.getGenre();
        return genre != null ? genre : "unknown_album";
    }

    public static String getMediaSubtitle(Context ctx, MediaWrapper media) {
        if (media.getType() == MediaWrapper.TYPE_AUDIO)
            return media.getNowPlaying() != null
                    ? media.getNowPlaying()
                    : getMediaArtist(ctx, media) + " - " + getMediaAlbum(ctx, media);
        else
            return "";
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void commitPreferences(SharedPreferences.Editor editor) {
        if (AndroidUtil.isGingerbreadOrLater())
            editor.apply();
        else
            editor.commit();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static boolean deleteFile(String path) {
        boolean deleted = false;
        path = Uri.decode(Strings.removeFileProtocole(path));
        //Delete from Android Medialib, for consistency with device MTP storing and other apps listing content:// media
        if (AndroidUtil.isHoneycombOrLater()) {
            ContentResolver cr = VodApplication.getAppContext().getContentResolver();
            String[] selectionArgs = {path};
            deleted = cr.delete(MediaStore.Files.getContentUri("external"),
                    MediaStore.Files.FileColumns.DATA + "=?", selectionArgs) > 0;
        }
        File file = new File(path);
        if (file.exists())
            deleted |= file.delete();
        return deleted;
    }

    public static boolean recursiveDelete(Context context, File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles())
                recursiveDelete(context, child);
            return fileOrDirectory.delete();
        } else {
            return deleteFile(fileOrDirectory.getPath());
        }
    }

    public static boolean close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean canWrite(String path) {
        if (path == null)
            return false;
        if (path.startsWith("file://"))
            path = path.substring(7);
        if (!path.startsWith("/"))
            return false;
        if (path.startsWith(AndroidDevices.EXTERNAL_PUBLIC_DIRECTORY))
            return true;
        if (AndroidUtil.isLolliPopOrLater())
            return false;
        File file = new File(path);
        return (file.exists() && file.canWrite());
    }

    public static boolean isCallable(Intent intent) {
        List<ResolveInfo> list = VodApplication.getAppContext().getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static boolean isBlackThemeEnabled() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(VodApplication.getAppContext());
        return pref.getBoolean("enable_black_theme", false);
    }
}
