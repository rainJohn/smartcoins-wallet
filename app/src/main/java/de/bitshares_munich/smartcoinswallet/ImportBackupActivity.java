package de.bitshares_munich.smartcoinswallet;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.luminiasoft.bitshares.Address;
import com.luminiasoft.bitshares.BrainKey;
import com.luminiasoft.bitshares.FileBin;
import com.luminiasoft.bitshares.interfaces.WitnessResponseListener;
import com.luminiasoft.bitshares.models.AccountProperties;
import com.luminiasoft.bitshares.models.BaseResponse;
import com.luminiasoft.bitshares.models.WitnessResponse;
import com.luminiasoft.bitshares.ws.GetAccountNameById;
import com.luminiasoft.bitshares.ws.GetAccountsByAddress;

import org.bitcoinj.core.ECKey;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ar.com.daidalos.afiledialog.FileChooserDialog;
import ar.com.daidalos.afiledialog.FileChooserLabels;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.bitshares_munich.models.AccountDetails;
import de.bitshares_munich.utils.BinHelper;
import de.bitshares_munich.utils.Crypt;
import de.bitshares_munich.utils.PermissionManager;

public class ImportBackupActivity extends BaseActivity {
    @Bind(R.id.tvFileChoosenBin)
    TextView tvFileChoosenBin;

    @Bind(R.id.etPinBin)
    EditText etPinBin;

    ArrayList<Integer> bytes;

    Activity myActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_backup);
        setTitle(getResources().getString(R.string.app_name));
        ButterKnife.bind(this);
        setBackButton(true);
        myActivity = this;
        progressDialog = new ProgressDialog(this);
    }

    @OnClick(R.id.btnChooseFile)
    public void onChooseFile(){
        chooseBinFile();
    }

    @OnClick(R.id.btnWalletBin)
    public void onClickbtnWalletBin()
    {
        String pinText = etPinBin.getText().toString();

        if (pinText.length() == 0)
        {
            Toast.makeText(getApplicationContext(), R.string.please_enter_brainkey, Toast.LENGTH_SHORT).show();
        }
        else
        {
            if (pinText.length() < 5)
            {
                Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin, Toast.LENGTH_SHORT).show();
            }
            else if (pinText.length() < 5)
            {
                Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin_confirm, Toast.LENGTH_SHORT).show();
            }
            else
            {
                showDialog("",getString(R.string.importing_keys_from_bin_file));
                get_account_from_brainkey(pinText);
            }
        }
    }

    FileChooserDialog dialog;

    private void chooseBinFile() {
        if (dialog == null) {
            dialog = new FileChooserDialog(this);
            dialog.addListener(this.onFileSelectedListener);
            dialog.setFolderMode(false);
            dialog.setCanCreateFiles(false);
            dialog.setShowCancelButton(true);
            dialog.setShowOnlySelectable(false);
            dialog.setFilter(".*bin");


            // Activate the confirmation dialogs.
            dialog.setShowConfirmation(true, true);
            // Define the labels.
            FileChooserLabels labels = new FileChooserLabels();
            labels.createFileDialogAcceptButton = getApplicationContext().getString(R.string.ok);
            labels.createFileDialogCancelButton = getApplicationContext().getString(R.string.cancel);
            labels.labelSelectButton = getApplicationContext().getString(R.string.select);
            labels.messageConfirmSelection = getApplicationContext().getString(R.string.are_you_sure);
            labels.labelConfirmYesButton = getApplicationContext().getString(R.string.txt_yes);
            labels.labelConfirmNoButton = getApplicationContext().getString(R.string.txt_no);
            labels.labelCancelButton = getApplicationContext().getString(R.string.cancel);
            dialog.setLabels(labels);
        }

        // Show the dialog.
        dialog.show();

    }

    private FileChooserDialog.OnFileSelectedListener onFileSelectedListener = new FileChooserDialog.OnFileSelectedListener() {
        public void onFileSelected(Dialog source, File file) {
            source.hide();
            onSuccess(file.getAbsolutePath());
            tvFileChoosenBin.setText(file.getName());
        }

        public void onFileSelected(Dialog source, File folder, String name) {
            source.hide();
        }
    };

    void onSuccess(String filepath){
        PermissionManager manager = new PermissionManager();
        manager.verifyStoragePermissions(this);

        bytes = new BinHelper().getBytesFromBinFile(filepath);
    }

    /*private void postData(String method,String pin, ArrayList<Integer> _bytes){
        try{
            String postReceiverUrl = getString(R.string.account_from_brainkey_url);

            HttpParams httpParameters = new BasicHttpParams();
            HttpClient httpClient = new DefaultHttpClient(httpParameters);

            httpClient.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
            httpClient.getParams().setParameter("http.socket.timeout", 2000);
            httpClient.getParams().setParameter("http.protocol.content-charset", HTTP.UTF_8);
            httpParameters.setBooleanParameter("http.protocol.expect-continue", false);

            HttpPost httpPost = new HttpPost(postReceiverUrl);
            httpPost.getParams().setParameter("http.socket.timeout", 5000);

            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("method", method));
            nameValuePairs.add(new BasicNameValuePair("pin", pin));

            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
            httpPost.setEntity(formEntity);

            HttpResponse response = httpClient.execute(httpPost);

            java.io.BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            String result = sb.toString();

            Log.e("result", result);


        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/


    ProgressDialog progressDialog;
    private void showDialog(String title, String msg) {
        if (progressDialog != null) {
            if (!progressDialog.isShowing()) {
                progressDialog.setTitle(title);
                progressDialog.setMessage(msg);
                progressDialog.show();
            }
        }
    }

    private void hideDialog() {

        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.cancel();
            }
        }

    }

    public void get_account_from_brainkey(final String pin) {

        try
        {
            byte[] inputFile = new byte[bytes.size()];
            for(int i = 0 ; i < bytes.size();i++){
                inputFile[i] = bytes.get(i).byteValue();
            }
            final String BrainKey = FileBin.getBrainkeyFromByte(inputFile,pin);
            com.luminiasoft.bitshares.BrainKey bKey = new BrainKey(BrainKey, 0);
            Address address = new Address(ECKey.fromPublicOnly(bKey.getPrivateKey().getPubKey()));
            final String privkey = Crypt.getInstance().encrypt_string(bKey.getWalletImportFormat());
            final String pubkey = address.toString();

            new WebsocketWorkerThread(new GetAccountsByAddress(address, new WitnessResponseListener() {
                @Override
                public void onSuccess(WitnessResponse response) {
                    if (response.result.getClass() == ArrayList.class) {
                        List list = (List) response.result;
                        if (list.size() > 0) {
                            if (list.get(0).getClass() == ArrayList.class) {
                                List sl = (List) list.get(0);
                                if (sl.size() > 0) {
                                    String accountId = (String) sl.get(0);
                                    getAccountById(accountId, privkey, pubkey, BrainKey,pin);
                                }else{
                                    hideDialog();
                                    Toast.makeText(getApplicationContext(), R.string.error_invalid_account, Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            hideDialog();
                            Toast.makeText(getApplicationContext(), R.string.error_invalid_account, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        hideDialog();
                        Toast.makeText(getApplicationContext(), R.string.try_again, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    hideDialog();
                    Toast.makeText(getApplicationContext(), R.string.unable_to_load_brainkey, Toast.LENGTH_SHORT).show();
                }
            }), 0).start();
        }
        catch (Exception e)
        {
            hideDialog();
            Toast.makeText(myActivity, myActivity.getString(R.string.please_make_sure_your_bin_file), Toast.LENGTH_LONG).show();
        }

    }

    private void getAccountById(final String accountId, final String privaKey, final String pubKey, final String brainkey, final String pinCode){
        try {
            new WebsocketWorkerThread((new GetAccountNameById(accountId, new WitnessResponseListener() {
                @Override
                public void onSuccess(WitnessResponse response) {
                    if (response.result.getClass() == ArrayList.class) {
                        List list = (List) response.result;
                        if (list.size() > 0) {
                            if (list.get(0).getClass() == AccountProperties.class) {
                                AccountProperties accountProperties = (AccountProperties) list.get(0);
                                AccountDetails accountDetails = new AccountDetails();
                                accountDetails.account_name = accountProperties.name;
                                accountDetails.account_id = accountProperties.id;
                                accountDetails.wif_key = privaKey;
                                accountDetails.pub_key = pubKey;
                                accountDetails.isSelected = true;
                                accountDetails.status = "success";
                                accountDetails.brain_key = brainkey;
                                accountDetails.pinCode = pinCode;

                                BinHelper myBinHelper = new BinHelper();
                                myBinHelper.addWallet(accountDetails, getApplicationContext(),myActivity);
                                Intent intent;

                                if ( myBinHelper.numberOfWalletAccounts(getApplicationContext()) <= 1 )
                                {
                                    intent = new Intent(getApplicationContext(), BackupBrainkeyActivity.class);
                                }
                                else
                                {
                                    intent = new Intent(getApplicationContext(), TabActivity.class);
                                }

                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();

                            } else {
                                Toast.makeText(getApplicationContext(), "Didn't get Account properties", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            hideDialog();
                            Toast.makeText(getApplicationContext(), R.string.try_again, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        hideDialog();
                        Toast.makeText(getApplicationContext(), R.string.try_again, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    Toast.makeText(getApplicationContext(), R.string.unable_to_load_brainkey, Toast.LENGTH_SHORT).show();
                }
            })), 0).start();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.txt_no_internet_connection, Toast.LENGTH_SHORT).show();
        }


    }

    @OnClick(R.id.btnCancelBin)
    public void OnCancel(Button button){
        this.finish();
    }


}
