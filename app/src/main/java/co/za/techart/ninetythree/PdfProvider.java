package co.za.techart.ninetythree;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PdfProvider extends ContentProvider {
    public static final String AUTH="co.za.techart.ninetythree.files";

    @Override public boolean onCreate(){return true;}

    static File root(Context c){
        File f=c.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if(f!=null && !f.exists()) f.mkdirs();
        return f;
    }

    public static Uri uriFor(Context c, File f){
        return new Uri.Builder().scheme("content").authority(AUTH).appendPath(f.getName()).build();
    }

    File resolve(Uri u){
        File base=root(getContext());
        String name=u.getLastPathSegment();
        if(base==null || name==null) throw new SecurityException("Invalid file");
        File f=new File(base,name);
        try{String rootPath=base.getCanonicalPath();String path=f.getCanonicalPath();if(!path.startsWith(rootPath+File.separator))throw new SecurityException("Invalid path");}catch(IOException e){throw new SecurityException(e);}
        return f;
    }

    @Override public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException{return ParcelFileDescriptor.open(resolve(uri),ParcelFileDescriptor.MODE_READ_ONLY);}
    @Override public String getType(Uri uri){String ext=MimeTypeMap.getFileExtensionFromUrl(uri.toString());String type=MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);return type==null?"application/octet-stream":type;}
    @Override public Cursor query(Uri uri,String[] projection,String selection,String[] args,String sortOrder){File f=resolve(uri);MatrixCursor c=new MatrixCursor(new String[]{"_display_name","_size"});c.addRow(new Object[]{f.getName(),f.length()});return c;}
    @Override public String[] getStreamTypes(Uri uri,String mimeTypeFilter){return new String[]{getType(uri)};}
    @Override public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}
    @Override public int delete(Uri u,String s,String[]a){return 0;}
    @Override public int update(Uri u,ContentValues v,String s,String[]a){return 0;}
}
