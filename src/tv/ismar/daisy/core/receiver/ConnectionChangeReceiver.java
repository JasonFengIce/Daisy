package tv.ismar.daisy.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class ConnectionChangeReceiver extends BroadcastReceiver{
	 @Override   
     public void onReceive( Context context, Intent intent ) {   
         ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);   
         NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();   
//         connectivityManager.getActiveNetworkInfo().get
       }  
}
