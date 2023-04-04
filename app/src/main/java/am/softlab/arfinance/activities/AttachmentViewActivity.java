package am.softlab.arfinance.activities;

import static am.softlab.arfinance.utils.ActivityUtils.hideKeyboardInView;
import static am.softlab.arfinance.utils.DateTimeUtils.formatTimestampUnderline;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import am.softlab.arfinance.BuildConfig;
import am.softlab.arfinance.Constants;
import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityAttachmentViewBinding;

public class AttachmentViewActivity extends AppCompatActivity {

    //view binding
    private ActivityAttachmentViewBinding binding;

    private String mOperId, mCategoryName, mImageUrl;
    private Uri mImageUri;
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
        mOperId = getIntent().getStringExtra("operId");
        mCategoryName = getIntent().getStringExtra("categoryName");
        operationTimestamp = getIntent().getLongExtra("operationTimestamp", System.currentTimeMillis());
        mImageUrl = getIntent().getStringExtra("imageUrl");
        String imageUriStr = getIntent().getStringExtra("imageUri");
        if (imageUriStr == null || imageUriStr.isEmpty())
            mImageUri = null;
        else
            mImageUri = Uri.parse(imageUriStr);

        String attachmentName = mCategoryName + "_" + formatTimestampUnderline(operationTimestamp);
        binding.titleTv.setText(attachmentName);

        //binding.photoViewPv.setImageResource(R.drawable.back02);
        if (mImageUri != null)
            Glide.with(getApplicationContext())
                    .load(mImageUri)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressDialog.dismiss();
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressDialog.dismiss();
                            return false;
                        }
                    })
                    .placeholder(R.drawable.ic_no_photo_gray)
                    .into(binding.photoViewPv);
        else
            Glide.with(getApplicationContext())
                    .load(mImageUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressDialog.dismiss();
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressDialog.dismiss();
                            return false;
                        }
                    })
                    .placeholder(R.drawable.ic_no_photo_gray)
                    .into(binding.photoViewPv);

        //handle click, goBack
        binding.backBtn.setOnClickListener(v -> {
            hideKeyboardInView(this);
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
                        if (BuildConfig.DEBUG)
                            Log.d(TAG_DOWNLOAD, "onClick: Checking permission");

                        if (MyApplication.checkPermission(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Constants.WRITE_EXTERNAL_STORAGE))
                        {
                            if (BuildConfig.DEBUG)
                                Log.d(TAG_DOWNLOAD, "onClick: Permission already granted, can download image");
                            Toast.makeText(this, res.getString(R.string.downloading), Toast.LENGTH_SHORT).show();

                            MyApplication.downloadImage(this, mCategoryName, operationTimestamp, mImageUrl, mImageUri);
                        }
                    })
                    .setNegativeButton(res.getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
        });
    }
}