package de.bitshares_munich.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TimeUtils;
import android.widget.Toast;

import com.itextpdf.text.ExceptionConverter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

import butterknife.ButterKnife;
import de.bitshares_munich.Interfaces.AssetDelegate;
import de.bitshares_munich.Interfaces.IAccount;
import de.bitshares_munich.Interfaces.IAccountID;
import de.bitshares_munich.Interfaces.IAccountObject;
import de.bitshares_munich.Interfaces.IAssetObject;
import de.bitshares_munich.Interfaces.IBalancesDelegate;
import de.bitshares_munich.Interfaces.IExchangeRate;
import de.bitshares_munich.Interfaces.IRelativeHistory;
import de.bitshares_munich.Interfaces.ITransactionObject;
import de.bitshares_munich.autobahn.WebSocketConnection;
import de.bitshares_munich.autobahn.WebSocketException;
import de.bitshares_munich.smartcoinswallet.R;

/**
 * Created by qasim on 5/9/16.
 */
public class Application extends android.app.Application implements de.bitshares_munich.autobahn.WebSocket.WebSocketConnectionObserver {

//    public static WebSocket webSocketG;
    public static Context context;
    static IAccount iAccount;
    static IExchangeRate iExchangeRate;
    static IBalancesDelegate iBalancesDelegate_transactionActivity;
    static IBalancesDelegate iBalancesDelegate_ereceiptActivity;
    static IBalancesDelegate iBalancesDelegate_assetsActivity;
    static AssetDelegate iAssetDelegate;
    static IAccountID iAccountID;
    static ITransactionObject iTransactionObject;
    static IAccountObject iAccountObject;
    static IAssetObject iAssetObject;
    static IRelativeHistory iRelativeHistory;
    public static String blockHead = "";
    private static Activity currentActivity;

    public static String urlsSocketConnection[] =
            {
                    "wss://de.blockpay.ch:8089",                // German node
                    "wss://fr.blockpay.ch:8089",               // France node
                    "wss://bitshares.openledger.info/ws",      // Openledger node
           //         "wss://bitshares.dacplay.org:8089/ws"
                    //,"https://dele-puppy.com/ws"
                    //,"https://valen-tin.fr:8090"
            };

    //public static int counter = 0;

    public static String monitorAccountId;

    private static Handler warningHandler = new Handler();


    public static void setCurrentActivity(Activity _activity) {
        Application.currentActivity = _activity;
    }

    public static Activity getCurrentActivity() {
        return Application.currentActivity;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ButterKnife.setDebug(true);
        context = getApplicationContext();
        //showDialogPin();
        blockHead = "";
//        webSocketConnection();
        init();
    }

    public void init()
    {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if ( mConnection == null )
                {
                    mConnection = new WebSocketConnection();
                    webSocketConnection();
                }
            }
        },1000);

    }


    public static void  registerCallback(IAccount callbackClass) {
        iAccount = callbackClass;
    }

    public static void  registerCallbackIAccountID(IAccountID callbackClass) {
        iAccountID = callbackClass;
    }

    public static void registerExchangeRateCallback(IExchangeRate callbackClass) {
        iExchangeRate = callbackClass;
    }

    /*
    public void registerBalancesDelegate(IBalancesDelegate callbackClass) {
        iBalancesDelegate = callbackClass;
    }
    */

    public static void registerBalancesDelegateTransaction(IBalancesDelegate callbackClass) {
        iBalancesDelegate_transactionActivity = callbackClass;
    }

    public static void  registerBalancesDelegateEReceipt(IBalancesDelegate callbackClass) {
        iBalancesDelegate_ereceiptActivity = callbackClass;
    }

    public static void  registerBalancesDelegateAssets(IBalancesDelegate callbackClass) {
        iBalancesDelegate_assetsActivity = callbackClass;
    }

    public static void registerAssetDelegate(AssetDelegate callbackClass) {
        iAssetDelegate = callbackClass;
    }

    public static void registerTransactionObject(ITransactionObject callbackClass) {
        iTransactionObject = callbackClass;
    }

    public static void registerAccountObjectCallback(IAccountObject callbackClass) {
        iAccountObject = callbackClass;
    }

    public static void registerAssetObjectCallback(IAssetObject callbackClass) {
        iAssetObject = callbackClass;
    }

    public static void registerRelativeHistoryCallback(IRelativeHistory callbackClass) {
        iRelativeHistory = callbackClass;
    }

    private static void showWarningMessage(final String myEx) {
        if (getCurrentActivity() != null) {
            Log.d("exception websocket", "inside again");
            getCurrentActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, "Your system does not supports new SSL ciphering. Error : " + myEx, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

//    public static void webSocketConnection()
//    {
//        isReady = false;
//        int nodeIndex = new Random().nextInt(urlsSocketConnection.length);
//        final AsyncHttpGet get = new AsyncHttpGet(urlsSocketConnection[nodeIndex]);
//        get.setTimeout(10 * 1000);
//
//        Log.d("Connecting to node", urlsSocketConnection[nodeIndex]);
//        AsyncHttpClient.getDefaultInstance().websocket(get, null, new AsyncHttpClient.WebSocketConnectCallback() {
//
//            @Override
//            public void onCompleted(final Exception ex, WebSocket webSocket) {
//
//                if (ex != null)
//                {
//                    isReady = false;
//
////                    if ( ++counter >= urlsSocketConnection.length )
////                    {
////                        counter = 0;
////                    }
//
//                    Log.d("exception websocket", ex.toString());
//                    final Exception myEx = ex;
//                    Log.d("exception websocket", myEx.toString());
//                    try {
//                        final String exMessage = ex.getMessage();
//                        if (ex != null && exMessage != null && !exMessage.isEmpty() && exMessage.contains("handshake_failure"))
//                        {
//                            Log.d("exception3 websocket", "inside");
//
//                            try {
//
//                                Runnable updateTask = new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        showWarningMessage(exMessage);
//                                    }
//                                };
//                                warningHandler.postDelayed(updateTask, 1000);
//                            }
//                            catch (Exception e)
//                            {
//                                Log.d("exception websocket", e.getMessage());
//                            }
//                            //webSocketConnection();
//                            Log.d("exception websocket", "showWarningMessagegetting out");
//                        } else {
//                            Log.d("exception websocket", "webbb socket");
//                            if (webSocket != null) {
//                                Log.d("exception websocket", "inside webbb socket");
//                                if (webSocket.isOpen())
//                                {
//                                    Log.d("exception websocket", "is open");
//                                    webSocket.close();
//                                    Log.d("exception websocket", "closed");
//                                    return;
//                                }
//                            }
//                            webSocketConnection();
//                        }
//                    } catch (Exception e) {
//                        Log.d("exception websocket", e.getMessage());
//                    }
//                    //ex.printStackTrace();
//                    return;
//                }
//
//                webSocket.setClosedCallback(new CompletedCallback() {
//                    public void onCompleted(Exception ex) {
//                        isReady = false;
//                        webSocketConnection();
//                    }
//                });
//
//                webSocket.setEndCallback(new CompletedCallback() {
//                    public void onCompleted(Exception ex) {
//
//                    }
//                });
//
//                Log.d("exception websocket", "before making application.websocketg");
//                Application.webSocketG = webSocket;
//                Log.d("exception websocket", "sending initial socket");
//                sendInitialSocket(context);
//                Log.d("exception websocket", "completed");
//            }
//
//
//        });
//
//    }



public static int nodeIndex = 0;

    private void webSocketConnection()
    {
        Log.i("internetBlockpay", "in Application webSocketConnection");

        Log.i("Connect", "in Application webSocketConnection");
        isReady = false;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                nodeIndex = nodeIndex % urlsSocketConnection.length;

                Log.i("internetBlockpay", "preparing to connect to:" + urlsSocketConnection[nodeIndex]);

                connect(urlsSocketConnection[nodeIndex]);
                nodeIndex++;

            }
        }, 500);

    }

    public static Boolean isReady = false;

    public static void stringTextRecievedWs(String s) {

//        if (Application.webSocketG.isOpen()) {
//
//            Application.webSocketG.send(context.getString(R.string.login_api));
//            Application.webSocketG.setStringCallback(new WebSocket.StringCallback() {
//                public void onStringAvailable(String s) {

                    try {
                        //System.out.println("I got a string: " + s);
                        JSONObject jsonObject = new JSONObject(s);

                        if (jsonObject.has("id")) {
                            int id = jsonObject.getInt("id");

                            if (id == 1) {
                                if (s.contains("true")) {
                                    Application.send(context.getString(R.string.database_indentifier));
                                } else {
                                    Application.send(context.getString(R.string.login_api));
                                }
                            } else if (id == 2) {
                                Helper.storeIntSharePref(context, context.getString(R.string.sharePref_database), jsonObject.getInt("result"));
                                Application.send(context.getString(R.string.network_broadcast_identifier));
                            } else if (id == 3) {
                                Helper.storeIntSharePref(context, context.getString(R.string.sharePref_network_broadcast), jsonObject.getInt("result"));
                                Application.send(context.getString(R.string.history_identifier));
                            } else if (id == 4) {
                                Helper.storeIntSharePref(context, context.getString(R.string.sharePref_history), jsonObject.getInt("result"));
                                Application.send(context.getString(R.string.subscribe_callback));
                                //Application.webSocketG.send(context.getString(R.string.subscribe_callback_full_account));
                                isReady = true;

                                /*
                                Handler testHandler = new Handler();

                                Runnable testRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (iAssetDelegate != null)
                                        {
                                            iAssetDelegate.loadAll();
                                        }
                                    }
                                };

                                testHandler.postDelayed(testRunnable,2000);
                                */


                            } else if (id == 6) {
                                if (iAccount != null) {
                                    iAccount.checkAccount(jsonObject);
                                }
                            } else if (id == 100 || id == 200) {
                                JSONArray jsonArray = (JSONArray) jsonObject.get("result");
                                JSONObject obj = new JSONObject();
                                if (jsonArray.length() != 0) {
                                    obj = (JSONObject) jsonArray.get(1);
                                }
                                iExchangeRate.callback_exchange_rate(obj, id);
                            }
                            else if (id == 8)
                            {
                                if (iBalancesDelegate_transactionActivity != null)
                                {
                                    iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                                }
                            }
                            else if (id == 20)
                            {
                                if (iBalancesDelegate_transactionActivity != null)
                                {
                                    iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                                }
                            }
                            else if (id == 21)
                            {
                                if (iBalancesDelegate_transactionActivity != null)
                                {
                                    iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                                }
                            }
                            else if (id == 22)
                            {
                                if (iBalancesDelegate_transactionActivity != null)
                                {
                                    iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                                }
                            }
                            else if (id == 23)
                            {
                                if (iBalancesDelegate_transactionActivity != null)
                                {
                                    iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                                }
                            }
                            else if (id == 12)
                            {
                                if (iTransactionObject != null) {
                                    iTransactionObject.checkTransactionObject(jsonObject);
                                }
                            } else if (id == 13) {
                                if (iAccountObject != null) {
                                    iAccountObject.accountObjectCallback(jsonObject);
                                }
                            } else if (id == 14) {
                                if (iAssetObject != null) {
                                    iAssetObject.assetObjectCallback(jsonObject);
                                }
                            } else if (id == 9)
                            {
                                if (iBalancesDelegate_transactionActivity != null) {
                                    iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                                }
                            } else if (id == 10) {
                                if (iBalancesDelegate_transactionActivity != null) {
                                    iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                                }
                            } else if (id == 11)
                            {
                                if (iBalancesDelegate_transactionActivity != null) {
                                    iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                                }
                            } else if (id == 99) {
                                if (iBalancesDelegate_assetsActivity != null) {
                                    SupportMethods.testing("assests", 99, "account_name");
                                    iBalancesDelegate_assetsActivity.OnUpdate(s, id);
                                }
                            } else if (id == 999) {
                                if (iBalancesDelegate_assetsActivity != null) {
                                    SupportMethods.testing("assests", 999, "account_name");
                                    iBalancesDelegate_assetsActivity.OnUpdate(s, id);
                                }
                            } else if (id == 15) {
                                if (iAssetDelegate != null) {
                                    iAssetDelegate.getLifetime(s, id);
                                }
                            } else if (id == 151) {
                                if (iAccountID != null) {
                                    iAccountID.accountId(s);
                                }
                            } else if (id == 160 || id == 161) {
                                if (iRelativeHistory != null) {
                                    iRelativeHistory.relativeHistoryCallback(jsonObject);
                                }
                            } else if (id == 17) {
                                //Log.d("account_update", jsonObject.toString());
                            }
                            else if (id == 18) {
                                if (iBalancesDelegate_ereceiptActivity != null) {
                                    iBalancesDelegate_ereceiptActivity.OnUpdate(s, id);
                                }
                            }
                            else if (id == 19) {
                                if (iBalancesDelegate_ereceiptActivity != null) {
                                    iBalancesDelegate_ereceiptActivity.OnUpdate(s, id);
                                }
                            }
                        } else if (jsonObject.has("method")) {
                            if (jsonObject.getString("method").equals("notice")) {
                                if (jsonObject.has("params")) {
                                    int id = jsonObject.getJSONArray("params").getInt(0);
                                    JSONArray values = jsonObject.getJSONArray("params").getJSONArray(1);

                                    if (id == 7)
                                    {
                                        headBlockNumber(values.toString());

                                        if (monitorAccountId != null && !monitorAccountId.isEmpty() && values.toString().contains(monitorAccountId))
                                        {
                                            if (iAssetDelegate != null)
                                            {
                                                iAssetDelegate.loadAll();
                                            }
                                            Log.d("Notice Update", values.toString());
                                        }
                                        //headBlockNumber(s);
                                    }
                                    else {
                                        Log.d("other notice", values.toString());
                                    }

                                }
                            }
                        }


                    } catch (JSONException e) {

                    }
                }
//            });
//
//
//        }
//    }

    private static void sendInitialSocket(final Context context)
    {

        if (Application.mIsConnected)
        {
            Application.send(context.getString(R.string.login_api));
        }
    }

    private static String headBlockNumber(String json) {

        String result = "";
        if (json.contains(context.getString(R.string.head_block_number))) {
            int start = json.lastIndexOf(context.getString(R.string.head_block_number));
            int length = 19;
            start = start + length;
            int end = start;
            for (; end < json.length(); end++) {
                if (json.substring(end, end + 1).equals(",")) {
                    break;
                }
            }
            result = "block# " + json.substring(start, end);
        } else {
            result = Application.blockHead;
        }

        return blockHead = result;
    }


    //WebSocketConnection

    public static void send(String message)
    {
        if (mIsConnected)
        {
            mConnection.sendTextMessage(message);
        }
    }

    private static boolean mIsConnected = false;
    static String connectedSocket;

    public static void disconnect() {
        Log.i("internetBlockpay", "Disconnect");
        if(mConnection!=null) {
            mConnection.disconnect();
        }
    }

    @Override
    public void onOpen()
    {
        Log.i("internetBlockpay", "open internet");
        mIsConnected = true;
        Toast.makeText(context, getResources().getString(R.string.connected_to)+ ": "+connectedSocket,Toast.LENGTH_SHORT).show();
        sendInitialSocket(context);
    }

    Handler connectionHandler = new Handler();
    public void checkConnection(){
        connectionHandler.removeCallbacksAndMessages(null);
        connectionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if(checkInternetConnection()) {{
                        webSocketConnection();
                    }
                }else {
                    connectionHandler.postDelayed(this,500);
                }

            }
        }, 500);
    }


    @Override
    public void onClose(WebSocketCloseNotification code, String reason)
    {
        Log.i("internetBlockpay", "close internet");
        mIsConnected = false;
        checkConnection();
    }

    @Override
    public void onTextMessage(String payload)
    {
      stringTextRecievedWs(payload);
    }

    @Override
    public void onRawTextMessage(byte[] payload) {
    }

    @Override
    public void onBinaryMessage(byte[] payload) {
    }

    private static WebSocketConnection mConnection;
    private static URI mServerURI;

    @NonNull
    public static Boolean isConnected(){
        return mConnection!=null && (mConnection.isConnected());
    }

    private void connect(String node)
    {
        Log.i("internetBlockpay", "connecting to internet");
        Log.i("Connect", "connect(String node) called");
        try
        {
            if ( !mIsConnected )
            {
                Log.i("Connect", "Inside when not mIsConnected");
                mServerURI = new URI(node);
                connectedSocket = node;
                mConnection.connect(mServerURI, this);
            }
        }
        catch (URISyntaxException e)
        {
            String message = e.getLocalizedMessage();
            Log.i("Connect", "Inside catch block when not mIsConnected, got exception, msg is:" + message);
            checkConnection();
        }
        catch (WebSocketException e)
        {
            String message = e.getLocalizedMessage();
            Log.i("Connect", "Inside catch block when not mIsConnected, got exception, msg is:" + message);
            if ( !mIsConnected ) {
                checkConnection();
            }
        }
    }

    Boolean checkInternetConnection(){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}

