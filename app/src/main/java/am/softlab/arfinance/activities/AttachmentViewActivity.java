package am.softlab.arfinance.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import am.softlab.arfinance.Constants;
import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityAttachmentViewBinding;

public class AttachmentViewActivity extends AppCompatActivity {

    //view binding
    private ActivityAttachmentViewBinding binding;

    private String operId, categoryName, imageUrl;
    private long operationTimestamp;

    //progress dialog
    private ProgressDialog progressDialog;

    //resources
    private Resources res;

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";
    private static final String TAG = "IMAGEVIEW_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAttachmentViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);
        //show progress dialog
        progressDialog.setMessage(res.getString(R.string.loading_attachment));
        progressDialog.show();

        //operation id get from intent started from AdapterOperation
        operId = getIntent().getStringExtra("operId");
        categoryName = getIntent().getStringExtra("categoryName");
        operationTimestamp = getIntent().getLongExtra("operationTimestamp", System.currentTimeMillis());
        imageUrl = getIntent().getStringExtra("imageUrl");

        String attachmentName = categoryName + "_" + MyApplication.formatTimestamp2(operationTimestamp);
        binding.titleTv.setText(attachmentName);

        //binding.photoViewPv.setImageResource(R.drawable.back02);
        Glide.with(getApplicationContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_no_photo_gray)
                .into(binding.photoViewPv);

        progressDialog.dismiss();

        //handle click, goBack
        binding.backBtn.setOnClickListener(v -> {
            MyApplication.hideKeyboard(this);
            onBackPressed();
        });

        //handle click, download
        binding.downloadImageBtn.setOnClickListener(v -> {
            //confirm download dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(res.getString(R.string.download))
                    .setMessage(res.getString(R.string.sure_download_image))
                    .setPositiveButton(res.getString(R.string.download), (dialogInterface, i) -> {
                        //begin download
                        Log.d(TAG_DOWNLOAD, "onClick: Checking permission");
                        if (MyApplication.checkPermission(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Constants.WRITE_EXTERNAL_STORAGE))
                        {
                            Log.d(TAG_DOWNLOAD, "onClick: Permission already granted, can download image");
                            Toast.makeText(this, res.getString(R.string.downloading), Toast.LENGTH_SHORT).show();
                            MyApplication.downloadImage(this, categoryName, operationTimestamp, imageUrl);
                        }
                    })
                    .setNegativeButton(res.getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
        });
    }
}