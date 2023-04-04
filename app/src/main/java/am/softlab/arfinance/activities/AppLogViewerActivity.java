package am.softlab.arfinance.activities;

import static am.softlab.arfinance.utils.ActivityUtils.hideKeyboardInView;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityAppLogViewerBinding;
import am.softlab.arfinance.utils.AppLog;
import am.softlab.arfinance.utils.ToastUtils;

public class AppLogViewerActivity extends AppCompatActivity {
    private static final int ID_COPY_TO_CLIPBOARD = 1;

    private ActivityAppLogViewerBinding binding;

    private static final String TAG = "LOG_VIEWER_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppLogViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(new LogAdapter(this));

        binding.copyBtn.setOnClickListener(v -> {
            hideKeyboardInView(this);
            copyAppLogToClipboard();
        });

        //handle click, goback
        binding.backBtn.setOnClickListener(v -> {
            hideKeyboardInView(this);
            onBackPressed();
        });
    }

    private class LogAdapter extends BaseAdapter {
        private final ArrayList<String> mEntries;
        private final LayoutInflater mInflater;

        private LogAdapter(Context context) {
            mEntries = AppLog.toHtmlList(context);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mEntries.size();
        }

        @Override
        public Object getItem(int position) {
            return mEntries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final LogViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.row_logviewer, parent, false);
                holder = new LogViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (LogViewHolder) convertView.getTag();
            }

            // take the header lines (app version & device name) into account or else the
            // line numbers shown here won't match the line numbers when the log is shared
            int lineNum = position - AppLog.HEADER_LINE_COUNT + 1;
            if (lineNum > 0) {
                holder.txtLineNumber.setText(String.format("%02d", lineNum));
                holder.txtLineNumber.setVisibility(View.VISIBLE);
            } else {
                holder.txtLineNumber.setVisibility(View.GONE);
            }

            holder.txtLogEntry.setText(Html.fromHtml(mEntries.get(position)));

            return convertView;
        }

        private class LogViewHolder {
            private final TextView txtLineNumber;
            private final TextView txtLogEntry;

            LogViewHolder(View view) {
                txtLineNumber = (TextView) view.findViewById(R.id.text_line);
                txtLogEntry = (TextView) view.findViewById(R.id.text_log);
            }
        }
    }

    private void copyAppLogToClipboard() {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("AppLog", AppLog.toPlainText(this)));
            ToastUtils.showToast(this, R.string.logs_copied_to_clipboard);
        } catch (Exception e) {
            AppLog.e(TAG, e);
            ToastUtils.showToast(this, R.string.error_copy_to_clipboard);
        }
    }

}